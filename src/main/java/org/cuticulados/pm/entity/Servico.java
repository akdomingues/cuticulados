package org.cuticulados.pm.entity;

//CLASSE E TABELA SERVICO

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
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

//CRIA A TABELA NO BANCO

@Entity
@Table(name = "servico")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //REGRAS DA TABELA

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(name = "valor_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorBase;

    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "servicos")
    private List<Profissional> profissionais = new ArrayList<>();

    //RELACIONAMENTO UM PARA MTS

    @OneToMany(mappedBy = "servico")
    private List<ServicoProduto> produtosUtilizados = new ArrayList<>();

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
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getValorBase() { return valorBase; }
    public void setValorBase(BigDecimal valorBase) { this.valorBase = valorBase; }
    public Integer getDuracaoMinutos() { return duracaoMinutos; }
    public void setDuracaoMinutos(Integer duracaoMinutos) { this.duracaoMinutos = duracaoMinutos; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<Profissional> getProfissionais() { return profissionais; }
    public void setProfissionais(List<Profissional> profissionais) { this.profissionais = profissionais; }
    public List<ServicoProduto> getProdutosUtilizados() { return produtosUtilizados; }
    public void setProdutosUtilizados(List<ServicoProduto> produtos) { this.produtosUtilizados = produtos; }

    //COMPARA OS OBJTS

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Servico servico = (Servico) o;
        return Objects.equals(id, servico.id);
    }

    //GERA UM NUMERO BASEADO NO ID

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

