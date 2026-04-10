package org.cuticulados.pm.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.entity.Produto;
import org.cuticulados.pm.entity.VendaAvulsa;
import org.cuticulados.pm.repository.ProdutoRepository;
import org.cuticulados.pm.repository.VendaAvulsaRepository;

public class VendaAvulsaService {

    //repositórios para o acesso de dados
    private final VendaAvulsaRepository vendaRepo = new VendaAvulsaRepository();
    private final ProdutoRepository produtoRepo = new ProdutoRepository();

    //registra uma venda com  validação de estoque
    public void registrarVenda(VendaAvulsa venda) {
        try {
            Optional<Produto> opProduto = produtoRepo.buscarPorId(venda.getProduto().getId());
            if (opProduto.isEmpty()) {
                System.out.println("Produto nao encontrado.");
                return;
            }

            Produto produto = opProduto.get();

            //verifica se tem o estoque suficiente
            if (produto.getQuantidadeEstoque() < venda.getQuantidade()) {
                System.out.println("Estoque insuficiente. Disponivel: " + produto.getQuantidadeEstoque());
                return;
            }

            //define os valores da venda
            venda.setPrecoUnitario(produto.getPrecoVenda());
            venda.setTotal(venda.getPrecoUnitario() * venda.getQuantidade());
            venda.setDataVenda(LocalDateTime.now());

            vendaRepo.salvar(venda);

            //atualizar o estoque
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - venda.getQuantidade());
            produtoRepo.salvar(produto);

            System.out.println("Venda registrada com sucesso. Total: R$ " + venda.getTotal());
        } catch (Exception e) {
            System.out.println("Erro ao registrar venda avulsa: " + e.getMessage());
        }
    }

    //lista de todas as vendas
    public List<VendaAvulsa> listarTodas() {
        try {
            return vendaRepo.listarTodas();
        } catch (Exception e) {
            System.out.println("Erro ao listar vendas avulsas: " + e.getMessage());
            return List.of();
        }
    }

    //remove a venda pelo ID
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