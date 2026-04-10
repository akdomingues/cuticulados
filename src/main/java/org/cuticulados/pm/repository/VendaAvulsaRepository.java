package org.cuticulados.pm.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.VendaAvulsa;

import jakarta.persistence.EntityManager;

public class VendaAvulsaRepository {

    //salva e atualiza a venda
    public void salvar(VendaAvulsa venda) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            if (venda.getId() == null) {
                em.persist(venda); // novo registro
            } else {
                em.merge(venda); // atualização
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Erro ao salvar venda avulsa: " + e.getMessage());
        }
    }

    //buscar a venda por ID
    public Optional<VendaAvulsa> buscarPorId(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            VendaAvulsa v = em.find(VendaAvulsa.class, id);
            return Optional.ofNullable(v);
        } catch (Exception e) {
            System.err.println("Erro ao buscar venda avulsa: " + e.getMessage());
            return Optional.empty();
        }
    }

    //lista todas as vendas
    public List<VendaAvulsa> listarTodas() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT v FROM VendaAvulsa v JOIN FETCH v.produto JOIN FETCH v.profissional ORDER BY v.dataVenda DESC",
                            VendaAvulsa.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar vendas avulsas: " + e.getMessage());
            return List.of();
        }
    }

    //buscar a venda por data
    public List<VendaAvulsa> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT v FROM VendaAvulsa v WHERE v.dataVenda BETWEEN :inicio AND :fim ORDER BY v.dataVenda",
                            VendaAvulsa.class)
                    .setParameter("inicio", inicio)
                    .setParameter("fim", fim)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar vendas por periodo: " + e.getMessage());
            return List.of();
        }
    }

    //remover a venda pelo ID
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