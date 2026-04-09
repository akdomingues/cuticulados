package org.cuticulados.pm.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.entity.Produto;
import org.cuticulados.pm.entity.VendaAvulsa;
import org.cuticulados.pm.repository.ProdutoRepository;
import org.cuticulados.pm.repository.VendaAvulsaRepository;

/**
 * Serviço responsável pelas regras de negócio relacionadas a vendas avulsas de produtos.
 *
 * <p>Controla o processo completo de uma venda: validação de estoque,
 * cálculo do total, registro da venda e atualização automática do estoque.
 * Garante que não seja possível vender mais produtos do que o disponível.</p>
 */
public class VendaAvulsaService {

    /** Repositório para acesso aos dados de venda avulsa no banco. */
    private final VendaAvulsaRepository vendaRepo = new VendaAvulsaRepository();

    /** Repositório de produtos para verificação e atualização do estoque. */
    private final ProdutoRepository produtoRepo = new ProdutoRepository();

    /**
     * Registra uma nova venda avulsa de produto.
     *
     * <p>Regras aplicadas:</p>
     * <ul>
     *   <li>Verifica se o produto existe no banco</li>
     *   <li>Valida se há estoque suficiente para a quantidade solicitada</li>
     *   <li>Calcula o total (preço unitário × quantidade)</li>
     *   <li>Debita automaticamente a quantidade vendida do estoque</li>
     * </ul>
     *
     * @param venda objeto com os dados da venda (produto, quantidade e profissional)
     */
    public void registrarVenda(VendaAvulsa venda) {
        try {
            Optional<Produto> opProduto = produtoRepo.buscarPorId(venda.getProduto().getId());
            if (opProduto.isEmpty()) {
                System.out.println("Produto nao encontrado.");
                return;
            }

            Produto produto = opProduto.get();

            // verifica se ha estoque suficiente antes de vender
            if (produto.getQuantidadeEstoque() < venda.getQuantidade()) {
                System.out.println("Estoque insuficiente. Disponivel: " + produto.getQuantidadeEstoque());
                return;
            }

            // define preco unitario e total com base no cadastro do produto
            venda.setPrecoUnitario(produto.getPrecoVenda());
            venda.setTotal(venda.getPrecoUnitario() * venda.getQuantidade());
            venda.setDataVenda(LocalDateTime.now());

            vendaRepo.salvar(venda);

            // debita a quantidade vendida do estoque
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - venda.getQuantidade());
            produtoRepo.salvar(produto);

            System.out.println("Venda registrada com sucesso. Total: R$ " + venda.getTotal());
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
}