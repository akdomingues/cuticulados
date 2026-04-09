package org.cuticulados.pm.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Entidade que representa um cliente do salão.
 *
 * <p>Herda os dados básicos de {@link Usuario} (nome, login, senha, etc.)
 * e adiciona informações específicas do cliente: CPF, telefone e
 * controle de fidelidade.</p>
 *
 * <p>O campo {@code tipoCliente} ("novo" ou "frequente") é atualizado
 * automaticamente pela trigger {@code trg_promove_cliente} no banco
 * e também verificado pelo {@code ClienteService}.</p>
 */
@Entity
@Table(name = "cliente")
public class Cliente extends Usuario {

    /** CPF do cliente, único e obrigatório (formato: 000.000.000-00). */
    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    /** Telefone de contato do cliente. */
    @Column(nullable = false, length = 20)
    private String telefone;

    /**
     * Classificação de fidelidade do cliente.
     * Valores possíveis: "novo" (padrão) ou "frequente" (≥ 3 atendimentos no mês).
     */
    @Column(name = "tipo_cliente", nullable = false)
    private String tipoCliente = "novo";

    /** Contador de atendimentos realizados no mês corrente. */
    @Column(name = "total_atendimentos_mes", nullable = false)
    private Integer totalAtendimentosMes = 0;

    /** Lista de agendamentos vinculados a este cliente. */
    @OneToMany(mappedBy = "cliente")
    private List<Agendamento> agendamentos = new ArrayList<>();

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(String tipoCliente) { this.tipoCliente = tipoCliente; }
    public Integer getTotalAtendimentosMes() { return totalAtendimentosMes; }
    public void setTotalAtendimentosMes(Integer total) { this.totalAtendimentosMes = total; }
    public List<Agendamento> getAgendamentos() { return agendamentos; }
    public void setAgendamentos(List<Agendamento> agendamentos) { this.agendamentos = agendamentos; }
}