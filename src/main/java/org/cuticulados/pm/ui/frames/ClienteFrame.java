package org.cuticulados.pm.ui.frames;

import org.cuticulados.pm.entity.Agendamento;
import org.cuticulados.pm.entity.Cliente;
import org.cuticulados.pm.entity.Servico;
import org.cuticulados.pm.service.AgendamentoService;
import org.cuticulados.pm.service.ServicoService;
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
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Janela principal do perfil CLIENTE.
 * Exibe sidebar de navegação, topbar com dados do usuário e área central
 * que alterna entre "Meus Agendamentos" e "Serviços Disponíveis".
 */
public class ClienteFrame extends JFrame {

    // services
    private final AgendamentoService agendamentoService;
    private final ServicoService     servicoService;

    // dados do usuário logado
    private final Cliente clienteLogado;

    // layout central
    private JPanel painelCentral;
    private CardLayout cardLayout;

    // referências dos nav buttons para destacar ativo
    private JButton btnNavAgendamentos;
    private JButton btnNavServicos;

    // cards
    private static final String CARD_AGENDAMENTOS = "agendamentos";
    private static final String CARD_SERVICOS     = "servicos";

    // formatar data e hora
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // cliente frame
    public ClienteFrame(Cliente cliente) {
        this.clienteLogado      = cliente;
        this.agendamentoService = new AgendamentoService();
        this.servicoService     = new ServicoService();

        configurarJanela();
        inicializarLayout();
        navegarPara(CARD_AGENDAMENTOS);
    }

    // configuração de tamanho

    private void configurarJanela() {
        setTitle("NailGestor — Área do Cliente");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 620);
        setMinimumSize(new Dimension(780, 520));
        setLocationRelativeTo(null);
        getContentPane().setBackground(AppColors.FUNDO_APP);
    }

    // layout principal do

    private void inicializarLayout() {
        setLayout(new BorderLayout());
        add(criarSidebar(),     BorderLayout.WEST);
        add(criarAreaPrincipal(), BorderLayout.CENTER);
    }

    // sidebar

    private JPanel criarSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(AppColors.ROXO);
        sidebar.setPreferredSize(new Dimension(AppDimensions.SIDEBAR_WIDTH, 0));

        sidebar.add(criarSidebarLogo());
        sidebar.add(criarSidebarSeparador());
        sidebar.add(criarSidebarNavTitulo("ÁREA DO CLIENTE"));
        sidebar.add(criarNavButton("Meus Agendamentos", CARD_AGENDAMENTOS, true));
        sidebar.add(criarNavButton("Serviços",           CARD_SERVICOS,     false));
        sidebar.add(Box.createVerticalGlue());
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

        JLabel iconeLogo = new JLabel("💅");
        iconeLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        iconeLogo.setForeground(AppColors.DOURADO);
        iconeLogo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nome = new JLabel("NailGestor");
        nome.setFont(AppFonts.LOGO);
        nome.setForeground(Color.WHITE);
        nome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Sistema de Gestão");
        sub.setFont(AppFonts.SMALL);
        sub.setForeground(new Color(255, 255, 255, 128));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        logo.add(iconeLogo);
        logo.add(Box.createVerticalStrut(4));
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

    /**
     * Cria um botão de navegação da sidebar.
     * Armazena referência em campo para poder alterar estado "ativo" posteriormente.
     */
    private JButton criarNavButton(String texto, String card, boolean isAgendamentos) {
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
                new EmptyBorder(9, 13, 9, 16)
        ));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setRolloverEnabled(false);

        // hover: cor opaca pré-calculada (blend 8% branco sobre ROXO) evita artefato de alpha no Swing
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!card.equals(cardAtivo())) btn.setBackground(new Color(105, 56, 100));
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!card.equals(cardAtivo())) btn.setBackground(AppColors.ROXO);
            }
        });

        btn.addActionListener(e -> navegarPara(card));

        if (isAgendamentos) {
            btnNavAgendamentos = btn;
        } else {
            btnNavServicos = btn;
        }

        return btn;
    }

    private String cardAtivo() {
        // fala qual card ta ativo pela aparência dos botões
        if (btnNavAgendamentos != null
                && !btnNavAgendamentos.getBackground().equals(AppColors.ROXO)
                && !btnNavAgendamentos.getBackground().equals(new Color(255, 255, 255, 20))) {
            return CARD_AGENDAMENTOS;
        }
        return CARD_SERVICOS;
    }

    private JPanel criarSidebarUsuario() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        painel.setOpaque(false);

        // avatarzinho em circulo com a inciial do nome
        JPanel avatar = criarAvatar(clienteLogado.getNome());

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel lblNome = new JLabel(abreviarNome(clienteLogado.getNome()));
        lblNome.setFont(AppFonts.SMALL);
        lblNome.setForeground(new Color(255, 255, 255, 218));

        JLabel lblPerfil = new JLabel("Cliente");
        lblPerfil.setFont(new Font(AppFonts.FAMILY, Font.PLAIN, 10));
        lblPerfil.setForeground(new Color(255, 255, 255, 102));

        info.add(lblNome);
        info.add(lblPerfil);

        painel.add(avatar);
        painel.add(info);
        return painel;
    }

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

    // area principal com topbar e cards
    private JPanel criarAreaPrincipal() {
        JPanel area = new JPanel(new BorderLayout());
        area.setBackground(AppColors.FUNDO_APP);

        cardLayout   = new CardLayout();
        painelCentral = new JPanel(cardLayout);
        painelCentral.setBackground(AppColors.FUNDO_APP);
        painelCentral.add(criarPainelAgendamentos(), CARD_AGENDAMENTOS);
        painelCentral.add(criarPainelServicos(),     CARD_SERVICOS);

        area.add(painelCentral, BorderLayout.CENTER);
        return area;
    }

    // card dos meus agendamentos
    private JPanel criarPainelAgendamentos() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(AppColors.FUNDO_APP);
        painel.add(criarTopbarCard("Meus Agendamentos"), BorderLayout.NORTH);

        // tabela de agendamentos do cliente logado
        String[] colunas = {"Serviço", "Profissional", "Data", "Horário", "Status"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tabela = criarTabela(model);
        aplicarRenderizadorAgendamentos(tabela, model);

        // btn de atualizar
        JPanel conteudo = new JPanel(new BorderLayout(0, 12));
        conteudo.setOpaque(false);
        conteudo.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel barraSuperior = new JPanel(new BorderLayout());
        barraSuperior.setOpaque(false);

        JButton btnAtualizar = new JButton("↻  Atualizar");
        AppTheme.styleOutlineButton(btnAtualizar, true);
        btnAtualizar.addActionListener(e -> carregarAgendamentos(model));

        barraSuperior.add(btnAtualizar, BorderLayout.EAST);

        JScrollPane scroll = embrulharTabela(tabela);
        conteudo.add(barraSuperior, BorderLayout.NORTH);
        conteudo.add(scroll, BorderLayout.CENTER);
        painel.add(conteudo, BorderLayout.CENTER);

        // carga inicial feita ao navegar
        painel.putClientProperty("modelAgendamentos", model);
        return painel;
    }

    private void carregarAgendamentos(DefaultTableModel model) {
        model.setRowCount(0);
        List<Agendamento> todos = agendamentoService.listarTodos();
        for (Agendamento ag : todos) {
            if (ag.getCliente() == null) continue;
            if (!ag.getCliente().getId().equals(clienteLogado.getId())) continue;

            String servicos = ag.getServicos().stream()
                    .map(as -> as.getServico().getDescricao())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("—");

            String profNome = ag.getProfissional() != null
                    ? ag.getProfissional().getNome() : "—";

            String data    = ag.getDataHoraInicio() != null
                    ? ag.getDataHoraInicio().toLocalDate().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—";
            String horario = ag.getDataHoraInicio() != null
                    ? ag.getDataHoraInicio().toLocalTime().format(
                        DateTimeFormatter.ofPattern("HH:mm")) : "—";

            model.addRow(new Object[]{
                    servicos, profNome, data, horario, ag.getStatus().name()
            });
        }
    }

    private void aplicarRenderizadorAgendamentos(JTable tabela, DefaultTableModel model) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setFont(AppFonts.TABLE);

                if (!sel) {
                    setBackground(row % 2 == 0
                            ? AppColors.FUNDO_CARD : AppColors.FUNDO_LINHA_ALTERNADA);
                    setForeground(AppColors.TEXTO_CORPO);
                }

                // coluna Status com cor
                if (col == 4 && val != null) {
                    String status = val.toString();
                    if (!sel) {
                        switch (status) {
                            case "CONCLUIDO" -> {
                                setForeground(AppColors.STATUS_CONC);
                                setBackground(new Color(0xE3, 0xF0, 0xDC));
                            }
                            case "CANCELADO" -> {
                                setForeground(AppColors.PERIGO);
                                setBackground(new Color(0xFA, 0xDD, 0xDD));
                            }
                            default -> {
                                setForeground(AppColors.STATUS_PEND);
                                setBackground(new Color(0xFF, 0xF3, 0xD6));
                            }
                        }
                    }
                    setText(formatarStatus(status));
                }
                return this;
            }
        };
        for (int i = 0; i < 5; i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(r);
        }
        // larguras
        int[] larg = {220, 160, 100, 70, 100};
        for (int i = 0; i < larg.length; i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(larg[i]);
        }
    }

    // card dos serviços disponíveis
    private JPanel criarPainelServicos() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(AppColors.FUNDO_APP);
        painel.add(criarTopbarCard("Serviços Disponíveis"), BorderLayout.NORTH);

        String[] colunas = {"Descrição", "Valor Base (R$)", "Duração (min)"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tabela = criarTabela(model);
        aplicarRenderizadorServicos(tabela);

        JPanel conteudo = new JPanel(new BorderLayout(0, 12));
        conteudo.setOpaque(false);
        conteudo.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel barraSuperior = new JPanel(new BorderLayout());
        barraSuperior.setOpaque(false);

        JButton btnAtualizar = new JButton("↻  Atualizar");
        AppTheme.styleOutlineButton(btnAtualizar, true);
        btnAtualizar.addActionListener(e -> carregarServicos(model));

        barraSuperior.add(btnAtualizar, BorderLayout.EAST);
        conteudo.add(barraSuperior, BorderLayout.NORTH);
        conteudo.add(embrulharTabela(tabela), BorderLayout.CENTER);
        painel.add(conteudo, BorderLayout.CENTER);
        painel.putClientProperty("modelServicos", model);
        return painel;
    }

    private void carregarServicos(DefaultTableModel model) {
        model.setRowCount(0);
        for (Servico s : servicoService.listarTodos()) {
            model.addRow(new Object[]{
                    s.getDescricao(),
                    String.format("R$ %.2f", s.getValorBase()),
                    s.getDuracaoMinutos() + " min"
            });
        }
    }

    private void aplicarRenderizadorServicos(JTable tabela) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setFont(AppFonts.TABLE);
                if (!sel) {
                    setBackground(row % 2 == 0
                            ? AppColors.FUNDO_CARD : AppColors.FUNDO_LINHA_ALTERNADA);
                    setForeground(AppColors.TEXTO_CORPO);
                }
                if (col == 1 && !sel) setForeground(AppColors.ROXO_CLARO);
                return this;
            }
        };
        int[] larg = {280, 130, 100};
        for (int i = 0; i < 3; i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(r);
            tabela.getColumnModel().getColumn(i).setPreferredWidth(larg[i]);
        }
    }

    // navegação
    private void navegarPara(String card) {
        cardLayout.show(painelCentral, card);
        atualizarEstadoBotoesNav(card);
        carregarDadosCard(card);
    }

    private void atualizarEstadoBotoesNav(String cardAtivo) {
        aplicarEstiloNavAtivo(btnNavAgendamentos, cardAtivo.equals(CARD_AGENDAMENTOS));
        aplicarEstiloNavAtivo(btnNavServicos,     cardAtivo.equals(CARD_SERVICOS));
    }

    private void aplicarEstiloNavAtivo(JButton btn, boolean ativo) {
        if (btn == null) return;
        if (ativo) {
            btn.setBackground(new Color(112, 65, 107));
            btn.setForeground(Color.WHITE);
            btn.setBorder(new CompoundBorder(
                    new MatteBorder(0, 3, 0, 0, AppColors.DOURADO),
                    new EmptyBorder(9, 13, 9, 16)
            ));
        } else {
            btn.setBackground(AppColors.ROXO);
            btn.setForeground(new Color(255, 255, 255, 184));
            btn.setBorder(new CompoundBorder(
                    new MatteBorder(0, 3, 0, 0, AppColors.ROXO),
                    new EmptyBorder(9, 13, 9, 16)
            ));
        }
    }

    @SuppressWarnings("unchecked")
    private void carregarDadosCard(String card) {
        for (Component comp : painelCentral.getComponents()) {
            if (!(comp instanceof JPanel p)) continue;

            Object modelAg  = p.getClientProperty("modelAgendamentos");
            Object modelSv  = p.getClientProperty("modelServicos");

            if (card.equals(CARD_AGENDAMENTOS) && modelAg instanceof DefaultTableModel m) {
                carregarAgendamentos(m);
            }
            if (card.equals(CARD_SERVICOS) && modelSv instanceof DefaultTableModel m) {
                carregarServicos(m);
            }
        }
    }

    // ajuda de ui
    private JPanel criarTopbarCard(String titulo) {
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

        // badge com nome do cliente
        JLabel badge = new JLabel(clienteLogado.getNome());
        badge.setFont(AppFonts.SMALL);
        badge.setForeground(AppColors.BADGE_FREQ_FRENTE);
        badge.setOpaque(true);
        badge.setBackground(AppColors.BADGE_FREQ_FUNDO);
        badge.setBorder(new EmptyBorder(3, 10, 3, 10));
        topbar.add(badge, BorderLayout.EAST);

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

    private JScrollPane embrulharTabela(JTable tabela) {
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(new LineBorder(AppColors.BORDA, 1, false));
        scroll.getViewport().setBackground(AppColors.FUNDO_CARD);
        return scroll;
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

    // utilitários de ui
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

    private String formatarStatus(String status) {
        return switch (status) {
            case "CONCLUIDO" -> "Concluído";
            case "CANCELADO" -> "Cancelado";
            default          -> "Pendente";
        };
    }
}
