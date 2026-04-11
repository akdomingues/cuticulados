package org.cuticulados.pm.entity;

/**
 * Perfis de acesso de um usuário no sistema.
 *
 * Usado em {@link Usuario} para controlar o que cada pessoa pode fazer:
 * ADMIN tem acesso total, PROFISSIONAL acessa as funcionalidades de atendimento,
 * e CLIENTE tem acesso restrito ao próprio histórico.
 */
public enum TipoUsuario {
    ADMIN,
    PROFISSIONAL,
    CLIENTE
}