package org.cuticulados.pm.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.entity.Agendamento;
import org.cuticulados.pm.entity.AgendamentoServico;
import org.cuticulados.pm.entity.Cliente;
import org.cuticulados.pm.entity.StatusAgendamento;
import org.cuticulados.pm.repository.AgendamentoRepository;

/**
 * Regras de negócio de agendamentos.
 *
 * Além do CRUD, controla: conflito de horário (RN1), desconto de 10% para
 * clientes frequentes (RN2), e transições de status — não conclui cancelado (RN3),
 * não cancela concluído (RN4), não remove concluído (RN5).
 */
public class AgendamentoService {

    /** Repositório para acesso aos dados de agendamento no banco. */
    private final AgendamentoRepository agendamentoRepo = new AgendamentoRepository();

    /**
     * Cria um agendamento validando cliente, profissional, conflito de horário
     * e calculando o valor final antes de persistir.
     *
     * @param agendamento objeto com os dados do agendamento
     */
    public void criarAgendamento(Agendamento agendamento) {
        try {
            if (agendamento.getCliente() == null || agendamento.getProfissional() == null) {
                System.out.println("Cliente e profissional sao obrigatorios.");
                return;
            }

            // RN1: verifica conflito de horario do profissional
            boolean conflita = agendamentoRepo.existeConflito(
                    agendamento.getProfissional(),
                    agendamento.getDataHoraInicio(),
                    agendamento.getDataHoraFim());
            if (conflita) {
                System.out.println("Conflito de horario! O profissional ja tem atendimento nesse periodo.");
                return;
            }

            calcularValorFinal(agendamento);
            agendamento.setStatus(StatusAgendamento.PENDENTE);
            agendamentoRepo.salvar(agendamento);
            System.out.println("Agendamento criado com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro ao criar agendamento: " + e.getMessage());
        }
    }

    /**
     * Busca um agendamento pelo ID.
     *
     * @param id identificador do agendamento
     * @return {@code Optional} com o agendamento, ou vazio se não encontrado
     */
    public Optional<Agendamento> buscarPorId(Long id) {
        try {
            return agendamentoRepo.buscarPorId(id);
        } catch (Exception e) {
            System.out.println("Erro ao buscar agendamento: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Lista todos os agendamentos cadastrados.
     *
     * @return lista de agendamentos
     */
    public List<Agendamento> listarTodos() {
        try {
            return agendamentoRepo.listarTodos();
        } catch (Exception e) {
            System.out.println("Erro ao listar agendamentos: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Atualiza os dados de um agendamento existente.
     *
     * @param agendamento objeto com os dados atualizados (deve ter ID preenchido)
     */
    public void atualizarAgendamento(Agendamento agendamento) {
        try {
            if (agendamentoRepo.buscarPorId(agendamento.getId()).isEmpty()) {
                System.out.println("Agendamento nao encontrado.");
                return;
            }
            agendamentoRepo.salvar(agendamento);
            System.out.println("Agendamento atualizado.");
        } catch (Exception e) {
            System.out.println("Erro ao atualizar agendamento: " + e.getMessage());
        }
    }

    /**
     * Remove um agendamento pelo ID.
     * RN5: não permite remover agendamentos concluídos para preservar o histórico financeiro.
     *
     * @param id identificador do agendamento
     */
    public void removerAgendamento(Long id) {
        try {
            Optional<Agendamento> existente = agendamentoRepo.buscarPorId(id);
            if (existente.isEmpty()) {
                System.out.println("Agendamento nao encontrado.");
                return;
            }
            Agendamento a = existente.get();
            if (a.getStatus() == StatusAgendamento.CONCLUIDO) {
                System.out.println("Nao e permitido remover agendamento concluido.");
                return;
            }
            agendamentoRepo.deletar(id);
            System.out.println("Agendamento removido.");
        } catch (Exception e) {
            System.out.println("Erro ao remover agendamento: " + e.getMessage());
        }
    }

    /**
     * Calcula o valor final somando os serviços e aplicando descontos.
     * RN2: clientes "frequentes" (≥ 3 atendimentos no mês) recebem 10% de desconto.
     *
     * @param agendamento agendamento com a lista de serviços preenchida
     */
    public void calcularValorFinal(Agendamento agendamento) {
        try {
            double total = 0.0;
            if (agendamento.getServicos() != null) {
                for (AgendamentoServico as : agendamento.getServicos()) {
                    double parcial = as.getPrecoAplicado() * as.getQuantidade();
                    parcial -= (parcial * as.getDescontoAplicado() / 100.0);
                    total += parcial;
                }
            }

            // RN2: desconto de fidelidade para clientes frequentes
            Cliente cliente = agendamento.getCliente();
            if (cliente != null && "frequente".equals(cliente.getTipoCliente())) {
                total *= 0.9;
            }

            agendamento.setValorFinal(total);
        } catch (Exception e) {
            System.out.println("Erro ao calcular valor final: " + e.getMessage());
            agendamento.setValorFinal(0.0);
        }
    }

    /**
     * Muda o status do agendamento para CONCLUIDO.
     * RN3: não é possível concluir um agendamento já cancelado.
     *
     * @param id identificador do agendamento
     */
    public void concluirAgendamento(Long id) {
        try {
            Optional<Agendamento> op = agendamentoRepo.buscarPorId(id);
            if (op.isEmpty()) {
                System.out.println("Agendamento nao encontrado.");
                return;
            }
            Agendamento a = op.get();
            if (a.getStatus() == StatusAgendamento.CONCLUIDO) {
                System.out.println("Agendamento ja esta concluido.");
                return;
            }
            if (a.getStatus() == StatusAgendamento.CANCELADO) {
                System.out.println("Nao e possivel concluir um agendamento cancelado.");
                return;
            }
            a.setStatus(StatusAgendamento.CONCLUIDO);
            agendamentoRepo.salvar(a);
            System.out.println("Agendamento #" + id + " concluido.");
        } catch (Exception e) {
            System.out.println("Erro ao concluir agendamento: " + e.getMessage());
        }
    }

    /**
     * Muda o status do agendamento para CANCELADO.
     * RN4: não é possível cancelar um agendamento já concluído.
     *
     * @param id identificador do agendamento
     */
    public void cancelarAgendamento(Long id) {
        try {
            Optional<Agendamento> op = agendamentoRepo.buscarPorId(id);
            if (op.isEmpty()) {
                System.out.println("Agendamento nao encontrado.");
                return;
            }
            Agendamento a = op.get();
            if (a.getStatus() == StatusAgendamento.CONCLUIDO) {
                System.out.println("Nao e possivel cancelar um agendamento concluido.");
                return;
            }
            a.setStatus(StatusAgendamento.CANCELADO);
            agendamentoRepo.salvar(a);
            System.out.println("Agendamento #" + id + " cancelado.");
        } catch (Exception e) {
            System.out.println("Erro ao cancelar agendamento: " + e.getMessage());
        }
    }

    /**
     * Busca agendamentos dentro de um período de tempo.
     *
     * @param inicio data/hora de início
     * @param fim    data/hora de fim
     * @return lista de agendamentos no período
     */
    public List<Agendamento> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        try {
            return agendamentoRepo.buscarPorPeriodo(inicio, fim);
        } catch (Exception e) {
            System.out.println("Erro ao buscar agendamentos por periodo: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Busca agendamentos filtrados pelo status.
     *
     * @param status situação desejada
     * @return lista de agendamentos com o status informado
     */
    public List<Agendamento> buscarPorStatus(StatusAgendamento status) {
        try {
            return agendamentoRepo.buscarPorStatus(status);
        } catch (Exception e) {
            System.out.println("Erro ao buscar agendamentos por status: " + e.getMessage());
            return List.of();
        }
    }
}