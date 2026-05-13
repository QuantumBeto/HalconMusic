package com.halconmusic.ui.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.halconmusic.dao.UsuarioDAO;
import com.halconmusic.ui.UITheme;

/**
 * Pantalla de inicio de sesión.
 * Llama a onLogin con { ID_USUARIO, NOMBRE, TIPO } si las credenciales son válidas.
 */
public class LoginPanel extends JPanel {

    private final Consumer<String[]> onLogin;
    private final UsuarioDAO         usuarioDAO;

    private JTextField  fieldId;
    private JPasswordField fieldPass;
    private JLabel      lblError;
    private JButton     btnLogin;

    public LoginPanel(Consumer<String[]> onLogin) {
        this.onLogin     = onLogin;
        this.usuarioDAO  = new UsuarioDAO();

        setBackground(UITheme.BG);
        setLayout(new GridBagLayout());   // centrado perfecto

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0;
        add(buildCard(), gc);
    }

    private JPanel buildCard() {
        // Tarjeta central
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                // Borde sutil dorado
                g2.setColor(new Color(UITheme.ACCENT.getRed(), UITheme.ACCENT.getGreen(),
                                      UITheme.ACCENT.getBlue(), 60));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 48, 40, 48));
        card.setPreferredSize(new Dimension(400, 480));

        // ── Logo ──────────────────────────────────────────
        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        logoRow.setOpaque(false);
        logoRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel iconBox = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.ACCENT);
                g2.fillRoundRect(0, 0, 40, 40, 10, 10);
                g2.setColor(UITheme.BG);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                g2.drawString("H", 11, 28);
                g2.dispose();
            }
        };
        iconBox.setOpaque(false);
        iconBox.setPreferredSize(new Dimension(40, 40));

        JLabel lblLogo = new JLabel("<html><span style='color:#F0F0F0'>Halcon</span>"
                                  + "<span style='color:#C8A84B'>Music</span></html>");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 20));

        logoRow.add(iconBox);
        logoRow.add(lblLogo);

        // ── Bienvenida ─────────────────────────────────────
        JLabel lblWelcome = new JLabel("Bienvenido de vuelta");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblWelcome.setForeground(UITheme.TEXT);
        lblWelcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Inicia sesión para continuar escuchando");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Campo ID de usuario ───────────────────────────
        JLabel lblId = fieldLabel("ID de usuario");
        fieldId = styledField("Ej: US001");

        // ── Campo contraseña ──────────────────────────────
        JLabel lblP = fieldLabel("Contraseña");
        fieldPass = new JPasswordField();
        fieldPass.setBackground(UITheme.CARD);
        fieldPass.setForeground(UITheme.TEXT);
        fieldPass.setCaretColor(UITheme.ACCENT);
        fieldPass.setFont(UITheme.FONT_BODY);
        fieldPass.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        fieldPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        fieldPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Enter en contraseña = login
        fieldPass.addActionListener(e -> intentarLogin());

        // ── Error label ───────────────────────────────────
        lblError = new JLabel(" ");
        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(new Color(0xFF, 0x44, 0x44));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Botón Entrar ──────────────────────────────────
        btnLogin = new JButton("Iniciar sesión") {
            boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                    @Override public void mouseExited (MouseEvent e) { hover = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? UITheme.ACCENT2 : UITheme.ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(UITheme.BG);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                              (getHeight() + fm.getAscent()) / 2 - 3);
                g2.dispose();
            }
        };
        btnLogin.setPreferredSize(new Dimension(304, 44));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnLogin.setContentAreaFilled(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.addActionListener(e -> intentarLogin());

        // ── Ensamble ──────────────────────────────────────
        card.add(logoRow);
        card.add(Box.createVerticalStrut(28));
        card.add(lblWelcome);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(32));
        card.add(lblId);
        card.add(Box.createVerticalStrut(6));
        card.add(fieldId);
        card.add(Box.createVerticalStrut(16));
        card.add(lblP);
        card.add(Box.createVerticalStrut(6));
        card.add(fieldPass);
        card.add(Box.createVerticalStrut(10));
        card.add(lblError);
        card.add(Box.createVerticalStrut(10));
        card.add(btnLogin);

        return card;
    }

    private void intentarLogin() {
        String id   = fieldId.getText().trim();
        String pass = new String(fieldPass.getPassword());

        if (id.isEmpty() || pass.isEmpty()) {
            lblError.setText("Completa todos los campos.");
            return;
        }

        btnLogin.setEnabled(false);
        lblError.setText("Verificando...");
        lblError.setForeground(UITheme.MUTED);

        // BD en hilo secundario para no bloquear EDT
        new Thread(() -> {
            String[] datos = usuarioDAO.autenticar(id, pass);
            SwingUtilities.invokeLater(() -> {
                btnLogin.setEnabled(true);
                if (datos != null) {
                    onLogin.accept(datos);
                } else {
                    lblError.setText("ID o contraseña incorrectos.");
                    lblError.setForeground(new Color(0xFF, 0x44, 0x44));
                    fieldPass.setText("");
                }
            });
        }).start();
    }

    // ── Helpers ──────────────────────────────────────────
    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField();
        f.setBackground(UITheme.CARD);
        f.setForeground(UITheme.TEXT);
        f.setCaretColor(UITheme.ACCENT);
        f.setFont(UITheme.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.putClientProperty("JTextField.placeholderText", placeholder);
        return f;
    }
}