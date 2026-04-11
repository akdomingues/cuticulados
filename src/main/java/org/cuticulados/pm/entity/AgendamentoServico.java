package org.cuticulados.pm.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Associação entre {@link Agendamento} e {@link Servico}.
 *
 * Representa um serviço realizado dentro de um agendamento, guardando o preço
 * e o desconto aplicados no momento. Assim, alterações futuras no preço do serviço
 * não afetam o histórico financeiro.
 */
@Entity
@Table(name = "agendamento_servico")
public class AgendamentoServico {

    /** Identificador único gerado automaticamente pelo banco. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Agendamento ao qual este serviço pertence.
     * Relacionamento muitos-para-um.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agendamento_id", nullable = false)
    private Agendamento agendamento;

    /**
     * Serviço realizado neste item do agendamento.
     * Relacionamento muitos-para-um.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    /** Quantidade de vezes que este serviço foi realizado no agendamento. */
    @Column(nullable = false)
    private Integer quantidade = 1;

    /** Preço cobrado no momento do agendamento (pode diferir do valor base atual). */
    @Column(name = "preco_aplicado", nullable = false)
    private Double precoAplicado;

    /** Percentual de desconto aplicado sobre o preço (0 a 100). */
    @Column(name = "desconto_aplicado", nullable = false)
    private Double descontoAplicado = 0.0;

    /** Tempo real de execução do serviço em minutos (preenchido ao concluir). */
    @Column(name = "tempo_real")
    private Integer tempoReal;

    /** Data e hora de criação do registro. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

//=====
    /**
     * Preenche {@code createdAt} antes de inserir o registro, evitando erro de NOT NULL.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
//=====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Agendamento getAgendamento() { return agendamento; }
    public void setAgendamento(Agendamento a) { this.agendamento = a; }
    public Servico getServico() { return servico; }
    public void setServico(Servico s) { this.servico = s; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer q) { this.quantidade = q; }
    public Double getPrecoAplicado() { return precoAplicado; }
    public void setPrecoAplicado(Double p) { this.precoAplicado = p; }
    public Double getDescontoAplicado() { return descontoAplicado; }
    public void setDescontoAplicado(Double d) { this.descontoAplicado = d; }
    public Integer getTempoReal() { return tempoReal; }
    public void setTempoReal(Integer t) { this.tempoReal = t; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgendamentoServico as = (AgendamentoServico) o;
        return Objects.equals(id, as.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}