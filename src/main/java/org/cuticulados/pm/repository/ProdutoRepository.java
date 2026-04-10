package org.cuticulados.pm.repository;

//RELAÇÃO DE PRODUTO COM BANCO

import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.Produto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

public class ProdutoRepository {

    //SALVAR

    public void salvar(Produto produto) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            if (produto.getId() == null) {
                em.persist(produto);
            } else {
                em.merge(produto);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Erro ao salvar produto: " + e.getMessage());
        }
    }

    //BUSCAR

    public Optional<Produto> buscarPorId(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            Produto p = em.find(Produto.class, id);
            return Optional.ofNullable(p);
        } catch (Exception e) {
            System.err.println("Erro ao buscar produto: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Produto> buscarPorNome(String nome) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            Produto p = em.createQuery(
                            "SELECT p FROM Produto p WHERE LOWER(p.nome) = LOWER(:nome)", Produto.class)
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

    public List<Produto> listarTodos() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery("FROM Produto", Produto.class).getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar produtos: " + e.getMessage());
            return List.of();
        }
    }

    public List<Produto> buscarEstoqueBaixo() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT p FROM Produto p WHERE p.quantidadeEstoque <= p.quantidadeMinima", Produto.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar estoque baixo: " + e.getMessage());
            return List.of();
        }
    }

    //DELETAR

    public void deletar(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            Produto p = em.find(Produto.class, id);
            if (p != null) {
                em.remove(p);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Erro ao deletar produto: " + e.getMessage());
        }
    }
}

