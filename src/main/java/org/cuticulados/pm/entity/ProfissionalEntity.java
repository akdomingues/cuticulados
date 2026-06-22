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

@Entity(name = "Profissional")
@Table(name = "profissional")
public class ProfissionalEntity extends UsuarioEntity {

    @Column(nullable = false, length = 100)
    private String especialidade;

    @ManyToMany
    @JoinTable(
        name = "profissional_servico",
        joinColumns = @JoinColumn(name = "profissional_id"),
        inverseJoinColumns = @JoinColumn(name = "servico_id")
    )
    private List<ServicoEntity> servicoEntities = new ArrayList<>();

    @OneToMany(mappedBy = "profissionalEntity")
    private List<AgendamentoEntity> agendamentoEntities = new ArrayList<>();

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }
    public List<AgendamentoEntity> getAgendamentos() { return agendamentoEntities; }
    public void setAgendamentos(List<AgendamentoEntity> agendamentoEntities) { this.agendamentoEntities = agendamentoEntities; }
    public List<ServicoEntity> getServicos() { return servicoEntities; }
    public void setServicos(List<ServicoEntity> servicoEntities) { this.servicoEntities = servicoEntities; }
}