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

/**
 * Serviço responsável pela geração de relatórios do salão.
 *
 * <p>Consolida informações de agendamentos, movimentações financeiras
 * e estoque para exibição no terminal. Também calcula o saldo do caixa
 * com base no total de entradas e saídas registradas.</p>
 */
public class RelatorioService {

    /** Repositório de agendamentos para consultas do relatório. */
    private final AgendamentoRepository agendamentoRepo = new AgendamentoRepository();

    /** Repositório de transações para cálculo financeiro. */
    private final TransacaoRepository transacaoRepo = new TransacaoRepository();

    /** Repositório de produtos para relatório de estoque. */
    private final ProdutoRepository produtoRepo = new ProdutoRepository();

    /**
     * Gera e exibe no terminal o relatório de agendamentos de um período.
     *
     * @param inicio data de início do período
     * @param fim    data de fim do período
     */
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

    /**
     * Calcula e retorna o saldo financeiro atual do salão.
     *
     * <p>O saldo é obtido subtraindo o total de saídas do total de entradas.</p>
     *
     * @return saldo atual (entradas - saídas)
     */
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

    /**
     * Gera e exibe no terminal o relatório financeiro de um período,
     * listando todas as transações e os totais de entradas, saídas e saldo.
     *
     * @param inicio data de início do período
     * @param fim    data de fim do período
     */
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

    /**
     * Gera e exibe no terminal o relatório de estoque atual, sinalizando
     * os produtos com quantidade abaixo do mínimo configurado.
     */
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

//=====
    /**
     * Exibe no terminal o saldo geral do caixa do salão.
     *
     * <p>Reutiliza o método {@link #calcularSaldo()} para evitar
     * duplicação de código. O saldo é composto por entradas e saídas
     * buscadas no banco de dados via {@code TransacaoRepository}.</p>
     */
    public void imprimirSaldo() {
        try {
            Double entradas = transacaoRepo.somarPorTipo(TipoTransacao.ENTRADA);
            Double saidas   = transacaoRepo.somarPorTipo(TipoTransacao.SAIDA);
            Double saldo    = calcularSaldo(); // reutiliza o metodo existente

            System.out.println("=== Saldo Geral ===");
            System.out.printf(" Total entradas: R$ %.2f%n", entradas);
            System.out.printf(" Total saidas:   R$ %.2f%n", saidas);
            System.out.printf(" Saldo atual:    R$ %.2f%n", saldo);
        } catch (Exception e) {
            System.out.println("Erro ao imprimir saldo: " + e.getMessage());
        }
    }
//=====
}