package org.cuticulados.pm.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.cuticulados.pm.entity.Agendamento;
import org.cuticulados.pm.entity.AgendamentoServico;
import org.cuticulados.pm.entity.Cliente;
import org.cuticulados.pm.entity.Profissional;
import org.cuticulados.pm.entity.Servico;
import org.cuticulados.pm.entity.StatusAgendamento;
import org.cuticulados.pm.service.AgendamentoService;
import org.cuticulados.pm.service.ClienteService;
import org.cuticulados.pm.service.ServicoService;
import org.cuticulados.pm.service.UsuarioService;

public class PainelAgendamentos extends JPanel {

    private final AgendamentoService agendamentoService;

    private final ClienteService clienteService;

    private final UsuarioService usuarioService;

    private final ServicoService servicoService;

    private JTable tabela;

    private DefaultTableModel tableModel;

    private final SimpleDateFormat sdf =
            new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public PainelAgendamentos() {

        this.agendamentoService =
                new AgendamentoService();

        this.clienteService =
                new ClienteService();

        this.usuarioService =
                new UsuarioService();

        this.servicoService =
                new ServicoService();

        setLayout(new BorderLayout());

        inicializarComponentes();

        carregarDados();
    }

    // cria tabela e botoes
    private void inicializarComponentes() {

        JPanel topo =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT
                        )
                );

        JButton btnCriar =
                criarBotao("Criar");

        JButton btnConcluir =
                criarBotao("Concluir");

        JButton btnCancelar =
                criarBotao("Cancelar");

        JButton btnRemover =
                criarBotao("Remover");

        JButton btnFiltro =
                criarBotao("Filtrar");

        topo.add(btnCriar);

        topo.add(btnConcluir);

        topo.add(btnCancelar);

        topo.add(btnRemover);

        topo.add(btnFiltro);

        add(topo, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{
                        "ID",
                        "Status",
                        "Início",
                        "Fim",
                        "Cliente",
                        "Profissional",
                        "Valor Final"
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

        tabela.getTableHeader().setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        tabela.setDefaultRenderer(
                Object.class,
                new StatusRenderer()
        );

        JScrollPane scroll =
                new JScrollPane(tabela);

        add(scroll, BorderLayout.CENTER);

        btnCriar.addActionListener(e ->
                abrirDialogCriacao()
        );

        btnConcluir.addActionListener(e ->
                concluirAgendamento()
        );

        btnCancelar.addActionListener(e ->
                cancelarAgendamento()
        );

        btnRemover.addActionListener(e ->
                removerAgendamento()
        );

        btnFiltro.addActionListener(e ->
                abrirFiltroPeriodo()
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

            List<Agendamento> lista =
                    agendamentoService.listarTodos();

            for (Agendamento ag : lista) {

                tableModel.addRow(new Object[]{
                        ag.getId(),
                        ag.getStatus(),
                        sdf.format(ag.getInicio()),
                        sdf.format(ag.getFim()),
                        ag.getCliente().getNome(),
                        ag.getProfissional().getNome(),
                        "R$ " + ag.getValorFinal()
                });
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

    // dialog de criacao
    private void abrirDialogCriacao() {

        JDialog dialog =
                new JDialog();

        dialog.setTitle(
                "Novo Agendamento"
        );

        dialog.setSize(520, 520);

        dialog.setLocationRelativeTo(this);

        dialog.setLayout(null);

        JLabel lblCliente =
                new JLabel("Cliente");

        lblCliente.setBounds(20, 20, 120, 25);

        JComboBox<Cliente> cbCliente =
                new JComboBox<>();

        clienteService.listarTodos()
                .forEach(cbCliente::addItem);

        cbCliente.setBounds(20, 45, 450, 35);

        JLabel lblProf =
                new JLabel("Profissional");

        lblProf.setBounds(20, 95, 120, 25);

        JComboBox<Profissional> cbProf =
                new JComboBox<>();

        usuarioService
                .listarPorTipo("PROFISSIONAL")
                .forEach(p ->
                        cbProf.addItem(
                                (Profissional) p
                        )
                );

        cbProf.setBounds(20, 120, 450, 35);

        JLabel lblInicio =
                new JLabel("Início");

        lblInicio.setBounds(20, 170, 120, 25);

        JSpinner spInicio =
                new JSpinner(
                        new SpinnerDateModel()
                );

        spInicio.setEditor(
                new JSpinner.DateEditor(
                        spInicio,
                        "dd/MM/yyyy HH:mm"
                )
        );

        spInicio.setBounds(20, 195, 200, 35);

        JLabel lblFim =
                new JLabel("Fim");

        lblFim.setBounds(250, 170, 120, 25);

        JSpinner spFim =
                new JSpinner(
                        new SpinnerDateModel()
                );

        spFim.setEditor(
                new JSpinner.DateEditor(
                        spFim,
                        "dd/MM/yyyy HH:mm"
                )
        );

        spFim.setBounds(250, 195, 220, 35);

        JLabel lblServicos =
                new JLabel("Serviços");

        lblServicos.setBounds(20, 250, 120, 25);

        DefaultListModel<Servico> model =
                new DefaultListModel<>();

        servicoService.listarTodos()
                .forEach(model::addElement);

        JList<Servico> lista =
                new JList<>(model);

        lista.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        );

        JScrollPane scroll =
                new JScrollPane(lista);

        scroll.setBounds(20, 275, 450, 110);

        JButton btnSalvar =
                criarBotao("Confirmar");

        btnSalvar.setBounds(
                160,
                420,
                180,
                40
        );

        btnSalvar.addActionListener(e -> {

            try {

                Agendamento ag =
                        new Agendamento();

                ag.setCliente(
                        (Cliente)
                                cbCliente.getSelectedItem()
                );

                ag.setProfissional(
                        (Profissional)
                                cbProf.getSelectedItem()
                );

                ag.setInicio(
                        (Date)
                                spInicio.getValue()
                );

                ag.setFim(
                        (Date)
                                spFim.getValue()
                );

                ag.setStatus(
                        StatusAgendamento.PENDENTE
                );

                // monta os servicos do agendamento
                for (Servico s :
                        lista.getSelectedValuesList()) {

                    AgendamentoServico as =
                            new AgendamentoServico();

                    as.setServico(s);

                    as.setAgendamento(ag);

                    as.setPrecoAplicado(
                            s.getValorBase()
                    );

                    as.setQuantidade(1);

                    ag.getServicos().add(as);
                }

                agendamentoService
                        .criarAgendamento(ag);

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

        dialog.add(lblCliente);

        dialog.add(cbCliente);

        dialog.add(lblProf);

        dialog.add(cbProf);

        dialog.add(lblInicio);

        dialog.add(spInicio);

        dialog.add(lblFim);

        dialog.add(spFim);

        dialog.add(lblServicos);

        dialog.add(scroll);

        dialog.add(btnSalvar);

        dialog.setVisible(true);
    }

    // conclui agendamento
    private void concluirAgendamento() {

        int linha =
                tabela.getSelectedRow();

        if (linha == -1) {
            return;
        }

        try {

            Integer id =
                    (Integer)
                            tableModel.getValueAt(
                                    linha,
                                    0
                            );

            agendamentoService
                    .concluirAgendamento(id);

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

    // cancela agendamento
    private void cancelarAgendamento() {

        int linha =
                tabela.getSelectedRow();

        if (linha == -1) {
            return;
        }

        try {

            Integer id =
                    (Integer)
                            tableModel.getValueAt(
                                    linha,
                                    0
                            );

            agendamentoService
                    .cancelarAgendamento(id);

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

    // remove agendamento
    private void removerAgendamento() {

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

            agendamentoService
                    .removerAgendamento(id);

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

    // filtro por periodo
    private void abrirFiltroPeriodo() {

        JDialog dialog =
                new JDialog();

        dialog.setTitle("Filtrar");

        dialog.setSize(350, 220);

        dialog.setLocationRelativeTo(this);

        dialog.setLayout(null);

        JLabel lblInicio =
                new JLabel("Data Inicial");

        lblInicio.setBounds(20, 20, 120, 25);

        JSpinner spInicio =
                new JSpinner(
                        new SpinnerDateModel()
                );

        spInicio.setEditor(
                new JSpinner.DateEditor(
                        spInicio,
                        "dd/MM/yyyy"
                )
        );

        spInicio.setBounds(20, 45, 280, 35);

        JLabel lblFim =
                new JLabel("Data Final");

        lblFim.setBounds(20, 90, 120, 25);

        JSpinner spFim =
                new JSpinner(
                        new SpinnerDateModel()
                );

        spFim.setEditor(
                new JSpinner.DateEditor(
                        spFim,
                        "dd/MM/yyyy"
                )
        );

        spFim.setBounds(20, 115, 280, 35);

        JButton btnBuscar =
                criarBotao("Buscar");

        btnBuscar.setBounds(
                90,
                160,
                150,
                35
        );

        btnBuscar.addActionListener(e -> {

            try {

                tableModel.setRowCount(0);

                List<Agendamento> lista =
                        agendamentoService
                                .buscarPorPeriodo(
                                        (Date)
                                                spInicio.getValue(),
                                        (Date)
                                                spFim.getValue()
                                );

                for (Agendamento ag : lista) {

                    tableModel.addRow(
                            new Object[]{
                                    ag.getId(),
                                    ag.getStatus(),
                                    sdf.format(
                                            ag.getInicio()
                                    ),
                                    sdf.format(
                                            ag.getFim()
                                    ),
                                    ag.getCliente()
                                            .getNome(),
                                    ag.getProfissional()
                                            .getNome(),
                                    "R$ " +
                                            ag.getValorFinal()
                            }
                    );
                }

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

        dialog.add(lblInicio);

        dialog.add(spInicio);

        dialog.add(lblFim);

        dialog.add(spFim);

        dialog.add(btnBuscar);

        dialog.setVisible(true);
    }

    // pinta linhas por status
    private static class StatusRenderer
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
                            1
                    ).toString();

            if (!isSelected) {

                switch (status) {

                    case "PENDENTE":

                        c.setBackground(
                                new Color(
                                        255,
                                        243,
                                        214
                                )
                        );

                        break;

                    case "CONCLUIDO":

                        c.setBackground(
                                new Color(
                                        227,
                                        240,
                                        220
                                )
                        );

                        break;

                    case "CANCELADO":

                        c.setBackground(
                                new Color(
                                        250,
                                        221,
                                        221
                                )
                        );

                        break;

                    default:

                        c.setBackground(
                                Color.WHITE
                        );
                }
            }

            return c;
        }
    }
}