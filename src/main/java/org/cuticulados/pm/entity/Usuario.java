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
 * Entidade base que representa um usuário do sistema.
 *
 * <p>Utiliza herança JPA do tipo {@code JOINED}: os dados comuns a todos
 * os usuários ficam nesta tabela, enquanto cada subclasse
 * ({@link Cliente}, {@link Profissional}) possui sua própria tabela
 * com as informações específicas.</p>
 *
 * <p>O tipo do usuário é controlado pelo ENUM {@link TipoUsuario},
 * permitindo distinguir ADMIN, PROFISSIONAL e CLIENTE sem criar
 * entidades separadas para cada papel.</p>
 *
 * <p>Implementa soft delete por meio do campo {@code deletedAt}:
 * ao invés de remover o registro do banco, apenas registra a data
 * da exclusão lógica.</p>
 */
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
public class Usuario {

    /** Identificador único gerado automaticamente pelo banco (auto-increment). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome completo do usuário. */
    @Column(nullable = false, length = 100)
    private String nome;

    /** E-mail único do usuário, usado para contato. */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /** Login único para autenticação no sistema. */
    @Column(nullable = false, unique = true, length = 50)
    private String login;

    /** Senha do usuário (armazenada como texto neste projeto acadêmico). */
    @Column(nullable = false, length = 255)
    private String senha;

    /**
     * Tipo/papel do usuário no sistema.
     * Gravado como texto no banco (ex: "ADMIN", "CLIENTE", "PROFISSIONAL").
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario tipo;

    /** Data e hora de criação do registro. Não é atualizado após a inserção. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Data e hora da última atualização do registro. */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Data e hora da exclusão lógica. Nulo enquanto o usuário estiver ativo. */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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
     * Atualiza o campo {@code updatedAt} com a data/hora atual.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Verifica se o usuário foi excluído logicamente.
     *
     * @return {@code true} se {@code deletedAt} estiver preenchido
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Verifica se o usuário possui perfil de administrador.
     *
     * @return {@code true} se o tipo for {@code ADMIN}
     */
    public boolean isAdmin() {
        return TipoUsuario.ADMIN.equals(this.tipo);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public TipoUsuario getTipo() { return tipo; }
    public void setTipo(TipoUsuario tipo) { this.tipo = tipo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}