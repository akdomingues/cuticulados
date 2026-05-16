package org.cuticulados.pm.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.entity.Agendamento;
import org.cuticulados.pm.entity.AgendamentoServico;
import org.cuticulados.pm.entity.Cliente;
import org.cuticulados.pm.entity.Produto;
import org.cuticulados.pm.entity.Profissional;
import org.cuticulados.pm.entity.Servico;
import org.cuticulados.pm.entity.ServicoProduto;
import org.cuticulados.pm.entity.StatusAgendamento;
import org.cuticulados.pm.repository.AgendamentoRepository;
import org.cuticulados.pm.repository.ProdutoRepository;

public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepo = new AgendamentoRepository();
    private final ProdutoRepository produtoRepo = new ProdutoRepository();

    // --- CRUD basico ---

    // retorna null em caso de sucesso, ou a mensagem de erro para exibir na UI
    public String criarAgendamento(Agendamento agendamento) {
        try {
            if (agendamento.getCliente() == null || agendamento.getProfissional() == null) {
                return "Cliente e profissional são obrigatórios.";
            }

            // regra 1: conflito de horario
            boolean conflita = agendamentoRepo.existeConflito(
                    agendamento.getProfissional(),
                    agendamento.getDataHoraInicio(),
                    agendamento.getDataHoraFim());
            if (conflita) {
                return "Conflito de horário! O profissional já tem atendimento nesse período.";
            }

            // RF05: bloquear se insumos insuficientes
            for (AgendamentoServico as : agendamento.getServicos()) {
                if (as.getServico() == null) continue;
                List<ServicoProduto> insumos = produtoRepo.buscarServicoProdutos(as.getServico().getId());
                for (ServicoProduto sp : insumos) {
                    Produto produto = sp.getProduto();
                    if (produto.getQuantidadeEstoque() < as.getQuantidade()) {
                        return "Estoque insuficiente para o produto '" + produto.getNome()
                                + "'. Disponível: " + produto.getQuantidadeEstoque()
                                + ", necessário: " + as.getQuantidade();
                    }
                }
            }

            calcularValorFinal(agendamento);
            agendamento.setStatus(StatusAgendamento.PENDENTE);
            agendamentoRepo.salvar(agendamento);
            return null; // sucesso
        } catch (Exception e) {
            return "Erro ao criar agendamento: " + e.getMessage();
        }
    }

    public Optional<Agendamento> buscarPorId(Long id) {
        try {
            return agendamentoRepo.buscarPorId(id);
        } catch (Exception e) {
            System.out.println("Erro ao buscar agendamento: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<Agendamento> listarTodos() {
        try {
            return agendamentoRepo.listarTodos();
        } catch (Exception e) {
            System.out.println("Erro ao listar agendamentos: " + e.getMessage());
            return List.of();
        }
    }

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

    // --- regra de negocio 2: calcular valor final com desconto de fidelidade ---
    public void calcularValorFinal(Agendamento agendamento) {
        try {
            BigDecimal total = BigDecimal.ZERO;
            if (agendamento.getServicos() != null) {
                for (AgendamentoServico as : agendamento.getServicos()) {
                    BigDecimal parcial = as.getPrecoAplicado().multiply(BigDecimal.valueOf(as.getQuantidade()));
                    parcial = parcial.subtract(parcial.multiply(as.getDescontoAplicado()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                    total = total.add(parcial);
                }
            }

            Cliente cliente = agendamento.getCliente();
            total = cliente.calcularDesconto(total);

            agendamento.setValorFinal(total.doubleValue());
        } catch (Exception e) {
            System.out.println("Erro ao calcular valor final: " + e.getMessage());
            agendamento.setValorFinal(0.0);
        }
    }

    // --- regra de negocio 3: concluir agendamento ---
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
            // RF04: baixa de estoque executada automaticamente pela trigger trg_baixa_estoque_servico (V6)

            System.out.println("Agendamento #" + id + " concluido.");
        } catch (Exception e) {
            System.out.println("Erro ao concluir agendamento: " + e.getMessage());
        }
    }

    // --- regra de negocio 4: cancelar agendamento ---
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

    public List<Agendamento> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        try {
            return agendamentoRepo.buscarPorPeriodo(inicio, fim);
        } catch (Exception e) {
            System.out.println("Erro ao buscar agendamentos por periodo: " + e.getMessage());
            return List.of();
        }
    }

    public List<Agendamento> buscarPorStatus(StatusAgendamento status) {
        try {
            return agendamentoRepo.buscarPorStatus(status);
        } catch (Exception e) {
            System.out.println("Erro ao buscar agendamentos por status: " + e.getMessage());
            return List.of();
        }
    }
}
