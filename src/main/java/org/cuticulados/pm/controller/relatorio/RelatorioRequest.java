package org.cuticulados.pm.controller.relatorio;

import java.time.LocalDate;

public record RelatorioRequest(
        LocalDate dataInicio,
        LocalDate dataFim
) {
}
