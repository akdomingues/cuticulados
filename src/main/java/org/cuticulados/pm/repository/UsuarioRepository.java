package org.cuticulados.pm.repository;

import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.Usuario;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

/**
 * Repositório responsável pelo acesso e manipulação dos dados de {@link Usuario}.
 *
 * <p>Inclui suporte a soft delete (exclusão lógica via {@code deletedAt})
 * e busca por login para autenticação.</p>
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
     * Busca um usuário pelo login para autenticação.
     *
     * <p>Retorna vazio se o login não existir ou se ocorrer algum erro na consulta.
     * O tratamento de {@code NoResultException} é feito separadamente para
     * não imprimir mensagem de erro em situações normais.</p>
     *
     * @param login login do usuário
     * @return {@code Optional} com o usuário encontrado, ou vazio se não existir
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
     * Lista todos os usuários ativos (que não foram excluídos logicamente).
     *
     * <p>Filtra apenas registros onde {@code deletedAt} é nulo,
     * ignorando usuários que sofreram exclusão lógica.</p>
     *
     * @return lista de usuários ativos
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
     * Realiza a exclusão lógica de um usuário, preenchendo o campo {@code deletedAt}.
     *
     * <p>O registro não é removido do banco; apenas a data de exclusão é registrada.
     * Assim o histórico de agendamentos e transações é preservado.</p>
     *
     * @param id identificador do usuário a ser excluído logicamente
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