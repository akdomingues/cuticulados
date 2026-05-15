package org.cuticulados.pm.ui.panels;

import org.cuticulados.pm.entity.Profissional;
import org.cuticulados.pm.entity.TipoUsuario;
import org.cuticulados.pm.entity.Usuario;
import org.cuticulados.pm.service.UsuarioService;
import org.cuticulados.pm.ui.theme.AppColors;
import org.cuticulados.pm.ui.theme.AppDimensions;
import org.cuticulados.pm.ui.theme.AppFonts;
import org.cuticulados.pm.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Painel de gerenciamento de profissionais do perfil ADMIN.
 * Exibe tabela com profissionais ativos e permite cadastrar e remover (soft delete).
 */
public class PainelProfissionais extends JPanel {

    // services
    private final UsuarioService usuarioService;

    // componentes principais
    private JTable tabela;
    private DefaultTableModel tableModel;
    private JTextField campoBusca;

    // profissionais atuais
    private List<Usuario> profissionaisAtuais;

    // colunas
    private static final String[] COLUNAS = {"ID", "Nome", "Login", "Especialidade"};

    // painel de profissionais
    public PainelProfissionais() {
        this.usuarioService = new UsuarioService();
        setLayout(new BorderLayout());
        setBackground(AppColors.FUNDO_APP);
        inicializarComponentes();
        carregarDados();
    }

    // inicialização
    private void inicializarComponentes() {
        add(criarTopbar(), BorderLayout.NORTH);
        add(criarConteudo(), BorderLayout.CENTER);
    }

    private JPanel criarTopbar() {
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(AppColors.FUNDO_CARD);
        topbar.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDA, 1, false),
                new EmptyBorder(10, 20, 10, 20)
        ));
        topbar.setPreferredSize(new Dimension(0, AppDimensions.TOPBAR_HEIGHT));

        JLabel titulo = new JLabel("Profissionais");
        titulo.setFont(AppFonts.TITLE);
        titulo.setForeground(AppColors.TEXTO_TITULO);
        topbar.add(titulo, BorderLayout.WEST);
        topbar.add(criarBotoesAcao(), BorderLayout.EAST);
        return topbar;
    }

    private JPanel criarBotoesAcao() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        painel.setOpaque(false);

        JButton btnCadastrar = new JButton("+ Cadastrar");
        JButton btnRemover = new JButton("Remover");

        AppTheme.stylePrimaryButton(btnCadastrar, false);
        AppTheme.styleOutlineButton(btnRemover, false);
        btnRemover.setForeground(AppColors.PERIGO);

        btnCadastrar.addActionListener(e -> abrirDialogCadastro());
        btnRemover.addActionListener(e -> confirmarRemocao());

        painel.add(btnCadastrar);
        painel.add(btnRemover);
        return painel;
    }

    private JPanel criarConteudo() {
        JPanel conteudo = new JPanel(new BorderLayout(0, 12));
        conteudo.setOpaque(false);
        conteudo.setBorder(new EmptyBorder(16, 20, 16, 20));
        conteudo.add(criarBarraBusca(), BorderLayout.NORTH);
        conteudo.add(criarPainelTabela(), BorderLayout.CENTER);
        return conteudo;
    }

    private JPanel criarBarraBusca() {
        JPanel wrapper = new JPanel(new BorderLayout(6, 0));
        wrapper.setBackground(AppColors.FUNDO_CARD);
        wrapper.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDA, 1, false),
                new EmptyBorder(4, 10, 4, 10)
        ));
        wrapper.setPreferredSize(new Dimension(300, AppDimensions.INPUT_HEIGHT));

        JLabel icone = new JLabel("🔍");
        icone.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        icone.setForeground(AppColors.TEXTO_SECUNDARIO);

        campoBusca = new JTextField();
        campoBusca.setFont(AppFonts.BODY);
        campoBusca.setForeground(AppColors.TEXTO_CORPO);
        campoBusca.setBorder(BorderFactory.createEmptyBorder());
        campoBusca.setOpaque(false);
        campoBusca.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarTabela(campoBusca.getText().trim());
            }
        });

        wrapper.add(icone, BorderLayout.WEST);
        wrapper.add(campoBusca, BorderLayout.CENTER);

        JPanel alinhado = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        alinhado.setOpaque(false);
        alinhado.add(wrapper);
        return alinhado;
    }

    private JScrollPane criarPainelTabela() {
        tableModel = new DefaultTableModel(COLUNAS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tabela = new JTable(tableModel);
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

        estilizarCabecalho();
        aplicarRenderizador();
        configurarLarguras();

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(new LineBorder(AppColors.BORDA, 1, false));
        scroll.getViewport().setBackground(AppColors.FUNDO_CARD);
        return scroll;
    }

    private void estilizarCabecalho() {
        JTableHeader h = tabela.getTableHeader();
        h.setFont(AppFonts.TABLE_HEADER);
        h.setBackground(AppColors.ROSA_PALIDO);
        h.setForeground(AppColors.TEXTO_TITULO);
        h.setPreferredSize(new Dimension(0, 34));
        h.setBorder(new LineBorder(AppColors.BORDA, 1, false));
        h.setReorderingAllowed(false);
    }

    private void aplicarRenderizador() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setFont(AppFonts.TABLE);
                if (!sel) {
                    setBackground(row % 2 == 0
                            ? AppColors.FUNDO_CARD
                            : AppColors.FUNDO_LINHA_ALTERNADA);
                    setForeground(AppColors.TEXTO_CORPO);
                }
                return this;
            }
        };
        for (int i = 0; i < COLUNAS.length; i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(r);
        }
    }

    private void configurarLarguras() {
        int[] larguras = {50, 200, 140, 200};
        for (int i = 0; i < larguras.length; i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }
        tabela.getColumnModel().getColumn(0).setMaxWidth(60);
    }

    // dados
    private void carregarDados() {
        profissionaisAtuais = usuarioService.listarPorTipo(TipoUsuario.PROFISSIONAL)
                .stream().filter(u -> !u.isDeleted()).toList();
        preencherTabela(profissionaisAtuais);
    }

    private void preencherTabela(List<Usuario> lista) {
        tableModel.setRowCount(0);
        for (Usuario u : lista) {
            String especialidade = (u instanceof Profissional p) ? p.getEspecialidade() : "—";
            tableModel.addRow(new Object[]{u.getId(), u.getNome(), u.getLogin(), especialidade});
        }
    }

    private void filtrarTabela(String termo) {
        if (profissionaisAtuais == null) return;
        if (termo.isBlank()) {
            preencherTabela(profissionaisAtuais);
            return;
        }
        String low = termo.toLowerCase();
        List<Usuario> filtrados = profissionaisAtuais.stream()
                .filter(u -> u.getNome().toLowerCase().contains(low)
                        || u.getLogin().toLowerCase().contains(low)
                        || (u instanceof Profissional p
                        && p.getEspecialidade() != null
                        && p.getEspecialidade().toLowerCase().contains(low)))
                .toList();
        preencherTabela(filtrados);
    }

    // ações do crud
    private void abrirDialogCadastro() {
        JDialog dialog = criarDialogBase("Cadastrar Profissional", 490, 360);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 16, 8, 16));
        GridBagConstraints gbc = criarGbc();

        JTextField fNome = criarCampo();
        JTextField fEmail = criarCampo();
        JTextField fLogin = criarCampo();
        JPasswordField fSenha = new JPasswordField();
        estilizarCampo(fSenha);
        JTextField fEspecialidade = criarCampo();

        adicionarCampoForm(form, gbc, "Nome", fNome, 0, 0, false);
        adicionarCampoForm(form, gbc, "E-mail", fEmail, 0, 1, false);
        adicionarCampoForm(form, gbc, "Login", fLogin, 1, 0, false);
        adicionarCampoForm(form, gbc, "Senha", fSenha, 1, 1, false);
        adicionarCampoForm(form, gbc, "Especialidade", fEspecialidade, 2, 0, true);

        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");
        AppTheme.stylePrimaryButton(btnSalvar, true);
        AppTheme.styleOutlineButton(btnCancelar, true);

        btnCancelar.addActionListener(e -> dialog.dispose());
        btnSalvar.addActionListener(e -> {
            String nome = fNome.getText().trim();
            String email = fEmail.getText().trim();
            String login = fLogin.getText().trim();
            String senha = new String(fSenha.getPassword()).trim();
            String especialidade = fEspecialidade.getText().trim();

            if (nome.isBlank() || email.isBlank() || login.isBlank()
                    || senha.isBlank() || especialidade.isBlank()) {
                JOptionPane.showMessageDialog(dialog,
                        "Todos os campos são obrigatórios", "Campos inválidos",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Profissional p = new Profissional();
            p.setNome(nome);
            p.setEmail(email);
            p.setLogin(login);
            p.setSenha(senha);
            p.setEspecialidade(especialidade);
            p.setTipo(TipoUsuario.PROFISSIONAL);

            String erro = usuarioService.cadastrarUsuario(p);
            if (erro != null) {
                JOptionPane.showMessageDialog(dialog, erro, "Erro ao cadastrar", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dialog.dispose();
            carregarDados();
            JOptionPane.showMessageDialog(PainelProfissionais.this,
                    "Profissional \"" + nome + "\" cadastrado com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        });

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(criarRodapeDialog(btnCancelar, btnSalvar), BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void confirmarRemocao() {
        int linhaSel = tabela.getSelectedRow();
        if (linhaSel < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um profissional na tabela para remover.",
                    "Nenhuma seleção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String nome = (String) tableModel.getValueAt(linhaSel, 1);
        Long id = (Long) tableModel.getValueAt(linhaSel, 0);

        int resp = JOptionPane.showConfirmDialog(this,
                "Deseja remover o profissional \"" + nome + "\"?\n"
                        + "A remoção é lógica (soft delete) — o registro será inativado.",
                "Confirmar Remoção",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            String erro = usuarioService.removerUsuario(id);
            if (erro != null) {
                JOptionPane.showMessageDialog(this, erro, "Erro ao remover", JOptionPane.ERROR_MESSAGE);
                return;
            }
            carregarDados();
            JOptionPane.showMessageDialog(this,
                    "Profissional \"" + nome + "\" removido com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ajuda de ui (msm padrão de PainelClientes)
    private JDialog criarDialogBase(String titulo, int largura, int altura) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = (owner instanceof Frame)
                ? new JDialog((Frame) owner, titulo, true)
                : new JDialog((Dialog) owner, titulo, true);

        dialog.setLayout(new BorderLayout());
        dialog.setSize(largura, altura);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.getContentPane().setBackground(AppColors.FUNDO_CARD);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppColors.ROXO);
        header.setBorder(new EmptyBorder(12, 16, 12, 16));
        header.setPreferredSize(new Dimension(0, 44));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(AppFonts.TITLE);
        lblTitulo.setForeground(Color.WHITE);
        header.add(lblTitulo, BorderLayout.WEST);
        dialog.add(header, BorderLayout.NORTH);

        return dialog;
    }

    private JTextField criarCampo() {
        JTextField f = new JTextField();
        estilizarCampo(f);
        return f;
    }

    private void estilizarCampo(JTextField f) {
        f.setFont(AppFonts.BODY);
        f.setForeground(AppColors.TEXTO_CORPO);
        f.setBackground(AppColors.FUNDO_APP);
        f.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDA, 1, false),
                new EmptyBorder(5, 8, 5, 8)
        ));
        f.setPreferredSize(new Dimension(0, AppDimensions.INPUT_HEIGHT));
    }

    private GridBagConstraints criarGbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 6, 4, 6);
        g.weightx = 1.0;
        return g;
    }

    private void adicionarCampoForm(JPanel form, GridBagConstraints gbc,
                                    String label, JComponent campo,
                                    int linha, int col, boolean full) {
        gbc.gridy = linha * 2;
        gbc.gridx = full ? 0 : col;
        gbc.gridwidth = full ? 2 : 1;

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(AppFonts.LABEL);
        lbl.setForeground(AppColors.TEXTO_SECUNDARIO);
        form.add(lbl, gbc);

        gbc.gridy = linha * 2 + 1;
        form.add(campo, gbc);
        gbc.gridwidth = 1;
    }

    private JPanel criarRodapeDialog(JButton btnCancelar, JButton btnSalvar) {
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        rodape.setBackground(AppColors.BOTAO_OUTLINE_FUNDO);
        rodape.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDA, 1, false),
                new EmptyBorder(2, 8, 2, 8)
        ));
        rodape.add(btnCancelar);
        rodape.add(btnSalvar);
        return rodape;
    }
}
