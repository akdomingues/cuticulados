package org.cuticulados.pm.service;

import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.entity.ClienteEntity;
import org.cuticulados.pm.repository.ClienteRepository;

public class ClienteService {

    private final ClienteRepository clienteRepo = new ClienteRepository();

    // --- CRUD basico ---

    public String cadastrarCliente(ClienteEntity clienteEntity) {
        try {
            if (clienteEntity.getCpf() == null || clienteEntity.getCpf().isBlank()) {
                return "CPF é obrigatório.";
            }
            Optional<ClienteEntity> existente = clienteRepo.buscarPorCpf(clienteEntity.getCpf());
            if (existente.isPresent()) {
                return "Já existe um cliente com esse CPF.";
            }
            clienteEntity.setTipoCliente("novo");
            clienteEntity.setTotalAtendimentosMes(0);
            clienteRepo.salvar(clienteEntity);
            return null;
        } catch (Exception e) {
            return "Erro ao cadastrar cliente: " + e.getMessage();
        }
    }

    public Optional<ClienteEntity> buscarPorId(Long id) {
        try {
            return clienteRepo.buscarPorId(id);
        } catch (Exception e) {
            System.out.println("Erro ao buscar cliente: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<ClienteEntity> listarTodos() {
        try {
            return clienteRepo.listarTodos();
        } catch (Exception e) {
            System.out.println("Erro ao listar clientes: " + e.getMessage());
            return List.of();
        }
    }

    public String atualizarCliente(ClienteEntity clienteEntity) {
        try {
            Optional<ClienteEntity> existente = clienteRepo.buscarPorId(clienteEntity.getId());
            if (existente.isEmpty()) {
                return "Cliente não encontrado.";
            }
            clienteRepo.salvar(clienteEntity);
            return null;
        } catch (Exception e) {
            return "Erro ao atualizar cliente: " + e.getMessage();
        }
    }

    public String removerCliente(Long id) {
        try {
            Optional<ClienteEntity> existente = clienteRepo.buscarPorId(id);
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
    public String verificarFidelidade(ClienteEntity clienteEntity) {
        try {
            Integer atendimentos = clienteEntity.getTotalAtendimentosMes();
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

