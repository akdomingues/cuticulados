package org.cuticulados.pm.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.TipoTransacao;
import org.cuticulados.pm.entity.TransacaoFinanceiraEntity;

import jakarta.persistence.EntityManager;

public class TransacaoRepository {

    public void salvar(TransacaoFinanceiraEntity transacao) {
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

    public Optional<TransacaoFinanceiraEntity> buscarPorId(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            TransacaoFinanceiraEntity t = em.find(TransacaoFinanceiraEntity.class, id);
            return Optional.ofNullable(t);
        } catch (Exception e) {
            System.err.println("Erro ao buscar transacao: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<TransacaoFinanceiraEntity> listarTodas() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "FROM TransacaoFinanceira t ORDER BY t.dataTransacao DESC", TransacaoFinanceiraEntity.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar transacoes: " + e.getMessage());
            return List.of();
        }
    }

    public List<TransacaoFinanceiraEntity> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT t FROM TransacaoFinanceira t WHERE t.dataTransacao BETWEEN :inicio AND :fim ORDER BY t.dataTransacao",
                            TransacaoFinanceiraEntity.class)
                    .setParameter("inicio", inicio)
                    .setParameter("fim", fim)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar transacoes por periodo: " + e.getMessage());
            return List.of();
        }
    }

    public List<TransacaoFinanceiraEntity> buscarPorTipo(TipoTransacao tipo) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT t FROM TransacaoFinanceira t WHERE t.tipo = :tipo ORDER BY t.dataTransacao DESC",
                            TransacaoFinanceiraEntity.class)
                    .setParameter("tipo", tipo)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar transacoes por tipo: " + e.getMessage());
            return List.of();
        }
    }

    /** Soma dos valores por tipo — JOIN com agendamento */
    public BigDecimal somarPorTipo(TipoTransacao tipo) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            BigDecimal resultado = em.createQuery(
                            "SELECT SUM(t.valor) FROM TransacaoFinanceira t WHERE t.tipo = :tipo", BigDecimal.class)
                    .setParameter("tipo", tipo)
                    .getSingleResult();
            return resultado != null ? resultado : BigDecimal.ZERO;
        } catch (Exception e) {
            System.err.println("Erro ao somar transacoes por tipo: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }
}
