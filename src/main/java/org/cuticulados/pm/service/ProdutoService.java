package org.cuticulados.pm.service;

//CRUD DE PRODUTO

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.entity.ProdutoEntity;
import org.cuticulados.pm.repository.ProdutoRepository;

public class ProdutoService {

    private final ProdutoRepository produtoRepo = new ProdutoRepository();

//CADASTRO

    public String cadastrarProduto(ProdutoEntity produtoEntity) {
        try {
            if (produtoEntity.getNome() == null || produtoEntity.getNome().isBlank()) {
                return "Nome do produto é obrigatório.";
            }
            if (produtoEntity.getPrecoVenda().compareTo(BigDecimal.ZERO) <= 0) {
                return "Preço de venda deve ser maior que zero.";
            }
            produtoRepo.salvar(produtoEntity);
            return null;
        } catch (Exception e) {
            return "Erro ao cadastrar produto: " + e.getMessage();
        }
    }

    public Optional<ProdutoEntity> buscarPorId(Long id) {
        try {
            return produtoRepo.buscarPorId(id);
        } catch (Exception e) {
            System.out.println("Erro ao buscar produto: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<ProdutoEntity> listarTodos() {
        try {
            return produtoRepo.listarTodos();
        } catch (Exception e) {
            System.out.println("Erro ao listar produtos: " + e.getMessage());
            return List.of();
        }
    }

    //ATUALIZAR

    public String atualizarProduto(ProdutoEntity produtoEntity) {
        try {
            Optional<ProdutoEntity> existente = produtoRepo.buscarPorId(produtoEntity.getId());
            if (existente.isEmpty()) {
                return "Produto não encontrado.";
            }
            produtoRepo.salvar(produtoEntity);
            return null;
        } catch (Exception e) {
            return "Erro ao atualizar produto: " + e.getMessage();
        }
    }

    //REMOVER

    public String removerProduto(Long id) {
        try {
            if (produtoRepo.buscarPorId(id).isEmpty()) {
                return "Produto não encontrado.";
            }
            produtoRepo.deletar(id);
            return null;
        } catch (Exception e) {
            return "Erro ao remover produto: " + e.getMessage();
        }
    }

    //BUSCA E VERIFICA O ESTOQUE
    public List<ProdutoEntity> verificarEstoqueBaixo() {
        try {
            List<ProdutoEntity> produtoEntities = produtoRepo.buscarEstoqueBaixo();
            if (produtoEntities.isEmpty()) {
                System.out.println("Nenhum produto com estoque baixo.");
            } else {
                for (ProdutoEntity p : produtoEntities) {
                    System.out.println(p.getNome() + " | estoque: " + p.getQuantidadeEstoque() + " | minimo: " +
                            p.getQuantidadeMinima());
                }
            }
            return produtoEntities;
        } catch (Exception e) {
            System.out.println("Erro ao verificar estoque baixo: " + e.getMessage());
            return List.of();
        }
    }
}
