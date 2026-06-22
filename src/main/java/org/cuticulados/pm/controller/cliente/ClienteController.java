package org.cuticulados.pm.controller.cliente;

import org.cuticulados.pm.controller.cliente.mapper.ClienteMapper;
import org.cuticulados.pm.entity.ClienteEntity;
import org.cuticulados.pm.service.ClienteService;

import java.util.List;
import java.util.Optional;

public class ClienteController {

    private final ClienteService clienteService = new ClienteService();

    public String cadastrarCliente(ClienteRequest request) {
        ClienteEntity entity = ClienteMapper.dtoToEntity(request);
        return clienteService.cadastrarCliente(entity);
    }

    // Sobrecarga para quando a UI já monta a entidade com todos os dados
    public String cadastrarCliente(ClienteEntity entity) {
        return clienteService.cadastrarCliente(entity);
    }

    public Optional<ClienteEntity> buscarPorId(Long id) {
        return clienteService.buscarPorId(id);
    }

    public List<ClienteEntity> listarTodos() {
        return clienteService.listarTodos();
    }

    public String atualizarCliente(ClienteEntity clienteEntity) {
        return clienteService.atualizarCliente(clienteEntity);
    }

    public String removerCliente(Long id) {
        return clienteService.removerCliente(id);
    }
}
