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

/**
 * Classe principal da aplicação Cuticulados.
 *
 * <p>Ponto de entrada do sistema. Inicializa o Flyway (migrações do banco)
 * e o JPA (conexão via EntityManagerFactory), em seguida exibe o menu
 * de autenticação e direciona o usuário para o menu correspondente
 * ao seu perfil ({@link TipoUsuario}).</p>
 *
 * <p>A estrutura de menus segue o padrão ensinado em aula:
 * a Main recebe as entradas do usuário e delega o processamento
 * para os Services, que aplicam as regras de negócio.</p>
 */
public class Main {

    // ---------------------------------------------------------------
    // Services — responsáveis pelas regras de negócio
    // ---------------------------------------------------------------

    /** Serviço de agendamentos (criação, conclusão, cancelamento). */
    private static final AgendamentoService agendamentoService = new AgendamentoService();

    /** Serviço de clientes (cadastro, fidelidade). */
    private static final ClienteService clienteService = new ClienteService();

    /** Serviço de profissionais via usuário. */
    private static final UsuarioService usuarioService = new UsuarioService();

    /** Serviço de produtos e estoque. */
    private static final ProdutoService produtoService = new ProdutoService();

    /** Serviço de serviços do salão. */
    private static final ServicoService servicoService = new ServicoService();

    /** Serviço de vendas avulsas. */
    private static final VendaAvulsaService vendaAvulsaService = new VendaAvulsaService();

    /** Serviço de relatórios financeiros e de estoque. */
    private static final RelatorioService relatorioService = new RelatorioService();

    /** Scanner compartilhado para leitura de entradas do usuário. */
    private static final Scanner scanner = new Scanner(System.in);

    /** Formato de data usado na leitura de datas do usuário (dd/MM/yyyy). */
    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Formato de data/hora usado na leitura de agendamentos (dd/MM/yyyy HH:mm). */
    private static final DateTimeFormatter FMT_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Método principal: inicializa infraestrutura e inicia o fluxo de autenticação.
     *
     * <p>Ordem de inicialização obrigatória:
     * <ol>
     *   <li>Flyway executa as migrations SQL (cria/atualiza tabelas)</li>
     *   <li>JPA é inicializado (cria a fábrica de EntityManagers)</li>
     *   <li>Menu de login é exibido ao usuário</li>
     * </ol>
     * </p>
     *
     * @param args argumentos de linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        try {
            System.out.println("=== Cuticulados — Sistema de Gestão ===");

            // Passo 1: executa as migrations antes de inicializar o JPA
            FlywayConfig.executarMigracoes();

            // Passo 2: inicializa a fábrica de EntityManagers
            JpaUtil.inicializar();

            // Passo 3: loop principal de autenticação
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
     * Exibe o menu de login e direciona o usuário ao menu correto após autenticação.
     *
     * @return {@code false} para encerrar o sistema, {@code true} para continuar
     */
    private static boolean menuLogin() {
        System.out.println("\n--- Login ---");
        System.out.print("Login: ");
        String login = scanner.nextLine().trim();
        System.out.print("Senha: ");
        String senha = scanner.nextLine().trim();

        Optional<Usuario> op = usuarioService.autenticar(login, senha);
        if (op.isEmpty()) {
            System.out.println("Autenticação falhou. Tente novamente ou digite 'sair' para encerrar.");
            System.out.print("Continuar? (s/n): ");
            String resp = scanner.nextLine().trim();
            return resp.equalsIgnoreCase("s");
        }

        Usuario usuario = op.get();
        System.out.println("Bem-vindo(a), " + usuario.getNome() + "! [" + usuario.getTipo() + "]");

        // Direciona para o menu de acordo com o perfil do usuário
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
     * Menu completo para usuários com perfil ADMIN.
     * Dá acesso a todas as funcionalidades do sistema.
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
     *
     * @param profissional usuário logado com perfil PROFISSIONAL
     */
    private static void menuProfissional(Usuario profissional) {
        boolean loop = true;
        while (loop) {
            System.out.println("\n=== Menu Profissional ===");
            System.out.println("1. Ver meus agendamentos");
            System.out.println("2. Concluir agendamento");
            System.out.println("3. Cancelar agendamento");
            System.out.println("4. Registrar venda avulsa");
            System.out.println("5. Ver estoque");
            System.out.println("0. Sair");
            System.out.print("Opção: ");
            String op = scanner.nextLine().trim();

            switch (op) {
                case "1" -> listarAgendamentos();
                case "2" -> concluirAgendamento();
                case "3" -> cancelarAgendamento();
                case "4" -> registrarVendaAvulsa();
                case "5" -> relatorioService.gerarRelatorioEstoque();
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
    // SUBMENU: CLIENTES
    // ---------------------------------------------------------------

    /**
     * Submenu de gerenciamento de clientes (CRUD).
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

    /** Lista todos os clientes no terminal. */
    private static void listarClientes() {
        List<Cliente> clientes = clienteService.listarTodos();
        if (clientes.isEmpty()) {
            System.out.println("Nenhum cliente cadastrado.");
            return;
        }
        System.out.println("=== Clientes ===");
        clientes.forEach(c -> System.out.printf(
                " [%d] %s | CPF: %s | Tel: %s | Tipo: %s | Atend/mês: %d%n",
                c.getId(), c.getNome(), c.getCpf(), c.getTelefone(),
                c.getTipoCliente(), c.getTotalAtendimentosMes()));
    }

    /** Solicita dados e cadastra um novo cliente. */
    private static void cadastrarCliente() {
        try {
            System.out.println("-- Cadastrar Cliente --");
            Cliente c = new Cliente();
            System.out.print("Nome: ");
            c.setNome(scanner.nextLine().trim());
            System.out.print("Email: ");
            c.setEmail(scanner.nextLine().trim());
            System.out.print("Login: ");
            c.setLogin(scanner.nextLine().trim());
            System.out.print("Senha: ");
            c.setSenha(scanner.nextLine().trim());
            System.out.print("CPF: ");
            c.setCpf(scanner.nextLine().trim());
            System.out.print("Telefone: ");
            c.setTelefone(scanner.nextLine().trim());
            c.setTipo(TipoUsuario.CLIENTE);
            clienteService.cadastrarCliente(c);
        } catch (Exception e) {
            System.out.println("Erro ao cadastrar cliente: " + e.getMessage());
        }
    }

    /** Solicita ID e novos dados para atualizar um cliente existente. */
    private static void atualizarCliente() {
        try {
            System.out.print("ID do cliente: ");
            Long id = Long.parseLong(scanner.nextLine().trim());
            Optional<Cliente> op = clienteService.buscarPorId(id);
            if (op.isEmpty()) {
                System.out.println("Cliente não encontrado.");
                return;
            }
            Cliente c = op.get();
            System.out.print("Novo nome (" + c.getNome() + "): ");
            String nome = scanner.nextLine().trim();
            if (!nome.isBlank()) c.setNome(nome);
            System.out.print("Novo telefone (" + c.getTelefone() + "): ");
            String tel = scanner.nextLine().trim();
            if (!tel.isBlank()) c.setTelefone(tel);
            clienteService.atualizarCliente(c);
        } catch (Exception e) {
            System.out.println("Erro ao atualizar cliente: " + e.getMessage());
        }
    }

    /** Solicita ID e remove um cliente. */
    private static void removerCliente() {
        try {
            System.out.print("ID do cliente: ");
            Long id = Long.parseLong(scanner.nextLine().trim());
            clienteService.removerCliente(id);
        } catch (Exception e) {
            System.out.println("Erro ao remover cliente: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // SUBMENU: PROFISSIONAIS
    // ---------------------------------------------------------------

    /**
     * Submenu de gerenciamento de profissionais (CRUD via UsuarioService).
     */
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

    /** Lista todos os profissionais ativos no terminal. */
    private static void listarProfissionais() {
        List<Usuario> profissionais = usuarioService.listarPorTipo(TipoUsuario.PROFISSIONAL);
        if (profissionais.isEmpty()) {
            System.out.println("Nenhum profissional cadastrado.");
            return;
        }
        System.out.println("=== Profissionais ===");
        profissionais.forEach(p -> System.out.printf(" [%d] %s | Login: %s%n",
                p.getId(), p.getNome(), p.getLogin()));
    }

    /** Solicita dados e cadastra um novo profissional. */
    private static void cadastrarProfissional() {
        try {
            System.out.println("-- Cadastrar Profissional --");
            Profissional p = new Profissional();
            System.out.print("Nome: ");
            p.setNome(scanner.nextLine().trim());
            System.out.print("Email: ");
            p.setEmail(scanner.nextLine().trim());
            System.out.print("Login: ");
            p.setLogin(scanner.nextLine().trim());
            System.out.print("Senha: ");
            p.setSenha(scanner.nextLine().trim());
            System.out.print("Especialidade: ");
            p.setEspecialidade(scanner.nextLine().trim());
            p.setTipo(TipoUsuario.PROFISSIONAL);
            usuarioService.cadastrarUsuario(p);
        } catch (Exception e) {
            System.out.println("Erro ao cadastrar profissional: " + e.getMessage());
        }
    }

    /** Solicita ID e realiza exclusão lógica de um usuário. */
    private static void removerUsuario() {
        try {
            System.out.print("ID do usuário: ");
            Long id = Long.parseLong(scanner.nextLine().trim());
            usuarioService.removerUsuario(id);
        } catch (Exception e) {
            System.out.println("Erro ao remover usuário: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // SUBMENU: SERVIÇOS
    // ---------------------------------------------------------------

    /**
     * Submenu de gerenciamento dos serviços do salão (CRUD).
     */
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

    /** Lista todos os serviços no terminal. */
    private static void listarServicos() {
        List<Servico> servicos = servicoService.listarTodos();
        if (servicos.isEmpty()) {
            System.out.println("Nenhum serviço cadastrado.");
            return;
        }
        System.out.println("=== Serviços ===");
        servicos.forEach(s -> System.out.printf(" [%d] %s | R$ %.2f | %d min%n",
                s.getId(), s.getDescricao(), s.getValorBase(), s.getDuracaoMinutos()));
    }

    /** Solicita dados e cadastra um novo serviço. */
    private static void cadastrarServico() {
        try {
            System.out.println("-- Cadastrar Serviço --");
            Servico s = new Servico();
            System.out.print("Descrição: ");
            s.setDescricao(scanner.nextLine().trim());
            System.out.print("Valor base: ");
            s.setValorBase(Double.parseDouble(scanner.nextLine().trim()));
            System.out.print("Duração (minutos): ");
            s.setDuracaoMinutos(Integer.parseInt(scanner.nextLine().trim()));
            servicoService.cadastrarServico(s);
        } catch (Exception e) {
            System.out.println("Erro ao cadastrar serviço: " + e.getMessage());
        }
    }

    /** Solicita ID e novos dados para atualizar um serviço existente. */
    private static void atualizarServico() {
        try {
            System.out.print("ID do serviço: ");
            Long id = Long.parseLong(scanner.nextLine().trim());
            Optional<Servico> op = servicoService.buscarPorId(id);
            if (op.isEmpty()) {
                System.out.println("Serviço não encontrado.");
                return;
            }
            Servico s = op.get();
            System.out.print("Novo valor base (" + s.getValorBase() + "): ");
            String val = scanner.nextLine().trim();
            if (!val.isBlank()) s.setValorBase(Double.parseDouble(val));
            servicoService.atualizarServico(s);
            System.out.println("Serviço atualizado.");
        } catch (Exception e) {
            System.out.println("Erro ao atualizar serviço: " + e.getMessage());
        }
    }

    /** Solicita ID e remove um serviço. */
    private static void removerServico() {
        try {
            System.out.print("ID do serviço: ");
            Long id = Long.parseLong(scanner.nextLine().trim());
            servicoService.removerServico(id);
        } catch (Exception e) {
            System.out.println("Erro ao remover serviço: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // SUBMENU: PRODUTOS
    // ---------------------------------------------------------------

    /**
     * Submenu de gerenciamento de produtos do estoque (CRUD).
     */
    private static void menuProdutos() {
        boolean loop = true;
        while (loop) {
            System.out.println("\n-- Produtos --");
            System.out.println("1. Listar estoque");
            System.out.println("2. Cadastrar produto");
            System.out.println("3. Atualizar produto");
            System.out.println("4. Remover produto");
            System.out.println("5. Verificar estoque baixo");
            System.out.println("0. Voltar");
            System.out.print("Opção: ");
            String op = scanner.nextLine().trim();

            switch (op) {
                case "1" -> produtoService.listarTodos().forEach(p -> System.out.printf(
                        " [%d] %s | estoque: %d | mín: %d | venda: R$ %.2f%n",
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

    /** Solicita dados e cadastra um novo produto. */
    private static void cadastrarProduto() {
        try {
            System.out.println("-- Cadastrar Produto --");
            Produto p = new Produto();
            System.out.print("Nome: ");
            p.setNome(scanner.nextLine().trim());
            System.out.print("Preço de custo: ");
            p.setPrecoCusto(Double.parseDouble(scanner.nextLine().trim()));
            System.out.print("Preço de venda: ");
            p.setPrecoVenda(Double.parseDouble(scanner.nextLine().trim()));
            System.out.print("Qtd estoque inicial: ");
            p.setQuantidadeEstoque(Integer.parseInt(scanner.nextLine().trim()));
            System.out.print("Qtd mínima: ");
            p.setQuantidadeMinima(Integer.parseInt(scanner.nextLine().trim()));
            produtoService.cadastrarProduto(p);
        } catch (Exception e) {
            System.out.println("Erro ao cadastrar produto: " + e.getMessage());
        }
    }

    /** Solicita ID e novos dados para atualizar um produto existente. */
    private static void atualizarProduto() {
        try {
            System.out.print("ID do produto: ");
            Long id = Long.parseLong(scanner.nextLine().trim());
            Optional<Produto> op = produtoService.buscarPorId(id);
            if (op.isEmpty()) {
                System.out.println("Produto não encontrado.");
                return;
            }
            Produto p = op.get();
            System.out.print("Novo preço de venda (" + p.getPrecoVenda() + "): ");
            String val = scanner.nextLine().trim();
            if (!val.isBlank()) p.setPrecoVenda(Double.parseDouble(val));
            System.out.print("Novo estoque (" + p.getQuantidadeEstoque() + "): ");
            String est = scanner.nextLine().trim();
            if (!est.isBlank()) p.setQuantidadeEstoque(Integer.parseInt(est));
            produtoService.atualizarProduto(p);
            System.out.println("Produto atualizado.");
        } catch (Exception e) {
            System.out.println("Erro ao atualizar produto: " + e.getMessage());
        }
    }

    /** Solicita ID e remove um produto. */
    private static void removerProduto() {
        try {
            System.out.print("ID do produto: ");
            Long id = Long.parseLong(scanner.nextLine().trim());
            produtoService.removerProduto(id);
        } catch (Exception e) {
            System.out.println("Erro ao remover produto: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // SUBMENU: AGENDAMENTOS
    // ---------------------------------------------------------------

    /**
     * Submenu de gerenciamento de agendamentos (CRUD + regras de negócio).
     */
    private static void menuAgendamentos() {
        boolean loop = true;
        while (loop) {
            System.out.println("\n-- Agendamentos --");
            System.out.println("1. Listar todos");
            System.out.println("2. Criar agendamento");
            System.out.println("3. Concluir agendamento");
            System.out.println("4. Cancelar agendamento");
            System.out.println("5. Remover agendamento");
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

    /** Lista todos os agendamentos no terminal. */
    private static void listarAgendamentos() {
        List<Agendamento> lista = agendamentoService.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("Nenhum agendamento encontrado.");
            return;
        }
        System.out.println("=== Agendamentos ===");
        lista.forEach(a -> System.out.printf(
                " [%d] %s | %s → %s | %s | R$ %.2f%n",
                a.getId(),
                a.getStatus(),
                a.getDataHoraInicio().format(FMT_DATA_HORA),
                a.getDataHoraFim().format(FMT_DATA_HORA),
                a.getCliente().getNome(),
                a.getValorFinal()));
    }

    /** Solicita dados e cria um novo agendamento. */
    private static void criarAgendamento() {
        try {
            System.out.println("-- Criar Agendamento --");

            listarClientes();
            System.out.print("ID do cliente: ");
            Long clienteId = Long.parseLong(scanner.nextLine().trim());
            Optional<Cliente> opC = clienteService.buscarPorId(clienteId);
            if (opC.isEmpty()) { System.out.println("Cliente não encontrado."); return; }

            listarProfissionais();
            System.out.print("ID do profissional: ");
            Long profId = Long.parseLong(scanner.nextLine().trim());
            List<Usuario> profs = usuarioService.listarPorTipo(TipoUsuario.PROFISSIONAL);
            Optional<Usuario> opP = profs.stream().filter(u -> u.getId().equals(profId)).findFirst();
            if (opP.isEmpty()) { System.out.println("Profissional não encontrado."); return; }

            System.out.print("Data/hora início (dd/MM/yyyy HH:mm): ");
            LocalDateTime inicio = LocalDateTime.parse(scanner.nextLine().trim(), FMT_DATA_HORA);
            System.out.print("Data/hora fim    (dd/MM/yyyy HH:mm): ");
            LocalDateTime fim = LocalDateTime.parse(scanner.nextLine().trim(), FMT_DATA_HORA);

            Agendamento ag = new Agendamento();
            ag.setCliente(opC.get());
            ag.setProfissional((Profissional) opP.get());
            ag.setDataHoraInicio(inicio);
            ag.setDataHoraFim(fim);

            agendamentoService.criarAgendamento(ag);
        } catch (DateTimeParseException e) {
            System.out.println("Formato de data inválido. Use: dd/MM/yyyy HH:mm");
        } catch (Exception e) {
            System.out.println("Erro ao criar agendamento: " + e.getMessage());
        }
    }

    /** Solicita ID e conclui um agendamento. */
    private static void concluirAgendamento() {
        try {
            System.out.print("ID do agendamento: ");
            Long id = Long.parseLong(scanner.nextLine().trim());
            agendamentoService.concluirAgendamento(id);
        } catch (Exception e) {
            System.out.println("Erro ao concluir agendamento: " + e.getMessage());
        }
    }

    /** Solicita ID e cancela um agendamento. */
    private static void cancelarAgendamento() {
        try {
            System.out.print("ID do agendamento: ");
            Long id = Long.parseLong(scanner.nextLine().trim());
            agendamentoService.cancelarAgendamento(id);
        } catch (Exception e) {
            System.out.println("Erro ao cancelar agendamento: " + e.getMessage());
        }
    }

    /** Solicita ID e remove um agendamento. */
    private static void removerAgendamento() {
        try {
            System.out.print("ID do agendamento: ");
            Long id = Long.parseLong(scanner.nextLine().trim());
            agendamentoService.removerAgendamento(id);
        } catch (Exception e) {
            System.out.println("Erro ao remover agendamento: " + e.getMessage());
        }
    }

    /** Solicita período e lista os agendamentos do intervalo. */
    private static void agendamentosPorPeriodo() {
        try {
            System.out.print("Data início (dd/MM/yyyy): ");
            LocalDate inicio = LocalDate.parse(scanner.nextLine().trim(), FMT_DATA);
            System.out.print("Data fim    (dd/MM/yyyy): ");
            LocalDate fim = LocalDate.parse(scanner.nextLine().trim(), FMT_DATA);
            relatorioService.gerarRelatorioAgendamentos(inicio, fim);
        } catch (DateTimeParseException e) {
            System.out.println("Formato de data inválido. Use: dd/MM/yyyy");
        } catch (Exception e) {
            System.out.println("Erro ao buscar agendamentos: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // SUBMENU: VENDAS AVULSAS
    // ---------------------------------------------------------------

    /**
     * Submenu de vendas avulsas de produtos.
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
                        " [%d] %s | %dx %s | Total: R$ %.2f%n",
                        v.getId(), v.getDataVenda(), v.getQuantidade(),
                        v.getProduto().getNome(), v.getTotal()));
                case "2" -> registrarVendaAvulsa();
                case "3" -> {
                    System.out.print("ID da venda: ");
                    try {
                        Long id = Long.parseLong(scanner.nextLine().trim());
                        vendaAvulsaService.removerVenda(id);
                    } catch (Exception e) {
                        System.out.println("Erro: " + e.getMessage());
                    }
                }
                case "0" -> loop = false;
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    /** Solicita dados e registra uma venda avulsa. */
    private static void registrarVendaAvulsa() {
        try {
            System.out.println("-- Registrar Venda Avulsa --");

            produtoService.listarTodos().forEach(p -> System.out.printf(
                    " [%d] %s | estoque: %d | R$ %.2f%n",
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
        } catch (Exception e) {
            System.out.println("Erro ao registrar venda: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // SUBMENU: RELATÓRIOS
    // ---------------------------------------------------------------

    /**
     * Submenu de relatórios financeiros e operacionais.
     */
    private static void menuRelatorios() {
        boolean loop = true;
        while (loop) {
            System.out.println("\n-- Relatórios --");
            System.out.println("1. Relatório de agendamentos por período");
            System.out.println("2. Relatório financeiro por período");
            System.out.println("3. Relatório de estoque");
            System.out.println("4. Saldo geral do caixa");
            System.out.println("0. Voltar");
            System.out.print("Opção: ");
            String op = scanner.nextLine().trim();

            switch (op) {
                case "1" -> agendamentosPorPeriodo();
                case "2" -> relatorioFinanceiroPorPeriodo();
                case "3" -> relatorioService.gerarRelatorioEstoque();
                case "4" -> relatorioService.imprimirSaldo();
                case "0" -> loop = false;
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    /** Solicita período e exibe o relatório financeiro. */
    private static void relatorioFinanceiroPorPeriodo() {
        try {
            System.out.print("Data início (dd/MM/yyyy): ");
            LocalDate inicio = LocalDate.parse(scanner.nextLine().trim(), FMT_DATA);
            System.out.print("Data fim    (dd/MM/yyyy): ");
            LocalDate fim = LocalDate.parse(scanner.nextLine().trim(), FMT_DATA);
            relatorioService.gerarRelatorioFinanceiro(inicio, fim);
        } catch (DateTimeParseException e) {
            System.out.println("Formato de data inválido. Use: dd/MM/yyyy");
        } catch (Exception e) {
            System.out.println("Erro ao gerar relatório financeiro: " + e.getMessage());
        }
    }
}