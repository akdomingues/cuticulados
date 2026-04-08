package org.cuticulados.pm.service;

import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.entity.Usuario;
import org.cuticulados.pm.entity.TipoUsuario;
import org.cuticulados.pm.repository.UsuarioRepository;

public class UsuarioService {

    private final UsuarioRepository usuarioRepo = new UsuarioRepository();

    public void cadastrarUsuario(Usuario usuario) {
        try {
            if (usuario.getLogin() == null || usuario.getLogin().isBlank()) {
                System.out.println("Login e obrigatorio.");
                return;
            }
            if (usuario.getSenha() == null || usuario.getSenha().length() < 4) {
                System.out.println("Senha deve ter no minimo 4 caracteres.");
                return;
            }
            usuarioRepo.salvar(usuario);
            System.out.println("Usuario " + usuario.getNome() + " cadastrado.");
        } catch (Exception e) {
            System.out.println("Erro ao cadastrar usuario: " + e.getMessage());
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

    public void removerUsuario(Long id) {
        try {
            usuarioRepo.deletarLogico(id);
            System.out.println("Usuario removido (soft delete).");
        } catch (Exception e) {
            System.out.println("Erro ao remover usuario: " + e.getMessage());
        }
    }
}

