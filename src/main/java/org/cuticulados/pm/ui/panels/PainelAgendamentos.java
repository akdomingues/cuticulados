package org.cuticulados.pm.ui.panels;

import org.cuticulados.pm.entity.Agendamento;
import org.cuticulados.pm.entity.AgendamentoServico;
import org.cuticulados.pm.entity.Cliente;
import org.cuticulados.pm.entity.Profissional;
import org.cuticulados.pm.entity.Servico;
import org.cuticulados.pm.entity.StatusAgendamento;
import org.cuticulados.pm.entity.TipoUsuario;
import org.cuticulados.pm.entity.Usuario;
import org.cuticulados.pm.service.AgendamentoService;
import org.cuticulados.pm.service.ClienteService;
import org.cuticulados.pm.service.ServicoService;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Painel de gerenciamento de agendamentos do perfil ADMIN.
 * Exibe tabela com cores por status e permite criar, concluir, cancelar, remover e filtrar.
 */
public class PainelAgendamentos extends JPanel {

    // services
    private final AgendamentoService agendamentoService;
    private final ClienteService     clienteService;
    private final ServicoService     servicoService;
    private final UsuarioService     usuarioService;

    // componentes principais
    private JTable tabela;
    private DefaultTableModel tableModel;

    // lista atual
    private List<Agendamento> agendamentosAtuais;

    // formato de data/hora exibido na tabela
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // colunas
    private static final String[] COLUNAS = {
            "ID", "Status", "Início", "Fim", "Cliente", "Profissional", "Valor (R$)"
    };

    // cores das linhas por status
    private static final Color COR_PENDENTE_BG  = new Color(0xFF, 0xF3, 0xD6);
    private static final Color COR_PENDENTE_FG  = AppColors.STATUS_PEND;
    private static final Color COR_CONCLUIDO_BG = new Color(0xE3, 0xF0, 0xDC);
    private static final Color COR_CONCLUIDO_FG = AppColors.STATUS_CONC;
    private static final Color COR_CANCELADO_BG = new Color(0xFA, 0xDD, 0xDD);
    private static final Color COR_CANCELADO_FG = AppColors.PERIGO;

    // painel agendamentos
    public PainelAgendamentos() {
        this.agendamentoService = new AgendamentoService();
        this.clienteService     = new ClienteService();
        this.servicoService     = new ServicoService();
        this.usuarioService     = new UsuarioService();
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

    // topbar com titulo e botões de ação
    private JPanel criarTopbar() {
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(AppColors.FUNDO_CARD);
        topbar.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDA, 1, false),
                new EmptyBorder(10, 20, 10, 20)
        ));
        topbar.setPreferredSize(new Dimension(0, AppDimensions.TOPBAR_HEIGHT));

        JLabel titulo = new JLabel("Agendamentos");
        titulo.setFont(AppFonts.TITLE);
        titulo.setForeground(AppColors.TEXTO_TITULO);
        topbar.add(titulo, BorderLayout.WEST);

        topbar.add(criarBotoesAcao(), BorderLayout.EAST);
        return topbar;
    }

    // painel com botões: criar / concluir / cancelar / remover / filtrar
    private JPanel criarBotoesAcao() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        painel.setOpaque(false);

        JButton btnCriar    = new JButton("+ Criar");
        JButton btnConcluir = new JButton("Concluir");
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnRemover  = new JButton("Remover");
        JButton btnFiltrar  = new JButton("Filtrar Período");

        AppTheme.stylePrimaryButton(btnCriar, false);
        AppTheme.styleOutlineButton(btnConcluir, false);
        AppTheme.styleOutlineButton(btnCancelar, false);
        AppTheme.styleOutlineButton(btnRemover, false);
        AppTheme.styleOutlineButton(btnFiltrar, false);

        btnConcluir.setForeground(AppColors.STATUS_CONC);
        btnCancelar.setForeground(AppColors.STATUS_PEND);
        btnRemover.setForeground(AppColors.PERIGO);

        btnCriar.addActionListener(e -> abrirDialogCriacao());
        btnConcluir.addActionListener(e -> concluirSelecionado());
        btnCancelar.addActionListener(e -> cancelarSelecionado());
        btnRemover.addActionListener(e -> confirmarRemocao());
        btnFiltrar.addActionListener(e -> abrirDialogFiltro());

        painel.add(btnCriar);
        painel.add(btnConcluir);
        painel.add(btnCancelar);
        painel.add(btnRemover);
        painel.add(btnFiltrar);
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

    // cria o modelo da tabela com renderizador colorido por status
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
        aplicarRenderizadorStatus();
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

    // renderizador que colore a linha inteira conforme o status do agendamento
    private void aplicarRenderizadorStatus() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean selected, boolean focused, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focused, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setFont(AppFonts.TABLE);

                if (!selected) {
                    Object statusVal = t.getModel().getValueAt(row, 1);
                    String status = statusVal != null ? statusVal.toString() : "";
                    switch (status) {
                        case "CONCLUIDO" -> {
                            setBackground(COR_CONCLUIDO_BG);
                            setForeground(col == 1 ? COR_CONCLUIDO_FG : AppColors.TEXTO_CORPO);
                        }
                        case "CANCELADO" -> {
                            setBackground(COR_CANCELADO_BG);
                            setForeground(col == 1 ? COR_CANCELADO_FG : AppColors.TEXTO_CORPO);
                        }
                        default -> {
                            setBackground(COR_PENDENTE_BG);
                            setForeground(col == 1 ? COR_PENDENTE_FG : AppColors.TEXTO_CORPO);
                        }
                    }
                    // texto amigável na coluna de status
                    if (col == 1 && value != null) {
                        setText(formatarStatus(value.toString()));
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
        int[] larguras = {50, 100, 130, 130, 160, 160, 100};
        for (int i = 0; i < larguras.length; i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }
        tabela.getColumnModel().getColumn(0).setMaxWidth(60);
    }

    // dados
    // recarrega todos os agendamentos e preenche a tabela
    private void carregarDados() {
        agendamentosAtuais = agendamentoService.listarTodos();
        preencherTabela(agendamentosAtuais);
    }

    private void preencherTabela(List<Agendamento> lista) {
        tableModel.setRowCount(0);
        for (Agendamento ag : lista) {
            String inicio = ag.getDataHoraInicio() != null
                    ? ag.getDataHoraInicio().format(FMT) : "—";
            String fim = ag.getDataHoraFim() != null
                    ? ag.getDataHoraFim().format(FMT) : "—";
            String cliente = ag.getCliente() != null
                    ? ag.getCliente().getNome() : "—";
            String prof = ag.getProfissional() != null
                    ? ag.getProfissional().getNome() : "—";
            String valor = ag.getValorFinal() != null
                    ? String.format("R$ %.2f", ag.getValorFinal()) : "R$ 0,00";

            tableModel.addRow(new Object[]{
                    ag.getId(),
                    ag.getStatus().name(),
                    inicio, fim, cliente, prof, valor
            });
        }
    }

    // ações do crud

    private void concluirSelecionado() {
        int linhaSel = tabela.getSelectedRow();
        if (linhaSel < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um agendamento para concluir.",
                    "Nenhuma seleção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Long id = (Long) tableModel.getValueAt(linhaSel, 0);
        agendamentoService.concluirAgendamento(id);
        carregarDados();
        JOptionPane.showMessageDialog(this,
                "Agendamento #" + id + " concluído com sucesso!",
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cancelarSelecionado() {
        int linhaSel = tabela.getSelectedRow();
        if (linhaSel < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um agendamento para cancelar.",
                    "Nenhuma seleção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Long id = (Long) tableModel.getValueAt(linhaSel, 0);
        agendamentoService.cancelarAgendamento(id);
        carregarDados();
        JOptionPane.showMessageDialog(this,
                "Agendamento #" + id + " cancelado.",
                "Agendamento cancelado", JOptionPane.INFORMATION_MESSAGE);
    }

    private void confirmarRemocao() {
        int linhaSel = tabela.getSelectedRow();
        if (linhaSel < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um agendamento para remover.",
                    "Nenhuma seleção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Long id = (Long) tableModel.getValueAt(linhaSel, 0);
        int resp = JOptionPane.showConfirmDialog(this,
                "Deseja remover o agendamento #" + id + "?\nEsta ação não pode ser desfeita.",
                "Confirmar Remoção",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            agendamentoService.removerAgendamento(id);
            carregarDados();
            JOptionPane.showMessageDialog(this,
                    "Agendamento #" + id + " removido com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // abre dialog de filtro por período
    private void abrirDialogFiltro() {
        JDialog dialog = criarDialogBase("Filtrar por Período", 400, 220);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 16, 8, 16));
        GridBagConstraints gbc = criarGbc();

        JSpinner spInicio = criarSpinnerDataHora();
        JSpinner spFim    = criarSpinnerDataHora();

        adicionarCampoForm(form, gbc, "Data Início", spInicio, 0, 0, false);
        adicionarCampoForm(form, gbc, "Data Fim",    spFim,    0, 1, false);

        JButton btnFiltrar  = new JButton("Filtrar");
        JButton btnCancelar = new JButton("Cancelar");
        AppTheme.stylePrimaryButton(btnFiltrar, true);
        AppTheme.styleOutlineButton(btnCancelar, true);

        btnCancelar.addActionListener(e -> dialog.dispose());
        btnFiltrar.addActionListener(e -> {
            LocalDateTime inicio = spinnerParaLocalDateTime(spInicio);
            LocalDateTime fim    = spinnerParaLocalDateTime(spFim);
            List<Agendamento> filtrados = agendamentoService.buscarPorPeriodo(inicio, fim);
            preencherTabela(filtrados);
            dialog.dispose();
        });

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(criarRodapeDialog(btnCancelar, btnFiltrar), BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // dialog de criação de agendamento
    private void abrirDialogCriacao() {
        JDialog dialog = criarDialogBase("Novo Agendamento", 520, 500);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 16, 8, 16));
        GridBagConstraints gbc = criarGbc();

        // combobox de clientes
        List<Cliente> clientes = clienteService.listarTodos();
        JComboBox<Cliente> cbCliente = new JComboBox<>();
        clientes.forEach(cbCliente::addItem);
        estilizarCombo(cbCliente);
        cbCliente.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value != null ? value.getNome() : "");
            lbl.setFont(AppFonts.BODY);
            lbl.setBorder(new EmptyBorder(4, 8, 4, 8));
            if (isSelected) { lbl.setBackground(AppColors.ROSA_PALIDO); lbl.setOpaque(true); }
            return lbl;
        });

        // combobox de profissionais
        List<Usuario> profissionais = usuarioService.listarPorTipo(TipoUsuario.PROFISSIONAL)
                .stream().filter(u -> !u.isDeleted()).toList();
        JComboBox<Usuario> cbProfissional = new JComboBox<>();
        profissionais.forEach(cbProfissional::addItem);
        estilizarCombo(cbProfissional);
        cbProfissional.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value != null ? value.getNome() : "");
            lbl.setFont(AppFonts.BODY);
            lbl.setBorder(new EmptyBorder(4, 8, 4, 8));
            if (isSelected) { lbl.setBackground(AppColors.ROSA_PALIDO); lbl.setOpaque(true); }
            return lbl;
        });

        // spinners de data/hora para início e fim
        JSpinner spInicio = criarSpinnerDataHora();
        JSpinner spFim    = criarSpinnerDataHora();

        // JList de serviços com seleção múltipla
        List<Servico> todosServicos = servicoService.listarTodos();
        DefaultListModel<Servico> listModel = new DefaultListModel<>();
        todosServicos.forEach(listModel::addElement);

        JList<Servico> listaServicos = new JList<>(listModel);
        listaServicos.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listaServicos.setFont(AppFonts.BODY);
        listaServicos.setBackground(AppColors.FUNDO_APP);
        listaServicos.setForeground(AppColors.TEXTO_CORPO);
        listaServicos.setSelectionBackground(AppColors.ROSA_PALIDO);
        listaServicos.setSelectionForeground(AppColors.TEXTO_TITULO);
        listaServicos.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            String texto = value != null
                    ? value.getDescricao() + " — R$ " + String.format("%.2f", value.getValorBase())
                    : "";
            JLabel lbl = new JLabel(texto);
            lbl.setFont(AppFonts.BODY);
            lbl.setBorder(new EmptyBorder(4, 10, 4, 10));
            if (isSelected) {
                lbl.setBackground(AppColors.ROSA_PALIDO);
                lbl.setForeground(AppColors.TEXTO_TITULO);
                lbl.setOpaque(true);
            } else {
                lbl.setForeground(AppColors.TEXTO_CORPO);
            }
            return lbl;
        });

        JScrollPane scrollServicos = new JScrollPane(listaServicos);
        scrollServicos.setBorder(new LineBorder(AppColors.BORDA, 1, false));
        scrollServicos.setPreferredSize(new Dimension(0, 120));

        adicionarCampoForm(form, gbc, "Cliente",          cbCliente,    0, 0, false);
        adicionarCampoForm(form, gbc, "Profissional",     cbProfissional, 0, 1, false);
        adicionarCampoForm(form, gbc, "Início",           spInicio,     1, 0, false);
        adicionarCampoForm(form, gbc, "Fim",              spFim,        1, 1, false);
        adicionarCampoForm(form, gbc, "Serviços (ctrl+clique para múltiplos)",
                scrollServicos, 2, 0, true);

        JButton btnConfirmar = new JButton("Confirmar");
        JButton btnCancelar  = new JButton("Cancelar");
        AppTheme.stylePrimaryButton(btnConfirmar, true);
        AppTheme.styleOutlineButton(btnCancelar, true);

        btnCancelar.addActionListener(e -> dialog.dispose());
        btnConfirmar.addActionListener(e -> {
            Cliente clienteSel = (Cliente) cbCliente.getSelectedItem();
            Usuario profSel    = (Usuario)  cbProfissional.getSelectedItem();
            List<Servico> selecionados = listaServicos.getSelectedValuesList();

            if (clienteSel == null || profSel == null) {
                JOptionPane.showMessageDialog(dialog,
                        "Selecione cliente e profissional.", "Campos inválidos",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (selecionados.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Selecione ao menos um serviço.", "Campos inválidos",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDateTime inicio = spinnerParaLocalDateTime(spInicio);
            LocalDateTime fim    = spinnerParaLocalDateTime(spFim);
            if (!fim.isAfter(inicio)) {
                JOptionPane.showMessageDialog(dialog,
                        "A data/hora de fim deve ser posterior à data/hora de início.",
                        "Datas inválidas", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Agendamento ag = new Agendamento();
            ag.setCliente(clienteSel);
            ag.setProfissional((Profissional) profSel);
            ag.setDataHoraInicio(inicio);
            ag.setDataHoraFim(fim);

            // monta a lista de AgendamentoServico conforme exemplo do guia
            List<AgendamentoServico> agServicos = new ArrayList<>();
            for (Servico s : selecionados) {
                AgendamentoServico as = new AgendamentoServico();
                as.setServico(s);
                as.setAgendamento(ag);
                as.setPrecoAplicado(s.getValorBase());
                as.setQuantidade(1);
                agServicos.add(as);
            }
            ag.setServicos(agServicos);

            String erro = agendamentoService.criarAgendamento(ag);
            if (erro != null) {
                JOptionPane.showMessageDialog(dialog, erro, "Erro ao criar agendamento",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            dialog.dispose();
            carregarDados();
            JOptionPane.showMessageDialog(PainelAgendamentos.this,
                    "Agendamento criado com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        });

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(criarRodapeDialog(btnCancelar, btnConfirmar), BorderLayout.SOUTH);
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

    private JSpinner criarSpinnerDataHora() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd/MM/yyyy HH:mm");
        spinner.setEditor(editor);
        spinner.setFont(AppFonts.BODY);
        spinner.setPreferredSize(new Dimension(0, AppDimensions.INPUT_HEIGHT));
        spinner.setBorder(new LineBorder(AppColors.BORDA, 1, false));
        editor.getTextField().setFont(AppFonts.BODY);
        editor.getTextField().setBackground(AppColors.FUNDO_APP);
        editor.getTextField().setForeground(AppColors.TEXTO_CORPO);
        editor.getTextField().setBorder(new EmptyBorder(4, 6, 4, 6));
        return spinner;
    }

    private LocalDateTime spinnerParaLocalDateTime(JSpinner spinner) {
        Date date = (Date) spinner.getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
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

    private String formatarStatus(String status) {
        return switch (status) {
            case "CONCLUIDO" -> "Concluído";
            case "CANCELADO" -> "Cancelado";
            default          -> "Pendente";
        };
    }
}
