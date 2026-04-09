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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Entidade que registra uma movimentação financeira do salão.
 *
 * <p>Toda receita ou despesa do salão é registrada aqui.
 * Uma transação pode estar vinculada a um {@link Agendamento} concluído
 * ou a uma {@link VendaAvulsa} de produto.</p>
 *
 * <p>O saldo do caixa é calculado pelo {@code RelatorioService}
 * somando entradas e subtraindo saídas.</p>
 */
@Entity
@Table(name = "transacao_financeira")
public class TransacaoFinanceira {

    /** Identificador único gerado automaticamente pelo banco. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tipo da movimentação: ENTRADA (receita) ou SAIDA (despesa).
     * Gravado como texto no banco.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoTransacao tipo;

    /** Descrição da movimentação (ex: "Pagamento agendamento #5"). */
    @Column(nullable = false, length = 255)
    private String descricao;

    /** Valor monetário da transação. */
    @Column(nullable = false)
    private Double valor;

    /** Data e hora em que a transação foi registrada. */
    @Column(name = "data_transacao", nullable = false, updatable = false)
    private LocalDateTime dataTransacao;

    /**
     * Agendamento que originou esta transação (pode ser nulo em despesas avulsas).
     * Relacionamento um-para-um.
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agendamento_id", unique = true)
    private Agendamento agendamento;

    /**
     * Venda avulsa que originou esta transação (pode ser nulo em outras receitas).
     * Relacionamento um-para-um.
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "venda_avulsa_id", unique = true)
    private VendaAvulsa vendaAvulsa;

//=====
    /**
     * Executado automaticamente pelo JPA antes de inserir o registro.
     * Preenche o campo {@code dataTransacao} com a data/hora atual,
     * evitando erro de constraint NOT NULL no banco.
     */
    @PrePersist
    protected void onCreate() {
        this.dataTransacao = LocalDateTime.now();
    }
//=====

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