package org.cuticulados.pm.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.entity.Produto;
import org.cuticulados.pm.entity.Profissional;
import org.cuticulados.pm.entity.VendaAvulsa;
import org.cuticulados.pm.repository.ProdutoRepository;
import org.cuticulados.pm.repository.VendaAvulsaRepository;

public class VendaAvulsaService {

    private final VendaAvulsaRepository vendaRepo = new VendaAvulsaRepository();
    private final ProdutoRepository produtoRepo = new ProdutoRepository();

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
            venda.setTotal(venda.getPrecoUnitario().multiply(BigDecimal.valueOf(venda.getQuantidade())));
            venda.setDataVenda(LocalDateTime.now());

            vendaRepo.salvar(venda);

            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - venda.getQuantidade());
            produtoRepo.salvar(produto);

            System.out.println("Venda registrada com sucesso. Total: R$ " + venda.getTotal());
        } catch (Exception e) {
            System.out.println("Erro ao registrar venda avulsa: " + e.getMessage());
        }
    }

    public List<VendaAvulsa> listarTodas() {
        try {
            return vendaRepo.listarTodas();
        } catch (Exception e) {
            System.out.println("Erro ao listar vendas avulsas: " + e.getMessage());
            return List.of();
        }
    }

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

    public void relatorioVendasDoDia() {
        try {
            LocalDateTime inicio = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime fim = inicio.plusDays(1);
            List<VendaAvulsa> vendas = vendaRepo.listarTodas().stream()
                    .filter(v -> !v.getDataVenda().isBefore(inicio) && v.getDataVenda().isBefore(fim))
                    .toList();
            if (vendas.isEmpty()) {
                System.out.println("Nenhuma venda registrada hoje.");
                return;
            }
            vendas.forEach(v -> System.out.printf(
                    " [%d] %s | %dx %s | R$ %.2f | %s%n",
                    v.getId(), v.getDataVenda(),
                    v.getQuantidade(), v.getProduto().getNome(),
                    v.getTotal(), v.isFechado() ? "FECHADO" : "ABERTO"));
        } catch (Exception e) {
            System.out.println("Erro ao gerar relatorio: " + e.getMessage());
        }
    }

    public String fecharVenda(Long id) {
        try {
            Optional<VendaAvulsa> op = vendaRepo.buscarPorId(id);
            if (op.isEmpty()) {
                return "Venda não encontrada.";
            }
            VendaAvulsa venda = op.get();
            if (venda.isFechado()) {
                return "Esta venda já está fechada.";
            }
            venda.setFechado(true);
            vendaRepo.salvar(venda);
            return null;
        } catch (Exception e) {
            return "Erro ao fechar venda: " + e.getMessage();
        }
    }

    public void fecharDia(Profissional profissional) {
        try {
            LocalDateTime inicio = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime fim = inicio.plusDays(1);
            List<VendaAvulsa> vendas = vendaRepo.listarTodas().stream()
                    .filter(v -> v.getProfissional().equals(profissional)
                            && !v.getDataVenda().isBefore(inicio)
                            && v.getDataVenda().isBefore(fim)
                            && !v.isFechado())
                    .toList();
            vendas.forEach(v -> {
                v.setFechado(true);
                vendaRepo.salvar(v);
            });
            System.out.println("Dia fechado. Vendas encerradas: " + vendas.size());
        } catch (Exception e) {
            System.out.println("Erro ao fechar dia: " + e.getMessage());
        }
    }
}