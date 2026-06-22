package org.cuticulados.pm.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.AgendamentoEntity;
import org.cuticulados.pm.entity.ProfissionalEntity;
import org.cuticulados.pm.entity.StatusAgendamento;

import jakarta.persistence.EntityManager;

public class AgendamentoRepository {

    public void salvar(AgendamentoEntity agendamentoEntity) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            if (agendamentoEntity.getId() == null) {
                em.persist(agendamentoEntity);
            } else {
                em.merge(agendamentoEntity);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException(extrairMensagem(e), e);
        }
    }

    public Optional<AgendamentoEntity> buscarPorId(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            AgendamentoEntity a = em.createQuery(
                            "SELECT a FROM Agendamento a LEFT JOIN FETCH a.servicos LEFT JOIN FETCH a.clienteEntity WHERE a.id = :id",
                            AgendamentoEntity.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return Optional.ofNullable(a);
        } catch (Exception e) {
            System.err.println("Erro ao buscar agendamento: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<AgendamentoEntity> listarTodos() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            // DISTINCT evita duplicatas causadas pelo JOIN FETCH na coleção servicos.
            // Não fazemos JOIN FETCH de cliente/profissional pois ambos usam herança JOINED
            // no Hibernate 6, o que gera SQL incorreto quando combinado com JOIN FETCH.
            return em.createQuery(
                            "SELECT DISTINCT a FROM Agendamento a LEFT JOIN FETCH a.servicos ORDER BY a.dataHoraInicio DESC",
                            AgendamentoEntity.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar agendamentos: " + e.getMessage());
            return List.of();
        }
    }

    public List<AgendamentoEntity> buscarPorStatus(StatusAgendamento status) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT a FROM Agendamento a WHERE a.status = :status ORDER BY a.dataHoraInicio",
                            AgendamentoEntity.class)
                    .setParameter("status", status)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar agendamentos por status: " + e.getMessage());
            return List.of();
        }
    }

    /** JOIN entre Agendamento, Cliente e Profissional */
    public List<AgendamentoEntity> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT a FROM Agendamento a " +
                                    "JOIN a.clienteEntity c " +
                                    "JOIN a.profissionalEntity p " +
                                    "WHERE a.dataHoraInicio BETWEEN :inicio AND :fim " +
                                    "ORDER BY a.dataHoraInicio", AgendamentoEntity.class)
                    .setParameter("inicio", inicio)
                    .setParameter("fim", fim)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar agendamentos por periodo: " + e.getMessage());
            return List.of();
        }
    }

    /** Verifica se profissional tem conflito de horario */
    public boolean existeConflito(ProfissionalEntity profissionalEntity, LocalDateTime inicio, LocalDateTime fim) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            Long count = em.createQuery(
                            "SELECT COUNT(a) FROM Agendamento a WHERE a.profissionalEntity = :prof " +
                                    "AND a.status <> 'CANCELADO' " +
                                    "AND a.dataHoraInicio < :fim AND a.dataHoraFim > :inicio", Long.class)
                    .setParameter("prof", profissionalEntity)
                    .setParameter("inicio", inicio)
                    .setParameter("fim", fim)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            System.err.println("Erro ao verificar conflito: " + e.getMessage());
            return false;
        }
    }

    public void deletar(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            AgendamentoEntity a = em.find(AgendamentoEntity.class, id);
            if (a != null) {
                em.remove(a);
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
