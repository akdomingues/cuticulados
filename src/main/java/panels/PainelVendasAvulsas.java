package org.cuticulados.pm.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.cuticulados.pm.entity.Produto;
import org.cuticulados.pm.entity.Profissional;
import org.cuticulados.pm.entity.VendaAvulsa;
import org.cuticulados.pm.service.ProdutoService;
import org.cuticulados.pm.service.UsuarioService;
import org.cuticulados.pm.service.VendaAvulsaService;

public class PainelVendasAvulsas extends JPanel {

    private final VendaAvulsaService vendaService;

    private final ProdutoService produtoService;

    private final UsuarioService usuarioService;

    private JTable tabela;

    private DefaultTableModel tableModel;

    private final SimpleDateFormat sdf =
            new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public PainelVendasAvulsas() {

        this.vendaService =
                new VendaAvulsaService();

        this.produtoService =
                new ProdutoService();

        this.usuarioService =
                new UsuarioService();

        setLayout(new BorderLayout());

        inicializarComponentes();

        carregarDados();
    }

    private void inicializarComponentes() {

        JPanel topo =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT
                        )
                );

        JButton btnNova =
                criarBotao("Registrar Venda");

        JButton btnRemover =
                criarBotao("Remover");

        topo.add(btnNova);

        topo.add(btnRemover);

        add(topo, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{
                        "ID",
                        "Data",
                        "Produto",
                        "Qtd",
                        "Total",
                        "Status"
                }, 0
        );

        tabela = new JTable(tableModel);

        tabela.setRowHeight(32);

        tabela.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        tabela.setDefaultRenderer(
                Object.class,
                new VendaRenderer()
        );

        JScrollPane scroll =
                new JScrollPane(tabela);

        add(scroll, BorderLayout.CENTER);

        btnNova.addActionListener(e ->
                abrirDialogVenda()
        );

        btnRemover.addActionListener(e ->
                removerVenda()
        );
    }

    private JButton criarBotao(String texto) {

        JButton btn = new JButton(texto);

        btn.setFocusPainted(false);

        btn.setBackground(
                new Color(196, 82, 126)
        );

        btn.setForeground(Color.WHITE);

        btn.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        16,
                        10,
                        16
                )
        );

        return btn;
    }

    // atualiza tabela
    private void carregarDados() {

        tableModel.setRowCount(0);

        try {

            List<VendaAvulsa> vendas =
                    vendaService.listarTodas();

            for (VendaAvulsa v : vendas) {

                tableModel.addRow(
                        new Object[]{
                                v.getId(),
                                sdf.format(
                                        v.getDataVenda()
                                ),
                                v.getProduto()
                                        .getNome(),
                                v.getQuantidade(),
                                "R$ " +
                                        v.getValorTotal(),
                                v.isFechado()
                                        ? "FECHADO"
                                        : "ABERTO"
                        }
                );
            }

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // dialog da venda
    private void abrirDialogVenda() {

        JDialog dialog =
                new JDialog();

        dialog.setTitle(
                "Registrar Venda"
        );

        dialog.setSize(450, 420);

        dialog.setLocationRelativeTo(this);

        dialog.setLayout(null);

        JLabel lblProduto =
                new JLabel("Produto");

        lblProduto.setBounds(
                20,
                20,
                120,
                25
        );

        JComboBox<Produto> cbProduto =
                new JComboBox<>();

        produtoService.listarTodos()
                .forEach(cbProduto::addItem);

        // mostra estoque no combo
        cbProduto.setRenderer(
                new DefaultListCellRenderer() {

                    @Override
                    public Component getListCellRendererComponent(
                            JList<?> list,
                            Object value,
                            int index,
                            boolean isSelected,
                            boolean cellHasFocus
                    ) {

                        super.getListCellRendererComponent(
                                list,
                                value,
                                index,
                                isSelected,
                                cellHasFocus
                        );

                        Produto p =
                                (Produto) value;

                        setText(
                                p.getNome() +
                                        " | Estoque: " +
                                        p.getQuantidadeEstoque()
                        );

                        return this;
                    }
                }
        );

        cbProduto.setBounds(
                20,
                45,
                380,
                35
        );

        JLabel lblProf =
                new JLabel("Profissional");

        lblProf.setBounds(
                20,
                100,
                120,
                25
        );

        JComboBox<Profissional> cbProf =
                new JComboBox<>();

        usuarioService
                .listarPorTipo("PROFISSIONAL")
                .forEach(p ->
                        cbProf.addItem(
                                (Profissional) p
                        )
                );

        cbProf.setBounds(
                20,
                125,
                380,
                35
        );

        JLabel lblQtd =
                new JLabel("Quantidade");

        lblQtd.setBounds(
                20,
                180,
                120,
                25
        );

        Produto produtoInicial =
                (Produto)
                        cbProduto.getSelectedItem();

        JSpinner spQtd =
                new JSpinner(
                        new SpinnerNumberModel(
                                1,
                                1,
                                produtoInicial
                                        .getQuantidadeEstoque(),
                                1
                        )
                );

        spQtd.setBounds(
                20,
                205,
                120,
                35
        );

        JLabel lblTotal =
                new JLabel();

        lblTotal.setBounds(
                20,
                260,
                220,
                30
        );

        lblTotal.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        18
                )
        );

        atualizarTotal(
                cbProduto,
                spQtd,
                lblTotal
        );

        // atualiza valor
        ChangeListener listener =
                e -> atualizarTotal(
                        cbProduto,
                        spQtd,
                        lblTotal
                );

        spQtd.addChangeListener(listener);

        // atualiza estoque maximo
        cbProduto.addActionListener(e -> {

            Produto p =
                    (Produto)
                            cbProduto.getSelectedItem();

            SpinnerNumberModel model =
                    (SpinnerNumberModel)
                            spQtd.getModel();

            model.setMaximum(
                    p.getQuantidadeEstoque()
            );

            atualizarTotal(
                    cbProduto,
                    spQtd,
                    lblTotal
            );
        });

        JButton btnSalvar =
                criarBotao("Confirmar");

        btnSalvar.setBounds(
                120,
                320,
                180,
                40
        );

        btnSalvar.addActionListener(e -> {

            try {

                VendaAvulsa venda =
                        new VendaAvulsa();

                venda.setProduto(
                        (Produto)
                                cbProduto.getSelectedItem()
                );

                venda.setProfissional(
                        (Profissional)
                                cbProf.getSelectedItem()
                );

                venda.setQuantidade(
                        (Integer)
                                spQtd.getValue()
                );

                vendaService
                        .registrarVenda(venda);

                carregarDados();

                dialog.dispose();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        dialog.add(lblProduto);

        dialog.add(cbProduto);

        dialog.add(lblProf);

        dialog.add(cbProf);

        dialog.add(lblQtd);

        dialog.add(spQtd);

        dialog.add(lblTotal);

        dialog.add(btnSalvar);

        dialog.setVisible(true);
    }

    // atualiza total da venda
    private void atualizarTotal(
            JComboBox<Produto> cbProduto,
            JSpinner spQtd,
            JLabel lblTotal
    ) {

        Produto p =
                (Produto)
                        cbProduto.getSelectedItem();

        int qtd =
                (Integer)
                        spQtd.getValue();

        double total =
                qtd * p.getPrecoVenda();

        lblTotal.setText(
                "Total: R$ " +
                        String.format(
                                "%.2f",
                                total
                        )
        );
    }

    // remove venda
    private void removerVenda() {

        int linha =
                tabela.getSelectedRow();

        if (linha == -1) {
            return;
        }

        int op =
                JOptionPane.showConfirmDialog(
                        this,
                        "Deseja remover?",
                        "Confirmação",
                        JOptionPane.YES_NO_OPTION
                );

        if (op != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            Integer id =
                    (Integer)
                            tableModel.getValueAt(
                                    linha,
                                    0
                            );

            vendaService.removerVenda(id);

            carregarDados();

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // pinta venda fechada
    private static class VendaRenderer
            extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {

            Component c =
                    super.getTableCellRendererComponent(
                            table,
                            value,
                            isSelected,
                            hasFocus,
                            row,
                            column
                    );

            String status =
                    table.getValueAt(
                            row,
                            5
                    ).toString();

            if (!isSelected) {

                if ("FECHADO".equals(status)) {

                    c.setBackground(
                            new Color(
                                    230,
                                    230,
                                    230
                            )
                    );

                } else {

                    c.setBackground(
                            Color.WHITE
                    );
                }
            }

            return c;
        }
    }
}