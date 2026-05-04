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

@Entity
@Table(name = "venda_avulsa")
public class VendaAvulsa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private Double precoUnitario;

    @Column(nullable = false)
    private Double total;

    @Column(name = "data_venda", nullable = false, updatable = false)
    private LocalDateTime dataVenda;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @OneToOne(mappedBy = "vendaAvulsa")
    private TransacaoFinanceira transacao;

    @Column(nullable = false)
    private boolean fechado = false;

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