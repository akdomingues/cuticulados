package org.cuticulados.pm.repository;

import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.Cliente;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

public class ClienteRepository {

    public void salvar(Cliente cliente) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            if (cliente.getId() == null) {
                em.persist(cliente);
            } else {
                em.merge(cliente);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException(extrairMensagem(e), e);
        }
    }

    public Optional<Cliente> buscarPorId(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            Cliente c = em.find(Cliente.class, id);
            return Optional.ofNullable(c);
        } catch (Exception e) {
            System.err.println("Erro ao buscar cliente: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Cliente> buscarPorCpf(String cpf) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            Cliente c = em.createQuery(
                            "SELECT c FROM Cliente c WHERE c.cpf = :cpf", Cliente.class)
                    .setParameter("cpf", cpf)
                    .getSingleResult();
            return Optional.ofNullable(c);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Erro ao buscar cliente por cpf: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<Cliente> listarTodos() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT c FROM Cliente c LEFT JOIN FETCH c.agendamentos", Cliente.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar clientes: " + e.getMessage());
            return List.of();
        }
    }

    public List<Cliente> buscarPorTipo(String tipo) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT c FROM Cliente c WHERE c.tipoCliente = :tipo", Cliente.class)
                    .setParameter("tipo", tipo)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar clientes por tipo: " + e.getMessage());
            return List.of();
        }
    }

    public void deletar(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            Cliente c = em.find(Cliente.class, id);
            if (c != null) {
                em.remove(c);
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

