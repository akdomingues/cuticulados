// caminho: src/main/java/org/cuticulados/pm/repository/ServicoRepository.java
package org.cuticulados.pm.repository;

import jakarta.persistence.EntityManager;
import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.model.Servico;
import java.util.List;
import java.util.Optional;

public class ServicoRepository {

    public void salvar(Servico servico) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(servico);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void atualizar(Servico servico) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(servico);
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
            Servico s = em.find(Servico.class, id);
            if (s != null) em.remove(s);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<Servico> buscarPorId(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Servico.class, id));
        } finally {
            em.close();
        }
    }

    public List<Servico> listarTodos() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT s FROM Servico s ORDER BY s.descricao", Servico.class)
                     .getResultList();
        } finally {
            em.close();
        }
    }
}