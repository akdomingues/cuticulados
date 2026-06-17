package org.cuticulados.pm.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.entity.AgendamentoEntity;
import org.cuticulados.pm.entity.AgendamentoServicoEntity;
import org.cuticulados.pm.entity.ClienteEntity;
import org.cuticulados.pm.entity.ProdutoEntity;
import org.cuticulados.pm.entity.ServicoProdutoEntity;
import org.cuticulados.pm.entity.StatusAgendamento;
import org.cuticulados.pm.repository.AgendamentoRepository;
import org.cuticulados.pm.repository.ProdutoRepository;

public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepo = new AgendamentoRepository();
    private final ProdutoRepository produtoRepo = new ProdutoRepository();

    // --- CRUD basico ---

    // retorna null em caso de sucesso, ou a mensagem de erro para exibir na UI
    public String criarAgendamento(AgendamentoEntity agendamentoEntity) {
        try {
            if (agendamentoEntity.getCliente() == null || agendamentoEntity.getProfissional() == null) {
                return "Cliente e profissional são obrigatórios.";
            }

            // regra 1: conflito de horario
            boolean conflita = agendamentoRepo.existeConflito(
                    agendamentoEntity.getProfissional(),
                    agendamentoEntity.getDataHoraInicio(),
                    agendamentoEntity.getDataHoraFim());
            if (conflita) {
                return "Conflito de horário! O profissional já tem atendimento nesse período.";
            }

            // RF05: bloquear se insumos insuficientes
            for (AgendamentoServicoEntity as : agendamentoEntity.getServicos()) {
                if (as.getServico() == null) continue;
                List<ServicoProdutoEntity> insumos = produtoRepo.buscarServicoProdutos(as.getServico().getId());
                for (ServicoProdutoEntity sp : insumos) {
                    ProdutoEntity produtoEntity = sp.getProduto();
                    if (produtoEntity.getQuantidadeEstoque() < as.getQuantidade()) {
                        return "Estoque insuficiente para o produto '" + produtoEntity.getNome()
                                + "'. Disponível: " + produtoEntity.getQuantidadeEstoque()
                                + ", necessário: " + as.getQuantidade();
                    }
                }
            }

            calcularValorFinal(agendamentoEntity);
            agendamentoEntity.setStatus(StatusAgendamento.PENDENTE);
            agendamentoRepo.salvar(agendamentoEntity);
            return null; // sucesso
        } catch (Exception e) {
            return "Erro ao criar agendamento: " + e.getMessage();
        }
    }

    public Optional<AgendamentoEntity> buscarPorId(Long id) {
        try {
            return agendamentoRepo.buscarPorId(id);
        } catch (Exception e) {
            System.out.println("Erro ao buscar agendamento: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<AgendamentoEntity> listarTodos() {
        try {
            return agendamentoRepo.listarTodos();
        } catch (Exception e) {
            System.out.println("Erro ao listar agendamentos: " + e.getMessage());
            return List.of();
        }
    }

    public void atualizarAgendamento(AgendamentoEntity agendamentoEntity) {
        try {
            if (agendamentoRepo.buscarPorId(agendamentoEntity.getId()).isEmpty()) {
                System.out.println("Agendamento nao encontrado.");
                return;
            }
            agendamentoRepo.salvar(agendamentoEntity);
            System.out.println("Agendamento atualizado.");
        } catch (Exception e) {
            System.out.println("Erro ao atualizar agendamento: " + e.getMessage());
        }
    }

    public void removerAgendamento(Long id) {
        try {
            Optional<AgendamentoEntity> existente = agendamentoRepo.buscarPorId(id);
            if (existente.isEmpty()) {
                System.out.println("Agendamento nao encontrado.");
                return;
            }
            AgendamentoEntity a = existente.get();
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

    // --- regra de negocio 2: calcular valor final com desconto de fidelidade ---
    public void calcularValorFinal(AgendamentoEntity agendamentoEntity) {
        try {
            BigDecimal total = BigDecimal.ZERO;
            if (agendamentoEntity.getServicos() != null) {
                for (AgendamentoServicoEntity as : agendamentoEntity.getServicos()) {
                    BigDecimal parcial = as.getPrecoAplicado().multiply(BigDecimal.valueOf(as.getQuantidade()));
                    parcial = parcial.subtract(parcial.multiply(as.getDescontoAplicado()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                    total = total.add(parcial);
                }
            }

            ClienteEntity clienteEntity = agendamentoEntity.getCliente();
            total = clienteEntity.calcularDesconto(total);

            agendamentoEntity.setValorFinal(total.doubleValue());
        } catch (Exception e) {
            System.out.println("Erro ao calcular valor final: " + e.getMessage());
            agendamentoEntity.setValorFinal(0.0);
        }
    }

    // --- regra de negocio 3: concluir agendamento ---
    public void concluirAgendamento(Long id) {
        try {
            Optional<AgendamentoEntity> op = agendamentoRepo.buscarPorId(id);
            if (op.isEmpty()) {
                System.out.println("Agendamento nao encontrado.");
                return;
            }
            AgendamentoEntity a = op.get();
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
            // RF04: baixa de estoque executada automaticamente pela trigger trg_baixa_estoque_servico (V6)

            System.out.println("Agendamento #" + id + " concluido.");
        } catch (Exception e) {
            System.out.println("Erro ao concluir agendamento: " + e.getMessage());
        }
    }

    // --- regra de negocio 4: cancelar agendamento ---
    public void cancelarAgendamento(Long id) {
        try {
            Optional<AgendamentoEntity> op = agendamentoRepo.buscarPorId(id);
            if (op.isEmpty()) {
                System.out.println("Agendamento nao encontrado.");
                return;
            }
            AgendamentoEntity a = op.get();
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

    public List<AgendamentoEntity> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        try {
            return agendamentoRepo.buscarPorPeriodo(inicio, fim);
        } catch (Exception e) {
            System.out.println("Erro ao buscar agendamentos por periodo: " + e.getMessage());
            return List.of();
        }
    }

    public List<AgendamentoEntity> buscarPorStatus(StatusAgendamento status) {
        try {
            return agendamentoRepo.buscarPorStatus(status);
        } catch (Exception e) {
            System.out.println("Erro ao buscar agendamentos por status: " + e.getMessage());
            return List.of();
        }
    }
}
