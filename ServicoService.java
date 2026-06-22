// caminho: src/main/java/org/cuticulados/pm/service/ServicoService.java
package org.cuticulados.pm.service;

import org.cuticulados.pm.model.Servico;
import org.cuticulados.pm.repository.ServicoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ServicoService {

    private final ServicoRepository repo = new ServicoRepository();

    public void cadastrar(String descricao, BigDecimal valorBase, int duracaoMinutos) {
        try {
            if (valorBase.compareTo(BigDecimal.ZERO) <= 0)
                throw new IllegalArgumentException("Valor base deve ser maior que zero.");
            if (duracaoMinutos <= 0)
                throw new IllegalArgumentException("Duração deve ser maior que zero.");
            Servico s = new Servico();
            s.setDescricao(descricao);
            s.setValorBase(valorBase);
            s.setDuracaoMinutos(duracaoMinutos);
            repo.salvar(s);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao cadastrar serviço: " + e.getMessage(), e);
        }
    }

    public void atualizar(Long id, String descricao, BigDecimal valorBase, int duracaoMinutos) {
        try {
            Servico s = repo.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado."));
            s.setDescricao(descricao);
            s.setValorBase(valorBase);
            s.setDuracaoMinutos(duracaoMinutos);
            repo.atualizar(s);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar serviço: " + e.getMessage(), e);
        }
    }

    public void remover(Long id) {
        try {
            repo.deletar(id);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao remover serviço: " + e.getMessage(), e);
        }
    }

    public List<Servico> listarTodos() {
        try {
            return repo.listarTodos();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar serviços: " + e.getMessage(), e);
        }
    }

    public Optional<Servico> buscarPorId(Long id) {
        try {
            return repo.buscarPorId(id);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar serviço: " + e.getMessage(), e);
        }
    }
}