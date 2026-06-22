package org.cuticulados.pm.controller.cliente.mapper;

import org.cuticulados.pm.controller.cliente.ClienteRequest;
import org.cuticulados.pm.entity.ClienteEntity;
import org.cuticulados.pm.entity.TipoUsuario;

public class ClienteMapper {

    public static ClienteEntity dtoToEntity(ClienteRequest request) {
        ClienteEntity entity = new ClienteEntity();
        entity.setNome(request.nome());
        entity.setCpf(request.cpf());
        entity.setTelefone(request.telefone());
        entity.setEmail(request.email());
        entity.setLogin(request.login());
        entity.setSenha(request.senha());
        entity.setTipo(TipoUsuario.CLIENTE);
        return entity;
    }

    public static ClienteRequest entityToDto(ClienteEntity entity) {
        return new ClienteRequest(
                entity.getNome(),
                entity.getCpf(),
                entity.getTelefone(),
                entity.getEmail(),
                entity.getLogin(),
                null // senha nao e exposta
        );
    }
}
