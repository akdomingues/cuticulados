package org.cuticulados.pm.controller.agendamento.mapper;

import org.cuticulados.pm.controller.agendamento.AgendamentoRequest;
import org.cuticulados.pm.entity.AgendamentoEntity;
import org.cuticulados.pm.entity.ClienteEntity;
import org.cuticulados.pm.entity.ProfissionalEntity;

public class AgendamentoMapper {

    public static AgendamentoEntity dtoToEntity(AgendamentoRequest request) {
        AgendamentoEntity entity = new AgendamentoEntity();

        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(request.clienteId());
        entity.setCliente(cliente);

        ProfissionalEntity profissional = new ProfissionalEntity();
        profissional.setId(request.profissionalId());
        entity.setProfissional(profissional);

        entity.setDataHoraInicio(request.dataHoraInicio());//
        entity.setDataHoraFim(request.dataHoraFim());

        return entity;
    }

    public static AgendamentoRequest entityToDto(AgendamentoEntity entity) {
        return new AgendamentoRequest(
                entity.getCliente().getId(),
                entity.getProfissional().getId(),
                entity.getDataHoraInicio(),
                entity.getDataHoraFim()
        );
    }
}
