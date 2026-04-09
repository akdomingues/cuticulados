package org.cuticulados.pm.service;

import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.entity.TipoUsuario;
import org.cuticulados.pm.entity.Usuario;
import org.cuticulados.pm.repository.UsuarioRepository;

/**
 * Serviço responsável pelas regras de negócio relacionadas a usuários.
 *
 * <p>Controla o cadastro, autenticação e remoção lógica de usuários.
 * O controle de perfil (ADMIN, PROFISSIONAL, CLIENTE) é feito pelo
 * ENUM {@link TipoUsuario} dentro da própria entidade {@link Usuario},
 * sem necessidade de classes separadas para cada tipo.</p>
 *
 * <p>A autenticação compara o login e senha diretamente,
 * retornando o usuário logado para que a camada de apresentação
 * possa verificar as permissões pelo campo {@code tipo}.</p>
 */
public class UsuarioService {

    /** Repositório para acesso aos dados de usuário no banco. */
    private final UsuarioRepository usuarioRepo = new UsuarioRepository();

    /**
     * Cadastra um novo usuário com validações obrigatórias.
     *
     * <p>Verifica se o login foi informado e se a senha tem pelo menos 4 caracteres.</p>
     *
     * @param usuario objeto com os dados do usuário a ser cadastrado
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
     * Autentica um usuário verificando login e senha.
     *
     * <p>Se o login e senha corresponderem a um usuário ativo no banco,
     * retorna o objeto do usuário para que o sistema saiba o tipo/perfil
     * (ADMIN, PROFISSIONAL ou CLIENTE) e ajuste o menu exibido.</p>
     *
     * @param login login do usuário
     * @param senha senha informada
     * @return {@code Optional} com o usuário autenticado, ou vazio se as credenciais forem inválidas
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
     * Lista usuários filtrados pelo tipo/perfil.
     *
     * <p>Usa stream para filtrar em memória após buscar todos os usuários ativos.
     * Útil para listar apenas administradores ou apenas profissionais.</p>
     *
     * @param tipo perfil desejado (ADMIN, PROFISSIONAL ou CLIENTE)
     * @return lista de usuários com o tipo informado
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
     * Realiza a exclusão lógica de um usuário (soft delete).
     *
     * <p>O registro não é removido do banco; apenas a data de exclusão
     * é preenchida. Assim, o histórico de agendamentos é preservado.</p>
     *
     * @param id identificador do usuário a ser excluído logicamente
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