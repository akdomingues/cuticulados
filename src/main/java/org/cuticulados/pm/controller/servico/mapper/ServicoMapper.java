package org.cuticulados.pm.controller.servico.mapper;

import org.cuticulados.pm.controller.servico.ServicoRequest;
import org.cuticulados.pm.entity.ServicoEntity;

public class ServicoMapper {

    public static ServicoEntity dtoToEntity(ServicoRequest request) {
        ServicoEntity entity = new ServicoEntity();
        entity.setDescricao(request.descricao());
        entity.setValorBase(request.valorBase());
        entity.setDuracaoMinutos(request.duracaoMinutos());
        return entity;
    }

    public static ServicoRequest entityToDto(ServicoEntity entity) {
        return new ServicoRequest(
                entity.getDescricao(),
                entity.getValorBase(),
                entity.getDuracaoMinutos()
        );
    }
}
