package org.cuticulados.pm.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Entidade que representa um profissional do salão.
 *
 * Herda os dados de {@link Usuario} e adiciona a especialidade (ex: "nail designer").
 *
 * Tem relacionamento ManyToMany com {@link Servico}: um profissional pode realizar
 * vários serviços e vice-versa. A tabela intermediária é {@code profissional_servico}.
 */
@Entity
@Table(name = "profissional")
public class Profissional extends Usuario {

    /** Área de atuação do profissional (ex: "nail designer", "cabeleireiro"). */
    @Column(nullable = false, length = 100)
    private String especialidade;

    /** Lista de agendamentos realizados por este profissional. */
    @OneToMany(mappedBy = "profissional")
    private List<Agendamento> agendamentos = new ArrayList<>();

//===== adicionado (muitos para muitos)
    /**
     * Serviços que este profissional está habilitado a realizar.
     * A anotação @JoinTable define a tabela intermediária {@code profissional_servico}.
     */
    @ManyToMany
    @JoinTable(
            name = "profissional_servico",
            joinColumns = @JoinColumn(name = "profissional_id"),
            inverseJoinColumns = @JoinColumn(name = "servico_id")
    )
    private List<Servico> servicos = new ArrayList<>();
//=====

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }
    public List<Agendamento> getAgendamentos() { return agendamentos; }
    public void setAgendamentos(List<Agendamento> agendamentos) { this.agendamentos = agendamentos; }
    public List<Servico> getServicos() { return servicos; }
    public void setServicos(List<Servico> servicos) { this.servicos = servicos; }
}