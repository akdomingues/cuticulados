package org.cuticulados.pm.controller.usuario;

import org.cuticulados.pm.controller.usuario.mapper.UsuarioMapper;
import org.cuticulados.pm.entity.TipoUsuario;
import org.cuticulados.pm.entity.UsuarioEntity;
import org.cuticulados.pm.service.UsuarioService;

import java.util.List;
import java.util.Optional;

public class UsuarioController {

    private final UsuarioService usuarioService = new UsuarioService();

    public String cadastrarUsuario(UsuarioRequest request) {
        UsuarioEntity entity = UsuarioMapper.dtoToEntity(request);
        return usuarioService.cadastrarUsuario(entity);
    }

    public Optional<UsuarioEntity> autenticar(String login, String senha) {
        return usuarioService.autenticar(login, senha);
    }

    public UsuarioEntity buscarPorLogin(String login) {
        return usuarioService.buscarPorLogin(login);
    }

    public List<UsuarioEntity> listarTodos() {
        return usuarioService.listarTodos();
    }

    public List<UsuarioEntity> listarPorTipo(TipoUsuario tipo) {
        return usuarioService.listarPorTipo(tipo);
    }

    public String removerUsuario(Long id) {
        return usuarioService.removerUsuario(id);
    }
}
