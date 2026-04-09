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
 * <p>Herda os dados comuns de {@link Usuario} e adiciona a especialidade
 * do profissional (ex: "nail designer", "manicure").</p>
 *
 * <p>Possui relacionamento {@code @ManyToMany} com {@link Servico}:
 * um profissional pode realizar vários serviços, e um serviço pode ser
 * executado por vários profissionais. A tabela intermediária no banco
 * é {@code profissional_servico}.</p>
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
     * Lista de serviços que este profissional está habilitado a realizar.
     *
     * <p>Relacionamento muitos-para-muitos com {@link Servico}:
     * um profissional pode realizar vários serviços e um serviço pode
     * ser realizado por vários profissionais.</p>
     *
     * <p>A anotação {@code @JoinTable} define o nome da tabela intermediária
     * e as colunas de chave estrangeira que ligam as duas entidades.</p>
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