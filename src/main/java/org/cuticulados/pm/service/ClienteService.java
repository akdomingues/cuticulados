package org.cuticulados.pm.service;

import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.entity.Cliente;
import org.cuticulados.pm.entity.Usuario;
import org.cuticulados.pm.repository.ClienteRepository;

public class ClienteService {

    private final ClienteRepository clienteRepo = new ClienteRepository();

    // --- CRUD basico ---

    public String cadastrarCliente(Cliente cliente) {
        try {
            if (cliente.getCpf() == null || cliente.getCpf().isBlank()) {
                return "CPF é obrigatório.";
            }
            Optional<Cliente> existente = clienteRepo.buscarPorCpf(cliente.getCpf());
            if (existente.isPresent()) {
                return "Já existe um cliente com esse CPF.";
            }
            cliente.setTipoCliente("novo");
            cliente.setTotalAtendimentosMes(0);
            clienteRepo.salvar(cliente);
            return null;
        } catch (Exception e) {
            return "Erro ao cadastrar cliente: " + e.getMessage();
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

    public String atualizarCliente(Cliente cliente) {
        try {
            Optional<Cliente> existente = clienteRepo.buscarPorId(cliente.getId());
            if (existente.isEmpty()) {
                return "Cliente não encontrado.";
            }
            clienteRepo.salvar(cliente);
            return null;
        } catch (Exception e) {
            return "Erro ao atualizar cliente: " + e.getMessage();
        }
    }

    public String removerCliente(Long id) {
        try {
            Optional<Cliente> existente = clienteRepo.buscarPorId(id);
            if (existente.isEmpty()) {
                return "Cliente não encontrado.";
            }
            clienteRepo.deletar(id);
            return null;
        } catch (Exception e) {
            return "Erro ao remover cliente: " + e.getMessage();
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

