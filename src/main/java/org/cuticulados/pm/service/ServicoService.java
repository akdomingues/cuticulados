package org.cuticulados.pm.service;

//CRUD DE SERVICO

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.entity.Servico;
import org.cuticulados.pm.repository.ServicoRepository;

public class ServicoService {

    private final ServicoRepository servicoRepo = new ServicoRepository();

    //CADASTRO
    public void cadastrarServico(Servico servico) {
        try {
            if (servico.getDescricao() == null || servico.getDescricao().isBlank()) {
                System.out.println("Descricao e obrigatoria.");
                return;
            }
            if (servico.getValorBase().compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Valor base deve ser maior que zero.");
                return;
            }
            servicoRepo.salvar(servico);
            System.out.println("Servico cadastrado com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro ao cadastrar servico: " + e.getMessage());
        }
    }

    //BUSCA

    public Optional<Servico> buscarPorId(Long id) {
        try {
            return servicoRepo.buscarPorId(id);
        } catch (Exception e) {
            System.out.println("Erro ao buscar servico: " + e.getMessage());
            return Optional.empty();
        }
    }

    //LISTAR
    public List<Servico> listarTodos() {
        try {
            return servicoRepo.listarTodos();
        } catch (Exception e) {
            System.out.println("Erro ao listar servicos: " + e.getMessage());
            return List.of();
        }
    }

    //ATUALIZAR

    public void atualizarServico(Servico servico) {
        try {
            if (servicoRepo.buscarPorId(servico.getId()).isEmpty()) {
                System.out.println("Servico nao encontrado.");
                return;
            }
            servicoRepo.salvar(servico);
        } catch (Exception e) {
            System.out.println("Erro ao atualizar servico: " + e.getMessage());
        }
    }

    //DELETE
    public void removerServico(Long id) {
        try {
            if (servicoRepo.buscarPorId(id).isEmpty()) {
                System.out.println("Servico nao encontrado.");
                return;
            }
            servicoRepo.deletar(id);
            System.out.println("Servico removido com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro ao remover servico: " + e.getMessage());
        }
    }

    //BUSCA

    public List<Servico> buscarPorTermo(String termo) {
        try {
            return servicoRepo.buscarPorDescricao(termo);
        } catch (Exception e) {
            System.out.println("Erro ao buscar servico: " + e.getMessage());
            return List.of();
        }
    }
}
