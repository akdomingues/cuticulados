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
    public String cadastrarServico(Servico servico) {
        try {
            if (servico.getDescricao() == null || servico.getDescricao().isBlank()) {
                return "Descrição é obrigatória.";
            }
            if (servico.getValorBase().compareTo(BigDecimal.ZERO) <= 0) {
                return "Valor base deve ser maior que zero.";
            }
            servicoRepo.salvar(servico);
            return null;
        } catch (Exception e) {
            return "Erro ao cadastrar serviço: " + e.getMessage();
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

    public String atualizarServico(Servico servico) {
        try {
            if (servicoRepo.buscarPorId(servico.getId()).isEmpty()) {
                return "Serviço não encontrado.";
            }
            servicoRepo.salvar(servico);
            return null;
        } catch (Exception e) {
            return "Erro ao atualizar serviço: " + e.getMessage();
        }
    }

    //DELETE
    public String removerServico(Long id) {
        try {
            if (servicoRepo.buscarPorId(id).isEmpty()) {
                return "Serviço não encontrado.";
            }
            servicoRepo.deletar(id);
            return null;
        } catch (Exception e) {
            return "Erro ao remover serviço: " + e.getMessage();
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
