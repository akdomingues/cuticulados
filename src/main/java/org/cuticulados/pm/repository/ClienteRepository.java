package org.cuticulados.pm.repository;

import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.Cliente;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

/**
 * Repositório de {@link Cliente}: CRUD, busca por CPF e listagem com agendamentos.
 */
public class ClienteRepository {

    /**
     * Salva ou atualiza um cliente no banco de dados.
     *
     * @param cliente objeto a ser persistido
     */
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
            System.err.println("Erro ao salvar cliente: " + e.getMessage());
        }
    }

    /**
     * Busca um cliente pelo ID.
     *
     * @param id identificador do cliente
     * @return {@code Optional} com o cliente encontrado, ou vazio se não existir
     */
    public Optional<Cliente> buscarPorId(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            Cliente c = em.find(Cliente.class, id);
            return Optional.ofNullable(c);
        } catch (Exception e) {
            System.err.println("Erro ao buscar cliente: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Busca um cliente pelo CPF. Usado para impedir cadastro duplicado.
     *
     * @param cpf CPF a ser pesquisado (com ou sem formatação)
     * @return Optional com o cliente, ou vazio se não existir
     */
    public Optional<Cliente> buscarPorCpf(String cpf) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            Cliente c = em.createQuery(
                            "SELECT c FROM Cliente c WHERE c.cpf = :cpf",
                            Cliente.class)
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

    /**
     * Lista todos os clientes com agendamentos carregados via LEFT JOIN FETCH.
     * Clientes sem agendamentos também aparecem na listagem.
     *
     * @return lista de todos os clientes cadastrados
     */
    public List<Cliente> listarTodos() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT c FROM Cliente c LEFT JOIN FETCH c.agendamentos",
                            Cliente.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar clientes: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Lista clientes filtrados pelo tipo de fidelidade.
     *
     * @param tipo tipo de cliente ("novo" ou "frequente")
     * @return lista de clientes com o tipo informado
     */
    public List<Cliente> buscarPorTipo(String tipo) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT c FROM Cliente c WHERE c.tipoCliente = :tipo",
                            Cliente.class)
                    .setParameter("tipo", tipo)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar clientes por tipo: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Remove um cliente do banco de dados pelo ID.
     *
     * @param id identificador do cliente a ser removido
     */
    public void deletar(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            Cliente c = em.find(Cliente.class, id);
            if (c != null) {
                em.remove(c);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Erro ao deletar cliente: " + e.getMessage());
        }
    }
}