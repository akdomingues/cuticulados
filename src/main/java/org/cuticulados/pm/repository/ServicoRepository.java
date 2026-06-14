package org.cuticulados.pm.repository;

//RELACAO DE SERVICO COM O BANCO

import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.ServicoEntity;

import jakarta.persistence.EntityManager;

public class ServicoRepository {

    public void salvar(ServicoEntity servicoEntity) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            if (servicoEntity.getId() == null) {
                em.persist(servicoEntity);
            } else {
                em.merge(servicoEntity);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException(extrairMensagem(e), e);
        }
    }

    public Optional<ServicoEntity> buscarPorId(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            ServicoEntity s = em.find(ServicoEntity.class, id);
            return Optional.ofNullable(s);
        } catch (Exception e) {
            System.err.println("Erro ao buscar servico: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<ServicoEntity> listarTodos() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery("FROM Servico", ServicoEntity.class).getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar servicos: " + e.getMessage());
            return List.of();
        }
    }

    public List<ServicoEntity> buscarPorDescricao(String termo) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT s FROM Servico s WHERE LOWER(s.descricao) LIKE LOWER(:termo)", ServicoEntity.class)
                    .setParameter("termo", "%" + termo + "%")
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar servico por descricao: " + e.getMessage());
            return List.of();
        }
    }

    public List<ServicoEntity> listarComProdutos() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT DISTINCT s FROM Servico s LEFT JOIN FETCH s.produtosUtilizados", ServicoEntity.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar servicos com produtos: " + e.getMessage());
            return List.of();
        }
    }

    public void deletar(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            ServicoEntity s = em.find(ServicoEntity.class, id);
            if (s != null) {
                em.remove(s);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException(extrairMensagem(e), e);
        }
    }

    private static String extrairMensagem(Throwable e) {
        Throwable cause = e;
        while (cause.getCause() != null) cause = cause.getCause();
        String msg = cause.getMessage();
        return (msg != null && !msg.isBlank()) ? msg : e.getMessage();
    }
}
