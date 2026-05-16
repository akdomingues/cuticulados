package org.cuticulados.pm.ui.frames;

import org.cuticulados.pm.entity.Cliente;
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
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Optional;

/**
 * Janela de login da aplicação.
 * Autentica o usuário e abre a frame correspondente ao seu perfil.
 */
public class LoginFrame extends JFrame {

    // service
    private final UsuarioService usuarioService;

    // campos do formulário
    private JTextField campoLogin;
    private JPasswordField campoSenha;
    private JLabel lblErro;

    // login frame
    public LoginFrame() {
        this.usuarioService = new UsuarioService();
        configurarJanela();
        construirUI();
    }

    // configuração da janela
    private void configurarJanela() {
        setTitle("NailGestor — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(640, 520);
        setResizable(false);
        setLocationRelativeTo(null);
        getContentPane().setBackground(AppColors.FUNDO_APP);
    }

    // construção do layout do login
    private void construirUI() {
        setLayout(new BorderLayout());

        // painel central com card branco
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppColors.FUNDO_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDA, 1, false),
                new EmptyBorder(36, 90, 32, 90)
        ));

        // logo e título
        JLabel iconeLogo = new JLabel("💅", SwingConstants.CENTER);
        iconeLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        iconeLogo.setForeground(AppColors.ROSA);
        iconeLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel("NailGestor", SwingConstants.CENTER);
        lblTitulo.setFont(new Font(AppFonts.FAMILY, Font.BOLD, 22));
        lblTitulo.setForeground(AppColors.ROXO);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Sistema de Gestão", SwingConstants.CENTER);
        lblSub.setFont(AppFonts.BODY);
        lblSub.setForeground(AppColors.TEXTO_SECUNDARIO);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // campos de login e senha
        JLabel lblLogin = criarLabel("LOGIN");
        campoLogin = criarCampo("usuário");

        JLabel lblSenhaLbl = criarLabel("SENHA");
        campoSenha = new JPasswordField();
        estilizarCampo(campoSenha);
        campoSenha.setEchoChar('●');

        // label de erro (inicialmente invisível)
        lblErro = new JLabel("", SwingConstants.CENTER);
        lblErro.setFont(AppFonts.SMALL);
        lblErro.setForeground(AppColors.PERIGO);
        lblErro.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblErro.setVisible(false);

        // botão entrar
        JButton btnEntrar = new JButton("Entrar");
        AppTheme.stylePrimaryButton(btnEntrar, false);
        btnEntrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnEntrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, AppDimensions.BUTTON_HEIGHT));

        btnEntrar.addActionListener(e -> tentarLogin());

        // enter em qualquer campo submete
        KeyAdapter enterSubmit = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) tentarLogin();
            }
        };
        campoLogin.addKeyListener(enterSubmit);
        campoSenha.addKeyListener(enterSubmit);

        // monta o card
        card.add(iconeLogo);
        card.add(Box.createVerticalStrut(6));
        card.add(lblTitulo);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(32));
        card.add(lblLogin);
        card.add(Box.createVerticalStrut(4));
        card.add(campoLogin);
        card.add(Box.createVerticalStrut(16));
        card.add(lblSenhaLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(campoSenha);
        card.add(Box.createVerticalStrut(8));
        card.add(lblErro);
        card.add(Box.createVerticalStrut(20));
        card.add(btnEntrar);

        // centraliza o card no painel
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(AppColors.FUNDO_APP);
        wrapper.add(card);

        add(wrapper, BorderLayout.CENTER);

        // rodapé
        JPanel rodape = new JPanel();
        rodape.setBackground(AppColors.ROXO);
        rodape.setBorder(new EmptyBorder(6, 0, 6, 0));
        JLabel lblRodape = new JLabel("NailGestor © 2025", SwingConstants.CENTER);
        lblRodape.setFont(new Font(AppFonts.FAMILY, Font.PLAIN, 10));
        lblRodape.setForeground(new Color(255, 255, 255, 100));
        rodape.add(lblRodape);
        add(rodape, BorderLayout.SOUTH);
    }

    // tenta autenticar e abre a frame do perfil correspondente
    private void tentarLogin() {
        String login = campoLogin.getText().trim();
        String senha = new String(campoSenha.getPassword()).trim();

        if (login.isBlank() || senha.isBlank()) {
            mostrarErro("Preencha login e senha.");
            return;
        }

        Optional<Usuario> resultado = usuarioService.autenticar(login, senha);

        if (resultado.isEmpty()) {
            mostrarErro("Login ou senha incorretos.");
            campoSenha.setText("");
            campoSenha.requestFocus();
            return;
        }

        Usuario usuario = resultado.get();
        dispose();

        // abre a frame correta conforme o perfil do usuário
        switch (usuario.getTipo()) {
            case ADMIN -> new MainFrame(usuario).setVisible(true);
            case CLIENTE -> {
                if (usuario instanceof Cliente cliente) {
                    new ClienteFrame(cliente).setVisible(true);
                }
            }
            case PROFISSIONAL -> {
                if (usuario instanceof Profissional prof) {
                    new ProfissionalFrame(prof).setVisible(true);
                }
            }
        }
    }

    private void mostrarErro(String mensagem) {
        lblErro.setText(mensagem);
        lblErro.setVisible(true);
    }

    // ajuda de ui
    private JLabel criarLabel(String texto) {
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setFont(AppFonts.LABEL);
        lbl.setForeground(AppColors.TEXTO_SECUNDARIO);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, lbl.getPreferredSize().height));
        return lbl;
    }

    private JTextField criarCampo(String placeholder) {
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
                new EmptyBorder(6, 10, 6, 10)
        ));
        f.setPreferredSize(new Dimension(0, AppDimensions.INPUT_HEIGHT + 4));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, AppDimensions.INPUT_HEIGHT + 4));
        f.setAlignmentX(Component.CENTER_ALIGNMENT);
    }
}
