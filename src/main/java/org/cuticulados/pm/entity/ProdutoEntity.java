package org.cuticulados.pm.entity;

//CLASSE E TABELA PRODUTO

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

//CRIA A TABELA NO BANCO

@Entity
@Table(name = "produto")
public class ProdutoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //REGRAS DA TABELA

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "quantidade_estoque", nullable = false)
    private Integer quantidadeEstoque = 0;

    @Column(name = "quantidade_minima", nullable = false)
    private Integer quantidadeMinima = 0;

    @Column(name = "preco_custo", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoCusto;

    @Column(name = "preco_venda", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoVenda;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    //RELACIONAMENTO UM PARA MTS

    @OneToMany(mappedBy = "produto")
    private List<ServicoProdutoEntity> servicosAssociados = new ArrayList<>();

    //EXECUTA ANTES DE SALVAR

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    //ACESSA OS DADOS

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Integer getQuantidadeEstoque() { return quantidadeEstoque; }
    public void setQuantidadeEstoque(Integer qtd) { this.quantidadeEstoque = qtd; }
    public Integer getQuantidadeMinima() { return quantidadeMinima; }
    public void setQuantidadeMinima(Integer qtd) { this.quantidadeMinima = qtd; }
    public BigDecimal getPrecoCusto() { return precoCusto; }
    public void setPrecoCusto(BigDecimal preco) { this.precoCusto = preco; }
    public BigDecimal getPrecoVenda() { return precoVenda; }
    public void setPrecoVenda(BigDecimal preco) { this.precoVenda = preco; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<ServicoProdutoEntity> getServicosAssociados() { return servicosAssociados; }
    public void setServicosAssociados(List<ServicoProdutoEntity> lista) { this.servicosAssociados = lista; }

    //COMPARA OS OBJTS

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProdutoEntity produtoEntity = (ProdutoEntity) o;
        return Objects.equals(id, produtoEntity.id);
    }

    //GERA UM NUMERO BASEADO NO ID

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
