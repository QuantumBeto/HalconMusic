package com.halconmusic.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.halconmusic.dao.AlbumDAO;
import com.halconmusic.dao.ArtistaDAO;
import com.halconmusic.ui.UITheme;

public class CrearAlbumPanel extends JPanel {

    private final AlbumDAO   albumDAO   = new AlbumDAO();
    private final ArtistaDAO artistaDAO = new ArtistaDAO();

    private JTextField        txtTitulo;
    private JTextField        txtGenero;
    private JTextField        txtFecha;
    private JTextField        txtDuracion;
    private JTextField        txtCompositores;
    private JComboBox<String> comboArtista;
    private List<String[]>    artistas;
    private JLabel            lblMensaje;

    public CrearAlbumPanel() {
        setBackground(UITheme.BG);
        setLayout(new GridBagLayout());
        add(buildCard(), new GridBagConstraints());
    }

    private JPanel buildCard() {
        artistas = artistaDAO.obtenerIdsYNombres();

        JPanel card = new JPanel();
        card.setBackground(UITheme.SURFACE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(32, 40, 32, 40)));
        card.setPreferredSize(new Dimension(500, 520));

        JLabel titulo = new JLabel("Nuevo Álbum");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titulo.setForeground(UITheme.TEXT);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtTitulo      = campo("Título del álbum");
        txtGenero      = campo("Género (ej: Reggaeton, Pop)");
        txtFecha       = campo("Año de lanzamiento (ej: 2023)");
        txtDuracion    = campo("Duración total en segundos (ej: 1200)");
        txtCompositores = campo("Compositores (separados por coma)");

        String[] nombres = artistas.stream().map(a -> a[1]).toArray(String[]::new);
        comboArtista = new JComboBox<>(nombres);
        comboArtista.setBackground(UITheme.CARD);
        comboArtista.setForeground(UITheme.TEXT);
        comboArtista.setFont(UITheme.FONT_BODY);
        comboArtista.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        comboArtista.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboArtista.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? UITheme.ACCENT : UITheme.CARD);
                setForeground(isSelected ? UITheme.BG     : UITheme.TEXT);
                setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                return this;
            }
        });

        lblMensaje = new JLabel(" ");
        lblMensaje.setFont(UITheme.FONT_SMALL);
        lblMensaje.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnGuardar = crearBoton("Guardar Álbum");
        btnGuardar.addActionListener(e -> guardar());
        JButton btnLimpiar = crearBotonSecundario("Limpiar");
        btnLimpiar.addActionListener(e -> limpiar());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        botones.setOpaque(false);
        botones.setAlignmentX(Component.LEFT_ALIGNMENT);
        botones.add(btnGuardar);
        botones.add(btnLimpiar);

        card.add(titulo);
        card.add(Box.createVerticalStrut(20));
        card.add(etiqueta("Título *"));         card.add(Box.createVerticalStrut(4));
        card.add(txtTitulo);                    card.add(Box.createVerticalStrut(10));
        card.add(etiqueta("Artista *"));        card.add(Box.createVerticalStrut(4));
        card.add(comboArtista);                 card.add(Box.createVerticalStrut(10));
        card.add(etiqueta("Género *"));         card.add(Box.createVerticalStrut(4));
        card.add(txtGenero);                    card.add(Box.createVerticalStrut(10));
        card.add(etiqueta("Año *"));            card.add(Box.createVerticalStrut(4));
        card.add(txtFecha);                     card.add(Box.createVerticalStrut(10));
        card.add(etiqueta("Duración (seg) *")); card.add(Box.createVerticalStrut(4));
        card.add(txtDuracion);                  card.add(Box.createVerticalStrut(10));
        card.add(etiqueta("Compositores *"));   card.add(Box.createVerticalStrut(4));
        card.add(txtCompositores);              card.add(Box.createVerticalStrut(14));
        card.add(lblMensaje);                   card.add(Box.createVerticalStrut(8));
        card.add(botones);

        return card;
    }

    private void guardar() {
        String tit  = txtTitulo.getText().trim();
        String gen  = txtGenero.getText().trim();
        String comp = txtCompositores.getText().trim();
        if (tit.isEmpty() || gen.isEmpty() || comp.isEmpty()
                || txtFecha.getText().isBlank() || txtDuracion.getText().isBlank()) {
            mensaje("Completa todos los campos obligatorios.", false); return;
        }
        int fecha, duracion;
        try {
            fecha    = Integer.parseInt(txtFecha.getText().trim());
            duracion = Integer.parseInt(txtDuracion.getText().trim());
        } catch (NumberFormatException ex) {
            mensaje("Año y duración deben ser números enteros.", false); return;
        }
        int    idx   = comboArtista.getSelectedIndex();
        String idArt = artistas.get(idx)[0];
        final  String tituloFinal = tit;

        new Thread(() -> {
            boolean ok = albumDAO.insertar(tituloFinal, 0, gen, fecha, duracion, comp, idArt);
            SwingUtilities.invokeLater(() -> {
                if (ok) { mensaje("Álbum \"" + tituloFinal + "\" creado.", true); limpiar(); }
                else    { mensaje("Error al guardar.", false); }
            });
        }).start();
    }

    private void limpiar() {
        txtTitulo.setText(""); txtGenero.setText(""); txtFecha.setText("");
        txtDuracion.setText(""); txtCompositores.setText("");
        if (comboArtista.getItemCount() > 0) comboArtista.setSelectedIndex(0);
        lblMensaje.setText(" ");
    }

    private void mensaje(String t, boolean ok) {
        lblMensaje.setText(UITheme.emoji(ok ? "✅" : "❌", t));
        lblMensaje.setForeground(ok ? new Color(0x4CAF50) : new Color(0xFF4444));
    }

    private JTextField campo(String ph) {
        JTextField f = new JTextField();
        f.setBackground(UITheme.CARD); f.setForeground(UITheme.TEXT);
        f.setCaretColor(UITheme.ACCENT); f.setFont(UITheme.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.putClientProperty("JTextField.placeholderText", ph);
        return f;
    }

    private JLabel etiqueta(String t) {
        JLabel l = new JLabel(t); l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.MUTED); l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JButton crearBoton(String t) {
        JButton b = new JButton(t); b.setBackground(UITheme.ACCENT);
        b.setForeground(UITheme.BG); b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setOpaque(true); b.setContentAreaFilled(true);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(160, 36)); return b;
    }

    private JButton crearBotonSecundario(String t) {
        JButton b = new JButton(t); b.setBackground(UITheme.SURFACE);
        b.setForeground(UITheme.MUTED); b.setFont(UITheme.FONT_BODY);
        b.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(100, 36)); return b;
    }
}