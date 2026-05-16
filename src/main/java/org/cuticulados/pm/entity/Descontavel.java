package org.cuticulados.pm.entity;

import java.math.BigDecimal;

/**
 * Contrato para entidades que podem receber desconto no valor de um serviço.
 */
public interface Descontavel {
    /**
     * Calcula o valor final após aplicação do desconto.
     *
     * @param valorBruto valor antes do desconto
     * @return valor com desconto aplicado
     */
    BigDecimal calcularDesconto(BigDecimal valorBruto);
}
