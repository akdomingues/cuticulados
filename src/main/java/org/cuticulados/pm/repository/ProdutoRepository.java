package org.cuticulados.pm.repository;

//RELAÇÃO DE PRODUTO COM BANCO

import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.ProdutoEntity;
import org.cuticulados.pm.entity.ServicoProdutoEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

public class ProdutoRepository {

    //SALVAR

    public void salvar(ProdutoEntity produtoEntity) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            if (produtoEntity.getId() == null) {
                em.persist(produtoEntity);
            } else {
                em.merge(produtoEntity);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException(extrairMensagem(e), e);
        }
    }

    //BUSCAR

    public Optional<ProdutoEntity> buscarPorId(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            ProdutoEntity p = em.find(ProdutoEntity.class, id);
            return Optional.ofNullable(p);
        } catch (Exception e) {
            System.err.println("Erro ao buscar produto: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<ProdutoEntity> buscarPorNome(String nome) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            ProdutoEntity p = em.createQuery(
                            "SELECT p FROM Produto p WHERE LOWER(p.nome) = LOWER(:nome)", ProdutoEntity.class)
                    .setParameter("nome", nome)
                    .getSingleResult();
            return Optional.ofNullable(p);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Erro ao buscar produto por nome: " + e.getMessage());
            return Optional.empty();
        }
    }

    //LISTAR

    public List<ProdutoEntity> listarTodos() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery("FROM Produto", ProdutoEntity.class).getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar produtos: " + e.getMessage());
            return List.of();
        }
    }

    public List<ProdutoEntity> buscarEstoqueBaixo() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT p FROM Produto p WHERE p.quantidadeEstoque <= p.quantidadeMinima", ProdutoEntity.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar estoque baixo: " + e.getMessage());
            return List.of();
        }
    }

    public List<ServicoProdutoEntity> buscarServicoProdutos(Long servicoId) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT sp FROM ServicoProduto sp JOIN FETCH sp.produtoEntity WHERE sp.servicoEntity.id = :sid",
                    ServicoProdutoEntity.class)
                    .setParameter("sid", servicoId)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar insumos do servico: " + e.getMessage());
            return List.of();
        }
    }

    //DELETAR

    public void deletar(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            ProdutoEntity p = em.find(ProdutoEntity.class, id);
            if (p != null) {
                em.remove(p);
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

