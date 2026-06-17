package org.cuticulados.pm.ui.panels;

import org.cuticulados.pm.entity.ProdutoEntity;
import org.cuticulados.pm.controller.produto.ProdutoController;
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
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.List;

/**
 * Painel de gerenciamento de produtos do perfil ADMIN.
 * Exibe tabela com estoque; linhas com estoque ≤ mínimo aparecem em vermelho claro.
 */
public class PainelProdutos extends JPanel {

    // services
    private final ProdutoController produtoController;

    // componentes principais
    private JTable tabela;
    private DefaultTableModel tableModel;
    private JTextField campoBusca;

    // lista atual
    private List<ProdutoEntity> produtosAtuais;

    // colunas
    private static final String[] COLUNAS = {"ID", "Nome", "Estoque", "Mínimo", "Preço Custo", "Preço Venda"};

    // cores de alerta de estoque baixo
    private static final Color COR_ESTOQUE_BAIXO    = new Color(0xFA, 0xDD, 0xDD);
    private static final Color COR_ESTOQUE_BAIXO_FG = new Color(0xA0, 0x30, 0x30);

    // painel produtos
    public PainelProdutos() {
        this.produtoController = new ProdutoController();
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

        JLabel titulo = new JLabel("Produtos");
        titulo.setFont(AppFonts.TITLE);
        titulo.setForeground(AppColors.TEXTO_TITULO);
        topbar.add(titulo, BorderLayout.WEST);

        topbar.add(criarBotoesAcao(), BorderLayout.EAST);
        return topbar;
    }

    // painel com os botões cadastrar / atualizar / remover / alertas
    private JPanel criarBotoesAcao() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        painel.setOpaque(false);

        JButton btnCadastrar = new JButton("+ Cadastrar");
        JButton btnAtualizar = new JButton("Atualizar");
        JButton btnRemover   = new JButton("Remover");
        JButton btnAlertas   = new JButton("⚠ Estoque Baixo");

        AppTheme.stylePrimaryButton(btnCadastrar, false);
        AppTheme.styleOutlineButton(btnAtualizar, false);
        AppTheme.styleOutlineButton(btnRemover, false);
        AppTheme.styleOutlineButton(btnAlertas, false);
        btnRemover.setForeground(AppColors.PERIGO);
        btnAlertas.setForeground(AppColors.STATUS_PEND);

        btnCadastrar.addActionListener(e -> abrirDialogCadastro());
        btnAtualizar.addActionListener(e -> abrirDialogAtualizacao());
        btnRemover.addActionListener(e -> confirmarRemocao());
        btnAlertas.addActionListener(e -> abrirDialogAlertas());

        painel.add(btnCadastrar);
        painel.add(btnAtualizar);
        painel.add(btnRemover);
        painel.add(btnAlertas);
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

    // cria tabela com prepareRenderer para colorir linhas com estoque baixo
    private JScrollPane criarPainelTabela() {
        tableModel = new DefaultTableModel(COLUNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        // sobrescreve prepareRenderer para destacar estoque baixo em vermelho
        tabela = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    Object estoqueVal = getModel().getValueAt(row, 2);
                    Object minVal     = getModel().getValueAt(row, 3);
                    if (estoqueVal instanceof Integer estoque
                            && minVal instanceof Integer min
                            && estoque <= min) {
                        c.setBackground(COR_ESTOQUE_BAIXO);
                        c.setForeground(COR_ESTOQUE_BAIXO_FG);
                    }
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
                // preços em roxo claro
                if ((col == 4 || col == 5) && !selected) setForeground(AppColors.ROXO_CLARO);
                return this;
            }
        };
        for (int i = 0; i < COLUNAS.length; i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private void configurarLargurasColunas() {
        int[] larguras = {50, 200, 80, 80, 120, 120};
        for (int i = 0; i < larguras.length; i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }
        tabela.getColumnModel().getColumn(0).setMaxWidth(60);
    }

    // dados
    // recarrega a lista completa do banco e atualiza a tabela
    private void carregarDados() {
        produtosAtuais = produtoController.listarTodos();
        preencherTabela(produtosAtuais);
    }

    private void preencherTabela(List<ProdutoEntity> lista) {
        tableModel.setRowCount(0);
        for (ProdutoEntity p : lista) {
            tableModel.addRow(new Object[]{
                    p.getId(),
                    p.getNome(),
                    p.getQuantidadeEstoque(),   // Integer — usado pelo prepareRenderer
                    p.getQuantidadeMinima(),     // Integer — usado pelo prepareRenderer
                    String.format("R$ %.2f", p.getPrecoCusto()),
                    String.format("R$ %.2f", p.getPrecoVenda())
            });
        }
    }

    private void filtrarTabela(String termo) {
        if (produtosAtuais == null) return;
        if (termo.isBlank()) {
            preencherTabela(produtosAtuais);
            return;
        }
        String low = termo.toLowerCase();
        List<ProdutoEntity> filtrados = produtosAtuais.stream()
                .filter(p -> p.getNome().toLowerCase().contains(low))
                .toList();
        preencherTabela(filtrados);
    }

    // ações do crud
    private void abrirDialogCadastro() {
        JDialog dialog = criarDialogBase("Cadastrar Produto", 490, 370);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 16, 8, 16));
        GridBagConstraints gbc = criarGbc();

        JTextField fNome = criarCampo();

        SpinnerNumberModel modelCusto  = new SpinnerNumberModel(0.0, 0.0, 99999.99, 0.5);
        SpinnerNumberModel modelVenda  = new SpinnerNumberModel(0.0, 0.0, 99999.99, 0.5);
        SpinnerNumberModel modelEstoq  = new SpinnerNumberModel(0, 0, 99999, 1);
        SpinnerNumberModel modelMinimo = new SpinnerNumberModel(0, 0, 99999, 1);

        JSpinner fCusto  = criarSpinner(modelCusto);
        JSpinner fVenda  = criarSpinner(modelVenda);
        JSpinner fEstoq  = criarSpinner(modelEstoq);
        JSpinner fMinimo = criarSpinner(modelMinimo);

        adicionarCampoForm(form, gbc, "Nome", fNome, 0, 0, true);
        adicionarCampoForm(form, gbc, "Preço Custo (R$)", fCusto, 1, 0, false);
        adicionarCampoForm(form, gbc, "Preço Venda (R$)", fVenda, 1, 1, false);
        adicionarCampoForm(form, gbc, "Qtd Estoque", fEstoq, 2, 0, false);
        adicionarCampoForm(form, gbc, "Qtd Mínima", fMinimo, 2, 1, false);

        JButton btnSalvar   = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");
        AppTheme.stylePrimaryButton(btnSalvar, true);
        AppTheme.styleOutlineButton(btnCancelar, true);

        btnCancelar.addActionListener(e -> dialog.dispose());
        btnSalvar.addActionListener(e -> {
            String nome = fNome.getText().trim();
            if (nome.isBlank()) {
                JOptionPane.showMessageDialog(dialog,
                        "O nome é obrigatório.", "Campo inválido",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            ProdutoEntity p = new ProdutoEntity();
            p.setNome(nome);
            p.setPrecoCusto(BigDecimal.valueOf(((Number) fCusto.getValue()).doubleValue()));
            p.setPrecoVenda(BigDecimal.valueOf(((Number) fVenda.getValue()).doubleValue()));
            p.setQuantidadeEstoque(((Number) fEstoq.getValue()).intValue());
            p.setQuantidadeMinima(((Number) fMinimo.getValue()).intValue());
            String erro = produtoController.cadastrarProduto(p);
            if (erro != null) {
                JOptionPane.showMessageDialog(dialog, erro, "Erro ao cadastrar", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dialog.dispose();
            carregarDados();
            JOptionPane.showMessageDialog(PainelProdutos.this,
                    "Produto \"" + nome + "\" cadastrado com sucesso!",
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
                    "Selecione um produto na tabela para atualizar.",
                    "Nenhuma seleção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Long id = (Long) tableModel.getValueAt(linhaSel, 0);
        ProdutoEntity p = produtoController.buscarPorId(id).orElse(null);
        if (p == null) {
            JOptionPane.showMessageDialog(this,
                    "Produto não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = criarDialogBase("Atualizar Produto", 490, 260);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 16, 8, 16));
        GridBagConstraints gbc = criarGbc();

        SpinnerNumberModel modelVenda  = new SpinnerNumberModel(
                p.getPrecoVenda().doubleValue(), 0.0, 99999.99, 0.5);
        SpinnerNumberModel modelEstoq  = new SpinnerNumberModel(
                p.getQuantidadeEstoque().intValue(), 0, 99999, 1);

        JSpinner fVenda = criarSpinner(modelVenda);
        JSpinner fEstoq = criarSpinner(modelEstoq);

        adicionarCampoForm(form, gbc, "Preço Venda (R$)", fVenda, 0, 0, false);
        adicionarCampoForm(form, gbc, "Qtd Estoque", fEstoq, 0, 1, false);

        JButton btnSalvar   = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");
        AppTheme.stylePrimaryButton(btnSalvar, true);
        AppTheme.styleOutlineButton(btnCancelar, true);

        btnCancelar.addActionListener(e -> dialog.dispose());
        btnSalvar.addActionListener(e -> {
            p.setPrecoVenda(BigDecimal.valueOf(((Number) fVenda.getValue()).doubleValue()));
            p.setQuantidadeEstoque(((Number) fEstoq.getValue()).intValue());
            String erro = produtoController.atualizarProduto(p);
            if (erro != null) {
                JOptionPane.showMessageDialog(dialog, erro, "Erro ao atualizar", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dialog.dispose();
            carregarDados();
            JOptionPane.showMessageDialog(PainelProdutos.this,
                    "Produto atualizado com sucesso!",
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
                    "Selecione um produto na tabela para remover.",
                    "Nenhuma seleção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String nome = (String) tableModel.getValueAt(linhaSel, 1);
        Long id = (Long) tableModel.getValueAt(linhaSel, 0);

        int resp = JOptionPane.showConfirmDialog(this,
                "Deseja remover o produto \"" + nome + "\"?\nEsta ação não pode ser desfeita.",
                "Confirmar Remoção",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            String erro = produtoController.removerProduto(id);
            if (erro != null) {
                JOptionPane.showMessageDialog(this, erro, "Erro ao remover", JOptionPane.ERROR_MESSAGE);
                return;
            }
            carregarDados();
            JOptionPane.showMessageDialog(this,
                    "Produto \"" + nome + "\" removido com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // abre dialog listando apenas produtos abaixo do estoque mínimo
    private void abrirDialogAlertas() {
        List<ProdutoEntity> baixo = produtoController.verificarEstoqueBaixo();

        JDialog dialog = criarDialogBase("Alertas de Estoque Baixo", 500, 380);

        if (baixo.isEmpty()) {
            JLabel lblOk = new JLabel("Nenhum produto com estoque abaixo do mínimo.", SwingConstants.CENTER);
            lblOk.setFont(AppFonts.BODY);
            lblOk.setForeground(AppColors.STATUS_CONC);
            lblOk.setBorder(new EmptyBorder(24, 0, 0, 0));
            dialog.add(lblOk, BorderLayout.CENTER);
        } else {
            String[] colAlerta = {"Nome", "Estoque", "Mínimo"};
            DefaultTableModel alertModel = new DefaultTableModel(colAlerta, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            for (ProdutoEntity p : baixo) {
                alertModel.addRow(new Object[]{
                        p.getNome(), p.getQuantidadeEstoque(), p.getQuantidadeMinima()
                });
            }

            JTable tabelaAlerta = new JTable(alertModel);
            tabelaAlerta.setFont(AppFonts.TABLE);
            tabelaAlerta.setRowHeight(AppDimensions.ROW_HEIGHT);
            tabelaAlerta.setShowVerticalLines(false);
            tabelaAlerta.setBackground(AppColors.FUNDO_CARD);
            tabelaAlerta.getTableHeader().setBackground(AppColors.ROSA_PALIDO);
            tabelaAlerta.getTableHeader().setFont(AppFonts.TABLE_HEADER);

            JScrollPane scroll = new JScrollPane(tabelaAlerta);
            scroll.setBorder(new EmptyBorder(12, 16, 12, 16));
            dialog.add(scroll, BorderLayout.CENTER);
        }

        JButton btnFechar = new JButton("Fechar");
        AppTheme.styleOutlineButton(btnFechar, true);
        btnFechar.addActionListener(e -> dialog.dispose());

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        rodape.setBackground(AppColors.BOTAO_OUTLINE_FUNDO);
        rodape.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDA, 1, false),
                new EmptyBorder(2, 8, 2, 8)
        ));
        rodape.add(btnFechar);
        dialog.add(rodape, BorderLayout.SOUTH);
        dialog.setVisible(true);
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
