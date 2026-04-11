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
 * Ponto de entrada da aplicação Cuticulados.
 *
 * Gerencia toda a interação com o terminal: login, navegação entre menus e
 * coleta de dados. O {@link Scanner} é centralizado aqui — nenhuma outra
 * camada (service, repository) deve ler entrada do usuário diretamente.
 * Usa helpers de leitura ({@link #lerLong}, {@link #lerInt}, {@link #lerDouble},
 * {@link #lerTexto}) que repetem a pergunta até receber um valor válido,
 * evitando que o sistema trave ou feche por erro de digitação.
 */
public class Main {

    // ---------------------------------------------------------------
    // SERVIÇOS E INFRAESTRUTURA
    // ---------------------------------------------------------------

    private static final AgendamentoService agendamentoService  = new AgendamentoService();
    private static final ClienteService     clienteService      = new ClienteService();
    private static final UsuarioService     usuarioService      = new UsuarioService();
    private static final ProdutoService     produtoService      = new ProdutoService();
    private static final ServicoService     servicoService      = new ServicoService();
    private static final VendaAvulsaService vendaAvulsaService  = new VendaAvulsaService();
    private static final RelatorioService   relatorioService    = new RelatorioService();

    /** Único Scanner de toda a aplicação. */
    private static final Scanner scanner = new Scanner(System.in);

    private static final DateTimeFormatter FMT_DATA      = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ---------------------------------------------------------------
    // ENTRADA DO PROGRAMA
    // ---------------------------------------------------------------

    /**
     * Inicializa Flyway e JPA, depois mantém o loop de login rodando
     * até que o usuário escolha sair.
     *
     * @param args argumentos de linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        try {
            cabecalho("CUTICULADOS — SISTEMA DE GESTÃO");
            System.out.println("  Inicializando banco de dados...");
            FlywayConfig.executarMigracoes();
            JpaUtil.inicializar();
            ok("Sistema pronto.");

            boolean rodando = true;
            while (rodando) {
                rodando = telaLogin();
            }
        } catch (Exception e) {
            erro("Falha fatal ao iniciar: " + e.getMessage());
        } finally {
            JpaUtil.fechar();
            scanner.close();
        }
    }

    // ---------------------------------------------------------------
    // TELA DE LOGIN
    // ---------------------------------------------------------------

    /**
     * Exibe o prompt de login. Após autenticação bem-sucedida, redireciona
     * para o menu correspondente ao perfil do usuário.
     *
     * @return {@code true} para continuar o loop principal, {@code false} para encerrar
     */
    private static boolean telaLogin() {
        cabecalho("LOGIN");
        System.out.print("  Usuário : ");
        String login = scanner.nextLine().trim();
        System.out.print("  Senha   : ");
        String senha = scanner.nextLine().trim();

        Optional<Usuario> op = usuarioService.autenticar(login, senha);
        if (op.isEmpty()) {
            return confirmar("  Tentar novamente?");
        }

        Usuario usuario = op.get();
        ok("Bem-vindo(a), " + usuario.getNome() + "! [" + usuario.getTipo() + "]");

        switch (usuario.getTipo()) {
            case ADMIN        -> menuAdmin(usuario);
            case PROFISSIONAL -> menuProfissional(usuario);
            case CLIENTE      -> menuCliente(usuario);
        }
        return true;
    }

    // ---------------------------------------------------------------
    // MENU ADMINISTRADOR
    // ---------------------------------------------------------------

    /**
     * Menu principal do perfil ADMIN. Acesso completo a todos os módulos:
     * clientes, profissionais, serviços, estoque, agendamentos e relatórios.
     *
     * @param admin usuário logado com perfil ADMIN
     */
    private static void menuAdmin(Usuario admin) {
        boolean loop = true;
        while (loop) {
            cabecalho("MENU ADMINISTRADOR — " + admin.getNome());
            System.out.println("  1. Gerenciar Clientes");
            System.out.println("  2. Gerenciar Profissionais");
            System.out.println("  3. Gerenciar Serviços");
            System.out.println("  4. Gerenciar Estoque (Produtos)");
            System.out.println("  5. Gerenciar Agendamentos");
            System.out.println("  6. Vendas Avulsas");
            System.out.println("  7. Relatórios");
            System.out.println("  0. Sair");
            separador();

            switch (lerOpcao()) {
                case "1" -> menuClientes();
                case "2" -> menuProfissionais();
                case "3" -> menuServicos();
                case "4" -> menuProdutos();
                case "5" -> menuAgendamentos();
                case "6" -> menuVendasAvulsas();
                case "7" -> menuRelatorios();
                case "0" -> { ok("Sessão encerrada."); loop = false; }
                default  -> aviso("Opção inválida. Digite um número do menu.");
            }
        }
    }

    // ---------------------------------------------------------------
    // MENU PROFISSIONAL (ATENDENTE)
    // ---------------------------------------------------------------

    /**
     * Menu do perfil PROFISSIONAL. Focado em atendimento: agendamentos,
     * vendas avulsas, estoque e fechamento de dia.
     *
     * @param profissionalUsuario usuário logado com perfil PROFISSIONAL
     */
    private static void menuProfissional(Usuario profissionalUsuario) {
        boolean loop = true;
        while (loop) {
            cabecalho("MENU PROFISSIONAL — " + profissionalUsuario.getNome());
            System.out.println("  1. Ver agendamentos");
            System.out.println("  2. Concluir agendamento");
            System.out.println("  3. Cancelar agendamento");
            System.out.println("  4. Registrar venda avulsa");
            System.out.println("  5. Consultar estoque");
            System.out.println("  6. Finalizar dia");
            System.out.println("  7. Relatório de vendas do dia");
            System.out.println("  0. Sair");
            separador();

            switch (lerOpcao()) {
                case "1" -> listarAgendamentos();
                case "2" -> concluirAgendamento();
                case "3" -> cancelarAgendamento();
                case "4" -> registrarVendaAvulsa();
                case "5" -> relatorioService.gerarRelatorioEstoque();
                case "6" -> fecharDiaProfissional(profissionalUsuario);
                case "7" -> vendaAvulsaService.relatorioVendasDoDia();
                case "0" -> { ok("Sessão encerrada."); loop = false; }
                default  -> aviso("Opção inválida. Digite um número do menu.");
            }
        }
    }

    // ---------------------------------------------------------------
    // MENU CLIENTE
    // ---------------------------------------------------------------

    /**
     * Menu do perfil CLIENTE. Permite consultar os próprios agendamentos
     * e visualizar os serviços disponíveis.
     *
     * @param cliente usuário logado com perfil CLIENTE
     */
    private static void menuCliente(Usuario cliente) {
        boolean loop = true;
        while (loop) {
            cabecalho("MENU CLIENTE — " + cliente.getNome());
            System.out.println("  1. Meus agendamentos");
            System.out.println("  2. Serviços disponíveis");
            System.out.println("  0. Sair");
            separador();

            switch (lerOpcao()) {
                case "1" -> listarAgendamentos();
                case "2" -> listarServicosDisponiveis();
                case "0" -> { ok("Sessão encerrada."); loop = false; }
                default  -> aviso("Opção inválida. Digite um número do menu.");
            }
        }
    }

    // ---------------------------------------------------------------
    // MÓDULO: RELATÓRIOS
    // ---------------------------------------------------------------

    /** Submenu de relatórios gerenciais. */
    private static void menuRelatorios() {
        boolean loop = true;
        while (loop) {
            cabecalho("RELATÓRIOS");
            System.out.println("  1. Agendamentos por período");
            System.out.println("  2. Financeiro por período");
            System.out.println("  3. Estoque atual");
            System.out.println("  4. Saldo geral do caixa");
            System.out.println("  5. Vendas do dia (hoje)");
            System.out.println("  0. Voltar");
            separador();

            switch (lerOpcao()) {
                case "1" -> agendamentosPorPeriodo();
                case "2" -> relatorioFinanceiroPorPeriodo();
                case "3" -> relatorioService.gerarRelatorioEstoque();
                case "4" -> relatorioService.imprimirSaldo();
                case "5" -> vendaAvulsaService.relatorioVendasDoDia();
                case "0" -> loop = false;
                default  -> aviso("Opção inválida.");
            }
        }
    }

    // ---------------------------------------------------------------
    // MÓDULO: CLIENTES
    // ---------------------------------------------------------------

    /** Submenu de gerenciamento de clientes. */
    private static void menuClientes() {
        boolean loop = true;
        while (loop) {
            cabecalho("GESTÃO DE CLIENTES");
            System.out.println("  1. Listar clientes");
            System.out.println("  2. Cadastrar cliente");
            System.out.println("  3. Atualizar cliente");
            System.out.println("  4. Remover cliente");
            System.out.println("  0. Voltar");
            separador();

            switch (lerOpcao()) {
                case "1" -> listarClientes();
                case "2" -> cadastrarCliente();
                case "3" -> atualizarCliente();
                case "4" -> removerCliente();
                case "0" -> loop = false;
                default  -> aviso("Opção inválida.");
            }
        }
    }

    /** Exibe todos os clientes cadastrados com CPF, tipo e atendimentos do mês. */
    private static void listarClientes() {
        cabecalho("LISTA DE CLIENTES");
        List<Cliente> clientes = clienteService.listarTodos();
        if (clientes.isEmpty()) {
            aviso("Nenhum cliente cadastrado.");
            return;
        }
        System.out.printf("  %-4s %-25s %-14s %-10s %s%n", "ID", "Nome", "CPF", "Tipo", "Atend/mês");
        separador();
        clientes.forEach(c -> System.out.printf("  %-4d %-25s %-14s %-10s %d%n",
                c.getId(), c.getNome(), c.getCpf(),
                c.getTipoCliente(), c.getTotalAtendimentosMes()));
    }

    /** Coleta dados do novo cliente via terminal e delega o cadastro ao {@link ClienteService}. */
    private static void cadastrarCliente() {
        cabecalho("CADASTRAR CLIENTE");
        try {
            Cliente c = (Cliente) UsuarioFactory.criar(TipoUsuario.CLIENTE);
            c.setNome(lerTexto("  Nome      : "));
            c.setEmail(lerTexto("  E-mail    : "));
            c.setLogin(lerTexto("  Login     : "));
            c.setSenha(lerTexto("  Senha     : "));
            c.setCpf(lerTexto("  CPF       : "));
            c.setTelefone(lerTexto("  Telefone  : "));
            c.setTipo(TipoUsuario.CLIENTE);
            clienteService.cadastrarCliente(c);
            ok("Cliente \"" + c.getNome() + "\" cadastrado com sucesso!");
        } catch (Exception e) {
            erro("Não foi possível cadastrar o cliente: " + e.getMessage());
        }
    }

    /** Busca o cliente pelo ID e permite alterar nome e telefone. */
    private static void atualizarCliente() {
        cabecalho("ATUALIZAR CLIENTE");
        listarClientes();
        Long id = lerLong("  ID do cliente : ");
        Optional<Cliente> op = clienteService.buscarPorId(id);
        if (op.isEmpty()) {
            aviso("Cliente ID " + id + " não encontrado.");
            return;
        }
        Cliente c = op.get();

        System.out.print("  Novo nome [" + c.getNome() + "] (Enter para manter): ");
        String nome = scanner.nextLine().trim();
        if (!nome.isBlank()) c.setNome(nome);

        System.out.print("  Novo telefone [" + c.getTelefone() + "] (Enter para manter): ");
        String tel = scanner.nextLine().trim();
        if (!tel.isBlank()) c.setTelefone(tel);

        try {
            clienteService.atualizarCliente(c);
            ok("Cliente \"" + c.getNome() + "\" atualizado com sucesso!");
        } catch (Exception e) {
            erro("Erro ao atualizar: " + e.getMessage());
        }
    }

    /** Solicita o ID e remove o cliente. */
    private static void removerCliente() {
        cabecalho("REMOVER CLIENTE");
        listarClientes();
        Long id = lerLong("  ID do cliente a remover : ");
        Optional<Cliente> op = clienteService.buscarPorId(id);
        if (op.isEmpty()) {
            aviso("Cliente ID " + id + " não encontrado.");
            return;
        }
        if (!confirmar("  Confirma remoção de \"" + op.get().getNome() + "\"?")) {
            aviso("Operação cancelada.");
            return;
        }
        try {
            clienteService.removerCliente(id);
            ok("Cliente removido.");
        } catch (Exception e) {
            erro("Erro ao remover: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // MÓDULO: PROFISSIONAIS
    // ---------------------------------------------------------------

    /** Submenu de gerenciamento de profissionais. */
    private static void menuProfissionais() {
        boolean loop = true;
        while (loop) {
            cabecalho("GESTÃO DE PROFISSIONAIS");
            System.out.println("  1. Listar profissionais");
            System.out.println("  2. Cadastrar profissional");
            System.out.println("  3. Remover profissional (exclusão lógica)");
            System.out.println("  0. Voltar");
            separador();

            switch (lerOpcao()) {
                case "1" -> listarProfissionais();
                case "2" -> cadastrarProfissional();
                case "3" -> removerUsuario();
                case "0" -> loop = false;
                default  -> aviso("Opção inválida.");
            }
        }
    }

    /** Exibe todos os profissionais ativos. */
    private static void listarProfissionais() {
        cabecalho("LISTA DE PROFISSIONAIS");
        List<Usuario> profs = usuarioService.listarPorTipo(TipoUsuario.PROFISSIONAL);
        if (profs.isEmpty()) {
            aviso("Nenhum profissional cadastrado.");
            return;
        }
        System.out.printf("  %-4s %-25s %s%n", "ID", "Nome", "Login");
        separador();
        profs.forEach(p -> System.out.printf("  %-4d %-25s %s%n",
                p.getId(), p.getNome(), p.getLogin()));
    }

    /** Coleta dados do novo profissional e delega o cadastro ao {@link UsuarioService}. */
    private static void cadastrarProfissional() {
        cabecalho("CADASTRAR PROFISSIONAL");
        try {
            Profissional p = (Profissional) UsuarioFactory.criar(TipoUsuario.PROFISSIONAL);
            p.setNome(lerTexto("  Nome          : "));
            p.setEmail(lerTexto("  E-mail        : "));
            p.setLogin(lerTexto("  Login         : "));
            p.setSenha(lerTexto("  Senha         : "));
            p.setEspecialidade(lerTexto("  Especialidade : "));
            p.setTipo(TipoUsuario.PROFISSIONAL);
            usuarioService.cadastrarUsuario(p);
            ok("Profissional \"" + p.getNome() + "\" cadastrado com sucesso!");
        } catch (Exception e) {
            erro("Não foi possível cadastrar o profissional: " + e.getMessage());
        }
    }

    /** Solicita o ID e realiza exclusão lógica (soft delete) do usuário. */
    private static void removerUsuario() {
        cabecalho("REMOVER PROFISSIONAL");
        listarProfissionais();
        Long id = lerLong("  ID do profissional a remover : ");
        if (!confirmar("  Confirma exclusão lógica do ID " + id + "?")) {
            aviso("Operação cancelada.");
            return;
        }
        try {
            usuarioService.removerUsuario(id);
            ok("Profissional removido (exclusão lógica).");
        } catch (Exception e) {
            erro("Erro ao remover: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // MÓDULO: SERVIÇOS
    // ---------------------------------------------------------------

    /** Submenu de gerenciamento de serviços. */
    private static void menuServicos() {
        boolean loop = true;
        while (loop) {
            cabecalho("GESTÃO DE SERVIÇOS");
            System.out.println("  1. Listar serviços");
            System.out.println("  2. Cadastrar serviço");
            System.out.println("  3. Atualizar valor");
            System.out.println("  4. Remover serviço");
            System.out.println("  0. Voltar");
            separador();

            switch (lerOpcao()) {
                case "1" -> listarServicos();
                case "2" -> cadastrarServico();
                case "3" -> atualizarServico();
                case "4" -> removerServico();
                case "0" -> loop = false;
                default  -> aviso("Opção inválida.");
            }
        }
    }

    /** Exibe todos os serviços com valor base e duração. */
    private static void listarServicos() {
        cabecalho("LISTA DE SERVIÇOS");
        List<Servico> servicos = servicoService.listarTodos();
        if (servicos.isEmpty()) {
            aviso("Nenhum serviço cadastrado.");
            return;
        }
        System.out.printf("  %-4s %-30s %10s %8s%n", "ID", "Descrição", "Valor (R$)", "Duração");
        separador();
        servicos.forEach(s -> System.out.printf("  %-4d %-30s %10.2f %5dmin%n",
                s.getId(), s.getDescricao(), s.getValorBase(), s.getDuracaoMinutos()));
    }

    /** Exibe serviços disponíveis para o menu do cliente. */
    private static void listarServicosDisponiveis() {
        cabecalho("SERVIÇOS DISPONÍVEIS");
        List<Servico> servicos = servicoService.listarTodos();
        if (servicos.isEmpty()) {
            aviso("Nenhum serviço disponível no momento.");
            return;
        }
        servicos.forEach(s -> System.out.printf("  • %-30s R$ %6.2f  (%dmin)%n",
                s.getDescricao(), s.getValorBase(), s.getDuracaoMinutos()));
    }

    /** Coleta descrição, valor e duração e cadastra o serviço. */
    private static void cadastrarServico() {
        cabecalho("CADASTRAR SERVIÇO");
        try {
            Servico s = new Servico();
            s.setDescricao(lerTexto("  Descrição     : "));
            s.setValorBase(lerDouble("  Valor base (R$): "));
            s.setDuracaoMinutos(lerInt("  Duração (min)  : "));
            servicoService.cadastrarServico(s);
            ok("Serviço \"" + s.getDescricao() + "\" cadastrado com sucesso!");
        } catch (Exception e) {
            erro("Não foi possível cadastrar o serviço: " + e.getMessage());
        }
    }

    /** Busca o serviço pelo ID e permite alterar o valor base. */
    private static void atualizarServico() {
        cabecalho("ATUALIZAR SERVIÇO");
        listarServicos();
        Long id = lerLong("  ID do serviço : ");
        Optional<Servico> op = servicoService.buscarPorId(id);
        if (op.isEmpty()) {
            aviso("Serviço ID " + id + " não encontrado.");
            return;
        }
        Servico s = op.get();
        System.out.print("  Novo valor [R$ " + String.format("%.2f", s.getValorBase()) + "] (Enter para manter): ");
        String val = scanner.nextLine().trim();
        if (!val.isBlank()) {
            try {
                s.setValorBase(Double.parseDouble(val.replace(",", ".")));
            } catch (NumberFormatException e) {
                erro("Valor inválido. Nenhuma alteração realizada.");
                return;
            }
        }
        try {
            servicoService.atualizarServico(s);
            ok("Serviço \"" + s.getDescricao() + "\" atualizado. Novo valor: R$ " + String.format("%.2f", s.getValorBase()));
        } catch (Exception e) {
            erro("Erro ao atualizar: " + e.getMessage());
        }
    }

    /** Solicita o ID e remove o serviço. */
    private static void removerServico() {
        cabecalho("REMOVER SERVIÇO");
        listarServicos();
        Long id = lerLong("  ID do serviço a remover : ");
        Optional<Servico> op = servicoService.buscarPorId(id);
        if (op.isEmpty()) {
            aviso("Serviço ID " + id + " não encontrado.");
            return;
        }
        if (!confirmar("  Confirma remoção de \"" + op.get().getDescricao() + "\"?")) {
            aviso("Operação cancelada.");
            return;
        }
        try {
            servicoService.removerServico(id);
            ok("Serviço removido.");
        } catch (Exception e) {
            erro("Erro ao remover: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // MÓDULO: ESTOQUE (PRODUTOS)
    // ---------------------------------------------------------------

    /** Submenu de gerenciamento de estoque. */
    private static void menuProdutos() {
        boolean loop = true;
        while (loop) {
            cabecalho("GESTÃO DE ESTOQUE");
            System.out.println("  1. Listar estoque");
            System.out.println("  2. Cadastrar produto");
            System.out.println("  3. Atualizar produto");
            System.out.println("  4. Remover produto");
            System.out.println("  5. Alertas de estoque baixo");
            System.out.println("  0. Voltar");
            separador();

            switch (lerOpcao()) {
                case "1" -> listarEstoque();
                case "2" -> cadastrarProduto();
                case "3" -> atualizarProduto();
                case "4" -> removerProduto();
                case "5" -> produtoService.verificarEstoqueBaixo();
                case "0" -> loop = false;
                default  -> aviso("Opção inválida.");
            }
        }
    }

    /** Exibe todos os produtos com estoque atual, mínimo e preço. */
    private static void listarEstoque() {
        cabecalho("ESTOQUE ATUAL");
        List<Produto> produtos = produtoService.listarTodos();
        if (produtos.isEmpty()) {
            aviso("Nenhum produto cadastrado.");
            return;
        }
        System.out.printf("  %-4s %-28s %6s %6s %10s%n", "ID", "Nome", "Qtd", "Mín", "Preço (R$)");
        separador();
        produtos.forEach(p -> {
            String alerta = p.getQuantidadeEstoque() <= p.getQuantidadeMinima() ? " [!]" : "";
            System.out.printf("  %-4d %-28s %6d %6d %10.2f%s%n",
                    p.getId(), p.getNome(),
                    p.getQuantidadeEstoque(), p.getQuantidadeMinima(),
                    p.getPrecoVenda(), alerta);
        });
    }

    /** Coleta nome, preços e quantidades e cadastra o produto. */
    private static void cadastrarProduto() {
        cabecalho("CADASTRAR PRODUTO");
        try {
            Produto p = new Produto();
            p.setNome(lerTexto("  Nome            : "));
            p.setPrecoCusto(lerDouble("  Preço de custo  : "));
            p.setPrecoVenda(lerDouble("  Preço de venda  : "));
            p.setQuantidadeEstoque(lerInt("  Qtd em estoque  : "));
            p.setQuantidadeMinima(lerInt("  Qtd mínima      : "));
            produtoService.cadastrarProduto(p);
            ok("Produto \"" + p.getNome() + "\" cadastrado com sucesso!");
        } catch (Exception e) {
            erro("Não foi possível cadastrar o produto: " + e.getMessage());
        }
    }

    /** Busca o produto pelo ID e permite alterar preço de venda e estoque. */
    private static void atualizarProduto() {
        cabecalho("ATUALIZAR PRODUTO");
        listarEstoque();
        Long id = lerLong("  ID do produto : ");
        Optional<Produto> op = produtoService.buscarPorId(id);
        if (op.isEmpty()) {
            aviso("Produto ID " + id + " não encontrado.");
            return;
        }
        Produto p = op.get();

        System.out.print("  Novo preço de venda [R$ " + String.format("%.2f", p.getPrecoVenda()) + "] (Enter para manter): ");
        String val = scanner.nextLine().trim();
        if (!val.isBlank()) {
            try { p.setPrecoVenda(Double.parseDouble(val.replace(",", "."))); }
            catch (NumberFormatException e) { aviso("Valor ignorado (formato inválido)."); }
        }

        System.out.print("  Novo estoque [" + p.getQuantidadeEstoque() + "] (Enter para manter): ");
        String est = scanner.nextLine().trim();
        if (!est.isBlank()) {
            try { p.setQuantidadeEstoque(Integer.parseInt(est)); }
            catch (NumberFormatException e) { aviso("Quantidade ignorada (formato inválido)."); }
        }

        try {
            produtoService.atualizarProduto(p);
            ok("Produto \"" + p.getNome() + "\" atualizado com sucesso!");
        } catch (Exception e) {
            erro("Erro ao atualizar: " + e.getMessage());
        }
    }

    /** Solicita o ID e remove o produto. */
    private static void removerProduto() {
        cabecalho("REMOVER PRODUTO");
        listarEstoque();
        Long id = lerLong("  ID do produto a remover : ");
        Optional<Produto> op = produtoService.buscarPorId(id);
        if (op.isEmpty()) {
            aviso("Produto ID " + id + " não encontrado.");
            return;
        }
        if (!confirmar("  Confirma remoção de \"" + op.get().getNome() + "\"?")) {
            aviso("Operação cancelada.");
            return;
        }
        try {
            produtoService.removerProduto(id);
            ok("Produto removido.");
        } catch (Exception e) {
            erro("Erro ao remover: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // MÓDULO: AGENDAMENTOS
    // ---------------------------------------------------------------

    /** Submenu de gerenciamento de agendamentos. */
    private static void menuAgendamentos() {
        boolean loop = true;
        while (loop) {
            cabecalho("GESTÃO DE AGENDAMENTOS");
            System.out.println("  1. Listar agendamentos");
            System.out.println("  2. Criar agendamento");
            System.out.println("  3. Concluir agendamento");
            System.out.println("  4. Cancelar agendamento");
            System.out.println("  5. Remover agendamento");
            System.out.println("  6. Buscar por período");
            System.out.println("  0. Voltar");
            separador();

            switch (lerOpcao()) {
                case "1" -> listarAgendamentos();
                case "2" -> criarAgendamento();
                case "3" -> concluirAgendamento();
                case "4" -> cancelarAgendamento();
                case "5" -> removerAgendamento();
                case "6" -> agendamentosPorPeriodo();
                case "0" -> loop = false;
                default  -> aviso("Opção inválida.");
            }
        }
    }

    /** Exibe todos os agendamentos com status, horário, cliente e valor. */
    private static void listarAgendamentos() {
        cabecalho("AGENDAMENTOS");
        List<Agendamento> lista = agendamentoService.listarTodos();
        if (lista.isEmpty()) {
            aviso("Nenhum agendamento encontrado.");
            return;
        }
        System.out.printf("  %-4s %-10s %-17s %-17s %-20s %s%n",
                "ID", "Status", "Início", "Fim", "Cliente", "Valor (R$)");
        separador();
        lista.forEach(a -> System.out.printf("  %-4d %-10s %-17s %-17s %-20s %.2f%n",
                a.getId(), a.getStatus(),
                a.getDataHoraInicio().format(FMT_DATA_HORA),
                a.getDataHoraFim().format(FMT_DATA_HORA),
                a.getCliente().getNome(), a.getValorFinal()));
    }

    /**
     * Coleta cliente, profissional e horários, e cria o agendamento.
     * Repete a solicitação de datas se o formato for inválido.
     */
    private static void criarAgendamento() {
        cabecalho("CRIAR AGENDAMENTO");
        try {
            listarClientes();
            Long clienteId = lerLong("  ID do cliente       : ");
            Optional<Cliente> opC = clienteService.buscarPorId(clienteId);
            if (opC.isEmpty()) { aviso("Cliente não encontrado."); return; }

            listarProfissionais();
            Long profId = lerLong("  ID do profissional  : ");
            List<Usuario> profs = usuarioService.listarPorTipo(TipoUsuario.PROFISSIONAL);
            Optional<Usuario> opP = profs.stream().filter(u -> u.getId().equals(profId)).findFirst();
            if (opP.isEmpty()) { aviso("Profissional não encontrado."); return; }

            LocalDateTime inicio = lerDataHora("  Início (dd/MM/yyyy HH:mm): ");
            LocalDateTime fim    = lerDataHora("  Fim    (dd/MM/yyyy HH:mm): ");

            Agendamento ag = new Agendamento();
            ag.setCliente(opC.get());
            ag.setProfissional((Profissional) opP.get());
            ag.setDataHoraInicio(inicio);
            ag.setDataHoraFim(fim);
            agendamentoService.criarAgendamento(ag);
            ok("Agendamento criado para \"" + opC.get().getNome()
                    + "\" com " + opP.get().getNome() + " em "
                    + inicio.format(FMT_DATA_HORA) + "!");
        } catch (Exception e) {
            erro("Erro ao criar agendamento: " + e.getMessage());
        }
    }

    /** Solicita o ID e marca o agendamento como CONCLUIDO. */
    private static void concluirAgendamento() {
        cabecalho("CONCLUIR AGENDAMENTO");
        listarAgendamentos();
        Long id = lerLong("  ID do agendamento : ");
        try {
            agendamentoService.concluirAgendamento(id);
            ok("Agendamento ID " + id + " concluído com sucesso!");
        } catch (Exception e) {
            erro("Erro ao concluir: " + e.getMessage());
        }
    }

    /** Solicita o ID e marca o agendamento como CANCELADO. */
    private static void cancelarAgendamento() {
        cabecalho("CANCELAR AGENDAMENTO");
        listarAgendamentos();
        Long id = lerLong("  ID do agendamento : ");
        if (!confirmar("  Confirma cancelamento do agendamento ID " + id + "?")) {
            aviso("Operação cancelada.");
            return;
        }
        try {
            agendamentoService.cancelarAgendamento(id);
            ok("Agendamento ID " + id + " cancelado.");
        } catch (Exception e) {
            erro("Erro ao cancelar: " + e.getMessage());
        }
    }

    /** Solicita o ID e remove o agendamento (apenas se não estiver concluído). */
    private static void removerAgendamento() {
        cabecalho("REMOVER AGENDAMENTO");
        listarAgendamentos();
        Long id = lerLong("  ID do agendamento a remover : ");
        if (!confirmar("  Confirma remoção do agendamento ID " + id + "?")) {
            aviso("Operação cancelada.");
            return;
        }
        try {
            agendamentoService.removerAgendamento(id);
            ok("Agendamento ID " + id + " removido.");
        } catch (Exception e) {
            erro("Erro ao remover: " + e.getMessage());
        }
    }

    /** Solicita período e exibe o relatório de agendamentos. */
    private static void agendamentosPorPeriodo() {
        cabecalho("AGENDAMENTOS POR PERÍODO");
        LocalDate inicio = lerData("  Início (dd/MM/yyyy): ");
        LocalDate fim    = lerData("  Fim    (dd/MM/yyyy): ");
        relatorioService.gerarRelatorioAgendamentos(inicio, fim);
    }

    // ---------------------------------------------------------------
    // MÓDULO: VENDAS AVULSAS
    // ---------------------------------------------------------------

    /** Submenu de vendas avulsas de produtos. */
    private static void menuVendasAvulsas() {
        boolean loop = true;
        while (loop) {
            cabecalho("VENDAS AVULSAS");
            System.out.println("  1. Listar vendas");
            System.out.println("  2. Registrar venda");
            System.out.println("  3. Remover venda");
            System.out.println("  0. Voltar");
            separador();

            switch (lerOpcao()) {
                case "1" -> listarVendasAvulsas();
                case "2" -> registrarVendaAvulsa();
                case "3" -> removerVendaAvulsa();
                case "0" -> loop = false;
                default  -> aviso("Opção inválida.");
            }
        }
    }

    /** Exibe todas as vendas avulsas registradas. */
    private static void listarVendasAvulsas() {
        cabecalho("LISTA DE VENDAS AVULSAS");
        List<VendaAvulsa> vendas = vendaAvulsaService.listarTodas();
        if (vendas.isEmpty()) {
            aviso("Nenhuma venda registrada.");
            return;
        }
        System.out.printf("  %-4s %-17s %-25s %5s %10s %s%n",
                "ID", "Data/Hora", "Produto", "Qtd", "Total (R$)", "Status");
        separador();
        vendas.forEach(v -> System.out.printf("  %-4d %-17s %-25s %5d %10.2f %s%n",
                v.getId(), v.getDataVenda().format(FMT_DATA_HORA),
                v.getProduto().getNome(), v.getQuantidade(),
                v.getTotal(), v.isFechado() ? "FECHADO" : "ABERTO"));
    }

    /**
     * Coleta produto, profissional e quantidade, e registra a venda avulsa.
     * Lista produtos e profissionais disponíveis antes de solicitar os IDs.
     */
    private static void registrarVendaAvulsa() {
        cabecalho("REGISTRAR VENDA AVULSA");
        try {
            listarEstoque();
            Long prodId = lerLong("  ID do produto       : ");
            Optional<Produto> opP = produtoService.buscarPorId(prodId);
            if (opP.isEmpty()) { aviso("Produto não encontrado."); return; }

            listarProfissionais();
            Long profId = lerLong("  ID do profissional  : ");
            List<Usuario> profs = usuarioService.listarPorTipo(TipoUsuario.PROFISSIONAL);
            Optional<Usuario> opU = profs.stream().filter(u -> u.getId().equals(profId)).findFirst();
            if (opU.isEmpty()) { aviso("Profissional não encontrado."); return; }

            int qtd = lerInt("  Quantidade          : ");

            VendaAvulsa venda = new VendaAvulsa();
            venda.setProduto(opP.get());
            venda.setProfissional((Profissional) opU.get());
            venda.setQuantidade(qtd);
            vendaAvulsaService.registrarVenda(venda);
            ok("Venda de " + qtd + "x \"" + opP.get().getNome()
                    + "\" registrada por " + opU.get().getNome() + "!");
        } catch (Exception e) {
            erro("Erro ao registrar venda: " + e.getMessage());
        }
    }

    /** Solicita o ID e remove a venda avulsa. */
    private static void removerVendaAvulsa() {
        cabecalho("REMOVER VENDA AVULSA");
        listarVendasAvulsas();
        Long id = lerLong("  ID da venda a remover : ");
        if (!confirmar("  Confirma remoção da venda ID " + id + "?")) {
            aviso("Operação cancelada.");
            return;
        }
        try {
            vendaAvulsaService.removerVenda(id);
            ok("Venda ID " + id + " removida.");
        } catch (Exception e) {
            erro("Erro ao remover: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // FECHAMENTO DE DIA
    // ---------------------------------------------------------------

    /**
     * Recupera o {@link Profissional} a partir do usuário logado e delega
     * o fechamento ao {@link VendaAvulsaService}.
     *
     * @param usuario usuário logado com perfil PROFISSIONAL
     */
    private static void fecharDiaProfissional(Usuario usuario) {
        cabecalho("FINALIZAR DIA");
        try {
            List<Usuario> profs = usuarioService.listarPorTipo(TipoUsuario.PROFISSIONAL);
            Optional<Usuario> opProf = profs.stream()
                    .filter(u -> u.getId().equals(usuario.getId()))
                    .findFirst();

            if (opProf.isEmpty() || !(opProf.get() instanceof Profissional profissional)) {
                erro("Profissional não encontrado para o usuário logado.");
                return;
            }

            if (!confirmar("  Isso irá fechar todas as vendas abertas de hoje. Confirma?")) {
                aviso("Operação cancelada.");
                return;
            }

            vendaAvulsaService.fecharDia(profissional);
            ok("Dia finalizado com sucesso!");
        } catch (Exception e) {
            erro("Erro ao fechar dia: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // RELATÓRIO FINANCEIRO POR PERÍODO
    // ---------------------------------------------------------------

    /** Solicita datas de início e fim e exibe o relatório financeiro do período. */
    private static void relatorioFinanceiroPorPeriodo() {
        cabecalho("RELATÓRIO FINANCEIRO");
        LocalDate inicio = lerData("  Início (dd/MM/yyyy): ");
        LocalDate fim    = lerData("  Fim    (dd/MM/yyyy): ");
        relatorioService.gerarRelatorioFinanceiro(inicio, fim);
    }

    // ---------------------------------------------------------------
    // HELPERS DE LEITURA — Scanner centralizado aqui
    // ---------------------------------------------------------------

    /**
     * Lê a opção do menu como String simples (sem validação de tipo).
     * Usado nos switch-case que aceitam qualquer string.
     *
     * @return string digitada pelo usuário, sem espaços extras
     */
    private static String lerOpcao() {
        System.out.print("  Opção: ");
        return scanner.nextLine().trim();
    }

    /**
     * Lê um texto obrigatório: repete a pergunta enquanto o campo estiver vazio.
     *
     * @param prompt rótulo exibido antes do campo
     * @return string não-vazia digitada pelo usuário
     */
    private static String lerTexto(String prompt) {
        while (true) {
            System.out.print(prompt);
            String val = scanner.nextLine().trim();
            if (!val.isBlank()) return val;
            aviso("Campo obrigatório. Tente novamente.");
        }
    }

    /**
     * Lê um {@code long}: repete a pergunta se o valor digitado não for numérico.
     *
     * @param prompt rótulo exibido antes do campo
     * @return valor {@code long} válido
     */
    private static Long lerLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                aviso("Digite um número inteiro válido (ex: 1, 2, 10).");
            }
        }
    }

    /**
     * Lê um {@code int}: repete a pergunta se o valor digitado não for numérico.
     *
     * @param prompt rótulo exibido antes do campo
     * @return valor {@code int} válido
     */
    private static int lerInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                aviso("Digite um número inteiro válido (ex: 5, 10, 100).");
            }
        }
    }

    /**
     * Lê um {@code double}: aceita vírgula ou ponto como separador decimal.
     * Repete a pergunta se o valor não for numérico.
     *
     * @param prompt rótulo exibido antes do campo
     * @return valor {@code double} válido
     */
    private static double lerDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim().replace(",", "."));
            } catch (NumberFormatException e) {
                aviso("Digite um valor numérico válido (ex: 12.50 ou 12,50).");
            }
        }
    }

    /**
     * Lê uma data no formato {@code dd/MM/yyyy}: repete a pergunta se o formato for inválido.
     *
     * @param prompt rótulo exibido antes do campo
     * @return {@link LocalDate} válida
     */
    private static LocalDate lerData(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return LocalDate.parse(scanner.nextLine().trim(), FMT_DATA);
            } catch (DateTimeParseException e) {
                aviso("Formato inválido. Use: dd/MM/yyyy  (ex: 25/12/2025).");
            }
        }
    }

    /**
     * Lê uma data e hora no formato {@code dd/MM/yyyy HH:mm}: repete se o formato for inválido.
     *
     * @param prompt rótulo exibido antes do campo
     * @return {@link LocalDateTime} válida
     */
    private static LocalDateTime lerDataHora(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return LocalDateTime.parse(scanner.nextLine().trim(), FMT_DATA_HORA);
            } catch (DateTimeParseException e) {
                aviso("Formato inválido. Use: dd/MM/yyyy HH:mm  (ex: 25/12/2025 14:30).");
            }
        }
    }

    /**
     * Exibe uma pergunta de confirmação (s/n) e retorna {@code true} para "s".
     *
     * @param msg mensagem da pergunta
     * @return {@code true} se o usuário confirmar com "s" ou "S"
     */
    private static boolean confirmar(String msg) {
        System.out.print(msg + " (s/n): ");
        return scanner.nextLine().trim().equalsIgnoreCase("s");
    }

    // ---------------------------------------------------------------
    // HELPERS VISUAIS
    // ---------------------------------------------------------------

    /**
     * Imprime um cabeçalho centralizado em caixa de sinais de igual.
     *
     * @param titulo texto do cabeçalho
     */
    private static void cabecalho(String titulo) {
        String borda = "=".repeat(titulo.length() + 8);
        System.out.println("\n" + borda);
        System.out.println("    " + titulo);
        System.out.println(borda);
    }

    /** Imprime uma linha separadora leve. */
    private static void separador() {
        System.out.println("  " + "-".repeat(48));
    }

    /**
     * Imprime uma mensagem de sucesso prefixada com {@code [OK]}.
     *
     * @param msg mensagem de sucesso
     */
    private static void ok(String msg) {
        System.out.println("  [OK] " + msg);
    }

    /**
     * Imprime uma mensagem de erro prefixada com {@code [ERRO]}.
     *
     * @param msg descrição do erro
     */
    private static void erro(String msg) {
        System.out.println("  [ERRO] " + msg);
    }

    /**
     * Imprime um aviso ou mensagem informativa prefixada com {@code [!]}.
     *
     * @param msg texto do aviso
     */
    private static void aviso(String msg) {
        System.out.println("  [!] " + msg);
    }
}
