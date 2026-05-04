package org.cuticulados.pm.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.cuticulados.pm.config.JpaUtil;
import jakarta.persistence.EntityManager;

import org.cuticulados.pm.entity.Agendamento;
import org.cuticulados.pm.entity.Produto;
import org.cuticulados.pm.entity.TipoTransacao;
import org.cuticulados.pm.entity.TransacaoFinanceira;
import org.cuticulados.pm.repository.AgendamentoRepository;
import org.cuticulados.pm.repository.ProdutoRepository;
import org.cuticulados.pm.repository.TransacaoRepository;

/**
 * Gera relatórios do salão: agendamentos por período, financeiro e estoque.
 * Também calcula o saldo do caixa com base nas entradas e saídas registradas.
 */
public class RelatorioService {

    /**
     * Repositório de agendamentos para consultas do relatório.
     */
    private final AgendamentoRepository agendamentoRepo = new AgendamentoRepository();

    /**
     * Repositório de transações para cálculo financeiro.
     */
    private final TransacaoRepository transacaoRepo = new TransacaoRepository();

    /**
     * Repositório de produtos para relatório de estoque.
     */
    private final ProdutoRepository produtoRepo = new ProdutoRepository();

    /**
     * Gera e exibe no terminal o relatório de agendamentos de um período.
     *
     * @param inicio data de início do período
     * @param fim    data de fim do período
     */
    public void gerarRelatorioAgendamentos(LocalDate inicio, LocalDate fim) {
        try {
            List<Agendamento> lista = agendamentoRepo.buscarPorPeriodo(inicio.atStartOfDay(), fim.atTime(23, 59, 59));
            if (lista.isEmpty()) {
                System.out.println("Nenhum agendamento no periodo.");
                return;
            }
            System.out.println("=== Agendamentos de " + inicio + " ate " + fim + " ===");
            for (Agendamento a : lista) {
                System.out.printf(" #%d | %s | %s | %s | R$ %.2f%n", a.getId(), a.getDataHoraInicio(), a.getCliente().getNome(), a.getProfissional().getNome(), a.getValorFinal());
            }
        } catch (Exception e) {
            System.out.println("Erro ao gerar relatorio de agendamentos: " + e.getMessage());
        }
    }

    /**
     * Calcula o saldo atual do salão (entradas - saídas).
     *
     * @return saldo atual
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
     * Exibe o relatório financeiro do período com todas as transações e os totais de entradas, saídas e saldo.
     *
     * @param inicio data de início
     * @param fim    data de fim
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
                System.out.printf(" %s | %s | %s | R$ %.2f%n", t.getDataTransacao(), t.getTipo(), t.getDescricao(), t.getValor());
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
                System.out.printf(" %s | estoque: %d | minimo: %d | preco venda: R$ %.2f%s%n", p.getNome(), p.getQuantidadeEstoque(), p.getQuantidadeMinima(), p.getPrecoVenda(), status);
            }
        } catch (Exception e) {
            System.out.println("Erro ao gerar relatorio de estoque: " + e.getMessage());
        }
    }

//=====

    /**
     * Exibe o saldo geral do caixa com totais de entradas, saídas e saldo final.
     * Reutiliza {@link #calcularSaldo()} para evitar duplicação.
     */
    public void imprimirSaldo() {
        try {
            Double entradas = transacaoRepo.somarPorTipo(TipoTransacao.ENTRADA);
            Double saidas = transacaoRepo.somarPorTipo(TipoTransacao.SAIDA);
            Double saldo = calcularSaldo(); // reutiliza o metodo existente

            System.out.println("=== Saldo Geral ===");
            System.out.printf(" Total entradas: R$ %.2f%n", entradas);
            System.out.printf(" Total saidas:   R$ %.2f%n", saidas);
            System.out.printf(" Saldo atual:    R$ %.2f%n", saldo);
        } catch (Exception e) {
            System.out.println("Erro ao imprimir saldo: " + e.getMessage());
        }
    }
//=====

    /**
     * RF06: exibe o ranking dos serviços mais realizados no período, com contagem e faturamento.
     *
     * @param inicio data de início
     * @param fim    data de fim
     */
    public void gerarRankingServicos(LocalDate inicio, LocalDate fim) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            List<Object[]> resultado = em.createQuery(
                    "SELECT aserv.servico.descricao, COUNT(aserv.id), SUM(aserv.precoAplicado * aserv.quantidade) " +
                    "FROM AgendamentoServico aserv " +
                    "JOIN aserv.agendamento ag " +
                    "WHERE ag.dataHoraInicio BETWEEN :inicio AND :fim " +
                    "AND ag.status = 'CONCLUIDO' " +
                    "GROUP BY aserv.servico.descricao " +
                    "ORDER BY COUNT(aserv.id) DESC", Object[].class)
                    .setParameter("inicio", inicio.atStartOfDay())
                    .setParameter("fim", fim.atTime(23, 59, 59))
                    .getResultList();

            if (resultado.isEmpty()) {
                System.out.println("Nenhum servico concluido no periodo.");
                return;
            }
            System.out.println("=== Ranking de Servicos (" + inicio + " a " + fim + ") ===");
            int pos = 1;
            for (Object[] row : resultado) {
                System.out.printf(" %d. %s | %dx realizados | R$ %.2f faturado%n",
                        pos++, row[0], row[1], row[2]);
            }
        } catch (Exception e) {
            System.out.println("Erro ao gerar ranking de servicos: " + e.getMessage());
        }
    }
}