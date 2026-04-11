package org.cuticulados.pm.service;

import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.entity.Produto;
import org.cuticulados.pm.repository.ProdutoRepository;

/**
 * Regras de negócio de produtos do estoque.
 * Além do CRUD, alerta quando um produto atinge o estoque mínimo e precisa ser reposto.
 */
public class ProdutoService {

    /** Repositório para acesso aos dados de produto no banco. */
    private final ProdutoRepository produtoRepo = new ProdutoRepository();

    /**
     * Cadastra um produto verificando se o nome foi informado e se o preço de venda é positivo.
     *
     * @param produto objeto com os dados do produto
     */
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

    /**
     * Busca um produto pelo ID.
     *
     * @param id identificador do produto
     * @return {@code Optional} com o produto, ou vazio se não encontrado
     */
    public Optional<Produto> buscarPorId(Long id) {
        try {
            return produtoRepo.buscarPorId(id);
        } catch (Exception e) {
            System.out.println("Erro ao buscar produto: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Lista todos os produtos cadastrados no estoque.
     *
     * @return lista de produtos
     */
    public List<Produto> listarTodos() {
        try {
            return produtoRepo.listarTodos();
        } catch (Exception e) {
            System.out.println("Erro ao listar produtos: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Atualiza os dados de um produto existente.
     *
     * @param produto objeto com os dados atualizados (deve ter ID preenchido)
     */
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

    /**
     * Remove um produto pelo ID.
     *
     * @param id identificador do produto a ser removido
     */
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

    /**
     * Lista e exibe produtos com estoque abaixo do mínimo (quantidadeEstoque <= quantidadeMinima).
     *
     * @return lista de produtos que precisam de reposição
     */
    public List<Produto> verificarEstoqueBaixo() {
        try {
            List<Produto> produtos = produtoRepo.buscarEstoqueBaixo();
            if (produtos.isEmpty()) {
                System.out.println("Nenhum produto com estoque baixo.");
            } else {
                for (Produto p : produtos) {
                    System.out.println(p.getNome() + " | estoque: " + p.getQuantidadeEstoque()
                            + " | minimo: " + p.getQuantidadeMinima());
                }
            }
            return produtos;
        } catch (Exception e) {
            System.out.println("Erro ao verificar estoque baixo: " + e.getMessage());
            return List.of();
        }
    }
}