package org.cuticulados.pm.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "cliente")
public class Cliente extends Usuario implements Descontavel {

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(name = "tipo_cliente", nullable = false)
    private String tipoCliente = "novo";

    @Column(name = "total_atendimentos_mes", nullable = false)
    private Integer totalAtendimentosMes = 0;

    @OneToMany(mappedBy = "cliente")
    private List<Agendamento> agendamentos = new ArrayList<>();

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(String tipoCliente) { this.tipoCliente = tipoCliente; }
    public Integer getTotalAtendimentosMes() { return totalAtendimentosMes; }
    public void setTotalAtendimentosMes(Integer total) { this.totalAtendimentosMes = total; }
    public List<Agendamento> getAgendamentos() { return agendamentos; }
    public void setAgendamentos(List<Agendamento> agendamentos) { this.agendamentos = agendamentos; }

    /**
     * Clientes frequentes recebem 10% de desconto; demais pagam valor integral.
     */
    @Override
    public BigDecimal calcularDesconto(BigDecimal valorBruto) {
        if ("frequente".equals(tipoCliente)) {
            return valorBruto.multiply(new BigDecimal("0.9"));
        }
        return valorBruto;
    }
}

