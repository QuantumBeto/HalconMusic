package com.halconmusic.ui.panels;

import com.halconmusic.dao.ArtistaDAO;
import com.halconmusic.ui.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * REQ. 2 — Panel de creación de Artistas (solo visible para usuario tipo Artista/Premium).
 */
public class CrearArtistaPanel extends JPanel {

    private final ArtistaDAO artistaDAO = new ArtistaDAO();

    private JTextField  txtNombre;
    private JTextField  txtDescripcion;
    private JTextField  txtGenero;
    private JTextField  txtPais;
    private JLabel      lblMensaje;

    public CrearArtistaPanel() {
        setBackground(UITheme.BG);
        setLayout(new GridBagLayout());
        add(buildCard(), new GridBagConstraints());
    }

    private JPanel buildCard() {
        JPanel card = new JPanel();
        card.setBackground(UITheme.SURFACE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(32, 40, 32, 40)));
        card.setPreferredSize(new Dimension(480, 420));

        JLabel titulo = new JLabel("Nuevo Artista");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titulo.setForeground(UITheme.TEXT);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtNombre      = campo("Nombre del artista");
        txtDescripcion = campo("Descripción breve");
        txtGenero      = campo("Género principal (ej: Corridos, Pop)");
        txtPais        = campo("País de origen");

        lblMensaje = new JLabel(" ");
        lblMensaje.setFont(UITheme.FONT_SMALL);
        lblMensaje.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnGuardar = crearBoton("Guardar Artista");
        btnGuardar.addActionListener(e -> guardar());

        JButton btnLimpiar = crearBotonSecundario("Limpiar");
        btnLimpiar.addActionListener(e -> limpiar());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        botones.setOpaque(false);
        botones.setAlignmentX(Component.LEFT_ALIGNMENT);
        botones.add(btnGuardar);
        botones.add(btnLimpiar);

        card.add(titulo);
        card.add(Box.createVerticalStrut(24));
        card.add(etiqueta("Nombre *"));
        card.add(Box.createVerticalStrut(4));
        card.add(txtNombre);
        card.add(Box.createVerticalStrut(12));
        card.add(etiqueta("Descripción *"));
        card.add(Box.createVerticalStrut(4));
        card.add(txtDescripcion);
        card.add(Box.createVerticalStrut(12));
        card.add(etiqueta("Género principal *"));
        card.add(Box.createVerticalStrut(4));
        card.add(txtGenero);
        card.add(Box.createVerticalStrut(12));
        card.add(etiqueta("País de origen *"));
        card.add(Box.createVerticalStrut(4));
        card.add(txtPais);
        card.add(Box.createVerticalStrut(16));
        card.add(lblMensaje);
        card.add(Box.createVerticalStrut(8));
        card.add(botones);

        return card;
    }

    private void guardar() {
        String nombre = txtNombre.getText().trim();
        String desc   = txtDescripcion.getText().trim();
        String genero = txtGenero.getText().trim();
        String pais   = txtPais.getText().trim();

        if (nombre.isEmpty() || desc.isEmpty() || genero.isEmpty() || pais.isEmpty()) {
            mensaje("Completa todos los campos obligatorios.", false);
            return;
        }

        new Thread(() -> {
            boolean ok = artistaDAO.insertar(nombre, desc, genero, pais);
            SwingUtilities.invokeLater(() -> {
                if (ok) {
                    mensaje("✅ Artista \"" + nombre + "\" creado correctamente.", true);
                    limpiar();
                } else {
                    mensaje("❌ Error al guardar. Revisa los datos.", false);
                }
            });
        }).start();
    }

    private void limpiar() {
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtGenero.setText("");
        txtPais.setText("");
        lblMensaje.setText(" ");
    }

    private void mensaje(String texto, boolean exito) {
        lblMensaje.setText(texto);
        lblMensaje.setForeground(exito ? new Color(0x4CAF50) : new Color(0xFF4444));
    }

    private JTextField campo(String placeholder) {
        JTextField f = new JTextField();
        f.setBackground(UITheme.CARD);
        f.setForeground(UITheme.TEXT);
        f.setCaretColor(UITheme.ACCENT);
        f.setFont(UITheme.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.putClientProperty("JTextField.placeholderText", placeholder);
        return f;
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JButton crearBoton(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(UITheme.ACCENT);
        b.setForeground(UITheme.BG);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(160, 36));
        return b;
    }

    private JButton crearBotonSecundario(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(UITheme.SURFACE);
        b.setForeground(UITheme.MUTED);
        b.setFont(UITheme.FONT_BODY);
        b.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(100, 36));
        return b;
    }
}