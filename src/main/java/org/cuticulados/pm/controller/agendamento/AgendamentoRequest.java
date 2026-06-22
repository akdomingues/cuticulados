package org.cuticulados.pm.controller.agendamento;

import java.time.LocalDateTime;

public record AgendamentoRequest(
        Long clienteId,
        Long profissionalId,
        LocalDateTime dataHoraInicio,
        LocalDateTime dataHoraFim
) {
}
