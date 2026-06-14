package org.cuticulados.pm.controller.usuario.mapper;

import org.cuticulados.pm.controller.usuario.UsuarioRequest;
import org.cuticulados.pm.entity.UsuarioEntity;

public class UsuarioMapper {

    public static UsuarioEntity dtoToEntity(UsuarioRequest request) {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setNome(request.nome());
        entity.setEmail(request.email());
        entity.setLogin(request.login());
        entity.setSenha(request.senha());
        entity.setTipo(request.tipo());
        return entity;
    }

    public static UsuarioRequest entityToDto(UsuarioEntity entity) {
        return new UsuarioRequest(
                entity.getNome(),
                entity.getEmail(),
                entity.getLogin(),
                null, // senha nao e exposta
                entity.getTipo()
        );
    }
}
