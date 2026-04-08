package org.cuticulados.pm.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.cuticulados.pm.entity.Agendamento;
import org.cuticulados.pm.entity.Produto;
import org.cuticulados.pm.entity.TipoTransacao;
import org.cuticulados.pm.entity.TransacaoFinanceira;
import org.cuticulados.pm.repository.AgendamentoRepository;
import org.cuticulados.pm.repository.ProdutoRepository;
import org.cuticulados.pm.repository.TransacaoRepository;

public class RelatorioService {

    private final AgendamentoRepository agendamentoRepo = new AgendamentoRepository();
    private final TransacaoRepository transacaoRepo = new TransacaoRepository();
    private final ProdutoRepository produtoRepo = new ProdutoRepository();

    public void gerarRelatorioAgendamentos(LocalDate inicio, LocalDate fim) {
        try {
            List<Agendamento> lista = agendamentoRepo.buscarPorPeriodo(
                    inicio.atStartOfDay(), fim.atTime(23, 59, 59));
            if (lista.isEmpty()) {
                System.out.println("Nenhum agendamento no periodo.");
                return;
            }
            System.out.println("=== Agendamentos de " + inicio + " ate " + fim + " ===");
            for (Agendamento a : lista) {
                System.out.printf(" #%d | %s | %s | %s | R$ %.2f%n",
                        a.getId(),
                        a.getDataHoraInicio(),
                        a.getCliente().getNome(),
                        a.getProfissional().getNome(),
                        a.getValorFinal());
            }
        } catch (Exception e) {
            System.out.println("Erro ao gerar relatorio de agendamentos: " + e.getMessage());
        }
    }

    /** regra de negocio 5: calcular saldo financeiro */
    public Double calcularSaldo() {
        try {
            Double entradas = transacaoRepo.somarPorTipo(TipoTransacao.ENTRADA);
            Double saidas = transacaoRepo.somarPorTipo(TipoTransacao.SAIDA);
            return entradas - saidas;
        } catch (Exception e) {
            System.out.println("Erro ao calcular saldo: " + e.getMessage());
            return 0.0;
        }
    }

    public void gerarRelatorioFinanceiro(LocalDate inicio, LocalDate fim) {
        try {
            LocalDateTime dtInicio = inicio.atStartOfDay();
            LocalDateTime dtFim = fim.atTime(23, 59, 59);
            List<TransacaoFinanceira> transacoes = transacaoRepo.buscarPorPeriodo(dtInicio, dtFim);

            double totalEntradas = 0;
            double totalSaidas = 0;

            System.out.println("=== Relatorio Financeiro ===");
            for (TransacaoFinanceira t : transacoes) {
                System.out.printf(" %s | %s | %s | R$ %.2f%n",
                        t.getDataTransacao(),
                        t.getTipo(),
                        t.getDescricao(),
                        t.getValor());
                if (t.getTipo() == TipoTransacao.ENTRADA) {
                    totalEntradas += t.getValor();
                } else {
                    totalSaidas += t.getValor();
                }
            }

            System.out.printf("%nTotal entradas: R$ %.2f%n", totalEntradas);
            System.out.printf("Total saidas:   R$ %.2f%n", totalSaidas);
            System.out.printf("Saldo:          R$ %.2f%n", totalEntradas - totalSaidas);
        } catch (Exception e) {
            System.out.println("Erro ao gerar relatorio financeiro: " + e.getMessage());
        }
    }

    public void gerarRelatorioEstoque() {
        try {
            List<Produto> produtos = produtoRepo.listarTodos();
            System.out.println("=== Estoque Atual ===");
            for (Produto p : produtos) {
                String status = p.getQuantidadeEstoque() <= p.getQuantidadeMinima() ? " [BAIXO]" : "";
                System.out.printf(" %s | estoque: %d | minimo: %d | preco venda: R$ %.2f%s%n",
                        p.getNome(), p.getQuantidadeEstoque(), p.getQuantidadeMinima(),
                        p.getPrecoVenda(), status);
            }
        } catch (Exception e) {
            System.out.println("Erro ao gerar relatorio de estoque: " + e.getMessage());
        }
    }
}

public void imprimirSaldo() {
    try {
        Double entradas = transacaoRepo.somarPorTipo(TipoTransacao.ENTRADA);
        Double saidas   = transacaoRepo.somarPorTipo(TipoTransacao.SAIDA);
        Double saldo    = entradas - saidas;

        System.out.println("=== Saldo Geral ===");
        System.out.printf(" Total entradas: R$ %.2f%n", entradas);
        System.out.printf(" Total saidas:   R$ %.2f%n", saidas);
        System.out.printf(" Saldo atual:    R$ %.2f%n", saldo);
    } catch (Exception e) {
        System.out.println("Erro ao calcular saldo: " + e.getMessage());
    }
}