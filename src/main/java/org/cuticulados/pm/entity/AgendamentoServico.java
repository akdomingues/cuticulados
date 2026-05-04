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

/**entidade responsável pra repesentar um agendamento
 e os serviços realizados dentro dele.
*/
@Entity
@Table(name = "agendamento_servico")
public class AgendamentoServico {

    // ID unico gerado automaticamente pelo banco
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // relacionamento com o agendamento
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agendamento_id", nullable = false)
    private Agendamento agendamento;

    // relacionamento com o servico
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @Column(nullable = false)
    private Integer quantidade = 1;

    @Column(name = "preco_aplicado", nullable = false, precision = 10, scale = 2)
    private Double precoAplicado;

    @Column(name = "desconto_aplicado", nullable = false, precision = 10, scale = 2)
    private Double descontoAplicado = 0.0;

    @Column(name = "tempo_real")
    private Integer tempoReal;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

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

    // igualdade baseada no ID
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
