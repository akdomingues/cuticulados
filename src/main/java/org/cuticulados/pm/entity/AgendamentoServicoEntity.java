package org.cuticulados.pm.entity;

import java.math.BigDecimal;
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
@Entity(name = "AgendamentoServico")
@Table(name = "agendamento_servico")
public class AgendamentoServicoEntity {

    // ID unico gerado automaticamente pelo banco
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // relacionamento com o agendamento
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agendamento_id", nullable = false)
    private AgendamentoEntity agendamentoEntity;

    // relacionamento com o servico
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "servico_id", nullable = false)
    private ServicoEntity servicoEntity;

    @Column(nullable = false)
    private Integer quantidade = 1;

    @Column(name = "preco_aplicado", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoAplicado;

    @Column(name = "desconto_aplicado", nullable = false, precision = 10, scale = 2)
    private BigDecimal descontoAplicado = BigDecimal.ZERO;

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
    public AgendamentoEntity getAgendamento() { return agendamentoEntity; }
    public void setAgendamento(AgendamentoEntity a) { this.agendamentoEntity = a; }
    public ServicoEntity getServico() { return servicoEntity; }
    public void setServico(ServicoEntity s) { this.servicoEntity = s; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer q) { this.quantidade = q; }
    public BigDecimal getPrecoAplicado() { return precoAplicado; }
    public void setPrecoAplicado(BigDecimal p) { this.precoAplicado = p; }
    public BigDecimal getDescontoAplicado() { return descontoAplicado; }
    public void setDescontoAplicado(BigDecimal d) { this.descontoAplicado = d; }
    public Integer getTempoReal() { return tempoReal; }
    public void setTempoReal(Integer t) { this.tempoReal = t; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }

    // igualdade baseada no ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgendamentoServicoEntity as = (AgendamentoServicoEntity) o;
        return Objects.equals(id, as.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
