package org.cuticulados.pm.controller.venda.mapper;

import org.cuticulados.pm.controller.venda.VendaRequest;
import org.cuticulados.pm.entity.ProdutoEntity;
import org.cuticulados.pm.entity.ProfissionalEntity;
import org.cuticulados.pm.entity.VendaAvulsaEntity;

public class VendaMapper {

    public static VendaAvulsaEntity dtoToEntity(VendaRequest request) {
        VendaAvulsaEntity entity = new VendaAvulsaEntity();

        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(request.produtoId());
        entity.setProduto(produto);

        ProfissionalEntity profissional = new ProfissionalEntity();
        profissional.setId(request.profissionalId());
        entity.setProfissional(profissional);

        entity.setQuantidade(request.quantidade());
        return entity;
    }

    public static VendaRequest entityToDto(VendaAvulsaEntity entity) {
        return new VendaRequest(
                entity.getProduto().getId(),
                entity.getQuantidade(),
                entity.getProfissional().getId()
        );
    }
}
