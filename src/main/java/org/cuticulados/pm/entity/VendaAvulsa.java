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
import jakarta.persistence.Table;

/**
 * Entidade que representa a venda avulsa de um produto para um cliente.
 *
 * <p>Diferente do agendamento, a venda avulsa ocorre quando o cliente
 * compra um produto diretamente (ex: esmalte, base) sem contratar
 * um serviço de manicure.</p>
 *
 * <p>Ao ser registrada, uma {@link TransacaoFinanceira} de entrada
 * é criada automaticamente pelo {@code VendaAvulsaService}.</p>
 */
@Entity
@Table(name = "venda_avulsa")
public class VendaAvulsa {

    /** Identificador único gerado automaticamente pelo banco. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Produto vendido.
     * Muitas vendas podem referenciar o mesmo produto.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    /** Quantidade de unidades vendidas. */
    @Column(nullable = false)
    private Integer quantidade;

    /** Preço unitário cobrado no momento da venda. */
    @Column(name = "preco_unitario", nullable = false)
    private Double precoUnitario;

    /** Valor total da venda (quantidade × preço unitário). */
    @Column(nullable = false)
    private Double total;

    /** Data e hora em que a venda foi realizada. */
    @Column(name = "data_venda", nullable = false, updatable = false)
    private LocalDateTime dataVenda;

    /**
     * Profissional que realizou a venda.
     * Muitas vendas podem ser atribuídas ao mesmo profissional.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    /**
     * Transação financeira gerada por esta venda.
     * Relacionamento um-para-um.
     */
    @OneToOne(mappedBy = "vendaAvulsa")
    private TransacaoFinanceira transacao;

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