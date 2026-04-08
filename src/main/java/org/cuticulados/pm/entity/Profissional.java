package org.cuticulados.pm.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "profissional")
public class Profissional extends Usuario {

    @Column(nullable = false, length = 100)
    private String especialidade;

    @OneToMany(mappedBy = "profissional")
    private List<Agendamento> agendamentos = new ArrayList<>();

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }
    public List<Agendamento> getAgendamentos() { return agendamentos; }
    public void setAgendamentos(List<Agendamento> agendamentos) { this.agendamentos = agendamentos; }
}