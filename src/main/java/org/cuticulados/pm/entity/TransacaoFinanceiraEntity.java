package org.cuticulados.pm.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "transacao_financeira")
public class TransacaoFinanceiraEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoTransacao tipo;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_transacao", nullable = false, updatable = false)
    private LocalDateTime dataTransacao;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agendamento_id", unique = true)
    private AgendamentoEntity agendamentoEntity;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "venda_avulsa_id", unique = true)
    private VendaAvulsaEntity vendaAvulsaEntity;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TipoTransacao getTipo() { return tipo; }
    public void setTipo(TipoTransacao tipo) { this.tipo = tipo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String d) { this.descricao = d; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal v) { this.valor = v; }
    public LocalDateTime getDataTransacao() { return dataTransacao; }
    public void setDataTransacao(LocalDateTime d) { this.dataTransacao = d; }
    public AgendamentoEntity getAgendamento() { return agendamentoEntity; }
    public void setAgendamento(AgendamentoEntity a) { this.agendamentoEntity = a; }
    public VendaAvulsaEntity getVendaAvulsa() { return vendaAvulsaEntity; }
    public void setVendaAvulsa(VendaAvulsaEntity v) { this.vendaAvulsaEntity = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransacaoFinanceiraEntity t = (TransacaoFinanceiraEntity) o;
        return Objects.equals(id, t.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
