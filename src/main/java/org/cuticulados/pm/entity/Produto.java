package org.cuticulados.pm.entity;

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

/**
 * Entidade que representa um produto do estoque do salão.
 *
 * <p>Produtos são utilizados durante os serviços (ex: esmalte, acetona, gel UV)
 * e também podem ser vendidos avulsamente para clientes.</p>
 *
 * <p>O controle de estoque é feito pelos campos {@code quantidadeEstoque}
 * e {@code quantidadeMinima}. Quando o estoque atinge o mínimo, o produto
 * aparece nos alertas gerados pelo {@code ProdutoService}.</p>
 */
@Entity
@Table(name = "produto")
public class Produto {

    /** Identificador único gerado automaticamente pelo banco. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome do produto (ex: "esmalte vermelho", "gel uv"). */
    @Column(nullable = false, length = 100)
    private String nome;

    /** Quantidade atual disponível no estoque. Não pode ser negativa. */
    @Column(name = "quantidade_estoque", nullable = false)
    private Integer quantidadeEstoque = 0;

    /** Quantidade mínima aceitável no estoque antes de gerar alerta. */
    @Column(name = "quantidade_minima", nullable = false)
    private Integer quantidadeMinima = 0;

    /** Preço de custo do produto (quanto o salão paga ao fornecedor). */
    @Column(name = "preco_custo", nullable = false)
    private Double precoCusto;

    /** Preço de venda do produto (quanto o cliente paga). */
    @Column(name = "preco_venda", nullable = false)
    private Double precoVenda;

    /** Data e hora de cadastro do produto. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Data e hora da última atualização do produto. */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Relação com os serviços que utilizam este produto. */
    @OneToMany(mappedBy = "produto")
    private List<ServicoProduto> servicosAssociados = new ArrayList<>();

    /**
     * Executado automaticamente pelo JPA antes de inserir o registro.
     * Preenche os campos de data de criação e atualização.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Executado automaticamente pelo JPA antes de atualizar o registro.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Integer getQuantidadeEstoque() { return quantidadeEstoque; }
    public void setQuantidadeEstoque(Integer qtd) { this.quantidadeEstoque = qtd; }
    public Integer getQuantidadeMinima() { return quantidadeMinima; }
    public void setQuantidadeMinima(Integer qtd) { this.quantidadeMinima = qtd; }
    public Double getPrecoCusto() { return precoCusto; }
    public void setPrecoCusto(Double preco) { this.precoCusto = preco; }
    public Double getPrecoVenda() { return precoVenda; }
    public void setPrecoVenda(Double preco) { this.precoVenda = preco; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<ServicoProduto> getServicosAssociados() { return servicosAssociados; }
    public void setServicosAssociados(List<ServicoProduto> lista) { this.servicosAssociados = lista; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produto produto = (Produto) o;
        return Objects.equals(id, produto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}