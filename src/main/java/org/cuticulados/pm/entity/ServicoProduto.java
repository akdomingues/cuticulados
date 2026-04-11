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
import jakarta.persistence.Table;

/**
 * Associação entre {@link Servico} e {@link Produto}.
 *
 * Indica quais produtos são usados em cada serviço — por exemplo,
 * "manicure" usa "esmalte" e "acetona". Tabela intermediária explícita,
 * o que permite adicionar campos extras (ex: quantidade) futuramente.
 */
@Entity
@Table(name = "servico_produto")
public class ServicoProduto {

    /** Identificador único gerado automaticamente pelo banco. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Serviço ao qual este produto está associado.
     * Relacionamento muitos-para-um: vários registros podem referenciar o mesmo serviço.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    /**
     * Produto utilizado neste serviço.
     * Relacionamento muitos-para-um: vários registros podem referenciar o mesmo produto.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    /** Data e hora de criação da associação. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Servico getServico() { return servico; }
    public void setServico(Servico s) { this.servico = s; }
    public Produto getProduto() { return produto; }
    public void setProduto(Produto p) { this.produto = p; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServicoProduto sp = (ServicoProduto) o;
        return Objects.equals(id, sp.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}