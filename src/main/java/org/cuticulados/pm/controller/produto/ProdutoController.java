package org.cuticulados.pm.controller.produto;

import org.cuticulados.pm.controller.produto.mapper.ProdutoMapper;
import org.cuticulados.pm.entity.ProdutoEntity;
import org.cuticulados.pm.service.ProdutoService;

import java.util.List;
import java.util.Optional;

public class ProdutoController {

    private final ProdutoService produtoService = new ProdutoService();

    public String cadastrarProduto(ProdutoRequest request) {
        ProdutoEntity entity = ProdutoMapper.dtoToEntity(request);
        return produtoService.cadastrarProduto(entity);
    }

    // Sobrecarga para quando a UI já monta a entidade com todos os dados
    public String cadastrarProduto(ProdutoEntity entity) {
        return produtoService.cadastrarProduto(entity);
    }

    public Optional<ProdutoEntity> buscarPorId(Long id) {
        return produtoService.buscarPorId(id);
    }

    public List<ProdutoEntity> listarTodos() {
        return produtoService.listarTodos();
    }

    public String atualizarProduto(ProdutoEntity produtoEntity) {
        return produtoService.atualizarProduto(produtoEntity);
    }

    public String removerProduto(Long id) {
        return produtoService.removerProduto(id);
    }

    public List<ProdutoEntity> verificarEstoqueBaixo() {
        return produtoService.verificarEstoqueBaixo();
    }
}
