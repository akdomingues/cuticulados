package org.cuticulados.pm.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.Profissional;
import org.cuticulados.pm.entity.VendaAvulsa;

import jakarta.persistence.EntityManager;

/**
 * Repositório de {@link VendaAvulsa}: CRUD e consultas por data e profissional
 * usadas no relatório e no fechamento do dia.
 */
public class VendaAvulsaRepository {

    /**
     * Salva ou atualiza uma venda avulsa no banco de dados.
     *
     * @param venda objeto a ser persistido
     */
    public void salvar(VendaAvulsa venda) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            if (venda.getId() == null) {
                em.persist(venda);
            } else {
                em.merge(venda);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Erro ao salvar venda avulsa: " + e.getMessage());
        }
    }

    /**
     * Busca uma venda avulsa pelo ID.
     *
     * @param id identificador da venda
     * @return {@code Optional} com a venda encontrada, ou vazio se não existir
     */
    public Optional<VendaAvulsa> buscarPorId(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            VendaAvulsa v = em.find(VendaAvulsa.class, id);
            return Optional.ofNullable(v);
        } catch (Exception e) {
            System.err.println("Erro ao buscar venda avulsa: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Lista todas as vendas avulsas, carregando produto e profissional via JOIN FETCH.
     *
     * @return lista de todas as vendas avulsas, ordenada por data decrescente
     */
    public List<VendaAvulsa> listarTodas() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT v FROM VendaAvulsa v " +
                                    "JOIN FETCH v.produto " +
                                    "JOIN FETCH v.profissional " +
                                    "ORDER BY v.dataVenda DESC",
                            VendaAvulsa.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar vendas avulsas: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Busca vendas avulsas realizadas dentro de um período de tempo.
     *
     * @param inicio data/hora de início do período
     * @param fim    data/hora de fim do período
     * @return lista de vendas no período, ordenada por data
     */
    public List<VendaAvulsa> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT v FROM VendaAvulsa v " +
                                    "JOIN FETCH v.produto " +
                                    "JOIN FETCH v.profissional " +
                                    "WHERE v.dataVenda BETWEEN :inicio AND :fim " +
                                    "ORDER BY v.dataVenda",
                            VendaAvulsa.class)
                    .setParameter("inicio", inicio)
                    .setParameter("fim", fim)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar vendas por periodo: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Busca vendas abertas (fechado = false) de um profissional em um período.
     * Usada no fechamento de dia.
     *
     * @param profissional profissional cujas vendas serão buscadas
     * @param inicio       início do período (normalmente 00:00 do dia)
     * @param fim          fim do período (normalmente 23:59 do dia)
     * @return lista de vendas abertas do profissional no período
     */
    public List<VendaAvulsa> buscarAbertasPorProfissionalEPeriodo(
            Profissional profissional, LocalDateTime inicio, LocalDateTime fim) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT v FROM VendaAvulsa v " +
                                    "JOIN FETCH v.produto " +
                                    "WHERE v.profissional = :prof " +
                                    "AND v.dataVenda BETWEEN :inicio AND :fim " +
                                    "AND v.fechado = false " +
                                    "ORDER BY v.dataVenda",
                            VendaAvulsa.class)
                    .setParameter("prof", profissional)
                    .setParameter("inicio", inicio)
                    .setParameter("fim", fim)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar vendas abertas: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Marca todas as vendas da lista como fechadas. Todas são atualizadas
     * em uma única transação para garantir consistência.
     *
     * @param vendas lista de vendas a serem fechadas
     */
    public void fecharVendas(List<VendaAvulsa> vendas) {
        if (vendas == null || vendas.isEmpty()) return;
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            for (VendaAvulsa v : vendas) {
                v.setFechado(true);
                em.merge(v);
            }
            em.getTransaction().commit();
            System.out.println(vendas.size() + " venda(s) fechada(s) com sucesso.");
        } catch (Exception e) {
            System.err.println("Erro ao fechar vendas: " + e.getMessage());
        }
    }

    /**
     * Remove uma venda avulsa do banco de dados pelo ID.
     *
     * @param id identificador da venda a ser removida
     */
    public void deletar(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            VendaAvulsa v = em.find(VendaAvulsa.class, id);
            if (v != null) {
                em.remove(v);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Erro ao deletar venda avulsa: " + e.getMessage());
        }
    }
}