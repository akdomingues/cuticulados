package org.cuticulados.pm.controller.produto;

import java.math.BigDecimal;

public record ProdutoRequest(
        String nome,
        Integer quantidadeEstoque,
        Integer quantidadeMinima,
        BigDecimal precoCusto,
        BigDecimal precoVenda
) {
}
