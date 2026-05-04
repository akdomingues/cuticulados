package org.cuticulados.pm.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.Agendamento;
import org.cuticulados.pm.entity.Profissional;
import org.cuticulados.pm.entity.StatusAgendamento;

import jakarta.persistence.EntityManager;

public class AgendamentoRepository {

    public void salvar(Agendamento agendamento) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            if (agendamento.getId() == null) {
                em.persist(agendamento);
            } else {
                em.merge(agendamento);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Erro ao salvar agendamento: " + e.getMessage());
        }
    }

    public Optional<Agendamento> buscarPorId(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            Agendamento a = em.createQuery(
                            "SELECT a FROM Agendamento a LEFT JOIN FETCH a.servicos LEFT JOIN FETCH a.cliente WHERE a.id = :id",
                            Agendamento.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return Optional.ofNullable(a);
        } catch (Exception e) {
            System.err.println("Erro ao buscar agendamento: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<Agendamento> listarTodos() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT a FROM Agendamento a LEFT JOIN FETCH a.cliente LEFT JOIN FETCH a.profissional",
                            Agendamento.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar agendamentos: " + e.getMessage());
            return List.of();
        }
    }

    public List<Agendamento> buscarPorStatus(StatusAgendamento status) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT a FROM Agendamento a WHERE a.status = :status ORDER BY a.dataHoraInicio",
                            Agendamento.class)
                    .setParameter("status", status)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar agendamentos por status: " + e.getMessage());
            return List.of();
        }
    }

    /** JOIN entre Agendamento, Cliente e Profissional */
    public List<Agendamento> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT a FROM Agendamento a " +
                                    "JOIN a.cliente c " +
                                    "JOIN a.profissional p " +
                                    "WHERE a.dataHoraInicio BETWEEN :inicio AND :fim " +
                                    "ORDER BY a.dataHoraInicio", Agendamento.class)
                    .setParameter("inicio", inicio)
                    .setParameter("fim", fim)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar agendamentos por periodo: " + e.getMessage());
            return List.of();
        }
    }

    /** Verifica se profissional tem conflito de horario */
    public boolean existeConflito(Profissional profissional, LocalDateTime inicio, LocalDateTime fim) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            Long count = em.createQuery(
                            "SELECT COUNT(a) FROM Agendamento a WHERE a.profissional = :prof " +
                                    "AND a.status <> 'CANCELADO' " +
                                    "AND a.dataHoraInicio < :fim AND a.dataHoraFim > :inicio", Long.class)
                    .setParameter("prof", profissional)
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
            Agendamento a = em.find(Agendamento.class, id);
            if (a != null) {
                em.remove(a);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Erro ao deletar agendamento: " + e.getMessage());
        }
    }
}
