package org.cuticulados.pm.controller.cliente;

public record ClienteRequest(
        String nome,
        String cpf,
        String telefone,
        String email,
        String login,
        String senha
) {
}
