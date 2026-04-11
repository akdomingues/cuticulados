package org.cuticulados.pm.repository;

import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.Usuario;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

/**
 * Repositório de {@link Usuario}: CRUD, busca por login e soft delete via {@code deletedAt}.
 */
public class UsuarioRepository {

    /**
     * Salva ou atualiza um usuário no banco de dados.
     *
     * @param usuario objeto a ser persistido
     */
    public void salvar(Usuario usuario) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            if (usuario.getId() == null) {
                em.persist(usuario);
            } else {
                em.merge(usuario);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Erro ao salvar usuario: " + e.getMessage());
        }
    }

    /**
     * Busca um usuário pelo ID.
     *
     * @param id identificador do usuário
     * @return {@code Optional} com o usuário encontrado, ou vazio se não existir
     */
    public Optional<Usuario> buscarPorId(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            Usuario u = em.find(Usuario.class, id);
            return Optional.ofNullable(u);
        } catch (Exception e) {
            System.err.println("Erro ao buscar usuario por id: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Busca um usuário pelo login. NoResultException é tratado separadamente
     * para não imprimir erro em situações normais (login inexistente).
     *
     * @param login login do usuário
     * @return Optional com o usuário, ou vazio se não existir
     */
    public Optional<Usuario> buscarPorLogin(String login) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            Usuario u = em.createQuery(
                            "SELECT u FROM Usuario u WHERE u.login = :login",
                            Usuario.class)
                    .setParameter("login", login)
                    .getSingleResult();
            return Optional.ofNullable(u);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Erro ao buscar usuario por login: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Lista usuários ativos, ou seja, onde {@code deletedAt} é nulo.
     *
     * @return lista de usuários que não foram excluídos logicamente
     */
    public List<Usuario> listarTodos() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT u FROM Usuario u WHERE u.deletedAt IS NULL",
                            Usuario.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar usuarios: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Realiza soft delete: preenche {@code deletedAt} sem remover o registro.
     * O histórico de agendamentos e transações do usuário é preservado.
     *
     * @param id identificador do usuário
     */
    public void deletarLogico(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            Usuario u = em.find(Usuario.class, id);
            if (u != null) {
                u.setDeletedAt(java.time.LocalDateTime.now());
                em.merge(u);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Erro ao deletar usuario (logico): " + e.getMessage());
        }
    }
}