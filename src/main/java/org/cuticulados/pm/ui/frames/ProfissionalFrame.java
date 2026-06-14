package org.cuticulados.pm.ui.frames;

import org.cuticulados.pm.entity.AgendamentoEntity;
import org.cuticulados.pm.entity.ProdutoEntity;
import org.cuticulados.pm.entity.ProfissionalEntity;
import org.cuticulados.pm.entity.VendaAvulsaEntity;
import org.cuticulados.pm.service.AgendamentoService;
import org.cuticulados.pm.service.ProdutoService;
import org.cuticulados.pm.service.VendaAvulsaService;
import org.cuticulados.pm.ui.theme.AppColors;
import org.cuticulados.pm.ui.theme.AppDimensions;
import org.cuticulados.pm.ui.theme.AppFonts;
import org.cuticulados.pm.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Janela principal do perfil PROFISSIONAL.
 * Sidebar de navegação, area central com cards para agenda, vendas, estoque e relatório.
 */
public class ProfissionalFrame extends JFrame {

    // services
    private final AgendamentoService agendamentoService;
    private final VendaAvulsaService vendaAvulsaService;
    private final ProdutoService     produtoService;

    // profissional logado
    private final ProfissionalEntity profissionalEntityLogado;

    // layout central
    private JPanel     painelCentral;
    private CardLayout cardLayout;

    // mapa card → botão nav para estado ativo
    private final Map<String, JButton> botoesNav = new HashMap<>();
    private String cardAtivo = CARD_AGENDAMENTOS;

    // identificadores dos cards
    private static final String CARD_AGENDAMENTOS = "agendamentos";
    private static final String CARD_REGISTRAR    = "registrar";
    private static final String CARD_RELATORIO    = "relatorio";
    private static final String CARD_ESTOQUE      = "estoque";

    // modelos de tabela e área de texto mantidos para refresh
    private DefaultTableModel modelAgendamentos;
    private DefaultTableModel modelEstoque;
    private JTextArea         areaRelatorio;
    private JTable            tabelaAgendamentos;

    // formatos de data
    private static final DateTimeFormatter FMT     = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_HR  = DateTimeFormatter.ofPattern("HH:mm");

    // cores de status (mesmo padrão de PainelAgendamentos)
    private static final Color COR_PENDENTE_BG  = new Color(0xFF, 0xF3, 0xD6);
    private static final Color COR_CONCLUIDO_BG = new Color(0xE3, 0xF0, 0xDC);
    private static final Color COR_CANCELADO_BG = new Color(0xFA, 0xDD, 0xDD);

    // profissional frame
    public ProfissionalFrame(ProfissionalEntity profissionalEntity) {
        this.profissionalEntityLogado = profissionalEntity;
        this.agendamentoService = new AgendamentoService();
        this.vendaAvulsaService = new VendaAvulsaService();
        this.produtoService     = new ProdutoService();
        configurarJanela();
        inicializarLayout();
        navegarPara(CARD_AGENDAMENTOS);
    }

    // configuração de tamanho e título
    private void configurarJanela() {
        setTitle("NailGestor — Área do Profissional");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 640);
        setMinimumSize(new Dimension(820, 520));
        setLocationRelativeTo(null);
        getContentPane().setBackground(AppColors.FUNDO_APP);
    }

    // layout principal: sidebar | área central | statusbar
    private void inicializarLayout() {
        setLayout(new BorderLayout());
        add(criarSidebar(),       BorderLayout.WEST);
        add(criarAreaPrincipal(), BorderLayout.CENTER);
        add(criarStatusBar(),     BorderLayout.SOUTH);
    }

    // ============================================================
    // SIDEBAR
    // ============================================================

    private JPanel criarSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(AppColors.ROXO);
        sidebar.setPreferredSize(new Dimension(AppDimensions.SIDEBAR_WIDTH, 0));

        sidebar.add(criarSidebarLogo());
        sidebar.add(criarSidebarSeparador());

        // seção agenda
        sidebar.add(criarSidebarNavTitulo("AGENDA"));
        sidebar.add(criarNavButton("Agendamentos", CARD_AGENDAMENTOS));

        sidebar.add(criarSidebarSeparador());

        // seção vendas
        sidebar.add(criarSidebarNavTitulo("VENDAS"));
        sidebar.add(criarNavButton("Registrar Venda",  CARD_REGISTRAR));
        sidebar.add(criarNavButton("Relatório do Dia", CARD_RELATORIO));

        sidebar.add(criarSidebarSeparador());

        // seção estoque
        sidebar.add(criarSidebarNavTitulo("ESTOQUE"));
        sidebar.add(criarNavButton("Ver Estoque", CARD_ESTOQUE));

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(criarSidebarSeparador());

        // botão especial de fechar dia
        sidebar.add(criarBotaoFecharDia());
        sidebar.add(criarSidebarSeparador());
        sidebar.add(criarSidebarUsuario());
        sidebar.add(criarSidebarSeparador());
        sidebar.add(criarBotaoSair());

        return sidebar;
    }

    private JPanel criarSidebarLogo() {
        JPanel logo = new JPanel();
        logo.setLayout(new BoxLayout(logo, BoxLayout.Y_AXIS));
        logo.setOpaque(false);
        logo.setBorder(new EmptyBorder(18, 16, 14, 16));

        JLabel nome = new JLabel("NailGestor");
        nome.setFont(new Font(AppFonts.FAMILY, Font.BOLD, 22));
        nome.setForeground(Color.WHITE);
        nome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Área do Profissional");
        sub.setFont(AppFonts.SMALL);
        sub.setForeground(new Color(255, 255, 255, 128));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        logo.add(nome);
        logo.add(Box.createVerticalStrut(2));
        logo.add(sub);
        return logo;
    }

    private JSeparator criarSidebarSeparador() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 26));
        sep.setBackground(new Color(255, 255, 255, 26));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private JLabel criarSidebarNavTitulo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font(AppFonts.FAMILY, Font.PLAIN, 9));
        lbl.setForeground(new Color(255, 255, 255, 90));
        lbl.setBorder(new EmptyBorder(12, 16, 4, 16));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton criarNavButton(String texto, String card) {
        JButton btn = new JButton(texto);
        btn.setFont(AppFonts.NAV);
        btn.setForeground(new Color(255, 255, 255, 184));
        btn.setBackground(AppColors.ROXO);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new CompoundBorder(
                new MatteBorder(0, 3, 0, 0, AppColors.ROXO),
                new EmptyBorder(9, 2, 9, 2)
        ));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setRolloverEnabled(false);

        // hover com cor opaca (evita artefato de alpha no Swing)
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!card.equals(cardAtivo)) btn.setBackground(new Color(105, 56, 100));
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!card.equals(cardAtivo)) btn.setBackground(AppColors.ROXO);
            }
        });

        btn.addActionListener(e -> navegarPara(card));
        botoesNav.put(card, btn);
        return btn;
    }

    // botão "Finalizar Dia" com destaque dourado
    private JPanel criarBotaoFecharDia() {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(8, 12, 8, 12));
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        JButton btn = new JButton("Finalizar Dia");
        btn.setFont(AppFonts.BUTTON);
        btn.setForeground(AppColors.DOURADO);
        btn.setBackground(AppColors.ROXO_ESCURO);
        btn.setOpaque(true);
        btn.setBorderPainted(true);
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(AppColors.DOURADO, 1, false),
                new EmptyBorder(7, 12, 7, 12)
        ));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> confirmarFecharDia());

        wrapper.add(btn);
        return wrapper;
    }

    // bloco do profissional logado no rodapé da sidebar
    private JPanel criarSidebarUsuario() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        painel.setOpaque(false);
        painel.setBorder(new EmptyBorder(2, 2, 2, 2));

        JPanel avatar = criarAvatar(profissionalEntityLogado.getNome());

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel lblNome = new JLabel(abreviarNome(profissionalEntityLogado.getNome()));
        lblNome.setFont(AppFonts.SMALL);
        lblNome.setForeground(new Color(255, 255, 255, 218));

        String especialidade = profissionalEntityLogado.getEspecialidade() != null
                ? profissionalEntityLogado.getEspecialidade() : "Profissional";
        JLabel lblEsp = new JLabel(especialidade);
        lblEsp.setFont(new Font(AppFonts.FAMILY, Font.PLAIN, 10));
        lblEsp.setForeground(new Color(255, 255, 255, 102));

        info.add(lblNome);
        info.add(lblEsp);
        painel.add(avatar);
        painel.add(info);
        return painel;
    }

    // círculo com as iniciais (mesmo padrão de MainFrame/ClienteFrame)
    private JPanel criarAvatar(String nome) {
        String iniciais = extrairIniciais(nome);
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.ROSA);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font(AppFonts.FAMILY, Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(iniciais)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(iniciais, x, y);
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(30, 30));
        return avatar;
    }

    // ============================================================
    // ÁREA PRINCIPAL
    // ============================================================

    private JPanel criarAreaPrincipal() {
        JPanel area = new JPanel(new BorderLayout());
        area.setBackground(AppColors.FUNDO_APP);

        cardLayout    = new CardLayout();
        painelCentral = new JPanel(cardLayout);
        painelCentral.setBackground(AppColors.FUNDO_APP);

        painelCentral.add(criarPainelAgendamentos(), CARD_AGENDAMENTOS);
        painelCentral.add(criarPainelRegistrarVenda(), CARD_REGISTRAR);
        painelCentral.add(criarPainelRelatorio(),     CARD_RELATORIO);
        painelCentral.add(criarPainelEstoque(),       CARD_ESTOQUE);

        area.add(painelCentral, BorderLayout.CENTER);
        return area;
    }

    // barra de status inferior
    private JPanel criarStatusBar() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(AppColors.ROXO_ESCURO);
        barra.setBorder(new EmptyBorder(4, 16, 4, 16));
        barra.setPreferredSize(new Dimension(0, 30));

        JLabel lblUsuario = new JLabel("Profissional: " + profissionalEntityLogado.getNome()
                + "  ·  " + profissionalEntityLogado.getLogin());
        lblUsuario.setFont(new Font(AppFonts.FAMILY, Font.PLAIN, 11));
        lblUsuario.setForeground(new Color(255, 255, 255, 160));
        barra.add(lblUsuario, BorderLayout.WEST);

        JLabel lblSistema = new JLabel("NailGestor — Sistema de Gestão");
        lblSistema.setFont(new Font(AppFonts.FAMILY, Font.PLAIN, 11));
        lblSistema.setForeground(new Color(255, 255, 255, 80));
        barra.add(lblSistema, BorderLayout.EAST);

        return barra;
    }

    // ============================================================
    // PAINÉIS DE CONTEÚDO
    // ============================================================

    // card: Agendamentos do profissional com concluir / cancelar
    private JPanel criarPainelAgendamentos() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(AppColors.FUNDO_APP);

        // botões de ação
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        acoes.setOpaque(false);

        JButton btnConcluir = new JButton("Concluir");
        JButton btnCancelar = new JButton("Cancelar");
        AppTheme.styleOutlineButton(btnConcluir, false);
        AppTheme.styleOutlineButton(btnCancelar, false);
        btnConcluir.setForeground(AppColors.STATUS_CONC);
        btnCancelar.setForeground(AppColors.STATUS_PEND);

        btnConcluir.addActionListener(e -> concluirAgendamentoSelecionado());
        btnCancelar.addActionListener(e -> cancelarAgendamentoSelecionado());

        acoes.add(btnConcluir);
        acoes.add(btnCancelar);
        painel.add(criarTopbar("Meus Agendamentos", acoes), BorderLayout.NORTH);

        // tabela
        String[] colunas = {"ID", "Status", "Início", "Fim", "Cliente", "Valor (R$)"};
        modelAgendamentos = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabelaAgendamentos = criarTabela(modelAgendamentos);
        aplicarRenderizadorStatus(tabelaAgendamentos);

        int[] larguras = {50, 100, 130, 130, 200, 100};
        for (int i = 0; i < larguras.length; i++) {
            tabelaAgendamentos.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }
        tabelaAgendamentos.getColumnModel().getColumn(0).setMaxWidth(60);

        JPanel conteudo = new JPanel(new BorderLayout());
        conteudo.setOpaque(false);
        conteudo.setBorder(new EmptyBorder(16, 20, 16, 20));
        conteudo.add(embrulharTabela(tabelaAgendamentos), BorderLayout.CENTER);
        painel.add(conteudo, BorderLayout.CENTER);
        return painel;
    }

    // card: Registrar Venda Avulsa
    private JPanel criarPainelRegistrarVenda() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(AppColors.FUNDO_APP);
        painel.add(criarTopbar("Registrar Venda Avulsa", null), BorderLayout.NORTH);

        // card branco com formulário centralizado
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(AppColors.FUNDO_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDA, 1, false),
                new EmptyBorder(24, 32, 24, 32)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.insets  = new Insets(4, 6, 4, 6);
        gbc.weightx = 1.0;

        // combobox de produtos com nome e estoque
        List<ProdutoEntity> produtoEntities = produtoService.listarTodos();
        JComboBox<ProdutoEntity> cbProduto = new JComboBox<>();
        produtoEntities.forEach(cbProduto::addItem);
        estilizarCombo(cbProduto);
        cbProduto.setRenderer((list, value, idx, isSelected, cellHasFocus) -> {
            String texto = value != null
                    ? value.getNome() + "  (estoque: " + value.getQuantidadeEstoque() + ")"
                    : "";
            JLabel lbl = new JLabel(texto);
            lbl.setFont(AppFonts.BODY);
            lbl.setBorder(new EmptyBorder(4, 8, 4, 8));
            if (isSelected) { lbl.setBackground(AppColors.ROSA_PALIDO); lbl.setOpaque(true); }
            return lbl;
        });

        // spinner de quantidade com max dinâmico
        ProdutoEntity primeiro = produtoEntities.isEmpty() ? null : produtoEntities.get(0);
        int estoqueInicial = primeiro != null ? Math.max(1, primeiro.getQuantidadeEstoque()) : 1;
        JSpinner spQtd = new JSpinner(new SpinnerNumberModel(1, 1, estoqueInicial, 1));
        estilizarSpinner(spQtd);

        // label de total em tempo real
        JLabel lblTotal = new JLabel("R$ 0,00");
        lblTotal.setFont(new Font(AppFonts.FAMILY, Font.BOLD, 18));
        lblTotal.setForeground(AppColors.ROXO);

        // atualiza total e max do spinner ao mudar produto ou quantidade
        Runnable atualizarTotal = () -> {
            ProdutoEntity p = (ProdutoEntity) cbProduto.getSelectedItem();
            int qtd = (Integer) spQtd.getValue();
            if (p != null && p.getPrecoVenda() != null) {
                BigDecimal total = p.getPrecoVenda().multiply(BigDecimal.valueOf(qtd));
                lblTotal.setText(String.format("R$ %.2f", total));
                int maxEstoque = Math.max(1, p.getQuantidadeEstoque());
                ((SpinnerNumberModel) spQtd.getModel()).setMaximum(maxEstoque);
                if (qtd > maxEstoque) spQtd.setValue(maxEstoque);
            }
        };
        cbProduto.addActionListener(e -> atualizarTotal.run());
        spQtd.addChangeListener(e -> atualizarTotal.run());
        if (primeiro != null) atualizarTotal.run();

        // botão confirmar
        JButton btnRegistrar = new JButton("Registrar Venda");
        AppTheme.stylePrimaryButton(btnRegistrar, false);
        btnRegistrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, AppDimensions.BUTTON_HEIGHT));
        btnRegistrar.addActionListener(e -> {
            ProdutoEntity p = (ProdutoEntity) cbProduto.getSelectedItem();
            if (p == null) {
                JOptionPane.showMessageDialog(this, "Selecione um produto.", "Campo inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int qtd = (Integer) spQtd.getValue();
            if (qtd > p.getQuantidadeEstoque()) {
                JOptionPane.showMessageDialog(this,
                        "Estoque insuficiente. Disponível: " + p.getQuantidadeEstoque(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            VendaAvulsaEntity venda = new VendaAvulsaEntity();
            venda.setProduto(p);
            venda.setQuantidade(qtd);
            venda.setProfissional(profissionalEntityLogado);
            vendaAvulsaService.registrarVenda(venda);

            JOptionPane.showMessageDialog(this,
                    "Venda registrada: " + qtd + "x " + p.getNome(),
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            // recarrega produtos para refletir novo estoque
            cbProduto.removeAllItems();
            produtoService.listarTodos().forEach(cbProduto::addItem);
            spQtd.setValue(1);
        });

        // monta o form
        gbc.gridy = 0; gbc.gridx = 0; gbc.gridwidth = 2;
        card.add(criarLabelCampo("PRODUTO"), gbc);

        gbc.gridy = 1;
        card.add(cbProduto, gbc);

        gbc.gridy = 2; gbc.gridwidth = 1;
        card.add(criarLabelCampo("QUANTIDADE"), gbc);

        gbc.gridx = 1;
        card.add(criarLabelCampo("TOTAL"), gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        card.add(spQtd, gbc);

        gbc.gridx = 1;
        card.add(lblTotal, gbc);

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 6, 4, 6);
        card.add(btnRegistrar, gbc);

        // centraliza o card na tela
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(24, 60, 24, 60));
        wrapper.add(card, new GridBagConstraints());
        painel.add(wrapper, BorderLayout.CENTER);
        return painel;
    }

    // card: Relatório do Dia
    private JPanel criarPainelRelatorio() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(AppColors.FUNDO_APP);

        JButton btnAtualizar = new JButton("↻  Atualizar");
        AppTheme.styleOutlineButton(btnAtualizar, false);
        painel.add(criarTopbar("Relatório do Dia", btnAtualizar), BorderLayout.NORTH);

        areaRelatorio = new JTextArea();
        areaRelatorio.setFont(new Font("Consolas", Font.PLAIN, 12));
        areaRelatorio.setForeground(AppColors.TEXTO_CORPO);
        areaRelatorio.setBackground(AppColors.FUNDO_CARD);
        areaRelatorio.setEditable(false);
        areaRelatorio.setMargin(new Insets(14, 16, 14, 16));

        JScrollPane scroll = new JScrollPane(areaRelatorio);
        scroll.setBorder(new LineBorder(AppColors.BORDA, 1, false));
        scroll.getViewport().setBackground(AppColors.FUNDO_CARD);

        JPanel conteudo = new JPanel(new BorderLayout());
        conteudo.setOpaque(false);
        conteudo.setBorder(new EmptyBorder(16, 20, 16, 20));
        conteudo.add(scroll, BorderLayout.CENTER);
        painel.add(conteudo, BorderLayout.CENTER);

        btnAtualizar.addActionListener(e -> carregarRelatorio());
        return painel;
    }

    // card: Estoque de Produtos (somente leitura, linhas baixas em vermelho)
    private JPanel criarPainelEstoque() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(AppColors.FUNDO_APP);
        painel.add(criarTopbar("Estoque de Produtos", null), BorderLayout.NORTH);

        String[] colunas = {"ID", "Nome", "Estoque", "Mínimo", "Preço Venda"};
        modelEstoque = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // renderizador que pinta baixo estoque em vermelho claro
        JTable tabela = new JTable(modelEstoque) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    int estoque = 0, minimo = 0;
                    try {
                        estoque = (Integer) modelEstoque.getValueAt(row, 2);
                        minimo  = (Integer) modelEstoque.getValueAt(row, 3);
                    } catch (Exception ignored) {}
                    c.setBackground(estoque <= minimo
                            ? new Color(0xFA, 0xDD, 0xDD)
                            : AppColors.FUNDO_CARD);
                }
                return c;
            }
        };

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

        int[] larguras = {50, 240, 80, 80, 130};
        for (int i = 0; i < larguras.length; i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }
        tabela.getColumnModel().getColumn(0).setMaxWidth(60);

        JPanel conteudo = new JPanel(new BorderLayout());
        conteudo.setOpaque(false);
        conteudo.setBorder(new EmptyBorder(16, 20, 16, 20));
        conteudo.add(embrulharTabela(tabela), BorderLayout.CENTER);
        painel.add(conteudo, BorderLayout.CENTER);
        return painel;
    }

    // ============================================================
    // DADOS
    // ============================================================

    private void carregarDados(String card) {
        switch (card) {
            case CARD_AGENDAMENTOS -> carregarAgendamentos();
            case CARD_RELATORIO    -> carregarRelatorio();
            case CARD_ESTOQUE      -> carregarEstoque();
        }
    }

    // filtra agendamentos do profissional logado
    private void carregarAgendamentos() {
        modelAgendamentos.setRowCount(0);
        List<AgendamentoEntity> todos = agendamentoService.listarTodos();
        for (AgendamentoEntity ag : todos) {
            if (ag.getProfissional() == null) continue;
            if (!ag.getProfissional().getId().equals(profissionalEntityLogado.getId())) continue;

            String inicio  = ag.getDataHoraInicio() != null ? ag.getDataHoraInicio().format(FMT) : "—";
            String fim     = ag.getDataHoraFim()    != null ? ag.getDataHoraFim().format(FMT)    : "—";
            String cliente = ag.getCliente()        != null ? ag.getCliente().getNome()           : "—";
            String valor   = ag.getValorFinal()     != null
                    ? String.format("R$ %.2f", ag.getValorFinal()) : "R$ 0,00";

            modelAgendamentos.addRow(new Object[]{
                    ag.getId(), ag.getStatus().name(), inicio, fim, cliente, valor
            });
        }
    }

    private void carregarRelatorio() {
        if (areaRelatorio == null) return;
        areaRelatorio.setText(montarRelatorioHoje());
        areaRelatorio.setCaretPosition(0);
    }

    private void carregarEstoque() {
        modelEstoque.setRowCount(0);
        for (ProdutoEntity p : produtoService.listarTodos()) {
            modelEstoque.addRow(new Object[]{
                    p.getId(),
                    p.getNome(),
                    p.getQuantidadeEstoque(),
                    p.getQuantidadeMinima(),
                    String.format("R$ %.2f", p.getPrecoVenda())
            });
        }
    }

    // monta o texto do relatório de vendas do dia para este profissional
    private String montarRelatorioHoje() {
        LocalDateTime inicio = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime fim    = inicio.plusDays(1);
        String hoje          = LocalDateTime.now().toLocalDate()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        List<VendaAvulsaEntity> vendas = vendaAvulsaService.listarTodas().stream()
                .filter(v -> v.getProfissional() != null
                        && v.getProfissional().getId().equals(profissionalEntityLogado.getId())
                        && !v.getDataVenda().isBefore(inicio)
                        && v.getDataVenda().isBefore(fim))
                .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("=== Relatório de Vendas — ").append(hoje).append(" ===\n\n");

        if (vendas.isEmpty()) {
            sb.append("Nenhuma venda registrada hoje.");
            return sb.toString();
        }

        BigDecimal totalGeral = BigDecimal.ZERO;
        for (VendaAvulsaEntity v : vendas) {
            sb.append(String.format("  [#%d] %s  |  %dx %-20s  |  R$ %8.2f  |  %s%n",
                    v.getId(),
                    v.getDataVenda().format(FMT_HR),
                    v.getQuantidade(),
                    v.getProduto().getNome(),
                    v.getTotal(),
                    v.isFechado() ? "FECHADO" : "ABERTO"));
            totalGeral = totalGeral.add(v.getTotal());
        }

        long abertas  = vendas.stream().filter(v -> !v.isFechado()).count();
        long fechadas = vendas.stream().filter(VendaAvulsaEntity::isFechado).count();

        sb.append("\n──────────────────────────────────────────────\n");
        sb.append(String.format("  Total do dia:       R$ %.2f%n", totalGeral));
        sb.append(String.format("  Vendas registradas: %d%n", vendas.size()));
        sb.append(String.format("  Em aberto: %d   |   Fechadas: %d%n", abertas, fechadas));
        return sb.toString();
    }

    // ações do crud

    private void concluirAgendamentoSelecionado() {
        int linha = tabelaAgendamentos.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um agendamento para concluir.",
                    "Nenhuma seleção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Long id = (Long) modelAgendamentos.getValueAt(linha, 0);
        agendamentoService.concluirAgendamento(id);
        carregarAgendamentos();
    }

    private void cancelarAgendamentoSelecionado() {
        int linha = tabelaAgendamentos.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um agendamento para cancelar.",
                    "Nenhuma seleção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Long id = (Long) modelAgendamentos.getValueAt(linha, 0);
        agendamentoService.cancelarAgendamento(id);
        carregarAgendamentos();
    }

    // confirmação e execução do fechamento do dia
    private void confirmarFecharDia() {
        int resp = JOptionPane.showConfirmDialog(this,
                "Deseja finalizar o dia?\nTodas as vendas abertas de hoje serão fechadas.",
                "Finalizar Dia",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            vendaAvulsaService.fecharDia(profissionalEntityLogado);
            JOptionPane.showMessageDialog(this,
                    "Dia finalizado com sucesso.",
                    "Dia Fechado", JOptionPane.INFORMATION_MESSAGE);
            if (cardAtivo.equals(CARD_RELATORIO)) carregarRelatorio();
        }
    }

    // ============================================================
    // NAVEGAÇÃO
    // ============================================================

    private void navegarPara(String card) {
        cardAtivo = card;
        cardLayout.show(painelCentral, card);
        atualizarEstadoBotoesNav(card);
        carregarDados(card);
    }

    private void atualizarEstadoBotoesNav(String ativo) {
        botoesNav.forEach((card, btn) -> aplicarEstiloNavAtivo(btn, card.equals(ativo)));
    }

    private void aplicarEstiloNavAtivo(JButton btn, boolean ativo) {
        if (btn == null) return;
        if (ativo) {
            btn.setBackground(new Color(112, 65, 107));
            btn.setForeground(Color.WHITE);
            btn.setBorder(new CompoundBorder(
                    new MatteBorder(0, 3, 0, 0, AppColors.DOURADO),
                    new EmptyBorder(9, 2, 9, 2)
            ));
        } else {
            btn.setBackground(AppColors.ROXO);
            btn.setForeground(new Color(255, 255, 255, 184));
            btn.setBorder(new CompoundBorder(
                    new MatteBorder(0, 3, 0, 0, AppColors.ROXO),
                    new EmptyBorder(9, 2, 9, 2)
            ));
        }
    }

    // ============================================================
    // AJUDA DE UI (mesmo padrão dos outros painéis)
    // ============================================================

    private JPanel criarTopbar(String titulo, JComponent acoes) {
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(AppColors.FUNDO_CARD);
        topbar.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDA, 1, false),
                new EmptyBorder(10, 20, 10, 20)
        ));
        topbar.setPreferredSize(new Dimension(0, AppDimensions.TOPBAR_HEIGHT));

        JLabel lbl = new JLabel(titulo);
        lbl.setFont(AppFonts.TITLE);
        lbl.setForeground(AppColors.TEXTO_TITULO);
        topbar.add(lbl, BorderLayout.WEST);

        if (acoes != null) topbar.add(acoes, BorderLayout.EAST);
        return topbar;
    }

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
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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

    private void aplicarRenderizadorStatus(JTable tabela) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setFont(AppFonts.TABLE);
                if (!sel) {
                    Object statusVal = t.getModel().getValueAt(row, 1);
                    String status = statusVal != null ? statusVal.toString() : "";
                    switch (status) {
                        case "CONCLUIDO" -> {
                            setBackground(COR_CONCLUIDO_BG);
                            setForeground(col == 1 ? AppColors.STATUS_CONC : AppColors.TEXTO_CORPO);
                        }
                        case "CANCELADO" -> {
                            setBackground(COR_CANCELADO_BG);
                            setForeground(col == 1 ? AppColors.PERIGO : AppColors.TEXTO_CORPO);
                        }
                        default -> {
                            setBackground(COR_PENDENTE_BG);
                            setForeground(col == 1 ? AppColors.STATUS_PEND : AppColors.TEXTO_CORPO);
                        }
                    }
                    if (col == 1 && value != null) setText(formatarStatus(value.toString()));
                }
                return this;
            }
        };
        for (int i = 0; i < 6; i++) tabela.getColumnModel().getColumn(i).setCellRenderer(renderer);
    }

    private JScrollPane embrulharTabela(JTable tabela) {
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(new LineBorder(AppColors.BORDA, 1, false));
        scroll.getViewport().setBackground(AppColors.FUNDO_CARD);
        return scroll;
    }

    private <T> void estilizarCombo(JComboBox<T> combo) {
        combo.setFont(AppFonts.BODY);
        combo.setBackground(AppColors.FUNDO_APP);
        combo.setForeground(AppColors.TEXTO_CORPO);
        combo.setBorder(new LineBorder(AppColors.BORDA, 1, false));
        combo.setPreferredSize(new Dimension(0, AppDimensions.INPUT_HEIGHT));
    }

    private void estilizarSpinner(JSpinner spinner) {
        spinner.setFont(AppFonts.BODY);
        spinner.setBorder(new LineBorder(AppColors.BORDA, 1, false));
        spinner.setPreferredSize(new Dimension(0, AppDimensions.INPUT_HEIGHT));
    }

    private JLabel criarLabelCampo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(AppFonts.LABEL);
        lbl.setForeground(AppColors.TEXTO_SECUNDARIO);
        return lbl;
    }

    private String formatarStatus(String status) {
        return switch (status) {
            case "CONCLUIDO" -> "Concluído";
            case "CANCELADO" -> "Cancelado";
            default          -> "Pendente";
        };
    }

    private JPanel criarBotaoSair() {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(6, 10, 10, 10));
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        JButton btn = new JButton("Sair");
        btn.setFont(AppFonts.BUTTON);
        btn.setForeground(new Color(255, 120, 120));
        btn.setBackground(AppColors.ROXO);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(7, 8, 7, 8));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(90, 40, 55)); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(AppColors.ROXO); }
        });
        btn.addActionListener(e -> {
            int resp = JOptionPane.showConfirmDialog(this,
                    "Deseja sair e voltar à tela de login?",
                    "Confirmar Saída", JOptionPane.YES_NO_OPTION);
            if (resp == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
        wrapper.add(btn);
        return wrapper;
    }

    private String extrairIniciais(String nome) {
        if (nome == null || nome.isBlank()) return "?";
        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 1) return partes[0].substring(0, 1).toUpperCase();
        return (partes[0].substring(0, 1) + partes[partes.length - 1].substring(0, 1)).toUpperCase();
    }

    private String abreviarNome(String nome) {
        if (nome == null || nome.isBlank()) return "";
        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 1) return partes[0];
        return partes[0] + " " + partes[partes.length - 1].substring(0, 1) + ".";
    }
}
