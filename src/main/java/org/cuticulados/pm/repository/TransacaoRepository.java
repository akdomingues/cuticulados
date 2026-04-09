package org.cuticulados.pm.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.TipoTransacao;
import org.cuticulados.pm.entity.TransacaoFinanceira;

import jakarta.persistence.EntityManager;

/**
 * Repositório responsável pelo acesso e manipulação dos dados de {@link TransacaoFinanceira}.
 *
 * <p>Fornece métodos para registrar movimentações financeiras e calcular
 * totais por tipo (entrada/saída), usados pelo {@code RelatorioService}
 * para gerar o saldo do caixa.</p>
 */
public class TransacaoRepository {

    /**
     * Salva ou atualiza uma transação financeira no banco de dados.
     *
     * @param transacao objeto a ser persistido
     */
    public void salvar(TransacaoFinanceira transacao) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            if (transacao.getId() == null) {
                em.persist(transacao);
            } else {
                em.merge(transacao);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Erro ao salvar transacao: " + e.getMessage());
        }
    }

    /**
     * Busca uma transação financeira pelo ID.
     *
     * @param id identificador da transação
     * @return {@code Optional} com a transação encontrada, ou vazio se não existir
     */
    public Optional<TransacaoFinanceira> buscarPorId(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            TransacaoFinanceira t = em.find(TransacaoFinanceira.class, id);
            return Optional.ofNullable(t);
        } catch (Exception e) {
            System.err.println("Erro ao buscar transacao: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Lista todas as transações financeiras em ordem decrescente de data.
     *
     * @return lista de todas as transações
     */
    public List<TransacaoFinanceira> listarTodas() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "FROM TransacaoFinanceira t ORDER BY t.dataTransacao DESC",
                            TransacaoFinanceira.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar transacoes: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Busca transações financeiras dentro de um período de tempo.
     *
     * @param inicio data/hora de início do período
     * @param fim    data/hora de fim do período
     * @return lista de transações no período, ordenada por data
     */
    public List<TransacaoFinanceira> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT t FROM TransacaoFinanceira t " +
                                    "WHERE t.dataTransacao BETWEEN :inicio AND :fim " +
                                    "ORDER BY t.dataTransacao",
                            TransacaoFinanceira.class)
                    .setParameter("inicio", inicio)
                    .setParameter("fim", fim)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar transacoes por periodo: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Busca transações filtradas pelo tipo (ENTRADA ou SAIDA).
     *
     * @param tipo tipo da transação desejada
     * @return lista de transações do tipo informado, ordenada por data
     */
    public List<TransacaoFinanceira> buscarPorTipo(TipoTransacao tipo) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT t FROM TransacaoFinanceira t " +
                                    "WHERE t.tipo = :tipo " +
                                    "ORDER BY t.dataTransacao DESC",
                            TransacaoFinanceira.class)
                    .setParameter("tipo", tipo)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar transacoes por tipo: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Soma o valor total de todas as transações de um determinado tipo.
     *
     * <p>Usado para calcular o total de entradas e o total de saídas
     * separadamente, permitindo ao {@code RelatorioService} calcular o saldo.</p>
     *
     * @param tipo tipo da transação (ENTRADA ou SAIDA)
     * @return soma dos valores; retorna 0.0 se não houver transações
     */
    public Double somarPorTipo(TipoTransacao tipo) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            Double resultado = em.createQuery(
                            "SELECT SUM(t.valor) FROM TransacaoFinanceira t WHERE t.tipo = :tipo",
                            Double.class)
                    .setParameter("tipo", tipo)
                    .getSingleResult();
            return resultado != null ? resultado : 0.0;
        } catch (Exception e) {
            System.err.println("Erro ao somar transacoes por tipo: " + e.getMessage());
            return 0.0;
        }
    }
}