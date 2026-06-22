// caminho: src/main/java/org/cuticulados/pm/service/ProdutoService.java
package org.cuticulados.pm.service;

import org.cuticulados.pm.model.Produto;
import org.cuticulados.pm.repository.ProdutoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProdutoService {

    private final ProdutoRepository repo = new ProdutoRepository();

    public void cadastrar(String nome, int estoque, int minimo, BigDecimal custo, BigDecimal venda) {
        try {
            if (estoque < 0) throw new IllegalArgumentException("Estoque não pode ser negativo.");
            if (venda.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Preço de venda inválido.");
            Produto p = new Produto();
            p.setNome(nome);
            p.setQuantidadeEstoque(estoque);
            p.setQuantidadeMinima(minimo);
            p.setPrecoCusto(custo);
            p.setPrecoVenda(venda);
            repo.salvar(p);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao cadastrar produto: " + e.getMessage(), e);
        }
    }

    public void atualizar(Long id, String nome, int minimo, BigDecimal custo, BigDecimal venda) {
        try {
            Produto p = repo.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
            p.setNome(nome);
            p.setQuantidadeMinima(minimo);
            p.setPrecoCusto(custo);
            p.setPrecoVenda(venda);
            repo.atualizar(p);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar produto: " + e.getMessage(), e);
        }
    }

    public void remover(Long id) {
        try {
            repo.deletar(id);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao remover produto: " + e.getMessage(), e);
        }
    }

    public void adicionarEstoque(Long id, int quantidade) {
        try {
            if (quantidade <= 0) throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
            Produto p = repo.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
            p.setQuantidadeEstoque(p.getQuantidadeEstoque() + quantidade);
            repo.atualizar(p);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao adicionar estoque: " + e.getMessage(), e);
        }
    }

    public List<Produto> listarTodos() {
        try {
            return repo.listarTodos();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar produtos: " + e.getMessage(), e);
        }
    }

    public List<Produto> listarAbaixoDoMinimo() {
        try {
            return repo.listarAbaixoDoMinimo();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao verificar estoque: " + e.getMessage(), e);
        }
    }

    public Optional<Produto> buscarPorId(Long id) {
        try {
            return repo.buscarPorId(id);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar produto: " + e.getMessage(), e);
        }
    }
}