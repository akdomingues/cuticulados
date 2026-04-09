package org.cuticulados.pm.ui;

import org.cuticulados.pm.config.FlywayConfig;
import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.*;
import org.cuticulados.pm.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final DateTimeFormatter FMT_DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_D  = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final UsuarioService    usuarioSvc   = new UsuarioService();
    private static final ClienteService    clienteSvc   = new ClienteService();
    private static final ProdutoService    produtoSvc   = new ProdutoService();
    private static final ServicoService    servicoSvc   = new ServicoService();
    private static final AgendamentoService agendSvc    = new AgendamentoService();
    private static final VendaAvulsaService vendaSvc    = new VendaAvulsaService();
    private static final RelatorioService  relatorioSvc = new RelatorioService();

    private static Usuario usuarioLogado = null;

    public static void main(String[] args) {
        System.out.println("Iniciando Cuticulados...");

        // Flyway OBRIGATORIAMENTE antes do JPA
        FlywayConfig.executarMigracoes();
        JpaUtil.inicializar();

        System.out.println("Sistema pronto!\n");

        telaLogin();

        JpaUtil.fechar();
        System.out.println("Ate logo!");
    }

    // ── LOGIN ─────────────────────────────────────────────────────────────
    private static void telaLogin() {
        while (true) {
            System.out.println("╔══════════════════════════╗");
            System.out.println("      CUTICULADOS           ");
            System.out.println("╚══════════════════════════╝");
            System.out.print("Login: ");
            String login = sc.nextLine().trim();
            System.out.print("Senha: ");
            String senha = sc.nextLine().trim();

            Optional<Usuario> op = usuarioSvc.autenticar(login, senha);
            if (op.isPresent()) {
                usuarioLogado = op.get();
                System.out.println("Bem-vinda, " + usuarioLogado.getNome() + "!\n");
                menuPrincipal();
                usuarioLogado = null;
            } else {
                System.out.println("Login ou senha incorretos.\n");
            }
        }
    }

    // ── MENU PRINCIPAL ────────────────────────────────────────────────────
    private static void menuPrincipal() {
        boolean isAdmin = usuarioLogado.getTipo() == TipoUsuario.ADMIN;
        boolean loop = true;

        while (loop) {
            System.out.println("\n══ MENU ════════════════════");
            System.out.println(" 1. Clientes");
            System.out.println(" 2. Produtos");
            System.out.println(" 3. Servicos");
            System.out.println(" 4. Agendamentos");
            System.out.println(" 5. Vendas avulsas");
            System.out.println(" 6. Relatorios");
            if (isAdmin) System.out.println(" 7. Usuarios [ADMIN]");
            System.out.println(" 0. Sair");
            System.out.print("Opcao: ");

            switch (lerInt()) {
                case 1 -> menuClientes();
                case 2 -> menuProdutos(isAdmin);
                case 3 -> menuServicos(isAdmin);
                case 4 -> menuAgendamentos();
                case 5 -> menuVendas();
                case 6 -> menuRelatorios();
                case 7 -> { if (isAdmin) menuUsuarios(); else System.out.println("Acesso negado."); }
                case 0 -> loop = false;
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    // ── CLIENTES ──────────────────────────────────────────────────────────
    private static void menuClientes() {
        boolean loop = true;
        while (loop) {
            System.out.println("\n── Clientes ──────────────");
            System.out.println(" 1. Listar todos");
            System.out.println(" 2. Cadastrar");
            System.out.println(" 3. Atualizar");
            System.out.println(" 4. Remover");
            System.out.println(" 0. Voltar");
            System.out.print("Opcao: ");

            switch (lerInt()) {
                case 1 -> {
                    List<Cliente> clientes = clienteSvc.listarTodos();
                    if (clientes.isEmpty()) {
                        System.out.println("Nenhum cliente cadastrado.");
                    } else {
                        clientes.forEach(c -> System.out.printf(
                                " [%d] %s | CPF: %s | Tel: %s | Tipo: %s%n",
                                c.getId(), c.getNome(), c.getCpf(),
                                c.getTelefone(), c.getTipoCliente()));
                    }
                }
                case 2 -> {
                    Cliente c = new Cliente();
                    System.out.print("Nome: ");        c.setNome(sc.nextLine());
                    System.out.print("Email: ");       c.setEmail(sc.nextLine());
                    System.out.print("Login: ");       c.setLogin(sc.nextLine());
                    System.out.print("Senha: ");       c.setSenha(sc.nextLine());
                    System.out.print("CPF: ");         c.setCpf(sc.nextLine());
                    System.out.print("Telefone: ");    c.setTelefone(sc.nextLine());
                    c.setTipo(TipoUsuario.CLIENTE);
                    clienteSvc.cadastrarCliente(c);
                }
                case 3 -> {
                    System.out.print("ID do cliente: ");
                    Long id = lerLong();
                    Optional<Cliente> op = clienteSvc.buscarPorId(id);
                    if (op.isEmpty()) { System.out.println("Nao encontrado."); break; }
                    Cliente c = op.get();
                    System.out.print("Novo nome [" + c.getNome() + "]: ");
                    String nome = sc.nextLine();
                    if (!nome.isBlank()) c.setNome(nome);
                    System.out.print("Novo telefone [" + c.getTelefone() + "]: ");
                    String tel = sc.nextLine();
                    if (!tel.isBlank()) c.setTelefone(tel);
                    clienteSvc.atualizarCliente(c);
                }
                case 4 -> {
                    System.out.print("ID do cliente: ");
                    clienteSvc.removerCliente(lerLong());
                }
                case 0 -> loop = false;
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    // ── PRODUTOS ──────────────────────────────────────────────────────────
    private static void menuProdutos(boolean isAdmin) {
        boolean loop = true;
        while (loop) {
            System.out.println("\n── Produtos ───────────────");
            System.out.println(" 1. Listar todos");
            System.out.println(" 2. Estoque baixo");
            if (isAdmin) {
                System.out.println(" 3. Cadastrar");
                System.out.println(" 4. Atualizar");
                System.out.println(" 5. Remover");
            }
            System.out.println(" 0. Voltar");
            System.out.print("Opcao: ");

            switch (lerInt()) {
                case 1 -> {
                    List<Produto> lista = produtoSvc.listarTodos();
                    if (lista.isEmpty()) {
                        System.out.println("Nenhum produto.");
                    } else {
                        lista.forEach(p -> System.out.printf(
                                " [%d] %s | Estoque: %d | Venda: R$ %.2f%n",
                                p.getId(), p.getNome(),
                                p.getQuantidadeEstoque(), p.getPrecoVenda()));
                    }
                }
                case 2 -> produtoSvc.verificarEstoqueBaixo();
                case 3 -> {
                    if (!isAdmin) { System.out.println("Acesso negado."); break; }
                    Produto p = new Produto();
                    System.out.print("Nome: ");             p.setNome(sc.nextLine());
                    System.out.print("Preco custo: ");      p.setPrecoCusto(lerDouble());
                    System.out.print("Preco venda: ");      p.setPrecoVenda(lerDouble());
                    System.out.print("Estoque inicial: ");  p.setQuantidadeEstoque(lerInt());
                    System.out.print("Qtd minima: ");       p.setQuantidadeMinima(lerInt());
                    produtoSvc.cadastrarProduto(p);
                }
                case 4 -> {
                    if (!isAdmin) { System.out.println("Acesso negado."); break; }
                    System.out.print("ID do produto: ");
                    Long id = lerLong();
                    Optional<Produto> op = produtoSvc.buscarPorId(id);
                    if (op.isEmpty()) { System.out.println("Nao encontrado."); break; }
                    Produto p = op.get();
                    System.out.print("Novo preco venda [" + p.getPrecoVenda() + "]: ");
                    String pv = sc.nextLine();
                    if (!pv.isBlank()) p.setPrecoVenda(Double.parseDouble(pv.replace(",", ".")));
                    System.out.print("Novo estoque [" + p.getQuantidadeEstoque() + "]: ");
                    String est = sc.nextLine();
                    if (!est.isBlank()) p.setQuantidadeEstoque(Integer.parseInt(est));
                    produtoSvc.atualizarProduto(p);
                    System.out.println("Produto atualizado.");
                }
                case 5 -> {
                    if (!isAdmin) { System.out.println("Acesso negado."); break; }
                    System.out.print("ID do produto: ");
                    produtoSvc.removerProduto(lerLong());
                }
                case 0 -> loop = false;
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    // ── SERVICOS ──────────────────────────────────────────────────────────
    private static void menuServicos(boolean isAdmin) {
        boolean loop = true;
        while (loop) {
            System.out.println("\n── Servicos ───────────────");
            System.out.println(" 1. Listar todos");
            System.out.println(" 2. Buscar por termo");
            if (isAdmin) {
                System.out.println(" 3. Cadastrar");
                System.out.println(" 4. Atualizar");
                System.out.println(" 5. Remover");
            }
            System.out.println(" 0. Voltar");
            System.out.print("Opcao: ");

            switch (lerInt()) {
                case 1 -> {
                    List<Servico> lista = servicoSvc.listarTodos();
                    if (lista.isEmpty()) {
                        System.out.println("Nenhum servico.");
                    } else {
                        lista.forEach(s -> System.out.printf(
                                " [%d] %s | R$ %.2f | %d min%n",
                                s.getId(), s.getDescricao(),
                                s.getValorBase(), s.getDuracaoMinutos()));
                    }
                }
                case 2 -> {
                    System.out.print("Termo: ");
                    List<Servico> resultado = servicoSvc.buscarPorTermo(sc.nextLine());
                    resultado.forEach(s -> System.out.printf(
                            " [%d] %s | R$ %.2f%n", s.getId(), s.getDescricao(), s.getValorBase()));
                }
                case 3 -> {
                    if (!isAdmin) { System.out.println("Acesso negado."); break; }
                    Servico s = new Servico();
                    System.out.print("Descricao: ");      s.setDescricao(sc.nextLine());
                    System.out.print("Valor base: ");     s.setValorBase(lerDouble());
                    System.out.print("Duracao (min): ");  s.setDuracaoMinutos(lerInt());
                    servicoSvc.cadastrarServico(s);
                }
                case 4 -> {
                    if (!isAdmin) { System.out.println("Acesso negado."); break; }
                    System.out.print("ID do servico: ");
                    Long id = lerLong();
                    Optional<Servico> op = servicoSvc.buscarPorId(id);
                    if (op.isEmpty()) { System.out.println("Nao encontrado."); break; }
                    Servico s = op.get();
                    System.out.print("Nova descricao [" + s.getDescricao() + "]: ");
                    String desc = sc.nextLine();
                    if (!desc.isBlank()) s.setDescricao(desc);
                    System.out.print("Novo valor [" + s.getValorBase() + "]: ");
                    String val = sc.nextLine();
                    if (!val.isBlank()) s.setValorBase(Double.parseDouble(val.replace(",", ".")));
                    servicoSvc.atualizarServico(s);
                    System.out.println("Servico atualizado.");
                }
                case 5 -> {
                    if (!isAdmin) { System.out.println("Acesso negado."); break; }
                    System.out.print("ID do servico: ");
                    servicoSvc.removerServico(lerLong());
                }
                case 0 -> loop = false;
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    // ── AGENDAMENTOS ──────────────────────────────────────────────────────
    private static void menuAgendamentos() {
        boolean loop = true;
        while (loop) {
            System.out.println("\n── Agendamentos ───────────");
            System.out.println(" 1. Listar todos");
            System.out.println(" 2. Listar por status");
            System.out.println(" 3. Novo agendamento");
            System.out.println(" 4. Concluir agendamento");
            System.out.println(" 5. Cancelar agendamento");
            System.out.println(" 6. Remover agendamento");
            System.out.println(" 0. Voltar");
            System.out.print("Opcao: ");

            switch (lerInt()) {
                case 1 -> {
                    List<Agendamento> lista = agendSvc.listarTodos();
                    if (lista.isEmpty()) {
                        System.out.println("Nenhum agendamento.");
                    } else {
                        lista.forEach(a -> System.out.printf(
                                " [%d] %s | %s | %s | R$ %.2f | %s%n",
                                a.getId(),
                                a.getDataHoraInicio().format(FMT_DT),
                                a.getCliente() != null ? a.getCliente().getNome() : "?",
                                a.getProfissional() != null ? a.getProfissional().getNome() : "?",
                                a.getValorFinal(),
                                a.getStatus()));
                    }
                }
                case 2 -> {
                    System.out.println("Status: 1-PENDENTE  2-CONCLUIDO  3-CANCELADO");
                    System.out.print("Escolha: ");
                    int op = lerInt();
                    StatusAgendamento status = switch (op) {
                        case 1 -> StatusAgendamento.PENDENTE;
                        case 2 -> StatusAgendamento.CONCLUIDO;
                        case 3 -> StatusAgendamento.CANCELADO;
                        default -> null;
                    };
                    if (status == null) { System.out.println("Status invalido."); break; }
                    List<Agendamento> filtrado = agendSvc.buscarPorStatus(status);
                    filtrado.forEach(a -> System.out.printf(
                            " [%d] %s | %s | R$ %.2f%n",
                            a.getId(),
                            a.getDataHoraInicio().format(FMT_DT),
                            a.getCliente() != null ? a.getCliente().getNome() : "?",
                            a.getValorFinal()));
                }
                case 3 -> novoAgendamento();
                case 4 -> {
                    System.out.print("ID do agendamento: ");
                    agendSvc.concluirAgendamento(lerLong());
                }
                case 5 -> {
                    System.out.print("ID do agendamento: ");
                    agendSvc.cancelarAgendamento(lerLong());
                }
                case 6 -> {
                    System.out.print("ID do agendamento: ");
                    agendSvc.removerAgendamento(lerLong());
                }
                case 0 -> loop = false;
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    private static void novoAgendamento() {
        try {
            System.out.println("\n  Clientes disponíveis:");
            clienteSvc.listarTodos().forEach(c ->
                    System.out.printf("  [%d] %s%n", c.getId(), c.getNome()));

            System.out.print("ID do cliente: ");
            Long clienteId = lerLong();
            Optional<Cliente> opCliente = clienteSvc.buscarPorId(clienteId);
            if (opCliente.isEmpty()) { System.out.println("Cliente nao encontrado."); return; }

            System.out.println("\n  Profissionais disponíveis:");
            usuarioSvc.listarPorTipo(TipoUsuario.PROFISSIONAL).forEach(u ->
                    System.out.printf("  [%d] %s%n", u.getId(), u.getNome()));

            System.out.print("ID da profissional: ");
            Long profId = lerLong();
            Optional<Usuario> opProf = usuarioSvc.listarTodos().stream()
                    .filter(u -> u.getId().equals(profId))
                    .findFirst();
            if (opProf.isEmpty()) { System.out.println("Profissional nao encontrada."); return; }

            System.out.print("Inicio (dd/MM/yyyy HH:mm): ");
            LocalDateTime inicio = lerDateTime();
            System.out.print("Fim    (dd/MM/yyyy HH:mm): ");
            LocalDateTime fim = lerDateTime();

            System.out.println("\n  Servicos disponíveis:");
            servicoSvc.listarTodos().forEach(s ->
                    System.out.printf("  [%d] %s | R$ %.2f%n", s.getId(), s.getDescricao(), s.getValorBase()));

            Agendamento ag = new Agendamento();
            ag.setCliente(opCliente.get());
            ag.setProfissional((Profissional) opProf.get());
            ag.setDataHoraInicio(inicio);
            ag.setDataHoraFim(fim);

            boolean addServico = true;
            while (addServico) {
                System.out.print("ID do servico (0 para finalizar): ");
                Long sid = lerLong();
                if (sid == 0) { addServico = false; continue; }
                Optional<Servico> opS = servicoSvc.buscarPorId(sid);
                if (opS.isEmpty()) { System.out.println("Servico nao encontrado."); continue; }

                AgendamentoServico as = new AgendamentoServico();
                as.setAgendamento(ag);
                as.setServico(opS.get());
                as.setPrecoAplicado(opS.get().getValorBase());
                as.setDescontoAplicado(0.0);
                as.setQuantidade(1);
                ag.getServicos().add(as);
            }

            agendSvc.criarAgendamento(ag);
        } catch (Exception e) {
            System.out.println("Erro ao criar agendamento: " + e.getMessage());
        }
    }

    // ── VENDAS AVULSAS ────────────────────────────────────────────────────
    private static void menuVendas() {
        boolean loop = true;
        while (loop) {
            System.out.println("\n── Vendas Avulsas ─────────");
            System.out.println(" 1. Listar todas");
            System.out.println(" 2. Registrar venda");
            System.out.println(" 3. Remover venda");
            System.out.println(" 0. Voltar");
            System.out.print("Opcao: ");

            switch (lerInt()) {
                case 1 -> {
                    List<VendaAvulsa> lista = vendaSvc.listarTodas();
                    if (lista.isEmpty()) {
                        System.out.println("Nenhuma venda registrada.");
                    } else {
                        lista.forEach(v -> System.out.printf(
                                " [%d] %s x%d | R$ %.2f | %s%n",
                                v.getId(),
                                v.getProduto().getNome(),
                                v.getQuantidade(),
                                v.getTotal(),
                                v.getDataVenda()));
                    }
                }
                case 2 -> {
                    try {
                        System.out.println("\n  Produtos disponíveis:");
                        produtoSvc.listarTodos().forEach(p ->
                                System.out.printf("  [%d] %s | Estoque: %d | R$ %.2f%n",
                                        p.getId(), p.getNome(), p.getQuantidadeEstoque(), p.getPrecoVenda()));

                        System.out.print("ID do produto: ");
                        Long prodId = lerLong();
                        Optional<Produto> opP = produtoSvc.buscarPorId(prodId);
                        if (opP.isEmpty()) { System.out.println("Produto nao encontrado."); break; }

                        System.out.println("\n  Profissionais:");
                        usuarioSvc.listarPorTipo(TipoUsuario.PROFISSIONAL).forEach(u ->
                                System.out.printf("  [%d] %s%n", u.getId(), u.getNome()));

                        System.out.print("ID da profissional: ");
                        Long profId = lerLong();
                        Optional<Usuario> opProf = usuarioSvc.listarTodos().stream()
                                .filter(u -> u.getId().equals(profId))
                                .findFirst();
                        if (opProf.isEmpty()) { System.out.println("Profissional nao encontrada."); break; }

                        System.out.print("Quantidade: ");
                        int qtd = lerInt();

                        VendaAvulsa venda = new VendaAvulsa();
                        venda.setProduto(opP.get());
                        venda.setProfissional((Profissional) opProf.get());
                        venda.setQuantidade(qtd);
                        vendaSvc.registrarVenda(venda);
                    } catch (Exception e) {
                        System.out.println("Erro: " + e.getMessage());
                    }
                }
                case 3 -> {
                    System.out.print("ID da venda: ");
                    vendaSvc.removerVenda(lerLong());
                }
                case 0 -> loop = false;
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    // ── RELATORIOS ────────────────────────────────────────────────────────
    private static void menuRelatorios() {
        boolean loop = true;
        while (loop) {
            System.out.println("\n── Relatorios ─────────────");
            System.out.println(" 1. Agendamentos por periodo");
            System.out.println(" 2. Financeiro por periodo");
            System.out.println(" 3. Estoque atual");
            System.out.println(" 4. Saldo geral");
            System.out.println(" 0. Voltar");
            System.out.print("Opcao: ");

            switch (lerInt()) {
                case 1 -> {
                    System.out.print("Inicio (dd/MM/yyyy): ");
                    LocalDate ini = lerDate();
                    System.out.print("Fim    (dd/MM/yyyy): ");
                    LocalDate fim = lerDate();
                    relatorioSvc.gerarRelatorioAgendamentos(ini, fim);
                }
                case 2 -> {
                    System.out.print("Inicio (dd/MM/yyyy): ");
                    LocalDate ini = lerDate();
                    System.out.print("Fim    (dd/MM/yyyy): ");
                    LocalDate fim = lerDate();
                    relatorioSvc.gerarRelatorioFinanceiro(ini, fim);
                }
                case 3 -> relatorioSvc.gerarRelatorioEstoque();
                case 4 -> {
                    Double saldo = relatorioSvc.calcularSaldo();
                    System.out.printf("Saldo geral: R$ %.2f%n", saldo);
                }
                case 0 -> loop = false;
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    // ── USUARIOS (Admin) ──────────────────────────────────────────────────
    private static void menuUsuarios() {
        boolean loop = true;
        while (loop) {
            System.out.println("\n── Usuarios [ADMIN] ───────");
            System.out.println(" 1. Listar todos");
            System.out.println(" 2. Cadastrar profissional");
            System.out.println(" 3. Remover usuario");
            System.out.println(" 0. Voltar");
            System.out.print("Opcao: ");

            switch (lerInt()) {
                case 1 -> {
                    List<Usuario> lista = usuarioSvc.listarTodos();
                    lista.forEach(u -> System.out.printf(
                            " [%d] %s (%s) | %s%n",
                            u.getId(), u.getNome(), u.getLogin(), u.getTipo()));
                }
                case 2 -> {
                    Profissional p = new Profissional();
                    System.out.print("Nome: ");          p.setNome(sc.nextLine());
                    System.out.print("Email: ");         p.setEmail(sc.nextLine());
                    System.out.print("Login: ");         p.setLogin(sc.nextLine());
                    System.out.print("Senha: ");         p.setSenha(sc.nextLine());
                    System.out.print("Especialidade: "); p.setEspecialidade(sc.nextLine());
                    p.setTipo(TipoUsuario.PROFISSIONAL);
                    usuarioSvc.cadastrarUsuario(p);
                }
                case 3 -> {
                    System.out.print("ID do usuario: ");
                    usuarioSvc.removerUsuario(lerLong());
                }
                case 0 -> loop = false;
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    // ── HELPERS ───────────────────────────────────────────────────────────
    private static int lerInt() {
        while (true) {
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.print("Digite um numero inteiro: "); }
        }
    }

    private static Long lerLong() {
        while (true) {
            try { return Long.parseLong(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.print("Digite um numero valido: "); }
        }
    }

    private static Double lerDouble() {
        while (true) {
            try { return Double.parseDouble(sc.nextLine().trim().replace(",", ".")); }
            catch (NumberFormatException e) { System.out.print("Formato invalido (ex: 29.90): "); }
        }
    }

    private static LocalDateTime lerDateTime() {
        while (true) {
            try { return LocalDateTime.parse(sc.nextLine().trim(), FMT_DT); }
            catch (DateTimeParseException e) { System.out.print("Formato: dd/MM/yyyy HH:mm: "); }
        }
    }

    private static LocalDate lerDate() {
        while (true) {
            try { return LocalDate.parse(sc.nextLine().trim(), FMT_D); }
            catch (DateTimeParseException e) { System.out.print("Formato: dd/MM/yyyy: "); }
        }
    }
}