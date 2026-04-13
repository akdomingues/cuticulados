package org.cuticulados.pm.entity;

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
public class TransacaoFinanceira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoTransacao tipo;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(nullable = false)
    private Double valor;

    @Column(name = "data_transacao", nullable = false, updatable = false)
    private LocalDateTime dataTransacao;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agendamento_id", unique = true)
    private Agendamento agendamento;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "venda_avulsa_id", unique = true)
    private VendaAvulsa vendaAvulsa;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TipoTransacao getTipo() { return tipo; }
    public void setTipo(TipoTransacao tipo) { this.tipo = tipo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String d) { this.descricao = d; }
    public Double getValor() { return valor; }
    public void setValor(Double v) { this.valor = v; }
    public LocalDateTime getDataTransacao() { return dataTransacao; }
    public void setDataTransacao(LocalDateTime d) { this.dataTransacao = d; }
    public Agendamento getAgendamento() { return agendamento; }
    public void setAgendamento(Agendamento a) { this.agendamento = a; }
    public VendaAvulsa getVendaAvulsa() { return vendaAvulsa; }
    public void setVendaAvulsa(VendaAvulsa v) { this.vendaAvulsa = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransacaoFinanceira t = (TransacaoFinanceira) o;
        return Objects.equals(id, t.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
