package org.cuticulados.pm.ui.panels;

import org.cuticulados.pm.entity.Cliente;
import org.cuticulados.pm.entity.TipoUsuario;
import org.cuticulados.pm.service.ClienteService;
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
 * Painel de gerenciamento de clientes do perfil ADMIN.
 * Exibe tabela com todos os clientes e permite cadastrar, atualizar e remover.
 */
public class PainelClientes extends JPanel {

    // services
    private final ClienteService clienteService;

    // componentes principais
    private JTable tabela;
    private DefaultTableModel tableModel;
    private JTextField campoBusca;

    // lista atual
    private List<Cliente> clientesAtuais;

    // nomes das colunas
    private static final String[] COLUNAS = {
            "ID", "Nome", "CPF", "Telefone", "Tipo", "Atend/mês"
    };

    // painel cliente
    public PainelClientes() {
        this.clienteService = new ClienteService();
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

    // topbar com titulo
    private JPanel criarTopbar() {
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(AppColors.FUNDO_CARD);
        topbar.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDA, 1, false),
                new EmptyBorder(10, 20, 10, 20)
        ));
        topbar.setPreferredSize(new Dimension(0, AppDimensions.TOPBAR_HEIGHT));

        JLabel titulo = new JLabel("Clientes");
        titulo.setFont(AppFonts.TITLE);
        titulo.setForeground(AppColors.TEXTO_TITULO);
        topbar.add(titulo, BorderLayout.WEST);

        topbar.add(criarBotoesAcao(), BorderLayout.EAST);
        return topbar;
    }

    // painel com os botões cadastrar / atualizar / remover
    private JPanel criarBotoesAcao() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        painel.setOpaque(false);

        JButton btnCadastrar = new JButton("+ Cadastrar");
        JButton btnAtualizar = new JButton("Atualizar");
        JButton btnRemover = new JButton("Remover");

        AppTheme.stylePrimaryButton(btnCadastrar, false);
        AppTheme.styleOutlineButton(btnAtualizar, false);
        AppTheme.styleOutlineButton(btnRemover, false);

        btnRemover.setForeground(AppColors.PERIGO);

        btnCadastrar.addActionListener(e -> abrirDialogCadastro());
        btnAtualizar.addActionListener(e -> abrirDialogAtualizacao());
        btnRemover.addActionListener(e -> confirmarRemocao());

        painel.add(btnCadastrar);
        painel.add(btnAtualizar);
        painel.add(btnRemover);
        return painel;
    }

    // barra de busca e tabela
    private JPanel criarConteudo() {
        JPanel conteudo = new JPanel(new BorderLayout(0, 12));
        conteudo.setOpaque(false);
        conteudo.setBorder(new EmptyBorder(16, 20, 16, 20));

        conteudo.add(criarBarraBusca(), BorderLayout.NORTH);
        conteudo.add(criarPainelTabela(), BorderLayout.CENTER);
        return conteudo;
    }

    // campo de busca e filtro
    private JPanel criarBarraBusca() {
        JPanel painel = new JPanel(new BorderLayout(6, 0));
        painel.setOpaque(false);
        painel.setMaximumSize(new Dimension(320, AppDimensions.INPUT_HEIGHT));

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

    // cria o modelo da tabela e o JTable no estilo
    private JScrollPane criarPainelTabela() {
        tableModel = new DefaultTableModel(COLUNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
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

        estilizarCabecalhoTabela();
        aplicarRenderizadorLinhas();
        configurarLargurasColunas();

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(new LineBorder(AppColors.BORDA, 1, false));
        scroll.getViewport().setBackground(AppColors.FUNDO_CARD);
        return scroll;
    }

    private void estilizarCabecalhoTabela() {
        JTableHeader header = tabela.getTableHeader();
        header.setFont(AppFonts.TABLE_HEADER);
        header.setBackground(AppColors.ROSA_PALIDO);
        header.setForeground(AppColors.TEXTO_TITULO);
        header.setPreferredSize(new Dimension(0, 34));
        header.setBorder(new LineBorder(AppColors.BORDA, 1, false));
        header.setReorderingAllowed(false);
    }

    private void aplicarRenderizadorLinhas() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean selected, boolean focused, int row, int col) {

                super.getTableCellRendererComponent(t, value, selected, focused, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setFont(AppFonts.TABLE);

                if (!selected) {
                    setBackground(row % 2 == 0
                            ? AppColors.FUNDO_CARD
                            : AppColors.FUNDO_LINHA_ALTERNADA);
                    setForeground(AppColors.TEXTO_CORPO);
                }

                // coluna do tipo
                if (col == 4 && value != null) {
                    String tipo = value.toString().toLowerCase();
                    if ("frequente".equals(tipo)) {
                        setForeground(selected ? AppColors.TEXTO_TITULO : AppColors.BADGE_FREQ_FRENTE);
                        if (!selected) setBackground(AppColors.BADGE_FREQ_FUNDO);
                    } else {
                        setForeground(selected ? AppColors.TEXTO_TITULO : AppColors.BADGE_NOVO_FRENTE);
                        if (!selected) setBackground(AppColors.BADGE_NOVO_FUNDO);
                    }
                }
                return this;
            }
        };

        for (int i = 0; i < COLUNAS.length; i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private void configurarLargurasColunas() {
        int[] larguras = {50, 200, 130, 120, 90, 80};
        for (int i = 0; i < larguras.length; i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }
        tabela.getColumnModel().getColumn(0).setMaxWidth(60);
    }

    // dados
    // recarrega a lista completa do banco e atualiza a tabela
    private void carregarDados() {
        clientesAtuais = clienteService.listarTodos();
        preencherTabela(clientesAtuais);
    }

    private void preencherTabela(List<Cliente> clientes) {
        tableModel.setRowCount(0);
        for (Cliente c : clientes) {
            tableModel.addRow(new Object[]{
                    c.getId(),
                    c.getNome(),
                    c.getCpf(),
                    c.getTelefone(),
                    c.getTipoCliente(),
                    c.getTotalAtendimentosMes()
            });
        }
    }

    private void filtrarTabela(String termo) {
        if (clientesAtuais == null) return;
        if (termo.isBlank()) {
            preencherTabela(clientesAtuais);
            return;
        }
        String low = termo.toLowerCase();
        List<Cliente> filtrados = clientesAtuais.stream()
                .filter(c -> c.getNome().toLowerCase().contains(low)
                        || c.getCpf().contains(low)
                        || (c.getTelefone() != null && c.getTelefone().contains(low)))
                .toList();
        preencherTabela(filtrados);
    }

    // ações do crud
    private void abrirDialogCadastro() {
        JDialog dialog = criarDialogBase("Cadastrar Cliente", 490, 380);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 16, 8, 16));
        GridBagConstraints gbc = criarGbc();

        JTextField fNome = criarCampo();
        JTextField fEmail = criarCampo();
        JTextField fLogin = criarCampo();
        JPasswordField fSenha = new JPasswordField();
        estilizarCampo(fSenha);
        JTextField fCpf = criarCampo();
        JTextField fTelefone = criarCampo();

        adicionarCampoForm(form, gbc, "Nome", fNome, 0, 0, false);
        adicionarCampoForm(form, gbc, "E-mail", fEmail, 0, 1, false);
        adicionarCampoForm(form, gbc, "Login", fLogin, 1, 0, false);
        adicionarCampoForm(form, gbc, "Senha", fSenha, 1, 1, false);
        adicionarCampoForm(form, gbc, "CPF", fCpf, 2, 0, false);
        adicionarCampoForm(form, gbc, "Telefone", fTelefone, 2, 1, false);

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
            String cpf = fCpf.getText().trim();
            String telefone = fTelefone.getText().trim();

            if (nome.isBlank() || email.isBlank() || login.isBlank()
                    || senha.isBlank() || cpf.isBlank() || telefone.isBlank()) {
                JOptionPane.showMessageDialog(dialog,
                        "Todos os campos são obrigatórios.", "Campos inválidos",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Cliente c = new Cliente();
            c.setNome(nome);
            c.setEmail(email);
            c.setLogin(login);
            c.setSenha(senha);
            c.setCpf(cpf);
            c.setTelefone(telefone);
            c.setTipo(TipoUsuario.CLIENTE);

            clienteService.cadastrarCliente(c);
            dialog.dispose();
            carregarDados();
        });

        JPanel rodape = criarRodapeDialog(btnCancelar, btnSalvar);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(rodape, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void abrirDialogAtualizacao() {
        int linhaSel = tabela.getSelectedRow();
        if (linhaSel < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um cliente na tabela para atualizar.",
                    "Nenhuma seleção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Long id = (Long) tableModel.getValueAt(linhaSel, 0);
        Cliente c = clienteService.buscarPorId(id).orElse(null);
        if (c == null) {
            JOptionPane.showMessageDialog(this,
                    "Cliente não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = criarDialogBase("Atualizar Cliente", 490, 280);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 16, 8, 16));
        GridBagConstraints gbc = criarGbc();

        JTextField fNome = criarCampo();
        JTextField fTelefone = criarCampo();
        JTextField fEmail = criarCampo();

        fNome.setText(c.getNome());
        fTelefone.setText(c.getTelefone());
        fEmail.setText(c.getEmail());

        adicionarCampoForm(form, gbc, "Nome", fNome, 0, 0, false);
        adicionarCampoForm(form, gbc, "Telefone", fTelefone, 0, 1, false);
        adicionarCampoForm(form, gbc, "E-mail", fEmail, 1, 0, true);

        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");
        AppTheme.stylePrimaryButton(btnSalvar, true);
        AppTheme.styleOutlineButton(btnCancelar, true);

        btnCancelar.addActionListener(e -> dialog.dispose());
        btnSalvar.addActionListener(e -> {
            String nome = fNome.getText().trim();
            String telefone = fTelefone.getText().trim();
            String email = fEmail.getText().trim();

            if (nome.isBlank() || telefone.isBlank() || email.isBlank()) {
                JOptionPane.showMessageDialog(dialog,
                        "Nome, Telefone e E-mail são obrigatório.",
                        "Campo inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            c.setNome(nome);
            c.setTelefone(telefone);
            c.setEmail(email);
            clienteService.atualizarCliente(c);
            dialog.dispose();
            carregarDados();
        });

        JPanel rodape = criarRodapeDialog(btnCancelar, btnSalvar);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(rodape, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void confirmarRemocao() {
        int linhaSel = tabela.getSelectedRow();
        if (linhaSel < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um cliente na tabela para remover.",
                    "Nenhuma seleção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String nome = (String) tableModel.getValueAt(linhaSel, 1);
        Long id = (Long) tableModel.getValueAt(linhaSel, 0);

        int resp = JOptionPane.showConfirmDialog(this,
                "Deseja remover o cliente \"" + nome + "\"?\nEsta ação não pode ser desfeita.",
                "Confirmar Remoção",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            clienteService.removerCliente(id);
            carregarDados();
        }
    }

    // ajuda de ui

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

        // header roxo
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

    /**
     * Adiciona um par label + campo ao formulário em GridBagLayout.
     *
     * @param form  painel alvo
     * @param gbc   constraints reutilizável
     * @param label texto do rótulo
     * @param campo componente de entrada
     * @param linha linha lógica (par de linhas label+campo = linha*2)
     * @param col   0 ou 1
     * @param full  true para ocupar as 2 colunas (grid-column: 1/-1)
     */
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
