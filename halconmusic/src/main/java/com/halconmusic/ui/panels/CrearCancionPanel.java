package com.halconmusic.ui.panels;

import com.halconmusic.dao.AlbumDAO;
import com.halconmusic.dao.ArtistaDAO;
import com.halconmusic.dao.CancionDAO;
import com.halconmusic.ui.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * REQ. 2 — Panel de creación de Canciones.
 */
public class CrearCancionPanel extends JPanel {

    private final CancionDAO  cancionDAO  = new CancionDAO();
    private final ArtistaDAO  artistaDAO  = new ArtistaDAO();
    private final AlbumDAO    albumDAO    = new AlbumDAO();

    private JTextField    txtNombre;
    private JTextField    txtGenero;
    private JTextField    txtEmocion;
    private JTextField    txtDuracion;
    private JTextField    txtFecha;
    private JTextField    txtFt;
    private JTextArea     txtLetra;
    private JComboBox<String> comboArtista;
    private JComboBox<String> comboAlbum;
    private List<String[]>    artistas;
    private List<String[]>    albumesList;
    private JLabel        lblMensaje;

    public CrearCancionPanel() {
        setBackground(UITheme.BG);
        setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane(buildCard());
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildCard() {
        artistas    = artistaDAO.obtenerIdsYNombres();
        albumesList = albumDAO.obtenerTodos().stream()
                          .map(a -> new String[]{ a.getIdAlbum(), a.getTitulo() })
                          .collect(java.util.stream.Collectors.toList());

        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(UITheme.BG);

        JPanel card = new JPanel();
        card.setBackground(UITheme.SURFACE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(32, 40, 32, 40)));
        card.setPreferredSize(new Dimension(520, 680));

        JLabel titulo = new JLabel("Nueva Canción");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titulo.setForeground(UITheme.TEXT);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtNombre   = campo("Nombre de la canción");
        txtGenero   = campo("Género (ej: Reggaeton, Balada)");
        txtEmocion  = campo("Emoción (ej: Alegre, Triste, Romántico)");
        txtDuracion = campo("Duración en segundos (ej: 210)");
        txtFecha    = campo("Año de lanzamiento (ej: 2023)");
        txtFt       = campo("Artista featuring (opcional)");

        // Combo Artista
        String[] nombresArt = artistas.stream().map(a -> a[1]).toArray(String[]::new);
        comboArtista = new JComboBox<>(nombresArt);
        estilizarCombo(comboArtista);

        // Combo Álbum (primer item = Sin álbum)
        String[] nombresAlb = new String[albumesList.size() + 1];
        nombresAlb[0] = "— Sin álbum —";
        for (int i = 0; i < albumesList.size(); i++) nombresAlb[i+1] = albumesList.get(i)[1];
        comboAlbum = new JComboBox<>(nombresAlb);
        estilizarCombo(comboAlbum);

        // Área de letra
        txtLetra = new JTextArea(5, 20);
        txtLetra.setBackground(UITheme.CARD);
        txtLetra.setForeground(UITheme.TEXT);
        txtLetra.setCaretColor(UITheme.ACCENT);
        txtLetra.setFont(UITheme.FONT_BODY);
        txtLetra.setLineWrap(true);
        txtLetra.setWrapStyleWord(true);
        txtLetra.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        JScrollPane scrollLetra = new JScrollPane(txtLetra);
        scrollLetra.setBorder(null);
        scrollLetra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        scrollLetra.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblMensaje = new JLabel(" ");
        lblMensaje.setFont(UITheme.FONT_SMALL);
        lblMensaje.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnGuardar = crearBoton("Guardar Canción");
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
        card.add(etq("Nombre *"));          card.add(Box.createVerticalStrut(4));
        card.add(txtNombre);                 card.add(Box.createVerticalStrut(10));
        card.add(etq("Artista *"));          card.add(Box.createVerticalStrut(4));
        card.add(comboArtista);              card.add(Box.createVerticalStrut(10));
        card.add(etq("Álbum"));              card.add(Box.createVerticalStrut(4));
        card.add(comboAlbum);               card.add(Box.createVerticalStrut(10));
        card.add(etq("Género *"));           card.add(Box.createVerticalStrut(4));
        card.add(txtGenero);                 card.add(Box.createVerticalStrut(10));
        card.add(etq("Emoción *"));          card.add(Box.createVerticalStrut(4));
        card.add(txtEmocion);                card.add(Box.createVerticalStrut(10));
        card.add(etq("Duración (seg) *"));   card.add(Box.createVerticalStrut(4));
        card.add(txtDuracion);               card.add(Box.createVerticalStrut(10));
        card.add(etq("Año *"));              card.add(Box.createVerticalStrut(4));
        card.add(txtFecha);                  card.add(Box.createVerticalStrut(10));
        card.add(etq("Featuring (opcional)")); card.add(Box.createVerticalStrut(4));
        card.add(txtFt);                     card.add(Box.createVerticalStrut(10));
        card.add(etq("Letra (opcional)"));   card.add(Box.createVerticalStrut(4));
        card.add(scrollLetra);               card.add(Box.createVerticalStrut(14));
        card.add(lblMensaje);                card.add(Box.createVerticalStrut(8));
        card.add(botones);

        outer.add(card, new GridBagConstraints());
        return outer;
    }

    private void guardar() {
        String nombre  = txtNombre.getText().trim();
        String genero  = txtGenero.getText().trim();
        String emocion = txtEmocion.getText().trim();
        String ft      = txtFt.getText().trim();
        String letra   = txtLetra.getText().trim();

        if (nombre.isEmpty() || genero.isEmpty() || emocion.isEmpty()
                || txtDuracion.getText().isBlank() || txtFecha.getText().isBlank()) {
            msg("Completa todos los campos obligatorios.", false);
            return;
        }

        int duracion, fecha;
        try {
            duracion = Integer.parseInt(txtDuracion.getText().trim());
            fecha    = Integer.parseInt(txtFecha.getText().trim());
        } catch (NumberFormatException ex) {
            msg("Duración y año deben ser números enteros.", false);
            return;
        }

        int idxArt = comboArtista.getSelectedIndex();
        String idArtista = artistas.get(idxArt)[0];
        String nombreArt = artistas.get(idxArt)[1];

        int idxAlb   = comboAlbum.getSelectedIndex();
        String idAlbum = (idxAlb == 0) ? null : albumesList.get(idxAlb - 1)[0];

        final String nFinal = nombre;
        new Thread(() -> {
            boolean ok = cancionDAO.insertar(
                nFinal, genero, nombreArt, emocion,
                duracion, fecha, ft.isEmpty() ? null : ft,
                letra.isEmpty() ? null : letra, idArtista, idAlbum);
            SwingUtilities.invokeLater(() -> {
                if (ok) { msg("✅ Canción \"" + nFinal + "\" creada.", true); limpiar(); }
                else    { msg("❌ Error al guardar la canción.", false); }
            });
        }).start();
    }

    private void limpiar() {
        txtNombre.setText(""); txtGenero.setText(""); txtEmocion.setText("");
        txtDuracion.setText(""); txtFecha.setText(""); txtFt.setText("");
        txtLetra.setText("");
        if (comboArtista.getItemCount() > 0) comboArtista.setSelectedIndex(0);
        if (comboAlbum.getItemCount() > 0)   comboAlbum.setSelectedIndex(0);
        lblMensaje.setText(" ");
    }

    private void msg(String t, boolean ok) {
        lblMensaje.setText(t);
        lblMensaje.setForeground(ok ? new Color(0x4CAF50) : new Color(0xFF4444));
    }

    private void estilizarCombo(JComboBox<String> c) {
        c.setBackground(UITheme.CARD); c.setForeground(UITheme.TEXT);
        c.setFont(UITheme.FONT_BODY);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
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
    private JLabel etq(String t) {
        JLabel l = new JLabel(t); l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.MUTED); l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
    private JButton crearBoton(String t) {
        JButton b = new JButton(t); b.setBackground(UITheme.ACCENT);
        b.setForeground(UITheme.BG); b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(170, 36)); return b;
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
