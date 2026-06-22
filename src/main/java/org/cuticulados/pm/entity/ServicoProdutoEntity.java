package org.cuticulados.pm.entity;

//CLASSE E TABELA ServicoProduto
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

//CRIA A TABELA NO BANCO

@Entity(name = "ServicoProduto")
@Table(name = "servico_produto")
public class ServicoProdutoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //RELACIONAMENTO MTS PARA UM
    //RELACIONA COM SERVICO

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "servico_id", nullable = false)
    private ServicoEntity servicoEntity;

    //RELACIONA COM PRODUTO

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produto_id", nullable = false)
    private ProdutoEntity produtoEntity;

    //REGRAS DA TABELA

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    //ACESSA OS DADOS

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ServicoEntity getServico() { return servicoEntity; }
    public void setServico(ServicoEntity s) { this.servicoEntity = s; }
    public ProdutoEntity getProduto() { return produtoEntity; }
    public void setProduto(ProdutoEntity p) { this.produtoEntity = p; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }

    //COMPARA OS OBJTS

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServicoProdutoEntity sp = (ServicoProdutoEntity) o;
        return Objects.equals(id, sp.id);
    }

    //GERA UM NUMERO BASEADO NO ID

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
