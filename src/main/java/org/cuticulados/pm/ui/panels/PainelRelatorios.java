package org.cuticulados.pm.ui.panels;

import org.cuticulados.pm.entity.ProdutoEntity;
import org.cuticulados.pm.controller.produto.ProdutoController;
import org.cuticulados.pm.controller.relatorio.RelatorioController;
import org.cuticulados.pm.controller.venda.VendaAvulsaController;
import org.cuticulados.pm.ui.theme.AppColors;
import org.cuticulados.pm.ui.theme.AppDimensions;
import org.cuticulados.pm.ui.theme.AppFonts;
import org.cuticulados.pm.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Painel de relatórios do perfil ADMIN.
 * Agrupa 6 abas: agendamentos, financeiro, estoque, saldo, vendas do dia e ranking.
 */
public class PainelRelatorios extends JPanel {

    // services
    private final RelatorioController   relatorioController;
    private final VendaAvulsaController vendaAvulsaController;
    private final ProdutoController     produtoController;

    // painel relatorios
    public PainelRelatorios() {
        this.relatorioController   = new RelatorioController();
        this.vendaAvulsaController = new VendaAvulsaController();
        this.produtoController     = new ProdutoController();
        setLayout(new BorderLayout());
        setBackground(AppColors.FUNDO_APP);
        inicializarComponentes();
    }

    // inicialização
    private void inicializarComponentes() {
        add(criarTopbar(), BorderLayout.NORTH);
        add(criarAbas(),   BorderLayout.CENTER);
    }

    // topbar simples com titulo
    private JPanel criarTopbar() {
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(AppColors.FUNDO_CARD);
        topbar.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDA, 1, false),
                new EmptyBorder(10, 20, 10, 20)
        ));
        topbar.setPreferredSize(new Dimension(0, AppDimensions.TOPBAR_HEIGHT));

        JLabel titulo = new JLabel("Relatórios");
        titulo.setFont(AppFonts.TITLE);
        titulo.setForeground(AppColors.TEXTO_TITULO);
        topbar.add(titulo, BorderLayout.WEST);
        return topbar;
    }

    // JTabbedPane com as 6 abas de relatório
    private JTabbedPane criarAbas() {
        JTabbedPane abas = new JTabbedPane();
        abas.setFont(AppFonts.BODY);
        abas.setBackground(AppColors.FUNDO_APP);
        abas.setBorder(new EmptyBorder(12, 16, 16, 16));

        abas.addTab("Agendamentos",    criarAbaAgendamentos());
        abas.addTab("Financeiro",      criarAbaFinanceiro());
        abas.addTab("Estoque Atual",   criarAbaEstoque());
        abas.addTab("Saldo Geral",     criarAbaSaldo());
        abas.addTab("Vendas do Dia",   criarAbaVendasDia());
        abas.addTab("Ranking Serviços",criarAbaRanking());

        return abas;
    }

    // aba 1 — agendamentos por período → JTextArea
    private JPanel criarAbaAgendamentos() {
        JPanel painel = criarPainelAba();

        JSpinner spInicio = criarSpinnerData();
        JSpinner spFim    = criarSpinnerData();
        JTextArea area    = criarAreaTexto();

        JButton btnGerar = new JButton("Gerar");
        AppTheme.stylePrimaryButton(btnGerar, true);
        btnGerar.addActionListener(e -> {
            LocalDate inicio = spinnerParaLocalDate(spInicio);
            LocalDate fim    = spinnerParaLocalDate(spFim);
            area.setText(capturarSaida(() ->
                    relatorioController.gerarRelatorioAgendamentos(inicio, fim)));
        });

        painel.add(criarBarraFiltro("Início", spInicio, "Fim", spFim, btnGerar), BorderLayout.NORTH);
        painel.add(new JScrollPane(area), BorderLayout.CENTER);
        return painel;
    }

    // aba 2 — financeiro por período → JTextArea
    private JPanel criarAbaFinanceiro() {
        JPanel painel = criarPainelAba();

        JSpinner spInicio = criarSpinnerData();
        JSpinner spFim    = criarSpinnerData();
        JTextArea area    = criarAreaTexto();

        JButton btnGerar = new JButton("Gerar");
        AppTheme.stylePrimaryButton(btnGerar, true);
        btnGerar.addActionListener(e -> {
            LocalDate inicio = spinnerParaLocalDate(spInicio);
            LocalDate fim    = spinnerParaLocalDate(spFim);
            area.setText(capturarSaida(() ->
                    relatorioController.gerarRelatorioFinanceiro(inicio, fim)));
        });

        painel.add(criarBarraFiltro("Início", spInicio, "Fim", spFim, btnGerar), BorderLayout.NORTH);
        painel.add(new JScrollPane(area), BorderLayout.CENTER);
        return painel;
    }

    // aba 3 — estoque atual → JTable com todos os produtos
    private JPanel criarAbaEstoque() {
        JPanel painel = criarPainelAba();

        String[] colunas = {"Nome", "Estoque", "Mínimo", "Preço Venda", "Status"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tabela = criarTabela(model);

        JButton btnAtualizar = new JButton("↻ Atualizar");
        AppTheme.styleOutlineButton(btnAtualizar, true);
        btnAtualizar.addActionListener(e -> carregarEstoque(model));

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        barra.setOpaque(false);
        barra.add(btnAtualizar);

        painel.add(barra, BorderLayout.NORTH);
        painel.add(embrulharTabela(tabela), BorderLayout.CENTER);

        carregarEstoque(model);
        return painel;
    }

    private void carregarEstoque(DefaultTableModel model) {
        model.setRowCount(0);
        List<ProdutoEntity> lista = produtoController.listarTodos();
        for (ProdutoEntity p : lista) {
            String status = p.getQuantidadeEstoque() <= p.getQuantidadeMinima() ? "⚠ BAIXO" : "OK";
            model.addRow(new Object[]{
                    p.getNome(),
                    p.getQuantidadeEstoque(),
                    p.getQuantidadeMinima(),
                    String.format("R$ %.2f", p.getPrecoVenda()),
                    status
            });
        }
    }

    // aba 4 — saldo geral → JTextArea com saída do imprimirSaldo
    private JPanel criarAbaSaldo() {
        JPanel painel = criarPainelAba();

        JTextArea area = criarAreaTexto();

        JButton btnAtualizar = new JButton("↻ Atualizar");
        AppTheme.styleOutlineButton(btnAtualizar, true);
        btnAtualizar.addActionListener(e ->
                area.setText(capturarSaida(() -> relatorioController.imprimirSaldo())));

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        barra.setOpaque(false);
        barra.add(btnAtualizar);

        painel.add(barra, BorderLayout.NORTH);
        painel.add(new JScrollPane(area), BorderLayout.CENTER);

        // carrega automaticamente ao abrir
        area.setText(capturarSaida(() -> relatorioController.imprimirSaldo()));
        return painel;
    }

    // aba 5 — vendas do dia → JTextArea
    private JPanel criarAbaVendasDia() {
        JPanel painel = criarPainelAba();

        JTextArea area = criarAreaTexto();

        JButton btnAtualizar = new JButton("↻ Atualizar");
        AppTheme.styleOutlineButton(btnAtualizar, true);
        btnAtualizar.addActionListener(e ->
                area.setText(capturarSaida(() -> vendaAvulsaController.relatorioVendasDoDia())));

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        barra.setOpaque(false);
        barra.add(btnAtualizar);

        painel.add(barra, BorderLayout.NORTH);
        painel.add(new JScrollPane(area), BorderLayout.CENTER);

        area.setText(capturarSaida(() -> vendaAvulsaController.relatorioVendasDoDia()));
        return painel;
    }

    // aba 6 — ranking de serviços por período → JTable
    private JPanel criarAbaRanking() {
        JPanel painel = criarPainelAba();

        JSpinner spInicio = criarSpinnerData();
        JSpinner spFim    = criarSpinnerData();

        String[] colunas = {"Posição", "Serviço", "Realizações"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabela = criarTabela(model);

        JButton btnGerar = new JButton("Gerar");
        AppTheme.stylePrimaryButton(btnGerar, true);
        btnGerar.addActionListener(e -> {
            LocalDate inicio = spinnerParaLocalDate(spInicio);
            LocalDate fim    = spinnerParaLocalDate(spFim);

            // captura saída do terminal e exibe em tabela simples de texto
            String saida = capturarSaida(() -> relatorioController.gerarRankingServicos(inicio, fim));
            model.setRowCount(0);
            String[] linhas = saida.split("\n");
            int pos = 1;
            for (String linha : linhas) {
                linha = linha.trim();
                if (linha.isEmpty() || linha.startsWith("===")) continue;
                // formato: " 1. Serviço | 3x realizados | R$ 150,00 faturado"
                model.addRow(new Object[]{pos++, linha});
            }
        });

        painel.add(criarBarraFiltro("Início", spInicio, "Fim", spFim, btnGerar), BorderLayout.NORTH);
        painel.add(embrulharTabela(tabela), BorderLayout.CENTER);
        return painel;
    }

    // ajuda de ui

    // painel base de aba com BorderLayout e padding
    private JPanel criarPainelAba() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(AppColors.FUNDO_APP);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));
        return p;
    }

    // barra com dois spinners de data e botão gerar
    private JPanel criarBarraFiltro(String lbl1, JSpinner sp1,
                                    String lbl2, JSpinner sp2,
                                    JButton btn) {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barra.setOpaque(false);

        JLabel label1 = new JLabel(lbl1 + ":");
        label1.setFont(AppFonts.LABEL);
        label1.setForeground(AppColors.TEXTO_SECUNDARIO);

        JLabel label2 = new JLabel(lbl2 + ":");
        label2.setFont(AppFonts.LABEL);
        label2.setForeground(AppColors.TEXTO_SECUNDARIO);

        barra.add(label1);
        barra.add(sp1);
        barra.add(Box.createHorizontalStrut(4));
        barra.add(label2);
        barra.add(sp2);
        barra.add(Box.createHorizontalStrut(8));
        barra.add(btn);
        return barra;
    }

    // spinner de data formatado (dd/MM/yyyy)
    private JSpinner criarSpinnerData() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd/MM/yyyy");
        spinner.setEditor(editor);
        spinner.setFont(AppFonts.BODY);
        spinner.setPreferredSize(new Dimension(120, AppDimensions.INPUT_HEIGHT));
        spinner.setBorder(new LineBorder(AppColors.BORDA, 1, false));
        editor.getTextField().setFont(AppFonts.BODY);
        editor.getTextField().setBackground(AppColors.FUNDO_APP);
        editor.getTextField().setBorder(new EmptyBorder(4, 6, 4, 6));
        return spinner;
    }

    // converte valor do JSpinner de data para LocalDate
    private LocalDate spinnerParaLocalDate(JSpinner spinner) {
        Date date = (Date) spinner.getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // área de texto para exibir saída do relatório
    private JTextArea criarAreaTexto() {
        JTextArea area = new JTextArea();
        area.setFont(new Font("Consolas", Font.PLAIN, 12));
        area.setForeground(AppColors.TEXTO_CORPO);
        area.setBackground(AppColors.FUNDO_CARD);
        area.setEditable(false);
        area.setMargin(new Insets(8, 12, 8, 12));
        area.setBorder(new LineBorder(AppColors.BORDA, 1, false));
        return area;
    }

    // tabela simples no estilo padrão do projeto
    private JTable criarTabela(DefaultTableModel model) {
        JTable tabela = new JTable(model);
        tabela.setFont(AppFonts.TABLE);
        tabela.setForeground(AppColors.TEXTO_CORPO);
        tabela.setBackground(AppColors.FUNDO_CARD);
        tabela.setSelectionBackground(AppColors.ROSA_PALIDO);
        tabela.setSelectionForeground(AppColors.TEXTO_TITULO);
        tabela.setGridColor(AppColors.BORDA);
        tabela.setRowHeight(AppDimensions.ROW_HEIGHT);
        tabela.setShowVerticalLines(false);
        tabela.setIntercellSpacing(new Dimension(0, 1));
        tabela.setFillsViewportHeight(true);

        JTableHeader header = tabela.getTableHeader();
        header.setFont(AppFonts.TABLE_HEADER);
        header.setBackground(AppColors.ROSA_PALIDO);
        header.setForeground(AppColors.TEXTO_TITULO);
        header.setPreferredSize(new Dimension(0, 34));
        header.setBorder(new LineBorder(AppColors.BORDA, 1, false));
        header.setReorderingAllowed(false);
        return tabela;
    }

    private JScrollPane embrulharTabela(JTable tabela) {
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(new LineBorder(AppColors.BORDA, 1, false));
        scroll.getViewport().setBackground(AppColors.FUNDO_CARD);
        return scroll;
    }

    // redireciona System.out durante a chamada e retorna o texto capturado
    private String capturarSaida(Runnable chamada) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream antigo = System.out;
        try (PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8)) {
            System.setOut(ps);
            chamada.run();
        } catch (Exception ex) {
            return "Erro ao gerar relatório: " + ex.getMessage();
        } finally {
            System.setOut(antigo);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }
}
