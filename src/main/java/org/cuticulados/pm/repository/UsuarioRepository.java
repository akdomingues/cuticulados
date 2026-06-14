package org.cuticulados.pm.repository;

import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.UsuarioEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

public class UsuarioRepository {

    public void salvar(UsuarioEntity usuarioEntity) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            if (usuarioEntity.getId() == null) {
                em.persist(usuarioEntity);
            } else {
                em.merge(usuarioEntity);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException(extrairMensagem(e), e);
        }
    }

    public Optional<UsuarioEntity> buscarPorId(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            UsuarioEntity u = em.find(UsuarioEntity.class, id);
            return Optional.ofNullable(u);
        } catch (Exception e) {
            System.err.println("Erro ao buscar usuario por id: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<UsuarioEntity> buscarPorLogin(String login) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            UsuarioEntity u = em.createQuery(
                            "SELECT u FROM Usuario u WHERE u.login = :login", UsuarioEntity.class)
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

    public List<UsuarioEntity> listarTodos() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery("SELECT u FROM Usuario u WHERE u.deletedAt IS NULL", UsuarioEntity.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar usuarios: " + e.getMessage());
            return List.of();
        }
    }

    public void deletarLogico(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            UsuarioEntity u = em.find(UsuarioEntity.class, id);
            if (u != null) {
                u.setDeletedAt(java.time.LocalDateTime.now());
                em.merge(u);
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

