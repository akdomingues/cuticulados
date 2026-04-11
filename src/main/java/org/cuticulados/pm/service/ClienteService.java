package org.cuticulados.pm.service;

import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.entity.Cliente;
import org.cuticulados.pm.repository.ClienteRepository;

/**
 * Regras de negócio de clientes.
 *
 * Além do CRUD, classifica o cliente como "frequente" ao atingir 3 ou mais
 * atendimentos no mês — o que também dá direito a desconto nos agendamentos.
 */
public class ClienteService {

    /** Repositório para acesso aos dados de cliente no banco. */
    private final ClienteRepository clienteRepo = new ClienteRepository();

    /**
     * Cadastra um cliente verificando se o CPF foi informado e se não está duplicado.
     * Novos clientes iniciam com tipo "novo" e zero atendimentos no mês.
     *
     * @param cliente objeto com os dados do cliente
     */
    public void cadastrarCliente(Cliente cliente) {
        try {
            if (cliente.getCpf() == null || cliente.getCpf().isBlank()) {
                System.out.println("CPF e obrigatorio.");
                return;
            }
            Optional<Cliente> existente = clienteRepo.buscarPorCpf(cliente.getCpf());
            if (existente.isPresent()) {
                System.out.println("Ja existe um cliente com esse CPF.");
                return;
            }
            cliente.setTipoCliente("novo");
            cliente.setTotalAtendimentosMes(0);
            clienteRepo.salvar(cliente);
            System.out.println("Cliente " + cliente.getNome() + " cadastrado com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro ao cadastrar cliente: " + e.getMessage());
        }
    }

    /**
     * Busca um cliente pelo ID.
     *
     * @param id identificador do cliente
     * @return {@code Optional} com o cliente, ou vazio se não encontrado
     */
    public Optional<Cliente> buscarPorId(Long id) {
        try {
            return clienteRepo.buscarPorId(id);
        } catch (Exception e) {
            System.out.println("Erro ao buscar cliente: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Lista todos os clientes cadastrados.
     *
     * @return lista de clientes
     */
    public List<Cliente> listarTodos() {
        try {
            return clienteRepo.listarTodos();
        } catch (Exception e) {
            System.out.println("Erro ao listar clientes: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Atualiza os dados de um cliente existente.
     *
     * @param cliente objeto com os dados atualizados (deve ter ID preenchido)
     */
    public void atualizarCliente(Cliente cliente) {
        try {
            Optional<Cliente> existente = clienteRepo.buscarPorId(cliente.getId());
            if (existente.isEmpty()) {
                System.out.println("Cliente nao encontrado.");
                return;
            }
            clienteRepo.salvar(cliente);
            System.out.println("Cliente atualizado com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro ao atualizar cliente: " + e.getMessage());
        }
    }

    /**
     * Remove um cliente pelo ID.
     *
     * @param id identificador do cliente a ser removido
     */
    public void removerCliente(Long id) {
        try {
            Optional<Cliente> existente = clienteRepo.buscarPorId(id);
            if (existente.isEmpty()) {
                System.out.println("Cliente nao encontrado.");
                return;
            }
            clienteRepo.deletar(id);
            System.out.println("Cliente removido com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro ao remover cliente: " + e.getMessage());
        }
    }

    /**
     * Retorna o nível de fidelidade do cliente baseado nos atendimentos do mês.
     * Com 3 ou mais atendimentos retorna "frequente" (10% de desconto); abaixo disso, "novo".
     *
     * @param cliente o cliente a ser avaliado
     * @return "frequente" ou "novo"
     */
    public String verificarFidelidade(Cliente cliente) {
        try {
            Integer atendimentos = cliente.getTotalAtendimentosMes();
            if (atendimentos == null) atendimentos = 0;
            if (atendimentos >= 3) {
                return "frequente";
            }
            return "novo";
        } catch (Exception e) {
            System.out.println("Erro ao verificar fidelidade: " + e.getMessage());
            return "erro";
        }
    }
}