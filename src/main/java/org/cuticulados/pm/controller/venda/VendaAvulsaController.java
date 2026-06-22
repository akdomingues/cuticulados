package org.cuticulados.pm.controller.venda;

import org.cuticulados.pm.controller.venda.mapper.VendaMapper;
import org.cuticulados.pm.entity.ProfissionalEntity;
import org.cuticulados.pm.entity.VendaAvulsaEntity;
import org.cuticulados.pm.service.VendaAvulsaService;

import java.util.List;

public class VendaAvulsaController {

    private final VendaAvulsaService vendaAvulsaService = new VendaAvulsaService();

    public void registrarVenda(VendaRequest request) {
        VendaAvulsaEntity entity = VendaMapper.dtoToEntity(request);
        vendaAvulsaService.registrarVenda(entity);
    }

    // Sobrecarga para quando a UI já monta a entidade completa (produto + profissional já carregados)
    public void registrarVenda(VendaAvulsaEntity venda) {
        vendaAvulsaService.registrarVenda(venda);
    }

    public List<VendaAvulsaEntity> listarTodas() {
        return vendaAvulsaService.listarTodas();
    }

    public void removerVenda(Long id) {
        vendaAvulsaService.removerVenda(id);
    }

    public String fecharVenda(Long id) {
        return vendaAvulsaService.fecharVenda(id);
    }

    public void fecharDia(ProfissionalEntity profissionalEntity) {
        vendaAvulsaService.fecharDia(profissionalEntity);
    }

    public void relatorioVendasDoDia() {
        vendaAvulsaService.relatorioVendasDoDia();
    }
}
