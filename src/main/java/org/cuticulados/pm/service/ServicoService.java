package org.cuticulados.pm.service;

import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.entity.Servico;
import org.cuticulados.pm.repository.ServicoRepository;

/**
 * Regras de negócio dos serviços do salão.
 * CRUD com validações: descrição obrigatória e valor base maior que zero.
 */
public class ServicoService {

    /** Repositório para acesso aos dados de serviço no banco. */
    private final ServicoRepository servicoRepo = new ServicoRepository();

    /**
     * Cadastra um serviço verificando se a descrição foi informada e se o valor base é positivo.
     *
     * @param servico objeto com os dados do serviço
     */
    public void cadastrarServico(Servico servico) {
        try {
            if (servico.getDescricao() == null || servico.getDescricao().isBlank()) {
                System.out.println("Descricao e obrigatoria.");
                return;
            }
            if (servico.getValorBase() <= 0) {
                System.out.println("Valor base deve ser maior que zero.");
                return;
            }
            servicoRepo.salvar(servico);
            System.out.println("Servico cadastrado com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro ao cadastrar servico: " + e.getMessage());
        }
    }

    /**
     * Busca um serviço pelo ID.
     *
     * @param id identificador do serviço
     * @return {@code Optional} com o serviço, ou vazio se não encontrado
     */
    public Optional<Servico> buscarPorId(Long id) {
        try {
            return servicoRepo.buscarPorId(id);
        } catch (Exception e) {
            System.out.println("Erro ao buscar servico: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Lista todos os serviços cadastrados.
     *
     * @return lista de serviços
     */
    public List<Servico> listarTodos() {
        try {
            return servicoRepo.listarTodos();
        } catch (Exception e) {
            System.out.println("Erro ao listar servicos: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Atualiza os dados de um serviço existente.
     *
     * @param servico objeto com os dados atualizados (deve ter ID preenchido)
     */
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

    /**
     * Remove um serviço pelo ID.
     *
     * @param id identificador do serviço a ser removido
     */
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

    /**
     * Busca serviços pela descrição usando busca parcial (LIKE).
     *
     * @param termo texto a ser pesquisado na descrição
     * @return lista de serviços que contêm o termo na descrição
     */
    public List<Servico> buscarPorTermo(String termo) {
        try {
            return servicoRepo.buscarPorDescricao(termo);
        } catch (Exception e) {
            System.out.println("Erro ao buscar servico: " + e.getMessage());
            return List.of();
        }
    }
}