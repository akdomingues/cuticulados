package org.cuticulados.pm.controller.venda;

public record VendaRequest(
        Long produtoId,
        Integer quantidade,
        Long profissionalId
) {
}
