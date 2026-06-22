package org.cuticulados.pm.controller.agendamento;

import org.cuticulados.pm.controller.agendamento.mapper.AgendamentoMapper;
import org.cuticulados.pm.entity.AgendamentoEntity;
import org.cuticulados.pm.service.AgendamentoService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AgendamentoController {

    private final AgendamentoService agendamentoService = new AgendamentoService();

    public String criarAgendamento(AgendamentoRequest request) {
        AgendamentoEntity entity = AgendamentoMapper.dtoToEntity(request);
        return agendamentoService.criarAgendamento(entity);
    }

    // Sobrecarga para quando a UI já monta a entidade com todos os dados
    public String criarAgendamento(AgendamentoEntity agendamentoEntity) {
        return agendamentoService.criarAgendamento(agendamentoEntity);
    }

    public Optional<AgendamentoEntity> buscarPorId(Long id) {
        return agendamentoService.buscarPorId(id);
    }

    public List<AgendamentoEntity> listarTodos() {
        return agendamentoService.listarTodos();
    }

    public void atualizarAgendamento(AgendamentoEntity agendamentoEntity) {
        agendamentoService.atualizarAgendamento(agendamentoEntity);
    }

    public void concluirAgendamento(Long id) {
        agendamentoService.concluirAgendamento(id);
    }

    public void cancelarAgendamento(Long id) {
        agendamentoService.cancelarAgendamento(id);
    }

    public void removerAgendamento(Long id) {
        agendamentoService.removerAgendamento(id);
    }

    public List<AgendamentoEntity> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return agendamentoService.buscarPorPeriodo(inicio, fim);
    }
}
