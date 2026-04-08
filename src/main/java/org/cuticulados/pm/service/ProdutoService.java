package org.cuticulados.pm.service;

import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.entity.Produto;
import org.cuticulados.pm.repository.ProdutoRepository;

public class ProdutoService {

    private final ProdutoRepository produtoRepo = new ProdutoRepository();

    public void cadastrarProduto(Produto produto) {
        try {
            if (produto.getNome() == null || produto.getNome().isBlank()) {
                System.out.println("Nome do produto e obrigatorio.");
                return;
            }
            if (produto.getPrecoVenda() <= 0) {
                System.out.println("Preco de venda deve ser maior que zero.");
                return;
            }
            produtoRepo.salvar(produto);
            System.out.println("Produto cadastrado com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro ao cadastrar produto: " + e.getMessage());
        }
    }

    public Optional<Produto> buscarPorId(Long id) {
        try {
            return produtoRepo.buscarPorId(id);
        } catch (Exception e) {
            System.out.println("Erro ao buscar produto: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<Produto> listarTodos() {
        try {
            return produtoRepo.listarTodos();
        } catch (Exception e) {
            System.out.println("Erro ao listar produtos: " + e.getMessage());
            return List.of();
        }
    }

    public void atualizarProduto(Produto produto) {
        try {
            Optional<Produto> existente = produtoRepo.buscarPorId(produto.getId());
            if (existente.isEmpty()) {
                System.out.println("Produto nao encontrado.");
                return;
            }
            produtoRepo.salvar(produto);
        } catch (Exception e) {
            System.out.println("Erro ao atualizar produto: " + e.getMessage());
        }
    }

    public void removerProduto(Long id) {
        try {
            if (produtoRepo.buscarPorId(id).isEmpty()) {
                System.out.println("Produto nao encontrado.");
                return;
            }
            produtoRepo.deletar(id);
            System.out.println("Produto removido com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro ao remover produto: " + e.getMessage());
        }
    }

    public List<Produto> verificarEstoqueBaixo() {
        try {
            List<Produto> produtos = produtoRepo.buscarEstoqueBaixo();
            if (produtos.isEmpty()) {
                System.out.println("Nenhum produto com estoque baixo.");
            } else {
                for (Produto p : produtos) {
                    System.out.println(p.getNome() + " | estoque: " + p.getQuantidadeEstoque() + " | minimo: " +
                            p.getQuantidadeMinima());
                }
            }
            return produtos;
        } catch (Exception e) {
            System.out.println("Erro ao verificar estoque baixo: " + e.getMessage());
            return List.of();
        }
    }
}
