package org.cuticulados.pm.controller.relatorio.mapper;

import org.cuticulados.pm.controller.relatorio.RelatorioRequest;

import java.time.LocalDate;

public class RelatorioMapper {

    // Retorna a data de inicio do request ou, se nula, o inicio do mes atual
    public static LocalDate resolverInicio(RelatorioRequest request) {
        if (request.dataInicio() != null) {
            return request.dataInicio();
        }
        return LocalDate.now().withDayOfMonth(1);
    }

    // Retorna a data de fim do request ou, se nula, hoje
    public static LocalDate resolverFim(RelatorioRequest request) {
        if (request.dataFim() != null) {
            return request.dataFim();
        }
        return LocalDate.now();
    }
}
