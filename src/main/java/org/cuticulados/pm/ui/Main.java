package org.cuticulados.pm.ui;

import org.cuticulados.pm.config.FlywayConfig;
import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.*;
import org.cuticulados.pm.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Ponto de entrada da aplicação Cuticulados.
 * Gerencia o ciclo de transição evolutiva do sistema, desativando os fluxos legados
 * de console para os módulos de Serviços, Produtos e Relatórios Gerenciais em favor das UIs Swing.
 */
public class Main {

    private static final AgendamentoService  agendamentoService  = new AgendamentoService();
    private static final ClienteService      clienteService      = new ClienteService();
    private static final UsuarioService      usuarioService      = new UsuarioService();
    private static final ProdutoService      produtoService      = new ProdutoService();
    private static final ServicoService      servicoService      = new ServicoService();
    private static final VendaAvulsaService  vendaAvulsaService  = new VendaAvulsaService();
    private static final RelatorioService    relatorioService    = new RelatorioService();

    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter FMT_DATA      = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Bootstrapping do ecossistema e despacho da interface gráfica para a thread Swing.
     *
     * @param args argumentos de linha de comando
     */
    public static void main(String[] args) {
        try {
            System.out.println("=== Cuticulados — Sistema de Gestão ===");
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
     * Autentica e direciona a sessão para consoles ativos ou barramentos visuais Swing.
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
        System.out.println("Bem-vindo(a), " + usuario.getNome() + "! [" + usuario.getTipo() + "]");

        switch (usuario.getTipo()) {
            case ADMIN        -> menuAdmin(usuario);
            case PROFISSIONAL -> menuProfissional(usuario);
            case CLIENTE      -> System.out.println("Acesso via Console desativado. Use a interface gráfica.");
        }
        return true;
    }

    /**
     * Controla as ações de nível de administrador, bloqueando acessos a cadastros base,
     * inventários e faturamentos gerados em console que já rodam em ambiente gráfico.
     *
     * @param admin Administrador autenticado
     */
    private static void menuAdmin(Usuario admin) {
        boolean loop = true;
        while (loop) {
            System.out.println("\n=== Menu Admin ===");
            System.out.println("1. Gerenciar Clientes (Migrado p/ Swing)");
            System.out.println("2. Gerenciar Profissionais (Migrado p/ Swing)");
            System.out.println("3. Gerenciar Serviços (Migrado p/ Swing)");
            System.out.println("4. Gerenciar Produtos (Migrado p/ Swing)");
            System.out.println("5. Gerenciar Agendamentos");
            System.out.println("6. Vendas Avulsas");
            System.out.println("7. Relatórios (Migrado p/ Swing)");
            System.out.println("0. Sair");
            System.out.print("Opção: ");
            String op = scanner.nextLine().trim();

            switch (op) {
                case "1", "2", "3", "4", "7" -> System.out.println("Operação migrada para interface gráfica.");
                case "5" -> menuAgendamentos();
                case "6" -> menuVendasAvulsas();
                case "0" -> loop = false;
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    /**
     * Provê controle aos profissionais de saúde e estética sobre suas agendas táticas de trabalho.
     *
     * @param profissionalUsuario Profissional autenticado
     */
    private static void menuProfissional(Usuario profissionalUsuario) {
        boolean loop = true;
        while (loop) {
            System.out.println("\n=== Menu Profissional ===");
            System.out.println("1. Ver agendamentos");
            System.out.println("2. Concluir agendamento");
            System.out.println("3. Cancelar agendamento");
            System.out.println("4. Registrar venda avulsa");
            System.out.println("5. Ver estoque (Migrado p/ Swing)");
            System.out.println("6. Finalizar dia");
            System.out.println("7. Relatório de vendas hoje (Migrado p/ Swing)");
            System.out.println("0. Sair");
            System.out.print("Opção: ");
            String op = scanner.nextLine().trim();

            switch (op) {
                case "1" -> listarAgendamentos();
                case "2" -> concluirAgendamento();
                case "3" -> cancelarAgendamento();
                case "4" -> registrarVendaAvulsa();
                case "5", "7" -> System.out.println("Acesse via Painel de Interface Gráfica.");
                case "6" -> fecharDiaProfissional(profissionalUsuario);
                case "0" -> loop = false;
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    /**
     * Efetua o fechamento contábil e consolidação de caixa associado ao profissional solicitante.
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

    /**
     * Suporte residual para injeção de dados relacionais em ordens de agendamento de console.
     */
    private static void listarClientes() {
        List<Cliente> clientes = clienteService.listarTodos();
        if (clientes.isEmpty()) { System.out.println("Nenhum cliente."); return; }
        clientes.forEach(c -> System.out.printf(" [%d] %s | CPF: %s | Tipo: %s%n",
                c.getId(), c.getNome(), c.getCpf(), c.getTipoCliente()));
    }

    /**
     * Retorna o corpo técnico ativo para vinculação nas rotinas de agendamento em console.
     */
    private static void listarProfissionais() {
        List<Usuario> profs = usuarioService.listarPorTipo(TipoUsuario.PROFISSIONAL);
        if (profs.isEmpty()) { System.out.println("Nenhum profissional."); return; }
        profs.forEach(p -> System.out.printf(" [%d] %s | Login: %s%n",
                p.getId(), p.getNome(), p.getLogin()));
    }

    /**
     * Indexa as especialidades cadastradas para consumo e amarração estrutural na criação de agendas.
     */
    private static void listarServicos() {
        List<Servico> servicos = servicoService.listarTodos();
        if (servicos.isEmpty()) { System.out.println("Nenhum serviço."); return; }
        servicos.forEach(s -> System.out.printf(" [%d] %s | R$ %.2f | %d min%n",
                s.getId(), s.getDescricao(), s.getValorBase(), s.getDuracaoMinutos()));
    }

    /**
     * Orquestra o painel de operações lógicas sobre os compromissos cronometrados do salão.
     */
    private static void menuAgendamentos() {
        boolean loop = true;
        while (loop) {
            System.out.println("\n-- Agendamentos --");
            System.out.println("1. Listar");
            System.out.println("2. Criar");
            System.out.println("3. Concluir");
            System.out.println("4. Cancelar");
            System.out.println("5. Remover");
            System.out.println("6. Buscar por período");
            System.out.println("0. Voltar");
            System.out.print("Opção: ");
            String op = scanner.nextLine().trim();
            switch (op) {
                case "1" -> listarAgendamentos();
                case "2" -> criarAgendamento();
                case "3" -> concluirAgendamento();
                case "4" -> cancelarAgendamento();
                case "5" -> removerAgendamento();
                case "6" -> agendamentosPorPeriodo();
                case "0" -> loop = false;
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    /**
     * Emite a listagem das agendas para triagem via terminal.
     */
    private static void listarAgendamentos() {
        List<Agendamento> lista = agendamentoService.listarTodos();
        if (lista.isEmpty()) { System.out.println("Nenhum agendamento."); return; }
        lista.forEach(a -> System.out.printf(" [%d] %s | %s → %s | %s | R$ %.2f%n",
                a.getId(), a.getStatus(), a.getDataHoraInicio().format(FMT_DATA_HORA),
                a.getDataHoraFim().format(FMT_DATA_HORA), a.getCliente().getNome(), a.getValorFinal()));
    }

    /**
     * Vincula clientes, colaboradores e procedimentos em um registro persistido de agendamento.
     */
    private static void criarAgendamento() {
        try {
            listarClientes();
            System.out.print("ID do cliente: ");
            Long clienteId = Long.parseLong(scanner.nextLine().trim());
            Optional<Cliente> opC = clienteService.buscarPorId(clienteId);
            if (opC.isEmpty()) { System.out.println("Não encontrado."); return; }

            listarProfissionais();
            System.out.print("ID do profissional: ");
            Long profId = Long.parseLong(scanner.nextLine().trim());
            List<Usuario> profs = usuarioService.listarPorTipo(TipoUsuario.PROFISSIONAL);
            Optional<Usuario> opP = profs.stream().filter(u -> u.getId().equals(profId)).findFirst();
            if (opP.isEmpty()) { System.out.println("Não encontrado."); return; }

            System.out.print("Início (dd/MM/yyyy HH:mm): ");
            LocalDateTime inicio = LocalDateTime.parse(scanner.nextLine().trim(), FMT_DATA_HORA);
            System.out.print("Fim    (dd/MM/yyyy HH:mm): ");
            LocalDateTime fim = LocalDateTime.parse(scanner.nextLine().trim(), FMT_DATA_HORA);

            Agendamento ag = new Agendamento();
            ag.setCliente(opC.get());
            ag.setProfissional((Profissional) opP.get());
            ag.setDataHoraInicio(inicio);
            ag.setDataHoraFim(fim);

            listarServicos();
            System.out.println("Adicione os serviços (Enter vazio para finalizar):");
            while (true) {
                System.out.print("ID do serviço (ou Enter para concluir): ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) break;
                try {
                    Long servicoId = Long.parseLong(input);
                    Optional<Servico> opS = servicoService.buscarPorId(servicoId);
                    if (opS.isEmpty()) { System.out.println("Serviço não encontrado."); continue; }
                    Servico servico = opS.get();
                    AgendamentoServico as = new AgendamentoServico();
                    as.setServico(servico);
                    as.setAgendamento(ag);
                    as.setPrecoAplicado(servico.getValorBase());
                    as.setQuantidade(1);
                    ag.getServicos().add(as);
                    System.out.println("Serviço adicionado: " + servico.getDescricao());
                } catch (NumberFormatException ex) {
                    System.out.println("ID inválido.");
                }
            }
            agendamentoService.criarAgendamento(ag);
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /**
     * Processa a entrega contábil e operacional de uma agenda via ID.
     */
    private static void concluirAgendamento() {
        try {
            System.out.print("ID: ");
            agendamentoService.concluirAgendamento(Long.parseLong(scanner.nextLine().trim()));
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /**
     * Efetua a invalidação controlada de uma reserva de horário na agenda.
     */
    private static void cancelarAgendamento() {
        try {
            System.out.print("ID: ");
            agendamentoService.cancelarAgendamento(Long.parseLong(scanner.nextLine().trim()));
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /**
     * Exclui fisicamente do banco de dados registros inconsistentes de agendamento.
     */
    private static void removerAgendamento() {
        try {
            System.out.print("ID: ");
            agendamentoService.removerAgendamento(Long.parseLong(scanner.nextLine().trim()));
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /**
     * Gera logs analíticos de agendamentos no console parametrizados por escopo temporal.
     */
    private static void agendamentosPorPeriodo() {
        try {
            System.out.print("Início (dd/MM/yyyy): ");
            LocalDate inicio = LocalDate.parse(scanner.nextLine().trim(), FMT_DATA);
            System.out.print("Fim    (dd/MM/yyyy): ");
            LocalDate fim = LocalDate.parse(scanner.nextLine().trim(), FMT_DATA);
            relatorioService.gerarRelatorioAgendamentos(inicio, fim);
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /**
     * Expõe controles legados para deságüe e lançamento comercial de itens do estoque físico.
     */
    private static void menuVendasAvulsas() {
        boolean loop = true;
        while (loop) {
            System.out.println("\n-- Vendas Avulsas --");
            System.out.println("1. Listar vendas");
            System.out.println("2. Registrar venda");
            System.out.println("3. Remover venda");
            System.out.println("0. Voltar");
            System.out.print("Opção: ");
            String op = scanner.nextLine().trim();
            switch (op) {
                case "1" -> vendaAvulsaService.listarTodas().forEach(v -> System.out.printf(
                        " [%d] %s | %dx %s | R$ %.2f | %s%n",
                        v.getId(), v.getDataVenda().format(FMT_DATA_HORA),
                        v.getQuantidade(), v.getProduto().getNome(),
                        v.getTotal(), v.isFechado() ? "FECHADO" : "ABERTO"));
                case "2" -> registrarVendaAvulsa();
                case "3" -> {
                    try {
                        System.out.print("ID: ");
                        vendaAvulsaService.removerVenda(Long.parseLong(scanner.nextLine().trim()));
                    } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
                }
                case "0" -> loop = false;
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    /**
     * Abate unidades físicas do inventário gerando faturamentos comerciais imediatos à vista.
     */
    private static void registrarVendaAvulsa() {
        try {
            produtoService.listarTodos().forEach(p -> System.out.printf(
                    " [%d] %s | qtd: %d | R$ %.2f%n",
                    p.getId(), p.getNome(), p.getQuantidadeEstoque(), p.getPrecoVenda()));
            System.out.print("ID do produto: ");
            Long prodId = Long.parseLong(scanner.nextLine().trim());
            Optional<Produto> opP = produtoService.buscarPorId(prodId);
            if (opP.isEmpty()) { System.out.println("Produto não encontrado."); return; }

            listarProfissionais();
            System.out.print("ID do profissional: ");
            Long profId = Long.parseLong(scanner.nextLine().trim());
            List<Usuario> profs = usuarioService.listarPorTipo(TipoUsuario.PROFISSIONAL);
            Optional<Usuario> opU = profs.stream().filter(u -> u.getId().equals(profId)).findFirst();
            if (opU.isEmpty()) { System.out.println("Profissional não encontrado."); return; }

            System.out.print("Quantidade: ");
            int qtd = Integer.parseInt(scanner.nextLine().trim());

            VendaAvulsa venda = new VendaAvulsa();
            venda.setProduto(opP.get());
            venda.setProfissional((Profissional) opU.get());
            venda.setQuantidade(qtd);
            vendaAvulsaService.registrarVenda(venda);
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }
}