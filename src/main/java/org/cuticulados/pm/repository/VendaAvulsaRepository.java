package org.cuticulados.pm.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.VendaAvulsa;

import jakarta.persistence.EntityManager;

/**
 * Repositório responsável pelo acesso e manipulação dos dados de {@link VendaAvulsa}.
 *
 * <p>Encapsula as operações de banco para vendas avulsas de produtos,
 * incluindo listagem com JOIN FETCH de produto e profissional.</p>
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
     * <p>Ordenado por data de venda em ordem decrescente (mais recentes primeiro).</p>
     *
     * @return lista de todas as vendas avulsas
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