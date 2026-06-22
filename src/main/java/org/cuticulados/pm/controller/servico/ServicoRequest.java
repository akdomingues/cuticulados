package org.cuticulados.pm.controller.servico;

import java.math.BigDecimal;

public record ServicoRequest(
        String descricao,
        BigDecimal valorBase,
        Integer duracaoMinutos
) {
}
