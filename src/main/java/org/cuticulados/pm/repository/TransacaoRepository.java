package org.cuticulados.pm.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.TipoTransacao;
import org.cuticulados.pm.entity.TransacaoFinanceira;

import jakarta.persistence.EntityManager;

public class TransacaoRepository {

    public void salvar(TransacaoFinanceira transacao) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            if (transacao.getId() == null) {
                em.persist(transacao);
            } else {
                em.merge(transacao);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Erro ao salvar transacao: " + e.getMessage());
        }
    }

    public Optional<TransacaoFinanceira> buscarPorId(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            TransacaoFinanceira t = em.find(TransacaoFinanceira.class, id);
            return Optional.ofNullable(t);
        } catch (Exception e) {
            System.err.println("Erro ao buscar transacao: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<TransacaoFinanceira> listarTodas() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "FROM TransacaoFinanceira t ORDER BY t.dataTransacao DESC", TransacaoFinanceira.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar transacoes: " + e.getMessage());
            return List.of();
        }
    }

    public List<TransacaoFinanceira> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT t FROM TransacaoFinanceira t WHERE t.dataTransacao BETWEEN :inicio AND :fim ORDER BY t.dataTransacao",
                            TransacaoFinanceira.class)
                    .setParameter("inicio", inicio)
                    .setParameter("fim", fim)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar transacoes por periodo: " + e.getMessage());
            return List.of();
        }
    }

    public List<TransacaoFinanceira> buscarPorTipo(TipoTransacao tipo) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT t FROM TransacaoFinanceira t WHERE t.tipo = :tipo ORDER BY t.dataTransacao DESC",
                            TransacaoFinanceira.class)
                    .setParameter("tipo", tipo)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar transacoes por tipo: " + e.getMessage());
            return List.of();
        }
    }

    /** Soma dos valores por tipo — JOIN com agendamento */
    public Double somarPorTipo(TipoTransacao tipo) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            Double resultado = em.createQuery(
                            "SELECT SUM(t.valor) FROM TransacaoFinanceira t WHERE t.tipo = :tipo", Double.class)
                    .setParameter("tipo", tipo)
                    .getSingleResult();
            return resultado != null ? resultado : 0.0;
        } catch (Exception e) {
            System.err.println("Erro ao somar transacoes por tipo: " + e.getMessage());
            return 0.0;
        }
    }
}

public Double somarPorTipo(TipoTransacao tipo) {
    EntityManager em = JpaUtil.getEntityManager();
    try {
        Double resultado = em.createQuery(
                        "SELECT SUM(t.valor) FROM TransacaoFinanceira t WHERE t.tipo = :tipo",
                        Double.class)
                .setParameter("tipo", tipo)
                .getSingleResult();
        return resultado != null ? resultado : 0.0;
    } finally {
        em.close();
    }
}