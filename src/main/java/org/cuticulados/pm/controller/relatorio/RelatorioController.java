package org.cuticulados.pm.controller.relatorio;

import org.cuticulados.pm.controller.relatorio.mapper.RelatorioMapper;
import org.cuticulados.pm.service.RelatorioService;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RelatorioController {

    private final RelatorioService relatorioService = new RelatorioService();

    // Via Request (para uso futuro / padrão da arquitetura)
    public void gerarRelatorioAgendamentos(RelatorioRequest request) {
        LocalDate inicio = RelatorioMapper.resolverInicio(request);
        LocalDate fim    = RelatorioMapper.resolverFim(request);
        relatorioService.gerarRelatorioAgendamentos(inicio, fim);
    }

    // Sobrecarga que o PainelRelatorios usa (passa LocalDate direto)
    public void gerarRelatorioAgendamentos(LocalDate inicio, LocalDate fim) {
        relatorioService.gerarRelatorioAgendamentos(inicio, fim);
    }

    public void gerarRelatorioFinanceiro(RelatorioRequest request) {
        LocalDate inicio = RelatorioMapper.resolverInicio(request);
        LocalDate fim    = RelatorioMapper.resolverFim(request);
        relatorioService.gerarRelatorioFinanceiro(inicio, fim);
    }

    // Sobrecarga que o PainelRelatorios usa
    public void gerarRelatorioFinanceiro(LocalDate inicio, LocalDate fim) {
        relatorioService.gerarRelatorioFinanceiro(inicio, fim);
    }

    public void gerarRelatorioEstoque() {
        relatorioService.gerarRelatorioEstoque();
    }

    public BigDecimal calcularSaldo() {
        return relatorioService.calcularSaldo();
    }

    public void imprimirSaldo() {
        relatorioService.imprimirSaldo();
    }

    public void gerarRankingServicos(RelatorioRequest request) {
        LocalDate inicio = RelatorioMapper.resolverInicio(request);
        LocalDate fim    = RelatorioMapper.resolverFim(request);
        relatorioService.gerarRankingServicos(inicio, fim);
    }

    // Sobrecarga que o PainelRelatorios usa
    public void gerarRankingServicos(LocalDate inicio, LocalDate fim) {
        relatorioService.gerarRankingServicos(inicio, fim);
    }
}
