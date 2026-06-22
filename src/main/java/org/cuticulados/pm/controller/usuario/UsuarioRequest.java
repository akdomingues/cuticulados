package org.cuticulados.pm.controller.usuario;

import org.cuticulados.pm.entity.TipoUsuario;

public record UsuarioRequest(
        String nome,
        String email,
        String login,
        String senha,
        TipoUsuario tipo
) {
}
