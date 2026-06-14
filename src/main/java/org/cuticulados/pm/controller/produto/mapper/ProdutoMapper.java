package org.cuticulados.pm.controller.produto.mapper;

import org.cuticulados.pm.controller.produto.ProdutoRequest;
import org.cuticulados.pm.entity.ProdutoEntity;

public class ProdutoMapper {

    public static ProdutoEntity dtoToEntity(ProdutoRequest request) {
        ProdutoEntity entity = new ProdutoEntity();
        entity.setNome(request.nome());
        entity.setQuantidadeEstoque(request.quantidadeEstoque());
        entity.setQuantidadeMinima(request.quantidadeMinima());
        entity.setPrecoCusto(request.precoCusto());
        entity.setPrecoVenda(request.precoVenda());
        return entity;
    }

    public static ProdutoRequest entityToDto(ProdutoEntity entity) {
        return new ProdutoRequest(
                entity.getNome(),
                entity.getQuantidadeEstoque(),
                entity.getQuantidadeMinima(),
                entity.getPrecoCusto(),
                entity.getPrecoVenda()
        );
    }
}
