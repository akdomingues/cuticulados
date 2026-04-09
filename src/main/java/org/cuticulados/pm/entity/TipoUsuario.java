package org.cuticulados.pm.entity;

/**
 * Enumeração que define os papéis/perfis de um usuário no sistema.
 *
 * <p>Utilizado na entidade {@code Usuario} com {@code @Enumerated(EnumType.STRING)}
 * para controlar o nível de acesso de cada pessoa cadastrada.</p>
 *
 * <ul>
 *   <li>{@code ADMIN} — acesso total ao sistema</li>
 *   <li>{@code PROFISSIONAL} — acesso às funcionalidades de atendimento</li>
 *   <li>{@code CLIENTE} — acesso restrito ao próprio histórico</li>
 * </ul>
 */
public enum TipoUsuario {
    ADMIN,
    PROFISSIONAL,
    CLIENTE
}