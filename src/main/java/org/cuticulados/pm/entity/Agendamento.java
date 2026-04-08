package org.cuticulados.pm.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

// entidade JPA
@Entity
@Table(name = "agendamento")
public class Agendamento {

    //o id e a chave primaria e o identity e o banco que gera automatico auto increment
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

 //datas do agendamento e horario
    @Column(name = "data_hora_inicio", nullable = false)
    private LocalDateTime dataHoraInicio;

    @Column(name = "data_hora_fim", nullable = false)
    private LocalDateTime dataHoraFim;

    //status do agendamento
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAgendamento status = StatusAgendamento.PENDENTE;

    //valor final
    @Column(name = "valor_final", nullable = false)
    private Double valorFinal = 0.0;

    //relacionamento com o cliente
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    //relacionamento com o profissional
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    //serviços de agendamento
    @OneToMany(mappedBy = "agendamento")
    private List<AgendamentoServico> servicos = new ArrayList<>();

    @OneToOne(mappedBy = "agendamento")
    private TransacaoFinanceira transacao;

    //controle da criação e as atualizações
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    //hook automatico execurta antes de salvar no banco e preenche as datas no automatico
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getDataHoraInicio() { return dataHoraInicio; }
    public void setDataHoraInicio(LocalDateTime d) { this.dataHoraInicio = d; }
    public LocalDateTime getDataHoraFim() { return dataHoraFim; }
    public void setDataHoraFim(LocalDateTime d) { this.dataHoraFim = d; }
    public StatusAgendamento getStatus() { return status; }
    public void setStatus(StatusAgendamento status) { this.status = status; }
    public Double getValorFinal() { return valorFinal; }
    public void setValorFinal(Double valor) { this.valorFinal = valor; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Profissional getProfissional() { return profissional; }
    public void setProfissional(Profissional p) { this.profissional = p; }
    public List<AgendamentoServico> getServicos() { return servicos; }
    public void setServicos(List<AgendamentoServico> servicos) { this.servicos = servicos; }
    public TransacaoFinanceira getTransacao() { return transacao; }
    public void setTransacao(TransacaoFinanceira t) { this.transacao = t; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agendamento a = (Agendamento) o;
        return Objects.equals(id, a.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

//o enum ele define o status possiveis do agendamento
public enum StatusAgendamento {
    PENDENTE,
    CONCLUIDO,
    CANCELADO
}
