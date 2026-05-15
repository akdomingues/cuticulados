package org.cuticulados.pm.ui;

import org.cuticulados.pm.config.FlywayConfig;
import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.Profissional;
import org.cuticulados.pm.entity.TipoUsuario;
import org.cuticulados.pm.entity.Usuario;
import org.cuticulados.pm.service.UsuarioService;
import org.cuticulados.pm.service.VendaAvulsaService;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Ponto de entrada da aplicação Cuticulados.
 * Reduz e desativa por completo os barramentos de agendamentos e vendas em formato de terminal,
 * mantendo apenas rotinas contábeis críticas residuais de encerramento diário de expediente.
 */
public class Main {

    private static final UsuarioService      usuarioService      = new UsuarioService();
    private static final VendaAvulsaService  vendaAvulsaService  = new VendaAvulsaService();
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Inicializa a pilha de persistência e expõe as telas operacionais Swing de faturamento.
     *
     * @param args argumentos de linha de comando
     */
    public static void main(String[] args) {
        try {
            System.out.println("=== Cuticulados — Inicializando Ambiente Visual ===");
            FlywayConfig.executarMigracoes();
            JpaUtil.inicializar();

            javax.swing.SwingUtilities.invokeLater(() ->
                    new org.cuticulados.pm.ui.frames.LoginFrame().setVisible(true));
        } catch (Exception e) {
            System.out.println("Erro fatal ao iniciar o sistema: " + e.getMessage());
            JpaUtil.fechar();
        }
    }

    /**
     * Intercepta acessos legados e avisa sobre a necessidade de migração gráfica.
     *
     * @return boolean controle do loop
     */
    private static boolean menuLogin() {
        System.out.println("\n--- Login ---");
        System.out.print("Login: ");
        String login = scanner.nextLine().trim();
        System.out.print("Senha: ");
        String senha = scanner.nextLine().trim();

        Optional<Usuario> op = usuarioService.autenticar(login, senha);
        if (op.isEmpty()) {
            System.out.print("Continuar? (s/n): ");
            return scanner.nextLine().trim().equalsIgnoreCase("s");
        }

        Usuario usuario = op.get();
        System.out.println("Autenticado. Redirecionando para interface Swing...");

        switch (usuario.getTipo()) {
            case ADMIN        -> menuAdmin(usuario);
            case PROFISSIONAL -> menuProfissional(usuario);
            case CLIENTE      -> System.out.println("Acesso console desativado.");
        }
        return true;
    }

    /**
     * Informa que as rotinas de nível tático/estratégico do administrador migraram para Swing.
     */
    private static void menuAdmin(Usuario admin) {
        System.out.println("\n=== Modo Administrador Totalmente Migrado para Swing ===");
        System.out.println("Encerre a aplicação e inicie pelo visualizador de UI.");
    }

    /**
     * Controla os fluxos residuais do colaborador, permitindo apenas fechamento transacional diário.
     */
    private static void menuProfissional(Usuario profissionalUsuario) {
        boolean loop = true;
        while (loop) {
            System.out.println("\n=== Menu Profissional (Estrutura Residual) ===");
            System.out.println("1-5, 7. Operações migradas para Swing");
            System.out.println("6. Finalizar dia (Única operação residual)");
            System.out.println("0. Sair");
            System.out.print("Opção: ");
            String op = scanner.nextLine().trim();

            switch (op) {
                case "6" -> fecharDiaProfissional(profissionalUsuario);
                case "0" -> loop = false;
                default  -> System.out.println("Acesse via Painel Visual.");
            }
        }
    }

    /**
     * Realiza a varredura contábil transacional das ordens do dia e fecha o caixa corporativo do colaborador.
     */
    private static void fecharDiaProfissional(Usuario usuario) {
        try {
            List<Usuario> profs = usuarioService.listarPorTipo(TipoUsuario.PROFISSIONAL);
            Optional<Usuario> opProf = profs.stream()
                    .filter(u -> u.getId().equals(usuario.getId()))
                    .findFirst();

            if (opProf.isEmpty() || !(opProf.get() instanceof Profissional profissional)) {
                System.out.println("Profissional não encontrado.");
                return;
            }

            System.out.println("\n=== Finalizar Dia ===");
            System.out.println("Isso irá fechar todas as vendas de hoje. Confirma? (s/n): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("s")) {
                return;
            }
            vendaAvulsaService.fecharDia(profissional);
        } catch (Exception e) {
            System.out.println("Erro ao fechar dia: " + e.getMessage());
        }
    }
}