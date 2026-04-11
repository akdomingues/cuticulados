package org.cuticulados.pm.repository;

import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.Produto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

/**
 * Repositório de {@link Produto}: CRUD, busca por nome e consulta de estoque baixo.
 */
public class ProdutoRepository {

    /**
     * Salva ou atualiza um produto no banco de dados.
     *
     * @param produto objeto a ser persistido
     */
    public void salvar(Produto produto) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            if (produto.getId() == null) {
                em.persist(produto);
            } else {
                em.merge(produto);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Erro ao salvar produto: " + e.getMessage());
        }
    }

    /**
     * Busca um produto pelo ID.
     *
     * @param id identificador do produto
     * @return {@code Optional} com o produto encontrado, ou vazio se não existir
     */
    public Optional<Produto> buscarPorId(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            Produto p = em.find(Produto.class, id);
            return Optional.ofNullable(p);
        } catch (Exception e) {
            System.err.println("Erro ao buscar produto: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Busca um produto pelo nome (sem diferenciar maiúsculas e minúsculas).
     *
     * @param nome nome do produto a ser pesquisado
     * @return {@code Optional} com o produto encontrado, ou vazio se não existir
     */
    public Optional<Produto> buscarPorNome(String nome) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            Produto p = em.createQuery(
                            "SELECT p FROM Produto p WHERE LOWER(p.nome) = LOWER(:nome)",
                            Produto.class)
                    .setParameter("nome", nome)
                    .getSingleResult();
            return Optional.ofNullable(p);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Erro ao buscar produto por nome: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Lista todos os produtos cadastrados no estoque.
     *
     * @return lista de todos os produtos
     */
    public List<Produto> listarTodos() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery("FROM Produto", Produto.class).getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar produtos: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Busca produtos com estoque atual menor ou igual ao mínimo configurado.
     * Usada pelo ProdutoService para alertar sobre itens que precisam de reposição.
     *
     * @return lista de produtos com estoque baixo
     */
    public List<Produto> buscarEstoqueBaixo() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT p FROM Produto p " +
                                    "WHERE p.quantidadeEstoque <= p.quantidadeMinima",
                            Produto.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar estoque baixo: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Remove um produto do banco de dados pelo ID.
     *
     * @param id identificador do produto a ser removido
     */
    public void deletar(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            Produto p = em.find(Produto.class, id);
            if (p != null) {
                em.remove(p);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Erro ao deletar produto: " + e.getMessage());
        }
    }
}