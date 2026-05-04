package org.cuticulados.pm.service;

import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.entity.Cliente;
import org.cuticulados.pm.entity.Usuario;
import org.cuticulados.pm.repository.ClienteRepository;

public class ClienteService {

    private final ClienteRepository clienteRepo = new ClienteRepository();

    // --- CRUD basico ---

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

    public Optional<Cliente> buscarPorId(Long id) {
        try {
            return clienteRepo.buscarPorId(id);
        } catch (Exception e) {
            System.out.println("Erro ao buscar cliente: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<Cliente> listarTodos() {
        try {
            return clienteRepo.listarTodos();
        } catch (Exception e) {
            System.out.println("Erro ao listar clientes: " + e.getMessage());
            return List.of();
        }
    }

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

    // --- regra de negocio 1: verificar fidelidade (sem ser CRUD) ---
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

