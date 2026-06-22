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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity(name = "VendaAvulsa")
@Table(name = "venda_avulsa")
public class VendaAvulsaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produto_id", nullable = false)
    private ProdutoEntity produtoEntity;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "data_venda", nullable = false, updatable = false)
    private LocalDateTime dataVenda;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profissional_id", nullable = false)
    private ProfissionalEntity profissionalEntity;

    @OneToOne(mappedBy = "vendaAvulsaEntity")
    private TransacaoFinanceiraEntity transacao;

    @Column(nullable = false)
    private boolean fechado = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ProdutoEntity getProduto() { return produtoEntity; }
    public void setProduto(ProdutoEntity p) { this.produtoEntity = p; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer q) { this.quantidade = q; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal p) { this.precoUnitario = p; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal t) { this.total = t; }
    public LocalDateTime getDataVenda() { return dataVenda; }
    public void setDataVenda(LocalDateTime d) { this.dataVenda = d; }
    public ProfissionalEntity getProfissional() { return profissionalEntity; }
    public void setProfissional(ProfissionalEntity p) { this.profissionalEntity = p; }
    public TransacaoFinanceiraEntity getTransacao() { return transacao; }
    public void setTransacao(TransacaoFinanceiraEntity t) { this.transacao = t; }
    public boolean isFechado() { return fechado; }
    public void setFechado(boolean fechado) { this.fechado = fechado; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VendaAvulsaEntity v = (VendaAvulsaEntity) o;
        return Objects.equals(id, v.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}