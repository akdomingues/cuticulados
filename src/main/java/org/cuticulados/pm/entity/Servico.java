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
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Entidade que representa um serviço oferecido pelo salão.
 *
 * <p>Exemplos: manicure, pedicure, alongamento de unhas, nail art.</p>
 *
 * <p>Cada serviço tem um valor base e uma duração estimada em minutos.
 * Pode estar associado a vários {@link Produto}s (via {@link ServicoProduto})
 * e pode ser realizado por vários {@link Profissional}s (via {@code @ManyToMany}).</p>
 */
@Entity
@Table(name = "servico")
public class Servico {

    /** Identificador único gerado automaticamente pelo banco. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Descrição do serviço (ex: "manicure", "pedicure"). */
    @Column(nullable = false, length = 200)
    private String descricao;

    /** Valor base cobrado pelo serviço (pode ter desconto aplicado no agendamento). */
    @Column(name = "valor_base", nullable = false)
    private Double valorBase;

    /** Duração estimada do serviço em minutos. */
    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;

    /** Data e hora de criação do registro. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Data e hora da última atualização. */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Produtos utilizados na execução deste serviço. */
    @OneToMany(mappedBy = "servico")
    private List<ServicoProduto> produtosUtilizados = new ArrayList<>();

//=====
    /**
     * Lista de profissionais habilitados para realizar este serviço.
     *
     * <p>Lado inverso do relacionamento {@code @ManyToMany} com {@link Profissional}.
     * A anotação {@code mappedBy} indica que o controle da tabela intermediária
     * está em {@code Profissional.servicos}.</p>
     */
    @ManyToMany(mappedBy = "servicos")
    private List<Profissional> profissionais = new ArrayList<>();
//=====

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
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Double getValorBase() { return valorBase; }
    public void setValorBase(Double valorBase) { this.valorBase = valorBase; }
    public Integer getDuracaoMinutos() { return duracaoMinutos; }
    public void setDuracaoMinutos(Integer duracaoMinutos) { this.duracaoMinutos = duracaoMinutos; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<ServicoProduto> getProdutosUtilizados() { return produtosUtilizados; }
    public void setProdutosUtilizados(List<ServicoProduto> produtos) { this.produtosUtilizados = produtos; }

//=====
    public List<Profissional> getProfissionais() { return profissionais; }
    public void setProfissionais(List<Profissional> profissionais) { this.profissionais = profissionais; }
//=====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Servico servico = (Servico) o;
        return Objects.equals(id, servico.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}