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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Entidade que representa a venda avulsa de um produto.
 *
 * Ocorre quando um produto (esmalte, base, etc.) é vendido diretamente,
 * sem estar vinculado a um agendamento de serviço.
 *
 * O campo {@code fechado} indica se a venda já entrou no fechamento de dia
 * do profissional — vendas fechadas não reaparecem em fechamentos futuros.
 */
@Entity
@Table(name = "venda_avulsa")
public class VendaAvulsa {

    /** Identificador único gerado automaticamente pelo banco. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Produto vendido nesta transação avulsa. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    /** Quantidade de unidades vendidas. */
    @Column(nullable = false)
    private Integer quantidade;

    /** Preço unitário cobrado no momento da venda (cópia do preço de venda do produto). */
    @Column(name = "preco_unitario", nullable = false)
    private Double precoUnitario;

    /** Valor total da venda ({@code precoUnitario * quantidade}). */
    @Column(nullable = false)
    private Double total;

    /** Data e hora em que a venda foi realizada. */
    @Column(name = "data_venda", nullable = false, updatable = false)
    private LocalDateTime dataVenda;

    /** Profissional que realizou a venda. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    /** Transação financeira gerada por esta venda (relacionamento um-para-um). */
    @OneToOne(mappedBy = "vendaAvulsa")
    private TransacaoFinanceira transacao;

    /**
     * Indica se esta venda foi incluída no fechamento de dia do profissional.
     * Valor padrão: {@code false} (venda em aberto).
     * Após o fechamento do dia, passa a {@code true}.
     */
    @Column(nullable = false)
    private boolean fechado = false;

    /**
     * Preenche a data da venda automaticamente antes da inserção no banco.
     */
    @PrePersist
    protected void onCreate() {
        if (this.dataVenda == null) {
            this.dataVenda = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Produto getProduto() { return produto; }
    public void setProduto(Produto p) { this.produto = p; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer q) { this.quantidade = q; }
    public Double getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(Double p) { this.precoUnitario = p; }
    public Double getTotal() { return total; }
    public void setTotal(Double t) { this.total = t; }
    public LocalDateTime getDataVenda() { return dataVenda; }
    public void setDataVenda(LocalDateTime d) { this.dataVenda = d; }
    public Profissional getProfissional() { return profissional; }
    public void setProfissional(Profissional p) { this.profissional = p; }
    public TransacaoFinanceira getTransacao() { return transacao; }
    public void setTransacao(TransacaoFinanceira t) { this.transacao = t; }
    public boolean isFechado() { return fechado; }
    public void setFechado(boolean fechado) { this.fechado = fechado; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VendaAvulsa v = (VendaAvulsa) o;
        return Objects.equals(id, v.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}