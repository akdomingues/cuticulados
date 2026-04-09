package org.cuticulados.pm.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.Agendamento;
import org.cuticulados.pm.entity.Profissional;
import org.cuticulados.pm.entity.StatusAgendamento;

import jakarta.persistence.EntityManager;

/**
 * Repositório responsável pelo acesso e manipulação dos dados de {@link Agendamento}.
 *
 * <p>Encapsula todas as operações de banco relacionadas a agendamentos,
 * incluindo CRUD básico e consultas personalizadas com JOIN.</p>
 *
 * <p>Cada método abre e fecha seu próprio {@code EntityManager},
 * seguindo o padrão ensinado em aula.</p>
 */
public class AgendamentoRepository {

    /**
     * Salva ou atualiza um agendamento no banco de dados.
     *
     * <p>Se o ID for nulo, insere um novo registro ({@code persist}).
     * Caso contrário, atualiza o registro existente ({@code merge}).</p>
     *
     * @param agendamento objeto a ser persistido
     */
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

    /**
     * Busca um agendamento pelo ID, carregando também seus serviços e cliente.
     *
     * <p>Utiliza {@code LEFT JOIN FETCH} para evitar consultas adicionais
     * ao acessar as listas de serviços e dados do cliente.</p>
     *
     * @param id identificador do agendamento
     * @return {@code Optional} com o agendamento encontrado, ou vazio se não existir
     */
    public Optional<Agendamento> buscarPorId(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            Agendamento a = em.createQuery(
                            "SELECT a FROM Agendamento a " +
                                    "LEFT JOIN FETCH a.servicos " +
                                    "LEFT JOIN FETCH a.cliente " +
                                    "WHERE a.id = :id",
                            Agendamento.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return Optional.ofNullable(a);
        } catch (Exception e) {
            System.err.println("Erro ao buscar agendamento: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Lista todos os agendamentos, carregando cliente e profissional de cada um.
     *
     * <p>Utiliza {@code LEFT JOIN FETCH} para carregar os dados relacionados
     * em uma única consulta ao banco.</p>
     *
     * @return lista de todos os agendamentos cadastrados
     */
    public List<Agendamento> listarTodos() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT a FROM Agendamento a " +
                                    "LEFT JOIN FETCH a.cliente " +
                                    "LEFT JOIN FETCH a.profissional",
                            Agendamento.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar agendamentos: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Lista agendamentos filtrados pelo status informado.
     *
     * @param status situação desejada (PENDENTE, CONCLUIDO ou CANCELADO)
     * @return lista de agendamentos com o status informado, ordenada por data
     */
    public List<Agendamento> buscarPorStatus(StatusAgendamento status) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT a FROM Agendamento a " +
                                    "WHERE a.status = :status " +
                                    "ORDER BY a.dataHoraInicio",
                            Agendamento.class)
                    .setParameter("status", status)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar agendamentos por status: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Busca agendamentos dentro de um período de tempo, usando INNER JOIN.
     *
     * <p>Demonstra o uso de {@code JOIN} (inner join) entre Agendamento,
     * Cliente e Profissional, retornando apenas registros que possuem
     * cliente e profissional associados.</p>
     *
     * @param inicio data/hora de início do período
     * @param fim    data/hora de fim do período
     * @return lista de agendamentos no período informado, ordenada por data
     */
    public List<Agendamento> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT a FROM Agendamento a " +
                                    "JOIN a.cliente c " +
                                    "JOIN a.profissional p " +
                                    "WHERE a.dataHoraInicio BETWEEN :inicio AND :fim " +
                                    "ORDER BY a.dataHoraInicio",
                            Agendamento.class)
                    .setParameter("inicio", inicio)
                    .setParameter("fim", fim)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar agendamentos por periodo: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Verifica se um profissional já possui agendamento no horário solicitado.
     *
     * <p>Regra de negócio de conflito de horário: retorna {@code true} se
     * houver sobreposição de horários para o profissional informado.</p>
     *
     * @param profissional profissional a verificar
     * @param inicio       início do intervalo a checar
     * @param fim          fim do intervalo a checar
     * @return {@code true} se houver conflito de horário
     */
    public boolean existeConflito(Profissional profissional, LocalDateTime inicio, LocalDateTime fim) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            Long count = em.createQuery(
                            "SELECT COUNT(a) FROM Agendamento a " +
                                    "WHERE a.profissional = :prof " +
                                    "AND a.status <> 'CANCELADO' " +
                                    "AND a.dataHoraInicio < :fim " +
                                    "AND a.dataHoraFim > :inicio",
                            Long.class)
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

    /**
     * Remove um agendamento do banco de dados pelo ID.
     *
     * @param id identificador do agendamento a ser removido
     */
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