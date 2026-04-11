package org.cuticulados.pm.entity;

/**
 * Classifica o tipo de uma transação financeira.
 * Usada em {@link TransacaoFinanceira} para diferenciar receitas de despesas no caixa.
 */
public enum TipoTransacao {
    /** Receita: pagamento recebido por serviço ou venda de produto. */
    ENTRADA,
    /** Despesa: saída de dinheiro do caixa (compra de produto, investimento, etc.). */
    SAIDA
}