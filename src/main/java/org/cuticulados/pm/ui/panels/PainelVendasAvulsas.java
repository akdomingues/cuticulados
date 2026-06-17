package org.cuticulados.pm.ui.panels;

import org.cuticulados.pm.entity.ProdutoEntity;
import org.cuticulados.pm.entity.ProfissionalEntity;
import org.cuticulados.pm.entity.TipoUsuario;
import org.cuticulados.pm.entity.UsuarioEntity;
import org.cuticulados.pm.entity.VendaAvulsaEntity;
import org.cuticulados.pm.controller.produto.ProdutoController;
import org.cuticulados.pm.controller.usuario.UsuarioController;
import org.cuticulados.pm.controller.venda.VendaAvulsaController;
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
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Painel de vendas avulsas do perfil ADMIN.
 * Exibe tabela com todas as vendas e permite registrar e remover.
 * Linhas com venda fechada aparecem em cinza claro.
 */
public class PainelVendasAvulsas extends JPanel {

    // services
    private final VendaAvulsaController vendaAvulsaController;
    private final ProdutoController     produtoController;
    private final UsuarioController     usuarioController;

    // componentes principais
    private JTable tabela;
    private DefaultTableModel tableModel;

    // lista atual
    private List<VendaAvulsaEntity> vendasAtuais;

    // formato de data exibido na tabela
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // cor de fundo para vendas fechadas
    private static final Color COR_FECHADO_BG = new Color(0xE8, 0xE8, 0xE8);
    private static final Color COR_FECHADO_FG = new Color(0x88, 0x88, 0x88);

    // colunas
    private static final String[] COLUNAS = {"ID", "Data", "Produto", "Qtd", "Total (R$)", "Status"};

    // painel vendas avulsas
    public PainelVendasAvulsas() {
        this.vendaAvulsaController = new VendaAvulsaController();
        this.produtoController     = new ProdutoController();
        this.usuarioController     = new UsuarioController();
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

        JLabel titulo = new JLabel("Vendas Avulsas");
        titulo.setFont(AppFonts.TITLE);
        titulo.setForeground(AppColors.TEXTO_TITULO);
        topbar.add(titulo, BorderLayout.WEST);

        topbar.add(criarBotoesAcao(), BorderLayout.EAST);
        return topbar;
    }

    // painel com botões registrar / fechar venda / remover
    private JPanel criarBotoesAcao() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        painel.setOpaque(false);

        JButton btnRegistrar = new JButton("+ Registrar Venda");
        JButton btnFechar    = new JButton("Fechar Venda");
        JButton btnRemover   = new JButton("Remover");

        AppTheme.stylePrimaryButton(btnRegistrar, false);
        AppTheme.styleOutlineButton(btnFechar, false);
        AppTheme.styleOutlineButton(btnRemover, false);
        btnFechar.setForeground(AppColors.DOURADO);
        btnRemover.setForeground(AppColors.PERIGO);

        btnRegistrar.addActionListener(e -> abrirDialogRegistro());
        btnFechar.addActionListener(e -> fecharVendaSelecionada());
        btnRemover.addActionListener(e -> confirmarRemocao());

        painel.add(btnRegistrar);
        painel.add(btnFechar);
        painel.add(btnRemover);
        return painel;
    }

    // área central com tabela
    private JPanel criarConteudo() {
        JPanel conteudo = new JPanel(new BorderLayout(0, 12));
        conteudo.setOpaque(false);
        conteudo.setBorder(new EmptyBorder(16, 20, 16, 20));
        conteudo.add(criarPainelTabela(), BorderLayout.CENTER);
        return conteudo;
    }

    // cria o modelo da tabela com renderizador para vendas fechadas
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

    // renderizador que aplica cinza nas linhas com venda fechada (col 5 == "FECHADO")
    private void aplicarRenderizadorLinhas() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean selected, boolean focused, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focused, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setFont(AppFonts.TABLE);

                if (!selected) {
                    Object statusVal = t.getModel().getValueAt(row, 5);
                    boolean fechado = "FECHADO".equals(statusVal);

                    if (fechado) {
                        setBackground(COR_FECHADO_BG);
                        setForeground(COR_FECHADO_FG);
                    } else {
                        setBackground(row % 2 == 0
                                ? AppColors.FUNDO_CARD
                                : AppColors.FUNDO_LINHA_ALTERNADA);
                        setForeground(AppColors.TEXTO_CORPO);
                        // coluna de total em roxo claro para vendas abertas
                        if (col == 4) setForeground(AppColors.ROXO_CLARO);
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
        int[] larguras = {50, 140, 180, 60, 110, 90};
        for (int i = 0; i < larguras.length; i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }
        tabela.getColumnModel().getColumn(0).setMaxWidth(60);
    }

    // dados
    // recarrega todas as vendas e preenche a tabela
    private void carregarDados() {
        vendasAtuais = vendaAvulsaController.listarTodas();
        preencherTabela(vendasAtuais);
    }

    private void preencherTabela(List<VendaAvulsaEntity> lista) {
        tableModel.setRowCount(0);
        for (VendaAvulsaEntity v : lista) {
            String data    = v.getDataVenda() != null ? v.getDataVenda().format(FMT) : "—";
            String produto = v.getProduto() != null ? v.getProduto().getNome() : "—";
            String total   = v.getTotal() != null
                    ? String.format("R$ %.2f", v.getTotal()) : "—";
            String status  = v.isFechado() ? "FECHADO" : "ABERTO";

            tableModel.addRow(new Object[]{
                    v.getId(), data, produto, v.getQuantidade(), total, status
            });
        }
    }

    // ações do crud

    private void fecharVendaSelecionada() {
        int linhaSel = tabela.getSelectedRow();
        if (linhaSel < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma venda na tabela para fechar.",
                    "Nenhuma seleção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String status = (String) tableModel.getValueAt(linhaSel, 5);
        if ("FECHADO".equals(status)) {
            JOptionPane.showMessageDialog(this,
                    "Esta venda já está fechada.",
                    "Venda já fechada", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Long id = (Long) tableModel.getValueAt(linhaSel, 0);
        int resp = JOptionPane.showConfirmDialog(this,
                "Deseja fechar a venda #" + id + "?\nUma venda fechada não pode ser reaberta.",
                "Confirmar Fechamento",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            String erro = vendaAvulsaController.fecharVenda(id);
            if (erro != null) {
                JOptionPane.showMessageDialog(this, erro, "Erro ao fechar venda", JOptionPane.ERROR_MESSAGE);
                return;
            }
            carregarDados();
            JOptionPane.showMessageDialog(this,
                    "Venda #" + id + " fechada com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void confirmarRemocao() {
        int linhaSel = tabela.getSelectedRow();
        if (linhaSel < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma venda na tabela para remover.",
                    "Nenhuma seleção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Long id = (Long) tableModel.getValueAt(linhaSel, 0);
        int resp = JOptionPane.showConfirmDialog(this,
                "Deseja remover a venda #" + id + "?\nEsta ação não pode ser desfeita.",
                "Confirmar Remoção",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            vendaAvulsaController.removerVenda(id);
            carregarDados();
            JOptionPane.showMessageDialog(this,
                    "Venda #" + id + " removida com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // dialog de registro de nova venda avulsa
    private void abrirDialogRegistro() {
        JDialog dialog = criarDialogBase("Registrar Venda Avulsa", 460, 340);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 16, 8, 16));
        GridBagConstraints gbc = criarGbc();

        // combobox de produtos com nome e estoque disponível
        List<ProdutoEntity> produtoEntities = produtoController.listarTodos();
        JComboBox<ProdutoEntity> cbProduto = new JComboBox<>();
        produtoEntities.forEach(cbProduto::addItem);
        estilizarCombo(cbProduto);
        cbProduto.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            String texto = value != null
                    ? value.getNome() + " (estoque: " + value.getQuantidadeEstoque() + ")"
                    : "";
            JLabel lbl = new JLabel(texto);
            lbl.setFont(AppFonts.BODY);
            lbl.setBorder(new EmptyBorder(4, 8, 4, 8));
            if (isSelected) { lbl.setBackground(AppColors.ROSA_PALIDO); lbl.setOpaque(true); }
            return lbl;
        });

        // combobox de profissionais ativos
        List<UsuarioEntity> profissionais = usuarioController.listarPorTipo(TipoUsuario.PROFISSIONAL)
                .stream().filter(u -> !u.isDeleted()).toList();
        JComboBox<UsuarioEntity> cbProfissional = new JComboBox<>();
        profissionais.forEach(cbProfissional::addItem);
        estilizarCombo(cbProfissional);
        cbProfissional.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value != null ? value.getNome() : "");
            lbl.setFont(AppFonts.BODY);
            lbl.setBorder(new EmptyBorder(4, 8, 4, 8));
            if (isSelected) { lbl.setBackground(AppColors.ROSA_PALIDO); lbl.setOpaque(true); }
            return lbl;
        });

        // spinner de quantidade — máximo é o estoque do produto selecionado
        ProdutoEntity produtoEntityAtual = (ProdutoEntity) cbProduto.getSelectedItem();
        int estoqueMax = produtoEntityAtual != null ? produtoEntityAtual.getQuantidadeEstoque() : 1;
        SpinnerNumberModel modelQtd = new SpinnerNumberModel(1, 1, Math.max(1, estoqueMax), 1);
        JSpinner spQtd = criarSpinner(modelQtd);

        // label com total calculado em tempo real
        JLabel lblTotal = new JLabel("Total: R$ 0,00");
        lblTotal.setFont(new Font(AppFonts.FAMILY, Font.BOLD, 13));
        lblTotal.setForeground(AppColors.ROXO_CLARO);

        // atualiza estoque máximo e total ao trocar produto
        cbProduto.addActionListener(e -> atualizarTotal(cbProduto, spQtd, modelQtd, lblTotal));

        // atualiza total ao mudar quantidade
        spQtd.addChangeListener(e -> atualizarTotal(cbProduto, spQtd, modelQtd, lblTotal));

        atualizarTotal(cbProduto, spQtd, modelQtd, lblTotal);

        adicionarCampoForm(form, gbc, "Produto",      cbProduto,     0, 0, true);
        adicionarCampoForm(form, gbc, "Profissional", cbProfissional, 1, 0, false);
        adicionarCampoForm(form, gbc, "Quantidade",   spQtd,         1, 1, false);

        // linha do total
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(8, 6, 4, 6);
        form.add(lblTotal, gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(4, 6, 4, 6);

        JButton btnConfirmar = new JButton("Confirmar");
        JButton btnCancelar  = new JButton("Cancelar");
        AppTheme.stylePrimaryButton(btnConfirmar, true);
        AppTheme.styleOutlineButton(btnCancelar, true);

        btnCancelar.addActionListener(e -> dialog.dispose());
        btnConfirmar.addActionListener(e -> {
            ProdutoEntity prodSel  = (ProdutoEntity) cbProduto.getSelectedItem();
            UsuarioEntity profSel  = (UsuarioEntity) cbProfissional.getSelectedItem();
            int quantidade   = ((Number) spQtd.getValue()).intValue();

            if (prodSel == null || profSel == null) {
                JOptionPane.showMessageDialog(dialog,
                        "Selecione produto e profissional.", "Campos inválidos",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (quantidade > prodSel.getQuantidadeEstoque()) {
                JOptionPane.showMessageDialog(dialog,
                        "Estoque insuficiente. Disponível: " + prodSel.getQuantidadeEstoque(),
                        "Estoque insuficiente", JOptionPane.WARNING_MESSAGE);
                return;
            }

            VendaAvulsaEntity venda = new VendaAvulsaEntity();
            venda.setProduto(prodSel);
            venda.setProfissional((ProfissionalEntity) profSel);
            venda.setQuantidade(quantidade);

            vendaAvulsaController.registrarVenda(venda);
            dialog.dispose();
            carregarDados();
            JOptionPane.showMessageDialog(PainelVendasAvulsas.this,
                    "Venda registrada com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        });

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(criarRodapeDialog(btnCancelar, btnConfirmar), BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // recalcula o total em tempo real conforme produto e quantidade selecionados
    private void atualizarTotal(JComboBox<ProdutoEntity> cbProduto,
                                JSpinner spQtd,
                                SpinnerNumberModel modelQtd,
                                JLabel lblTotal) {
        ProdutoEntity p = (ProdutoEntity) cbProduto.getSelectedItem();
        if (p == null) {
            lblTotal.setText("Total: R$ 0,00");
            return;
        }
        // ajusta o limite do spinner ao estoque do produto escolhido
        int estoque = Math.max(1, p.getQuantidadeEstoque());
        modelQtd.setMaximum(estoque);
        if (((Number) spQtd.getValue()).intValue() > estoque) {
            spQtd.setValue(estoque);
        }
        int qtd = ((Number) spQtd.getValue()).intValue();
        BigDecimal total = p.getPrecoVenda().multiply(BigDecimal.valueOf(qtd));
        lblTotal.setText(String.format("Total: R$ %.2f", total));
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

    private <T> void estilizarCombo(JComboBox<T> combo) {
        combo.setFont(AppFonts.BODY);
        combo.setBackground(AppColors.FUNDO_APP);
        combo.setForeground(AppColors.TEXTO_CORPO);
        combo.setBorder(new LineBorder(AppColors.BORDA, 1, false));
        combo.setPreferredSize(new Dimension(0, AppDimensions.INPUT_HEIGHT));
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

    private JPanel criarRodapeDialog(JButton btnCancelar, JButton btnConfirmar) {
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        rodape.setBackground(AppColors.BOTAO_OUTLINE_FUNDO);
        rodape.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDA, 1, false),
                new EmptyBorder(2, 8, 2, 8)
        ));
        rodape.add(btnCancelar);
        rodape.add(btnConfirmar);
        return rodape;
    }
}
