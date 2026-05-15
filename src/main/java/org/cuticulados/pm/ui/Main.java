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
 * Orquestra o ciclo de vida inicial do sistema e gerencia o fluxo de transição
 * das interfaces de Clientes e Profissionais para o ecossistema Java Swing.
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
     * Executa o bootstrap da aplicação, invocando as migrações do banco de dados,
     * inicializando o gerenciador de persistência e instanciando a interface visual.
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
     * Captura as credenciais de autenticação via console e valida o perfil de acesso do usuário.
     * Redireciona o fluxo para o terminal correspondente ou bloqueia em caso de módulos migrados.
     *
     * @return true para manter o loop ativo, false para encerrar a sessão
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
     * Exibe o menu administrativo central para recursos legados em console e intercepta
     * requisições direcionadas a módulos que já foram migrados para a interface gráfica.
     *
     * @param admin a entidade do usuário administrador autenticado
     */
    private static void menuAdmin(Usuario admin) {
        boolean loop = true;
        while (loop) {
            System.out.println("\n=== Menu Admin ===");
            System.out.println("1. Gerenciar Clientes (Migrado p/ Swing)");
            System.out.println("2. Gerenciar Profissionais (Migrado p/ Swing)");
            System.out.println("3. Gerenciar Serviços");
            System.out.println("4. Gerenciar Produtos");
            System.out.println("5. Gerenciar Agendamentos");
            System.out.println("6. Vendas Avulsas");
            System.out.println("7. Relatórios");
            System.out.println("0. Sair");
            System.out.print("Opção: ");
            String op = scanner.nextLine().trim();

            switch (op) {
                case "1", "2" -> System.out.println("Operação migrada para interface gráfica.");
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

    /**
     * Disponibiliza os fluxos operacionais de atendimento, estoque e fechamento diário
     * específicos para o perfil profissional através da interface de console.
     *
     * @param profissionalUsuario a entidade do usuário profissional autenticado
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
            System.out.println("6. Finalizar dia");
            System.out.println("7. Relatório de vendas hoje");
            System.out.println("0. Sair");
            System.out.print("Opção: ");
            String op = scanner.nextLine().trim();

            switch (op) {
                case "1" -> listarAgendamentos();
                case "2" -> concluirAgendamento();
                case "3" -> cancelarAgendamento();
                case "4" -> registrarVendaAvulsa();
                case "5" -> relatorioService.gerarRelatorioEstoque();
                case "6" -> fecharDiaProfissional(profissionalUsuario);
                case "7" -> vendaAvulsaService.relatorioVendasDoDia();
                case "0" -> loop = false;
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    /**
     * Agrupa e invoca as rotinas de relatórios analíticos, gerenciais, financeiros
     * e de inventário com base no estado de dados atualizado do sistema.
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

    /**
     * Processa a entrada de dados temporais e delega à camada de serviço a geração
     * do ranking de serviços de maior relevância comercial no intervalo estipulado.
     */
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

    /**
     * Valida o vínculo do usuário logado com o registro de profissionais e consolida
     * os lançamentos contábeis de vendas e ordens de serviço do dia corrente.
     *
     * @param usuario o usuário do tipo profissional que está solicitando o fechamento
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
     * Recupera todos os registros de clientes cadastrados na base de dados
     * e os exibe de forma formatada para visualização do console de administração.
     */
    private static void listarClientes() {
        List<Cliente> clientes = clienteService.listarTodos();
        if (clientes.isEmpty()) { System.out.println("Nenhum cliente."); return; }
        clientes.forEach(c -> System.out.printf(
                " [%d] %s | CPF: %s | Tipo: %s | Atend/mês: %d%n",
                c.getId(), c.getNome(), c.getCpf(),
                c.getTipoCliente(), c.getTotalAtendimentosMes()));
    }

    /**
     * Consulta e renderiza a listagem completa de usuários ativos que pertencem
     * ao perfil operacional de profissionais cadastrados no sistema.
     */
    private static void listarProfissionais() {
        List<Usuario> profs = usuarioService.listarPorTipo(TipoUsuario.PROFISSIONAL);
        if (profs.isEmpty()) { System.out.println("Nenhum profissional."); return; }
        profs.forEach(p -> System.out.printf(" [%d] %s | Login: %s%n",
                p.getId(), p.getNome(), p.getLogin()));
    }

    /**
     * Gerencia a navegação e operações do submenu de controle de serviços oferecidos
     * pelo estabelecimento através da leitura e escrita no terminal.
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

    /**
     * Imprime no console a relação completa de especialidades de serviços cadastrados,
     * incluindo precificação base e tempo estimado de duração em minutos.
     */
    private static void listarServicos() {
        List<Servico> servicos = servicoService.listarTodos();
        if (servicos.isEmpty()) { System.out.println("Nenhum serviço."); return; }
        servicos.forEach(s -> System.out.printf(" [%d] %s | R$ %.2f | %d min%n",
                s.getId(), s.getDescricao(), s.getValorBase(), s.getDuracaoMinutos()));
    }

    /**
     * Executa a captura interativa de metadados para persistência de uma nova entidade
     * de Serviço via regras de negócio expostas pela camada Service.
     */
    private static void cadastrarServico() {
        try {
            Servico s = new Servico();
            System.out.print("Descrição: ");        s.setDescricao(scanner.nextLine().trim());
            System.out.print("Valor base: ");       s.setValorBase(new BigDecimal(scanner.nextLine().trim()));
            System.out.print("Duração (min): ");    s.setDuracaoMinutos(Integer.parseInt(scanner.nextLine().trim()));
            servicoService.cadastrarServico(s);
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /**
     * Localiza um serviço específico pelo seu identificador primário e efetua a alteração
     * de sua política de preço base de acordo com os parâmetros definidos.
     */
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
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /**
     * Encaminha comandos de exclusão física para a camada de serviços a fim de remover
     * de forma definitiva um registro de serviço da base de dados relacional.
     */
    private static void removerServico() {
        try {
            System.out.print("ID: ");
            servicoService.removerServico(Long.parseLong(scanner.nextLine().trim()));
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /**
     * Provê a interface de fluxo interativo para o gerenciamento de produtos,
     * permitindo consultas, mutações de estoque, novos cadastros e emissão de alertas.
     */
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

    /**
     * Interage com o terminal para receber parâmetros operacionais e comerciais de um novo
     * Produto, efetuando sua inserção nas políticas de estoque da aplicação.
     */
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

    /**
     * Realiza a atualização física de valores de mercado e quantidades volumétricas em
     * estoque associados a uma entidade de mercadoria identificada via ID único.
     */
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
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /**
     * Solicita a remoção estrutural de um produto mapeado através da delegação direta
     * à camada correspondente do ProdutoService.
     */
    private static void removerProduto() {
        try {
            System.out.print("ID: ");
            produtoService.removerProduto(Long.parseLong(scanner.nextLine().trim()));
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /**
     * Renderiza o fluxo de submenus de gerenciamento de agendamentos operacionais do salão,
     * cobrindo estados de triagem, criação de ordens, cancelamentos e filtros temporais.
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
     * Varre a camada relacional coletando todas as ordens de agendamento registradas,
     * organizando os resultados por carimbos de tempo, identificadores e status.
     */
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
     * Coordena o fluxo complexo de montagem de um Agendamento, correlacionando de forma
     * transacional entidades de Clientes, Profissionais e múltiplos Serviços com validação de horários.
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
        } catch (DateTimeParseException e) {
            System.out.println("Formato inválido. Use: dd/MM/yyyy HH:mm");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    /**
     * Efetua a transição de estado de um agendamento específico para Concluído,
     * disparando regras financeiras colaterais e faturamento.
     */
    private static void concluirAgendamento() {
        try {
            System.out.print("ID: ");
            agendamentoService.concluirAgendamento(Long.parseLong(scanner.nextLine().trim()));
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /**
     * Altera o estado de integridade de uma agenda para o status Cancelado,
     * liberando a grade de horários do profissional envolvido.
     */
    private static void cancelarAgendamento() {
        try {
            System.out.print("ID: ");
            agendamentoService.cancelarAgendamento(Long.parseLong(scanner.nextLine().trim()));
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /**
     * Remove fisicamente um agendamento pendente que não registrou consolidação contábil.
     */
    private static void removerAgendamento() {
        try {
            System.out.print("ID: ");
            agendamentoService.removerAgendamento(Long.parseLong(scanner.nextLine().trim()));
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }

    /**
     * Processa delimitadores calendários inseridos no console para extrair
     * relatórios segmentados de atendimentos programados.
     */
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

    /**
     * Fornece controle transacional sobre a venda avulsa de itens do estoque de produtos
     * do salão, capturando listagens, inserções e estornos.
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
     * Registra de maneira síncrona a saída de um produto para comercialização direta,
     * vinculando o faturamento e deduzindo as unidades correspondentes da contagem física.
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

    /**
     * Executa a emissão agregada de balanços macrofinanceiros cruzando entradas e saídas
     * operacionais computadas no período cronológico requisitado.
     */
    private static void relatorioFinanceiroPorPeriodo() {
        try {
            System.out.print("Início (dd/MM/yyyy): ");
            LocalDate inicio = LocalDate.parse(scanner.nextLine().trim(), FMT_DATA);
            System.out.print("Fim    (dd/MM/yyyy): ");
            LocalDate fim = LocalDate.parse(scanner.nextLine().trim(), FMT_DATA);
            relatorioService.gerarRelatorioFinanceiro(inicio, fim);
        } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
    }
}