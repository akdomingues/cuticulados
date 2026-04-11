package org.cuticulados.pm.service;

import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.entity.TipoUsuario;
import org.cuticulados.pm.entity.Usuario;
import org.cuticulados.pm.repository.UsuarioRepository;

/**
 * Regras de negócio de usuários: cadastro, autenticação e soft delete.
 *
 * O perfil (ADMIN, PROFISSIONAL, CLIENTE) é controlado pelo enum {@link TipoUsuario}
 * na própria entidade, sem necessidade de classes separadas.
 * A autenticação compara login e senha diretamente e retorna o usuário para
 * que o menu correto seja exibido pela camada de apresentação.
 */
public class UsuarioService {

    /** Repositório para acesso aos dados de usuário no banco. */
    private final UsuarioRepository usuarioRepo = new UsuarioRepository();

    /**
     * Cadastra um usuário verificando se o login foi informado e se a senha tem ao menos 4 caracteres.
     *
     * @param usuario objeto com os dados do usuário
     */
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

    /**
     * Autentica um usuário comparando login e senha.
     * Se válidos, retorna o usuário para que o menu correto seja exibido conforme o perfil.
     *
     * @param login login do usuário
     * @param senha senha informada
     * @return Optional com o usuário autenticado, ou vazio se as credenciais forem inválidas
     */
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

    /**
     * Busca um usuário pelo login.
     *
     * @param login login do usuário
     * @return o usuário encontrado, ou {@code null} se não existir
     */
    public Usuario buscarPorLogin(String login) {
        try {
            return usuarioRepo.buscarPorLogin(login).orElse(null);
        } catch (Exception e) {
            System.out.println("Erro ao buscar usuario: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lista todos os usuários ativos (não excluídos logicamente).
     *
     * @return lista de usuários ativos
     */
    public List<Usuario> listarTodos() {
        try {
            return usuarioRepo.listarTodos();
        } catch (Exception e) {
            System.out.println("Erro ao listar usuarios: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Lista usuários ativos filtrados pelo perfil usando stream em memória.
     *
     * @param tipo ADMIN, PROFISSIONAL ou CLIENTE
     * @return lista de usuários com o perfil informado
     */
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

    /**
     * Realiza soft delete: preenche {@code deletedAt} sem remover o registro,
     * preservando o histórico de agendamentos do usuário.
     *
     * @param id identificador do usuário
     */
    public void removerUsuario(Long id) {
        try {
            usuarioRepo.deletarLogico(id);
            System.out.println("Usuario removido (soft delete).");
        } catch (Exception e) {
            System.out.println("Erro ao remover usuario: " + e.getMessage());
        }
    }
}