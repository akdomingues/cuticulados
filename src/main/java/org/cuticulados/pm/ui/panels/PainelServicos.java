package org.cuticulados.pm.ui.panels;

import org.cuticulados.pm.entity.Servico;
import org.cuticulados.pm.service.ServicoService;
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
import java.math.BigDecimal;
import java.util.List;

/**
 * Painel de gerenciamento de serviços do perfil ADMIN.
 * Exibe tabela com todos os serviços e permite cadastrar, atualizar e remover.
 */
public class PainelServicos extends JPanel {

    // services
    private final ServicoService servicoService;

    // componentes principais
    private JTable tabela;
    private DefaultTableModel tableModel;
    private JTextField campoBusca;

    // lista atual
    private List<Servico> servicosAtuais;

    // colunas
    private static final String[] COLUNAS = {"ID", "Descrição", "Valor Base (R$)", "Duração (min)"};

    // painel servicos
    public PainelServicos() {
        this.servicoService = new ServicoService();
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

    // topbar com titulo e botões
    private JPanel criarTopbar() {
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(AppColors.FUNDO_CARD);
        topbar.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDA, 1, false),
                new EmptyBorder(10, 20, 10, 20)
        ));
        topbar.setPreferredSize(new Dimension(0, AppDimensions.TOPBAR_HEIGHT));

        JLabel titulo = new JLabel("Serviços");
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
        JButton btnRemover   = new JButton("Remover");

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

    // campo de busca com filtro em tempo real
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
                // coluna de valor em roxo claro
                if (col == 2 && !selected) setForeground(AppColors.ROXO_CLARO);
                return this;
            }
        };
        for (int i = 0; i < COLUNAS.length; i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private void configurarLargurasColunas() {
        int[] larguras = {50, 280, 150, 120};
        for (int i = 0; i < larguras.length; i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }
        tabela.getColumnModel().getColumn(0).setMaxWidth(60);
    }

    // dados
    // recarrega a lista completa do banco e atualiza a tabela
    private void carregarDados() {
        servicosAtuais = servicoService.listarTodos();
        preencherTabela(servicosAtuais);
    }

    private void preencherTabela(List<Servico> lista) {
        tableModel.setRowCount(0);
        for (Servico s : lista) {
            tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getDescricao(),
                    String.format("R$ %.2f", s.getValorBase()),
                    s.getDuracaoMinutos() + " min"
            });
        }
    }

    private void filtrarTabela(String termo) {
        if (servicosAtuais == null) return;
        if (termo.isBlank()) {
            preencherTabela(servicosAtuais);
            return;
        }
        String low = termo.toLowerCase();
        List<Servico> filtrados = servicosAtuais.stream()
                .filter(s -> s.getDescricao().toLowerCase().contains(low))
                .toList();
        preencherTabela(filtrados);
    }

    // ações do crud
    private void abrirDialogCadastro() {
        JDialog dialog = criarDialogBase("Cadastrar Serviço", 460, 310);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 16, 8, 16));
        GridBagConstraints gbc = criarGbc();

        JTextField fDescricao = criarCampo();

        SpinnerNumberModel modelValor = new SpinnerNumberModel(0.0, 0.0, 99999.99, 0.5);
        JSpinner fValor = criarSpinner(modelValor);

        SpinnerNumberModel modelDuracao = new SpinnerNumberModel(30, 1, 9999, 5);
        JSpinner fDuracao = criarSpinner(modelDuracao);

        adicionarCampoForm(form, gbc, "Descrição", fDescricao, 0, 0, true);
        adicionarCampoForm(form, gbc, "Valor Base (R$)", fValor, 1, 0, false);
        adicionarCampoForm(form, gbc, "Duração (min)", fDuracao, 1, 1, false);

        JButton btnSalvar   = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");
        AppTheme.stylePrimaryButton(btnSalvar, true);
        AppTheme.styleOutlineButton(btnCancelar, true);

        btnCancelar.addActionListener(e -> dialog.dispose());
        btnSalvar.addActionListener(e -> {
            String descricao = fDescricao.getText().trim();
            if (descricao.isBlank()) {
                JOptionPane.showMessageDialog(dialog,
                        "A descrição é obrigatória.", "Campo inválido",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Servico s = new Servico();
            s.setDescricao(descricao);
            s.setValorBase(BigDecimal.valueOf(((Number) fValor.getValue()).doubleValue()));
            s.setDuracaoMinutos(((Number) fDuracao.getValue()).intValue());
            String erro = servicoService.cadastrarServico(s);
            if (erro != null) {
                JOptionPane.showMessageDialog(dialog, erro, "Erro ao cadastrar", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dialog.dispose();
            carregarDados();
            JOptionPane.showMessageDialog(PainelServicos.this,
                    "Serviço \"" + descricao + "\" cadastrado com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        });

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(criarRodapeDialog(btnCancelar, btnSalvar), BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void abrirDialogAtualizacao() {
        int linhaSel = tabela.getSelectedRow();
        if (linhaSel < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um serviço na tabela para atualizar.",
                    "Nenhuma seleção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Long id = (Long) tableModel.getValueAt(linhaSel, 0);
        Servico s = servicoService.buscarPorId(id).orElse(null);
        if (s == null) {
            JOptionPane.showMessageDialog(this,
                    "Serviço não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = criarDialogBase("Atualizar Serviço", 460, 310);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 16, 8, 16));
        GridBagConstraints gbc = criarGbc();

        JTextField fDescricao = criarCampo();
        fDescricao.setText(s.getDescricao());

        SpinnerNumberModel modelValor = new SpinnerNumberModel(
                s.getValorBase().doubleValue(), 0.0, 99999.99, 0.5);
        JSpinner fValor = criarSpinner(modelValor);

        SpinnerNumberModel modelDuracao = new SpinnerNumberModel(
                s.getDuracaoMinutos().intValue(), 1, 9999, 5);
        JSpinner fDuracao = criarSpinner(modelDuracao);

        adicionarCampoForm(form, gbc, "Descrição", fDescricao, 0, 0, true);
        adicionarCampoForm(form, gbc, "Valor Base (R$)", fValor, 1, 0, false);
        adicionarCampoForm(form, gbc, "Duração (min)", fDuracao, 1, 1, false);

        JButton btnSalvar   = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");
        AppTheme.stylePrimaryButton(btnSalvar, true);
        AppTheme.styleOutlineButton(btnCancelar, true);

        btnCancelar.addActionListener(e -> dialog.dispose());
        btnSalvar.addActionListener(e -> {
            String descricao = fDescricao.getText().trim();
            if (descricao.isBlank()) {
                JOptionPane.showMessageDialog(dialog,
                        "A descrição é obrigatória.", "Campo inválido",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            s.setDescricao(descricao);
            s.setValorBase(BigDecimal.valueOf(((Number) fValor.getValue()).doubleValue()));
            s.setDuracaoMinutos(((Number) fDuracao.getValue()).intValue());
            String erro = servicoService.atualizarServico(s);
            if (erro != null) {
                JOptionPane.showMessageDialog(dialog, erro, "Erro ao atualizar", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dialog.dispose();
            carregarDados();
            JOptionPane.showMessageDialog(PainelServicos.this,
                    "Serviço atualizado com sucesso!",
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
                    "Selecione um serviço na tabela para remover.",
                    "Nenhuma seleção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String descricao = (String) tableModel.getValueAt(linhaSel, 1);
        Long id = (Long) tableModel.getValueAt(linhaSel, 0);

        int resp = JOptionPane.showConfirmDialog(this,
                "Deseja remover o serviço \"" + descricao + "\"?\nEsta ação não pode ser desfeita.",
                "Confirmar Remoção",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            String erro = servicoService.removerServico(id);
            if (erro != null) {
                JOptionPane.showMessageDialog(this, erro, "Erro ao remover", JOptionPane.ERROR_MESSAGE);
                return;
            }
            carregarDados();
            JOptionPane.showMessageDialog(this,
                    "Serviço \"" + descricao + "\" removido com sucesso!",
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
        f.setFont(AppFonts.BODY);
        f.setForeground(AppColors.TEXTO_CORPO);
        f.setBackground(AppColors.FUNDO_APP);
        f.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDA, 1, false),
                new EmptyBorder(5, 8, 5, 8)
        ));
        f.setPreferredSize(new Dimension(0, AppDimensions.INPUT_HEIGHT));
        return f;
    }

    private JSpinner criarSpinner(SpinnerModel model) {
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(AppFonts.BODY);
        spinner.setPreferredSize(new Dimension(0, AppDimensions.INPUT_HEIGHT));
        spinner.setBorder(new LineBorder(AppColors.BORDA, 1, false));
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
        editor.getTextField().setFont(AppFonts.BODY);
        editor.getTextField().setForeground(AppColors.TEXTO_CORPO);
        editor.getTextField().setBackground(AppColors.FUNDO_APP);
        editor.getTextField().setBorder(new EmptyBorder(4, 6, 4, 6));
        return spinner;
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
