package org.cuticulados.pm.service;

import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.entity.Usuario;
import org.cuticulados.pm.entity.TipoUsuario;
import org.cuticulados.pm.repository.UsuarioRepository;

public class UsuarioService {

    private final UsuarioRepository usuarioRepo = new UsuarioRepository();

    public String cadastrarUsuario(Usuario usuario) {
        try {
            if (usuario.getLogin() == null || usuario.getLogin().isBlank()) {
                return "Login é obrigatório.";
            }
            if (usuario.getSenha() == null || usuario.getSenha().length() < 4) {
                return "Senha deve ter no mínimo 4 caracteres.";
            }
            usuarioRepo.salvar(usuario);
            return null;
        } catch (Exception e) {
            return "Erro ao cadastrar usuário: " + e.getMessage();
        }
    }

    public Optional<Usuario> autenticar(String login, String senha) {
        try {
            Optional<Usuario> op = usuarioRepo.buscarPorLogin(login);
            if (op.isPresent() && op.get().getSenha().equals(senha)) {
                return op;
            }
            System.out.println("Login ou senha incorretos.");
            return Optional.empty();
        } catch (Exception e) {
            System.out.println("Erro ao autenticar: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Usuario buscarPorLogin(String login) {
        try {
            return usuarioRepo.buscarPorLogin(login).orElse(null);
        } catch (Exception e) {
            System.out.println("Erro ao buscar usuario: " + e.getMessage());
            return null;
        }
    }

    public List<Usuario> listarTodos() {
        try {
            return usuarioRepo.listarTodos();
        } catch (Exception e) {
            System.out.println("Erro ao listar usuarios: " + e.getMessage());
            return List.of();
        }
    }

    public List<Usuario> listarPorTipo(TipoUsuario tipo) {
        try {
            return usuarioRepo.listarTodos().stream()
                    .filter(u -> u.getTipo() == tipo)
                    .toList();
        } catch (Exception e) {
            System.out.println("Erro ao listar usuarios por tipo: " + e.getMessage());
            return List.of();
        }
    }

    public String removerUsuario(Long id) {
        try {
            usuarioRepo.deletarLogico(id);
            return null;
        } catch (Exception e) {
            return "Erro ao remover profissional: " + e.getMessage();
        }
    }
}

