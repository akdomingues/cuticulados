package org.cuticulados.pm.controller.servico;

import org.cuticulados.pm.controller.servico.mapper.ServicoMapper;
import org.cuticulados.pm.entity.ServicoEntity;
import org.cuticulados.pm.service.ServicoService;

import java.util.List;
import java.util.Optional;

public class ServicoController {

    private final ServicoService servicoService = new ServicoService();

    public String cadastrarServico(ServicoRequest request) {
        ServicoEntity entity = ServicoMapper.dtoToEntity(request);
        return servicoService.cadastrarServico(entity);
    }

    // Sobrecarga para quando a UI já monta a entidade com todos os dados
    public String cadastrarServico(ServicoEntity entity) {
        return servicoService.cadastrarServico(entity);
    }

    public Optional<ServicoEntity> buscarPorId(Long id) {
        return servicoService.buscarPorId(id);
    }

    public List<ServicoEntity> listarTodos() {
        return servicoService.listarTodos();
    }

    public String atualizarServico(ServicoEntity servicoEntity) {
        return servicoService.atualizarServico(servicoEntity);
    }

    public String removerServico(Long id) {
        return servicoService.removerServico(id);
    }

    public List<ServicoEntity> buscarPorTermo(String termo) {
        return servicoService.buscarPorTermo(termo);
    }
}
