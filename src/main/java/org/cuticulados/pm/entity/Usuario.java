package org.cuticulados.pm.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Representa a entidade base para os usuários do sistema.
 * Esta classe utiliza a estratégia de herança {@code JOINED}, permitindo que subclasses
 * (como Cliente ou Funcionario) possuam suas próprias tabelas relacionadas à tabela principal "usuario".
 * Inclui suporte para auditoria de data de criação/atualização e suporte para exclusão lógica (soft delete).
 */
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
public class Usuario {

    /** Identificador único autoincrementado no banco de dados. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome completo do usuário. */
    @Column(nullable = false, length = 100)
    private String nome;

    /** Endereço de e-mail único para comunicação e recuperação de conta. */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /** Username utilizado para autenticação no sistema. */
    @Column(nullable = false, unique = true, length = 50)
    private String login;

    /** Hash da senha do usuário para garantir a segurança dos dados. */
    @Column(nullable = false, length = 255)
    private String senha;

    /** Define o perfil ou papel do usuário (ADMIN || CLIENTE || PROFISSIONAL). Armazenado como String no banco. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario tipo;

    /** Data e hora em que o registro foi criado. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Data e hora da última modificação do registro. */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Data e hora da exclusão lógica. Se nulo, o usuário é considerado ativo */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /** Inicializa os campos de auditoria de data. */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /** Atualiza o campo {@code updatedAt}. */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Verifica se o usuário sofreu exclusão lógica.
     * * @return {@code true} se o usuário estiver marcado como deletado; {@code false} caso contrário.
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    // Getters e Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setTipo(TipoUsuario tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    // Métodos Sobrescritos (equals e hashCode)

    /**
     * Compara a igualdade entre dois objetos Usuario baseando-se no ID.
     * * @param o Objeto a ser comparado.
     * @return {@code true} se forem o mesmo objeto ou possuírem o mesmo ID.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    /**
     * Gera o código hash para a instância, utilizando apenas o ID como base.
     * * @return Valor hash do objeto.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}