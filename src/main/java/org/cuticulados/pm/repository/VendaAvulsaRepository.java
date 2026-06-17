package org.cuticulados.pm.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.VendaAvulsaEntity;

import jakarta.persistence.EntityManager;

public class VendaAvulsaRepository {

    //salva e atualiza a venda
    public void salvar(VendaAvulsaEntity venda) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            if (venda.getId() == null) {
                em.persist(venda);
            } else {
                em.merge(venda);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException(extrairMensagem(e), e);
        }
    }

    //buscar a venda por ID
    public Optional<VendaAvulsaEntity> buscarPorId(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            VendaAvulsaEntity v = em.find(VendaAvulsaEntity.class, id);
            return Optional.ofNullable(v);
        } catch (Exception e) {
            System.err.println("Erro ao buscar venda avulsa: " + e.getMessage());
            return Optional.empty();
        }
    }

    //lista todas as vendas
    public List<VendaAvulsaEntity> listarTodas() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT v FROM VendaAvulsa v JOIN FETCH v.produto JOIN FETCH v.profissional ORDER BY v.dataVenda DESC",
                            VendaAvulsaEntity.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar vendas avulsas: " + e.getMessage());
            return List.of();
        }
    }

    //buscar a venda por data
    public List<VendaAvulsaEntity> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT v FROM VendaAvulsa v WHERE v.dataVenda BETWEEN :inicio AND :fim ORDER BY v.dataVenda",
                            VendaAvulsaEntity.class)
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
            VendaAvulsaEntity v = em.find(VendaAvulsaEntity.class, id);
            if (v != null) {
                em.remove(v);
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