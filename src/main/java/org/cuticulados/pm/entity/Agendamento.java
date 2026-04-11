package org.cuticulados.pm.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Entidade que representa um agendamento de atendimento no salão.
 *
 * Conecta um {@link Cliente} a um {@link Profissional} em um intervalo de tempo.
 * Um agendamento pode ter um ou mais serviços ({@link AgendamentoServico}).
 *
 * Fluxo de status: PENDENTE → CONCLUIDO ou CANCELADO.
 * As regras de transição são controladas pelo AgendamentoService.
 *
 * Ao ser concluído, gera automaticamente uma {@link TransacaoFinanceira} de entrada no caixa.
 */
@Entity
@Table(name = "agendamento")
public class Agendamento {

   /** Identificador único gerado automaticamente pelo banco (auto-increment). */
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   /** Data e hora de início do atendimento. */
   @Column(name = "data_hora_inicio", nullable = false)
   private LocalDateTime dataHoraInicio;

   /** Data e hora de término previsto do atendimento. */
   @Column(name = "data_hora_fim", nullable = false)
   private LocalDateTime dataHoraFim;

   /**
    * Situação atual do agendamento.
    * Gravado como texto no banco (ex: "PENDENTE", "CONCLUIDO").
    */
   @Enumerated(EnumType.STRING)
   @Column(nullable = false)
   private StatusAgendamento status = StatusAgendamento.PENDENTE;

   /** Valor total calculado para este agendamento, com descontos aplicados. */
   @Column(name = "valor_final", nullable = false)
   private Double valorFinal = 0.0;

   /**
    * Cliente que será atendido.
    * Muitos agendamentos podem pertencer a um mesmo cliente.
    */
   @ManyToOne(fetch = FetchType.EAGER)
   @JoinColumn(name = "cliente_id", nullable = false)
   private Cliente cliente;

   /**
    * Profissional responsável pelo atendimento.
    * Muitos agendamentos podem ser de um mesmo profissional.
    */
   @ManyToOne(fetch = FetchType.EAGER)
   @JoinColumn(name = "profissional_id", nullable = false)
   private Profissional profissional;

   /** Lista de serviços incluídos neste agendamento. */
   @OneToMany(mappedBy = "agendamento")
   private List<AgendamentoServico> servicos = new ArrayList<>();

   /**
    * Transação financeira gerada quando este agendamento é concluído.
    * Relacionamento um-para-um: cada agendamento gera no máximo uma transação.
    */
   @OneToOne(mappedBy = "agendamento")
   private TransacaoFinanceira transacao;

   /** Data e hora de criação do registro. Não é atualizado após a inserção. */
   @Column(name = "created_at", nullable = false, updatable = false)
   private LocalDateTime createdAt;

   /** Data e hora da última atualização do registro. */
   @Column(name = "updated_at", nullable = false)
   private LocalDateTime updatedAt;

   /**
    * Executado automaticamente pelo JPA antes de inserir o registro.
    * Preenche os campos de data de criação e atualização.
    */
   @PrePersist
   protected void onCreate() {
      this.createdAt = LocalDateTime.now();
      this.updatedAt = LocalDateTime.now();
   }

   public Long getId() { return id; }
   public void setId(Long id) { this.id = id; }
   public LocalDateTime getDataHoraInicio() { return dataHoraInicio; }
   public void setDataHoraInicio(LocalDateTime d) { this.dataHoraInicio = d; }
   public LocalDateTime getDataHoraFim() { return dataHoraFim; }
   public void setDataHoraFim(LocalDateTime d) { this.dataHoraFim = d; }
   public StatusAgendamento getStatus() { return status; }
   public void setStatus(StatusAgendamento status) { this.status = status; }
   public Double getValorFinal() { return valorFinal; }
   public void setValorFinal(Double valor) { this.valorFinal = valor; }
   public Cliente getCliente() { return cliente; }
   public void setCliente(Cliente cliente) { this.cliente = cliente; }
   public Profissional getProfissional() { return profissional; }
   public void setProfissional(Profissional p) { this.profissional = p; }
   public List<AgendamentoServico> getServicos() { return servicos; }
   public void setServicos(List<AgendamentoServico> servicos) { this.servicos = servicos; }
   public TransacaoFinanceira getTransacao() { return transacao; }
   public void setTransacao(TransacaoFinanceira t) { this.transacao = t; }
   public LocalDateTime getCreatedAt() { return createdAt; }
   public LocalDateTime getUpdatedAt() { return updatedAt; }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Agendamento a = (Agendamento) o;
      return Objects.equals(id, a.id);
   }

   @Override
   public int hashCode() {
      return Objects.hash(id);
   }
}