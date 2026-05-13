package org.cuticulados.pm.ui;

import org.cuticulados.pm.config.FlywayConfig;
import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.*;
import org.cuticulados.pm.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Ponto de entrada da aplicação Cuticulados.
 * Inicializa o Flyway e o JPA, exibe o menu de login e direciona para o menu
 * correspondente ao perfil do usuário ({@link TipoUsuario})
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
     * Método principal: inicializa infraestrutura e inicia o fluxo de autenticação.
     *
     * @param args argumentos de linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        try {
            System.out.println("=== Cuticulados — Sistema de Gestão ===");
            FlywayConfig.executarMigracoes();
            JpaUtil.inicializar();

            boolean rodando = true;
            while (rodando) {
                rodando = menuLogin();
            }
        } catch (Exception e) {
            System.out.println("Erro fatal ao iniciar o sistema: " + e.getMessage());
        } finally {
            JpaUtil.fechar();
            scanner.close();
        }
    }

    // ---------------------------------------------------------------
    // AUTENTICAÇÃO
    // ---------------------------------------------------------------

    /**
     * Exibe o prompt de login e autentica o usuário.
     * Após autenticação, redireciona para o menu do perfil ({@link TipoUsuario}) do usuário.
     *
     * @return true para continuar o loop principal, false para encerrar
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
            case CLIENTE      -> menuCliente(usuario);
        }
        return true;
    }

    // ---------------------------------------------------------------
    // MENU ADMIN
    // ---------------------------------------------------------------

    /**
     * Menu principal para usuários com perfil ADMIN.
     * Dá acesso completo a clientes, profissionais, serviços, produtos, agendamentos e relatórios.
     *
     * @param admin usuário logado com perfil ADMIN
     */
    private static void menuAdmin(Usuario admin) {
        boolean loop = true;
        while (loop) {
            System.out.println("\n=== Menu Admin ===");
            System.out.println("1. Gerenciar Clientes");
            System.out.println("2. Gerenciar Profissionais");
            System.out.println("3. Gerenciar Serviços");
            System.out.println("4. Gerenciar Produtos");
            System.out.println("5. Gerenciar Agendamentos");
            System.out.println("6. Vendas Avulsas");
            System.out.println("7. Relatórios");
            System.out.println("0. Sair");
            System.out.print("Opção: ");
            String op = scanner.nextLine().trim();

            switch (op) {
                case "1" -> menuClientes();
                case "2" -> menuProfissionais();
                case "3" -> menuServicos();
                case "4" -> menuProdutos();
                case "5" -> menuAgendamentos();
                case "6" -> menuVendasAvulsas();
                case "7" -> menuRelatorios();
                case "0" -> loop = false;
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    // ---------------------------------------------------------------
    // MENU PROFISSIONAL
    // ---------------------------------------------------------------

    /**
     * Menu para usuários com perfil PROFISSIONAL.
     * Inclui "Finalizar dia" para fechar todas as vendas em aberto do dia.
     *
     * @param profissionalUsuario usuário logado com perfil PROFISSIONAL
     */
    private static void menuProfissional(Usuario profissionalUsuario) {
        boolean loop = true;
        while (loop) {
            System.out.println("\n=== Menu Profissional ===");
            System.out.println("1. Ver agendamentos");
            System.out.println("2. Concluir agendamento");
            System.out.println("3. Cancelar agendamento");
            System.out.println("4. Registrar venda avulsa");
            System.out.println("5. Ver estoque");
            System.out.println("6. Finalizar dia");           // NOVO
            System.out.println("7. Relatório de vendas hoje"); // NOVO
            System.out.println("0. Sair");
            System.out.print("Opção: ");
            String op = scanner.nextLine().trim();

            switch (op) {
                case "1" -> listarAgendamentos();
                case "2" -> concluirAgendamento();
                case "3" -> cancelarAgendamento();
                case "4" -> registrarVendaAvulsa();
                case "5" -> relatorioService.gerarRelatorioEstoque();
                case "6" -> fecharDiaProfissional(profissionalUsuario);  // NOVO
                case "7" -> vendaAvulsaService.relatorioVendasDoDia();   // NOVO
                case "0" -> loop = false;
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    // ---------------------------------------------------------------
    // MENU CLIENTE
    // ---------------------------------------------------------------

    /**
     * Menu para usuários com perfil CLIENTE.
     * Permite consultar os próprios agendamentos e ver os serviços disponíveis.
     *
     * @param cliente usuário logado com perfil CLIENTE
     */
    private static void menuCliente(Usuario cliente) {
        boolean loop = true;
        while (loop) {
            System.out.println("\n=== Menu Cliente ===");
            System.out.println("1. Ver meus agendamentos");
            System.out.println("2. Ver serviços disponíveis");
            System.out.println("0. Sair");
            System.out.print("Opção: ");
            String op = scanner.nextLine().trim();

            switch (op) {
                case "1" -> listarAgendamentos();
                case "2" -> servicoService.listarTodos().forEach(s ->
                        System.out.printf(" [%d] %s — R$ %.2f (%dmin)%n",
                                s.getId(), s.getDescricao(), s.getValorBase(), s.getDuracaoMinutos()));
                case "0" -> loop = false;
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    // ---------------------------------------------------------------
    // SUBMENU: RELATÓRIOS
    // ---------------------------------------------------------------

    /**
     * Submenu de relatórios. A opção "Vendas do dia" consulta diretamente {@link VendaAvulsa}
     * pela data atual, corrigindo o problema de relatórios zerados quando transações não eram criadas.
     */
    private static void menuRelatorios() {
        boolean loop = true;
        while (loop) {
            System.out.println("\n-- Relatórios --");
            System.out.println("1. Agendamentos por período");
            System.out.println("2. Relatório financeiro por período");
            System.out.println("3. Estoque");
            System.out.println("4. Saldo geral do caixa");
            System.out.println("5. Vendas do dia (hoje)");
            System.out.println("6. Ranking de serviços por período");
            System.out.println("0. Voltar");
            System.out.print("Opção: ");
            String op = scanner.nextLine().trim();

            switch (op) {
                case "1" -> agendamentosPorPeriodo();
                case "2" -> relatorioFinanceiroPorPeriodo();
                case "3" -> relatorioService.gerarRelatorioEstoque();
                case "4" -> relatorioService.imprimirSaldo();
                case "5" -> vendaAvulsaService.relatorioVendasDoDia();
                case "6" -> rankingServicos();
                case "0" -> loop = false;
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    /** Solicita período e exibe o ranking de serviços mais realizados (RF06). */
    private static void rankingServicos() {
        try {
            System.out.print("Início (dd/MM/yyyy): ");
            LocalDate inicio = LocalDate.parse(scanner.nextLine().trim(), FMT_DATA);
            System.out.print("Fim    (dd/MM/yyyy): ");
            LocalDate fim = LocalDate.parse(scanner.nextLine().trim(), FMT_DATA);
            relatorioService.gerarRankingServicos(inicio, fim);
        } catch (DateTimeParseException e) {
            System.out.println("Formato inválido. Use: dd/MM/yyyy");
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    // ---------------------------------------------------------------
    // FECHAMENTO DE DIA — NOVO MÉTODO
    // ---------------------------------------------------------------

    /**
     * Recupera o Profissional a partir do usuário logado e delega o fechamento
     * ao VendaAvulsaService. Exibe erro se o usuário não for encontrado como profissional.
     *
     * @param usuario usuário logado com perfil PROFISSIONAL
     */
    private static void fecharDiaProfissional(Usuario usuario) {
        try {
            // Recupera o profissional pelo ID do usuário logado
            List<Usuario> profs = usuarioService.listarPorTipo(TipoUsuario.PROFISSIONAL);
            Optional<Usuario> opProf = profs.stream()
                    .filter(u -> u.getId().equals(usuario.getId()))
                    .findFirst();

            if (opProf.isEmpty()) {
                System.out.println("Profissional não encontrado.");
                return;
            }

            if (!(opProf.get() instanceof Profissional profissional)) {
                System.out.println("Usuário não é um profissional válido.");
                return;
            }

            System.out.println("\n=== Finalizar Dia ===");
            System.out.println("Isso irá fechar todas as vendas de hoje. Confirma? (s/n): ");
            String confirmacao = scanner.nextLine().trim();
            if (!confirmacao.equalsIgnoreCase("s")) {
                System.out.println("Operação cancelada.");
                return;
            }

            vendaAvulsaService.fecharDia(profissional);

        } catch (Exception e) {
            System.out.println("Erro ao fechar dia: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // SUBMENUS: CLIENTES, PROFISSIONAIS, SERVIÇOS, PRODUTOS
    // ---------------------------------------------------------------

    /**
     * Submenu de gerenciamento de clientes (listar, cadastrar, atualizar, remover).
     */
    private static void menuClientes() {
        boolean loop = true;
        while (loop) {
            System.out.println("\n-- Clientes --");
            System.out.println("1. Listar");
            System.out.println("2. Cadastrar");
            System.out.println("3. Atualizar");
            System.out.println("4. Remover");
            System.out.println("0. Voltar");
            System.out.print("Opção: ");
            String op = scanner.nextLine().trim();
            switch (op) {
                case "1" -> listarClientes();
                case "2" -> cadastrarCliente();
                case "3" -> atualizarCliente();
                case "4" -> removerCliente();
                case "0" -> loop = false;
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    /** Exibe no terminal todos os clientes cadastrados com CPF, tipo e atendimentos do mês. */
    private static void listarClientes() {
        List<Cliente> clientes = clienteService.listarTodos();
        if (clientes.isEmpty()) { System.out.println("Nenhum cliente."); return; }
        clientes.forEach(c -> System.out.printf(
                " [%d] %s | CPF: %s | Tipo: %s | Atend/mês: %d%n",
                c.getId(), c.getNome(), c.getCpf(),
                c.getTipoCliente(), c.getTotalAtendimentosMes()));
    }

    /** Coleta os dados do novo cliente via terminal e delega o cadastro ao {@link ClienteService}. */
    private static void cadastrarCliente() {
        try {
            Cliente c = new Cliente();
            System.out.print("Nome: ");       c.setNome(scanner.nextLine().trim());
            System.out.print("Email: ");      c.setEmail(scanner.nextLine().trim());
            System.out.print("Login: ");      c.setLogin(scanner.nextLine().trim());
            System.out.print("Senha: ");      c.setSenha(scanner.nextLine().trim());
            System.out.print("CPF: ");        c.setCpf(scanner.nextLine().trim());
            System.out.print("Telefone: ");   c.setTelefone(scanner.nextLine().trim());
            c.setTipo(TipoUsuario.CLIENTE);
            clienteService.cadastrarCliente(c);
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /** Busca o cliente pelo ID e permite alterar nome e telefone. */
    private static void atualizarCliente() {
        try {
            System.out.print("ID do cliente: ");
            Long id = Long.parseLong(scanner.nextLine().trim());
            Optional<Cliente> op = clienteService.buscarPorId(id);
            if (op.isEmpty()) { System.out.println("Não encontrado."); return; }
            Cliente c = op.get();
            System.out.print("Novo nome (" + c.getNome() + "): ");
            String nome = scanner.nextLine().trim();
            if (!nome.isBlank()) c.setNome(nome);
            System.out.print("Novo telefone (" + c.getTelefone() + "): ");
            String tel = scanner.nextLine().trim();
            if (!tel.isBlank()) c.setTelefone(tel);
            clienteService.atualizarCliente(c);
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /** Solicita o ID e remove o cliente pelo {@link ClienteService}. */
    private static void removerCliente() {
        try {
            System.out.print("ID: ");
            clienteService.removerCliente(Long.parseLong(scanner.nextLine().trim()));
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /** Submenu de gerenciamento de profissionais (listar, cadastrar, remover com soft delete). */
    private static void menuProfissionais() {
        boolean loop = true;
        while (loop) {
            System.out.println("\n-- Profissionais --");
            System.out.println("1. Listar");
            System.out.println("2. Cadastrar");
            System.out.println("3. Remover (exclusão lógica)");
            System.out.println("0. Voltar");
            System.out.print("Opção: ");
            String op = scanner.nextLine().trim();
            switch (op) {
                case "1" -> listarProfissionais();
                case "2" -> cadastrarProfissional();
                case "3" -> removerUsuario();
                case "0" -> loop = false;
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    /** Exibe no terminal todos os usuários com perfil PROFISSIONAL. */
    private static void listarProfissionais() {
        List<Usuario> profs = usuarioService.listarPorTipo(TipoUsuario.PROFISSIONAL);
        if (profs.isEmpty()) { System.out.println("Nenhum profissional."); return; }
        profs.forEach(p -> System.out.printf(" [%d] %s | Login: %s%n",
                p.getId(), p.getNome(), p.getLogin()));
    }

    /** Coleta os dados do novo profissional via terminal e delega o cadastro ao {@link UsuarioService}. */
    private static void cadastrarProfissional() {
        try {
            Profissional p = new Profissional();
            System.out.print("Nome: ");          p.setNome(scanner.nextLine().trim());
            System.out.print("Email: ");         p.setEmail(scanner.nextLine().trim());
            System.out.print("Login: ");         p.setLogin(scanner.nextLine().trim());
            System.out.print("Senha: ");         p.setSenha(scanner.nextLine().trim());
            System.out.print("Especialidade: "); p.setEspecialidade(scanner.nextLine().trim());
            p.setTipo(TipoUsuario.PROFISSIONAL);
            usuarioService.cadastrarUsuario(p);
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /** Solicita o ID e realiza a exclusão lógica (soft delete) do usuário. */
    private static void removerUsuario() {
        try {
            System.out.print("ID do usuário: ");
            usuarioService.removerUsuario(Long.parseLong(scanner.nextLine().trim()));
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /** Submenu de gerenciamento de serviços (listar, cadastrar, atualizar, remover). */
    private static void menuServicos() {
        boolean loop = true;
        while (loop) {
            System.out.println("\n-- Serviços --");
            System.out.println("1. Listar");
            System.out.println("2. Cadastrar");
            System.out.println("3. Atualizar");
            System.out.println("4. Remover");
            System.out.println("0. Voltar");
            System.out.print("Opção: ");
            String op = scanner.nextLine().trim();
            switch (op) {
                case "1" -> listarServicos();
                case "2" -> cadastrarServico();
                case "3" -> atualizarServico();
                case "4" -> removerServico();
                case "0" -> loop = false;
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    /** Exibe no terminal todos os serviços com descrição, valor base e duração. */
    private static void listarServicos() {
        List<Servico> servicos = servicoService.listarTodos();
        if (servicos.isEmpty()) { System.out.println("Nenhum serviço."); return; }
        servicos.forEach(s -> System.out.printf(" [%d] %s | R$ %.2f | %d min%n",
                s.getId(), s.getDescricao(), s.getValorBase(), s.getDuracaoMinutos()));
    }

    /** Coleta descrição, valor base e duração e cadastra o serviço via {@link ServicoService}. */
    private static void cadastrarServico() {
        try {
            Servico s = new Servico();
            System.out.print("Descrição: ");        s.setDescricao(scanner.nextLine().trim());
            System.out.print("Valor base: ");       s.setValorBase(new BigDecimal(scanner.nextLine().trim()));
            System.out.print("Duração (min): ");    s.setDuracaoMinutos(Integer.parseInt(scanner.nextLine().trim()));
            servicoService.cadastrarServico(s);
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /** Busca o serviço pelo ID e permite alterar o valor base. */
    private static void atualizarServico() {
        try {
            System.out.print("ID: ");
            Long id = Long.parseLong(scanner.nextLine().trim());
            Optional<Servico> op = servicoService.buscarPorId(id);
            if (op.isEmpty()) { System.out.println("Não encontrado."); return; }
            Servico s = op.get();
            System.out.print("Novo valor (" + s.getValorBase() + "): ");
            String val = scanner.nextLine().trim();
            if (!val.isBlank()) s.setValorBase(new BigDecimal(val));
            servicoService.atualizarServico(s);
            System.out.println("Atualizado.");
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /** Solicita o ID e remove o serviço pelo {@link ServicoService}. */
    private static void removerServico() {
        try {
            System.out.print("ID: ");
            servicoService.removerServico(Long.parseLong(scanner.nextLine().trim()));
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /** Submenu de gerenciamento de produtos do estoque (listar, cadastrar, atualizar, remover, alertas). */
    private static void menuProdutos() {
        boolean loop = true;
        while (loop) {
            System.out.println("\n-- Produtos --");
            System.out.println("1. Listar estoque");
            System.out.println("2. Cadastrar");
            System.out.println("3. Atualizar");
            System.out.println("4. Remover");
            System.out.println("5. Estoque baixo");
            System.out.println("0. Voltar");
            System.out.print("Opção: ");
            String op = scanner.nextLine().trim();
            switch (op) {
                case "1" -> produtoService.listarTodos().forEach(p -> System.out.printf(
                        " [%d] %s | qtd: %d | mín: %d | R$ %.2f%n",
                        p.getId(), p.getNome(), p.getQuantidadeEstoque(),
                        p.getQuantidadeMinima(), p.getPrecoVenda()));
                case "2" -> cadastrarProduto();
                case "3" -> atualizarProduto();
                case "4" -> removerProduto();
                case "5" -> produtoService.verificarEstoqueBaixo();
                case "0" -> loop = false;
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    /** Coleta nome, preços, estoque atual e mínimo e cadastra o produto via {@link ProdutoService}. */
    private static void cadastrarProduto() {
        try {
            Produto p = new Produto();
            System.out.print("Nome: ");             p.setNome(scanner.nextLine().trim());
            System.out.print("Preço custo: ");      p.setPrecoCusto(new BigDecimal(scanner.nextLine().trim()));
            System.out.print("Preço venda: ");      p.setPrecoVenda(new BigDecimal(scanner.nextLine().trim()));
            System.out.print("Qtd estoque: ");      p.setQuantidadeEstoque(Integer.parseInt(scanner.nextLine().trim()));
            System.out.print("Qtd mínima: ");       p.setQuantidadeMinima(Integer.parseInt(scanner.nextLine().trim()));
            produtoService.cadastrarProduto(p);
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /** Busca o produto pelo ID e permite alterar preço de venda e quantidade em estoque. */
    private static void atualizarProduto() {
        try {
            System.out.print("ID: ");
            Long id = Long.parseLong(scanner.nextLine().trim());
            Optional<Produto> op = produtoService.buscarPorId(id);
            if (op.isEmpty()) { System.out.println("Não encontrado."); return; }
            Produto p = op.get();
            System.out.print("Novo preço venda (" + p.getPrecoVenda() + "): ");
            String val = scanner.nextLine().trim();
            if (!val.isBlank()) p.setPrecoVenda(new BigDecimal(val));
            System.out.print("Novo estoque (" + p.getQuantidadeEstoque() + "): ");
            String est = scanner.nextLine().trim();
            if (!est.isBlank()) p.setQuantidadeEstoque(Integer.parseInt(est));
            produtoService.atualizarProduto(p);
            System.out.println("Atualizado.");
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /** Solicita o ID e remove o produto pelo {@link ProdutoService}. */
    private static void removerProduto() {
        try {
            System.out.print("ID: ");
            produtoService.removerProduto(Long.parseLong(scanner.nextLine().trim()));
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    // ---------------------------------------------------------------
    // AGENDAMENTOS
    // ---------------------------------------------------------------

    /** Submenu de gerenciamento de agendamentos (listar, criar, concluir, cancelar, remover, filtrar). */
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

    /** Exibe no terminal todos os agendamentos com status, horário, cliente e valor. */
    private static void listarAgendamentos() {
        List<Agendamento> lista = agendamentoService.listarTodos();
        if (lista.isEmpty()) { System.out.println("Nenhum agendamento."); return; }
        lista.forEach(a -> System.out.printf(
                " [%d] %s | %s → %s | %s | R$ %.2f%n",
                a.getId(), a.getStatus(),
                a.getDataHoraInicio().format(FMT_DATA_HORA),
                a.getDataHoraFim().format(FMT_DATA_HORA),
                a.getCliente().getNome(), a.getValorFinal()));
    }

    /**
     * Coleta cliente, profissional, horário, serviços e cria o agendamento.
     * Valida o formato de data/hora e verifica estoque antes de confirmar.
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
            Optional<Usuario> opP = profs.stream()
                    .filter(u -> u.getId().equals(profId)).findFirst();
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

            // seleção de serviços para o agendamento
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
        } catch (DateTimeParseException e) {
            System.out.println("Formato inválido. Use: dd/MM/yyyy HH:mm");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    /** Solicita o ID e marca o agendamento como CONCLUIDO via {@link AgendamentoService}. */
    private static void concluirAgendamento() {
        try {
            System.out.print("ID: ");
            agendamentoService.concluirAgendamento(Long.parseLong(scanner.nextLine().trim()));
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /** Solicita o ID e marca o agendamento como CANCELADO via {@link AgendamentoService}. */
    private static void cancelarAgendamento() {
        try {
            System.out.print("ID: ");
            agendamentoService.cancelarAgendamento(Long.parseLong(scanner.nextLine().trim()));
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /** Solicita o ID e remove o agendamento (apenas se não estiver concluído). */
    private static void removerAgendamento() {
        try {
            System.out.print("ID: ");
            agendamentoService.removerAgendamento(Long.parseLong(scanner.nextLine().trim()));
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /** Solicita datas de início e fim e exibe o relatório de agendamentos do período. */
    private static void agendamentosPorPeriodo() {
        try {
            System.out.print("Início (dd/MM/yyyy): ");
            LocalDate inicio = LocalDate.parse(scanner.nextLine().trim(), FMT_DATA);
            System.out.print("Fim    (dd/MM/yyyy): ");
            LocalDate fim = LocalDate.parse(scanner.nextLine().trim(), FMT_DATA);
            relatorioService.gerarRelatorioAgendamentos(inicio, fim);
        } catch (DateTimeParseException e) {
            System.out.println("Formato inválido. Use: dd/MM/yyyy");
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    // ---------------------------------------------------------------
    // VENDAS AVULSAS
    // ---------------------------------------------------------------

    /** Submenu de vendas avulsas de produtos (listar, registrar, remover). */
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
     * Coleta produto, profissional e quantidade, e registra a venda avulsa.
     * Lista produtos e profissionais disponíveis antes de solicitar os IDs.
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
            Optional<Usuario> opU = profs.stream()
                    .filter(u -> u.getId().equals(profId)).findFirst();
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

    /** Solicita datas de início e fim e exibe o relatório financeiro do período com totais e saldo. */
    private static void relatorioFinanceiroPorPeriodo() {
        try {
            System.out.print("Início (dd/MM/yyyy): ");
            LocalDate inicio = LocalDate.parse(scanner.nextLine().trim(), FMT_DATA);
            System.out.print("Fim    (dd/MM/yyyy): ");
            LocalDate fim = LocalDate.parse(scanner.nextLine().trim(), FMT_DATA);
            relatorioService.gerarRelatorioFinanceiro(inicio, fim);
        } catch (DateTimeParseException e) {
            System.out.println("Formato inválido. Use: dd/MM/yyyy");
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }
}