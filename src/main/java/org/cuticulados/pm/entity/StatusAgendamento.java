package org.cuticulados.pm.entity;

/**
 * Enumeração que representa os possíveis estados de um agendamento.
 *
 * <p>Um agendamento sempre começa como {@code PENDENTE} e pode evoluir
 * para {@code CONCLUIDO} ou {@code CANCELADO}. Transições inválidas
 * são controladas pelo {@code AgendamentoService}.</p>
 */
public enum StatusAgendamento {
    PENDENTE,
    CONCLUIDO,
    CANCELADO
}