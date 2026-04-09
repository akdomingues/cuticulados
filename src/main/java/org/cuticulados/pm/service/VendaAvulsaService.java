package org.cuticulados.pm.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.entity.Produto;
import org.cuticulados.pm.entity.Profissional;
import org.cuticulados.pm.entity.TipoTransacao;
import org.cuticulados.pm.entity.TransacaoFinanceira;
import org.cuticulados.pm.entity.VendaAvulsa;
import org.cuticulados.pm.repository.ProdutoRepository;
import org.cuticulados.pm.repository.TransacaoRepository;
import org.cuticulados.pm.repository.VendaAvulsaRepository;

/**
 * Serviço responsável pelas regras de negócio relacionadas a vendas avulsas de produtos.
 *
 * <p>Controla o processo completo de uma venda: validação de estoque,
 * cálculo do total, registro da venda e criação da transação financeira correspondente.</p>
 *
 * <p>Também gerencia o fechamento de dia do profissional, marcando as vendas
 * do dia como fechadas para evitar recontagem em fechamentos futuros.</p>
 */
public class VendaAvulsaService {

    private final VendaAvulsaRepository vendaRepo      = new VendaAvulsaRepository();
    private final ProdutoRepository     produtoRepo    = new ProdutoRepository();

    /**
     * Repositório de transações: usado para registrar a entrada financeira
     * gerada por cada venda avulsa.
     *
     * <p>CORREÇÃO DO RELATÓRIO ZERADO: anteriormente, ao registrar uma venda,
     * nenhuma TransacaoFinanceira era criada. Por isso os relatórios financeiros
     * sempre retornavam zero. Agora cada venda gera automaticamente uma transação
     * de ENTRADA no caixa.</p>
     */
    private final TransacaoRepository   transacaoRepo  = new TransacaoRepository();

    /**
     * Registra uma nova venda avulsa de produto.
     *
     * <p>Regras aplicadas:</p>
     * <ol>
     *   <li>Verifica se o produto existe</li>
     *   <li>Valida estoque suficiente</li>
     *   <li>Calcula preço unitário e total</li>
     *   <li>Salva a venda no banco</li>
     *   <li>Debita do estoque</li>
     *   <li>Cria uma {@link TransacaoFinanceira} de ENTRADA (corrige relatório zerado)</li>
     * </ol>
     *
     * @param venda objeto com produto, quantidade e profissional preenchidos
     */
    public void registrarVenda(VendaAvulsa venda) {
        try {
            Optional<Produto> opProduto = produtoRepo.buscarPorId(venda.getProduto().getId());
            if (opProduto.isEmpty()) {
                System.out.println("Produto nao encontrado.");
                return;
            }

            Produto produto = opProduto.get();

            if (produto.getQuantidadeEstoque() < venda.getQuantidade()) {
                System.out.println("Estoque insuficiente. Disponivel: " + produto.getQuantidadeEstoque());
                return;
            }

            venda.setPrecoUnitario(produto.getPrecoVenda());
            venda.setTotal(venda.getPrecoUnitario() * venda.getQuantidade());
            venda.setDataVenda(LocalDateTime.now());
            venda.setFechado(false);

            vendaRepo.salvar(venda);

            // Debita estoque
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - venda.getQuantidade());
            produtoRepo.salvar(produto);

            // CORREÇÃO: cria a TransacaoFinanceira de ENTRADA para que
            // os relatórios financeiros contabilizem esta venda corretamente.
            TransacaoFinanceira transacao = new TransacaoFinanceira();
            transacao.setTipo(TipoTransacao.ENTRADA);
            transacao.setDescricao("Venda avulsa: " + produto.getNome()
                    + " (x" + venda.getQuantidade() + ")");
            transacao.setValor(venda.getTotal());
            transacao.setVendaAvulsa(venda);
            transacaoRepo.salvar(transacao);

            System.out.printf("Venda registrada com sucesso. Total: R$ %.2f%n", venda.getTotal());

        } catch (Exception e) {
            System.out.println("Erro ao registrar venda avulsa: " + e.getMessage());
        }
    }

    /**
     * Lista todas as vendas avulsas registradas.
     *
     * @return lista de vendas avulsas
     */
    public List<VendaAvulsa> listarTodas() {
        try {
            return vendaRepo.listarTodas();
        } catch (Exception e) {
            System.out.println("Erro ao listar vendas avulsas: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Remove uma venda avulsa pelo ID.
     *
     * @param id identificador da venda a ser removida
     */
    public void removerVenda(Long id) {
        try {
            if (vendaRepo.buscarPorId(id).isEmpty()) {
                System.out.println("Venda nao encontrada.");
                return;
            }
            vendaRepo.deletar(id);
            System.out.println("Venda removida.");
        } catch (Exception e) {
            System.out.println("Erro ao remover venda: " + e.getMessage());
        }
    }

    /**
     * Realiza o fechamento de dia para um profissional.
     *
     * <p>Busca todas as vendas em aberto ({@code fechado = false}) do profissional
     * no dia atual, exibe um resumo e marca todas como fechadas.</p>
     *
     * <p>Após o fechamento, essas vendas não aparecerão em próximos fechamentos,
     * mas continuam visíveis nos relatórios históricos normais.</p>
     *
     * @param profissional profissional que está encerrando o expediente
     */
    public void fecharDia(Profissional profissional) {
        try {
            LocalDate hoje = LocalDate.now();
            LocalDateTime inicioDia = hoje.atStartOfDay();
            LocalDateTime fimDia   = hoje.atTime(23, 59, 59);

            List<VendaAvulsa> vendasAbertas = vendaRepo
                    .buscarAbertasPorProfissionalEPeriodo(profissional, inicioDia, fimDia);

            if (vendasAbertas.isEmpty()) {
                System.out.println("Nenhuma venda em aberto para hoje. Dia já encerrado ou sem vendas.");
                return;
            }

            // Exibe resumo antes de fechar
            System.out.println("\n=== Resumo do Dia — " + hoje + " ===");
            System.out.printf("%-30s %5s %12s%n", "Produto", "Qtd", "Total");
            System.out.println("-".repeat(50));

            double totalGeral = 0.0;
            for (VendaAvulsa v : vendasAbertas) {
                System.out.printf("%-30s %5d %12.2f%n",
                        v.getProduto().getNome(),
                        v.getQuantidade(),
                        v.getTotal());
                totalGeral += v.getTotal();
            }

            System.out.println("-".repeat(50));
            System.out.printf("TOTAL GERAL DO DIA: R$ %.2f%n", totalGeral);
            System.out.println("\nFechando dia...");

            vendaRepo.fecharVendas(vendasAbertas);
            System.out.println("Dia finalizado com sucesso!");

        } catch (Exception e) {
            System.out.println("Erro ao fechar dia: " + e.getMessage());
        }
    }

    /**
     * Gera e exibe o relatório de vendas do dia atual.
     *
     * <p>Consulta {@link VendaAvulsa} diretamente pela data de hoje,
     * independente do status de fechamento. Assim funciona mesmo que
     * as {@link TransacaoFinanceira} não tenham sido criadas anteriormente.</p>
     *
     * <p>IMPORTANTE: este método é a correção principal para o relatório zerado.
     * Ele não depende de TransacaoFinanceira — lê diretamente as vendas.</p>
     */
    public void relatorioVendasDoDia() {
        try {
            LocalDate hoje = LocalDate.now();
            LocalDateTime inicio = hoje.atStartOfDay();
            LocalDateTime fim    = hoje.atTime(23, 59, 59);

            List<VendaAvulsa> vendas = vendaRepo.buscarPorPeriodo(inicio, fim);

            System.out.println("\n=== Relatório de Vendas — " + hoje + " ===");

            if (vendas.isEmpty()) {
                System.out.println("Nenhuma venda registrada hoje.");
                return;
            }

            System.out.printf("%-25s %5s %12s %8s %-20s%n",
                    "Produto", "Qtd", "Total (R$)", "Status", "Profissional");
            System.out.println("-".repeat(75));

            double totalGeral = 0.0;
            for (VendaAvulsa v : vendas) {
                String status = v.isFechado() ? "FECHADO" : "ABERTO";
                System.out.printf("%-25s %5d %12.2f %8s %-20s%n",
                        v.getProduto().getNome(),
                        v.getQuantidade(),
                        v.getTotal(),
                        status,
                        v.getProfissional().getNome());
                totalGeral += v.getTotal();
            }

            System.out.println("-".repeat(75));
            System.out.printf("TOTAL DO DIA: R$ %.2f  (%d venda(s))%n", totalGeral, vendas.size());

        } catch (Exception e) {
            System.out.println("Erro ao gerar relatorio de vendas do dia: " + e.getMessage());
        }
    }
}