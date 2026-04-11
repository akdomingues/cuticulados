package org.cuticulados.pm.entity;

/**
 * Estados possíveis de um agendamento.
 *
 * Todo agendamento começa como PENDENTE e pode evoluir para CONCLUIDO ou CANCELADO.
 * Transições inválidas são bloqueadas pelo AgendamentoService.
 */
public enum StatusAgendamento {
    PENDENTE,
    CONCLUIDO,
    CANCELADO
}