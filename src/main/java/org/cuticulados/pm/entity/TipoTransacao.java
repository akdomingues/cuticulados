package org.cuticulados.pm.entity;

/**
 * Enumeração que classifica o tipo de uma transação financeira.
 *
 * <p>Utilizada na entidade {@code TransacaoFinanceira} para diferenciar
 * receitas de despesas no fluxo de caixa do salão.</p>
 */
public enum TipoTransacao {
    /** uma receita (pagamento por seriço ou venda)*/
    ENTRADA,
    /** uma despesa (investimento...)*/
    SAIDA
}