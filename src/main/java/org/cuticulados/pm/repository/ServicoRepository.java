package org.cuticulados.pm.repository;

//RELACAO DE SERVICO COM O BANCO

import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.Servico;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

public class ServicoRepository {

    public void salvar(Servico servico) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            if (servico.getId() == null) {
                em.persist(servico);
            } else {
                em.merge(servico);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Erro ao salvar servico: " + e.getMessage());
        }
    }

    public Optional<Servico> buscarPorId(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            Servico s = em.find(Servico.class, id);
            return Optional.ofNullable(s);
        } catch (Exception e) {
            System.err.println("Erro ao buscar servico: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<Servico> listarTodos() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery("FROM Servico", Servico.class).getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar servicos: " + e.getMessage());
            return List.of();
        }
    }

    public List<Servico> buscarPorDescricao(String termo) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT s FROM Servico s WHERE LOWER(s.descricao) LIKE LOWER(:termo)", Servico.class)
                    .setParameter("termo", "%" + termo + "%")
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar servico por descricao: " + e.getMessage());
            return List.of();
        }
    }

    public List<Servico> listarComProdutos() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT DISTINCT s FROM Servico s LEFT JOIN FETCH s.produtosUtilizados", Servico.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar servicos com produtos: " + e.getMessage());
            return List.of();
        }
    }

    public void deletar(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            Servico s = em.find(Servico.class, id);
            if (s != null) {
                em.remove(s);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Erro ao deletar servico: " + e.getMessage());
        }
    }
}
