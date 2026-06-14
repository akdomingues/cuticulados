package org.cuticulados.pm.ui.frames;

import org.cuticulados.pm.entity.UsuarioEntity;
import org.cuticulados.pm.ui.panels.PainelAgendamentos;
import org.cuticulados.pm.ui.panels.PainelClientes;
import org.cuticulados.pm.ui.panels.PainelProdutos;
import org.cuticulados.pm.ui.panels.PainelProfissionais;
import org.cuticulados.pm.ui.panels.PainelRelatorios;
import org.cuticulados.pm.ui.panels.PainelServicos;
import org.cuticulados.pm.ui.panels.PainelVendasAvulsas;
import org.cuticulados.pm.ui.theme.AppColors;
import org.cuticulados.pm.ui.theme.AppDimensions;
import org.cuticulados.pm.ui.theme.AppFonts;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Janela principal do perfil ADMIN.
 * Sidebar de navegação à esquerda e CardLayout no centro para alternar entre painéis.
 */
public class MainFrame extends JFrame {

    // dados do admin logado
    private final UsuarioEntity adminLogado;

    // layout central
    private JPanel painelCentral;
    private CardLayout cardLayout;

    // mapa card → botão de nav para controlar estado ativo
    private final Map<String, JButton> botoesNav = new HashMap<>();
    private String cardAtivo = CARD_CLIENTES;

    // identificadores dos cards
    private static final String CARD_CLIENTES      = "clientes";
    private static final String CARD_PROFISSIONAIS = "profissionais";
    private static final String CARD_SERVICOS      = "servicos";
    private static final String CARD_PRODUTOS      = "produtos";
    private static final String CARD_AGENDAMENTOS  = "agendamentos";
    private static final String CARD_VENDAS        = "vendas";
    private static final String CARD_RELATORIOS    = "relatorios";

    // main frame do admin
    public MainFrame(UsuarioEntity admin) {
        this.adminLogado = admin;
        configurarJanela();
        inicializarLayout();
        navegarPara(CARD_CLIENTES);
    }

    // configuração de tamanho e título
    private void configurarJanela() {
        setTitle("NailGestor — Gestão");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 680);
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(null);
        getContentPane().setBackground(AppColors.FUNDO_APP);
    }

    // layout principal: sidebar | área central | statusbar
    private void inicializarLayout() {
        setLayout(new BorderLayout());
        add(criarSidebar(),      BorderLayout.WEST);
        add(criarAreaPrincipal(), BorderLayout.CENTER);
        add(criarStatusBar(),    BorderLayout.SOUTH);
    }

    // sidebar roxa com logo, seções de nav e info do usuário
    private JPanel criarSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(AppColors.ROXO);
        sidebar.setPreferredSize(new Dimension(AppDimensions.SIDEBAR_WIDTH, 0));

        sidebar.add(criarSidebarLogo());
        sidebar.add(criarSidebarSeparador());

        // seção gestão
        sidebar.add(criarSidebarNavTitulo("GESTÃO"));
        sidebar.add(criarNavButton("Clientes",      CARD_CLIENTES));
        sidebar.add(criarNavButton("Profissionais", CARD_PROFISSIONAIS));
        sidebar.add(criarNavButton("Serviços",      CARD_SERVICOS));
        sidebar.add(criarNavButton("Produtos",      CARD_PRODUTOS));

        sidebar.add(criarSidebarSeparador());

        // seção operações
        sidebar.add(criarSidebarNavTitulo("OPERAÇÕES"));
        sidebar.add(criarNavButton("Agendamentos",  CARD_AGENDAMENTOS));
        sidebar.add(criarNavButton("Vendas Avulsas",CARD_VENDAS));

        sidebar.add(criarSidebarSeparador());

        // seção análise
        sidebar.add(criarSidebarNavTitulo("ANÁLISE"));
        sidebar.add(criarNavButton("Relatórios",    CARD_RELATORIOS));

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
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nome = new JLabel("NailGestor");
        nome.setFont(new Font(AppFonts.FAMILY, Font.BOLD, 22));
        nome.setForeground(Color.WHITE);
        nome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Painel Admin");
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

    /**
     * Cria um botão de navegação e armazena sua referência no mapa botoesNav.
     */
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

        // hover: cor opaca pré-calculada (blend 8% branco sobre ROXO) evita artefato de alpha no Swing
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

    // bloco de informações do usuário no rodapé da sidebar
    private JPanel criarSidebarUsuario() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        painel.setOpaque(false);
        painel.setBorder(new EmptyBorder(2, 2, 2, 2));

        JPanel avatar = criarAvatar(adminLogado.getNome());

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel lblNome = new JLabel(abreviarNome(adminLogado.getNome()));
        lblNome.setFont(AppFonts.SMALL);
        lblNome.setForeground(new Color(255, 255, 255, 218));

        JLabel lblPerfil = new JLabel("Administrador");
        lblPerfil.setFont(new Font(AppFonts.FAMILY, Font.PLAIN, 10));
        lblPerfil.setForeground(new Color(255, 255, 255, 102));

        info.add(lblNome);
        info.add(lblPerfil);

        painel.add(avatar);
        painel.add(info);
        return painel;
    }

    // círculo com as iniciais do nome (msm padrão do ClienteFrame)
    private JPanel criarAvatar(String nome) {
        String iniciais = extrairIniciais(nome);
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.DOURADO);
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

    // área principal com CardLayout e todos os painéis
    private JPanel criarAreaPrincipal() {
        JPanel area = new JPanel(new BorderLayout());
        area.setBackground(AppColors.FUNDO_APP);

        cardLayout    = new CardLayout();
        painelCentral = new JPanel(cardLayout);
        painelCentral.setBackground(AppColors.FUNDO_APP);

        // adiciona cada painel ao CardLayout
        painelCentral.add(new PainelClientes(),       CARD_CLIENTES);
        painelCentral.add(new PainelProfissionais(),   CARD_PROFISSIONAIS);
        painelCentral.add(new PainelServicos(),        CARD_SERVICOS);
        painelCentral.add(new PainelProdutos(),        CARD_PRODUTOS);
        painelCentral.add(new PainelAgendamentos(),    CARD_AGENDAMENTOS);
        painelCentral.add(new PainelVendasAvulsas(),   CARD_VENDAS);
        painelCentral.add(new PainelRelatorios(),      CARD_RELATORIOS);

        area.add(painelCentral, BorderLayout.CENTER);
        return area;
    }

    // barra de status inferior com nome do usuário logado
    private JPanel criarStatusBar() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(AppColors.ROXO_ESCURO);
        barra.setBorder(new EmptyBorder(4, 16, 4, 16));
        barra.setPreferredSize(new Dimension(0, 30));

        JLabel lblUsuario = new JLabel("Logado como: " + adminLogado.getNome()
                + "  ·  " + adminLogado.getLogin());
        lblUsuario.setFont(new Font(AppFonts.FAMILY, Font.PLAIN, 11));
        lblUsuario.setForeground(new Color(255, 255, 255, 160));
        barra.add(lblUsuario, BorderLayout.WEST);

        JLabel lblSistema = new JLabel("NailGestor — Sistema de Gestão");
        lblSistema.setFont(new Font(AppFonts.FAMILY, Font.PLAIN, 11));
        lblSistema.setForeground(new Color(255, 255, 255, 80));
        barra.add(lblSistema, BorderLayout.EAST);

        return barra;
    }

    // navegação
    private void navegarPara(String card) {
        cardAtivo = card;
        cardLayout.show(painelCentral, card);
        atualizarEstadoBotoesNav(card);
    }

    private void atualizarEstadoBotoesNav(String cardAtivo) {
        botoesNav.forEach((card, btn) ->
                aplicarEstiloNavAtivo(btn, card.equals(cardAtivo)));
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
}
