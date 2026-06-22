// caminho: src/main/java/org/cuticulados/pm/repository/ProdutoRepository.java
package org.cuticulados.pm.repository;

import jakarta.persistence.EntityManager;
import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.model.Produto;
import java.util.List;
import java.util.Optional;

public class ProdutoRepository {

    public void salvar(Produto produto) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(produto);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void atualizar(Produto produto) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(produto);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void deletar(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Produto p = em.find(Produto.class, id);
            if (p != null) em.remove(p);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<Produto> buscarPorId(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Produto.class, id));
        } finally {
            em.close();
        }
    }

    public List<Produto> listarTodos() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Produto p ORDER BY p.nome", Produto.class)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Produto> listarAbaixoDoMinimo() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM Produto p WHERE p.quantidadeEstoque < p.quantidadeMinima ORDER BY p.nome",
                Produto.class).getResultList();
        } finally {
            em.close();
        }
    }
}