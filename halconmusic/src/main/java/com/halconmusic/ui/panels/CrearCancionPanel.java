package com.halconmusic.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.halconmusic.dao.AlbumDAO;
import com.halconmusic.dao.ArtistaDAO;
import com.halconmusic.dao.CancionDAO;
import com.halconmusic.ui.UITheme;

public class CrearCancionPanel extends JPanel {

    private final CancionDAO      cancionDAO  = new CancionDAO();
    private final ArtistaDAO      artistaDAO  = new ArtistaDAO();
    private final AlbumDAO        albumDAO    = new AlbumDAO();

    private JTextField        txtNombre;
    private JTextField        txtGenero;
    private JTextField        txtEmocion;
    private JTextField        txtDuracion;
    private JTextField        txtFecha;
    private JTextField        txtFt;
    private JTextArea         txtLetra;
    private JComboBox<String> comboArtista;
    private JComboBox<String> comboAlbum;
    private List<String[]>    artistas;
    private List<String[]>    albumesList;
    private JLabel            lblMensaje;
    private java.io.File      archivoPortada;
    private java.io.File      archivoMusica;
    private java.io.File      archivoVideo;
    private JLabel            lblPortada;
    private JLabel            lblMusica;
    private JLabel            lblVideo;

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

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(UITheme.BG);
        outer.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JPanel card = new JPanel();
        card.setBackground(UITheme.SURFACE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(32, 40, 32, 40)));

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

        // Combo Álbum
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

        // ── Selector Portada ──────────────────────────────────────────
        JButton btnPortada = crearBotonArchivo(UITheme.emoji("📁", "Elegir imagen"));
        btnPortada.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Imágenes (JPG, PNG)", "jpg", "jpeg", "png"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                archivoPortada = fc.getSelectedFile();
                lblPortada.setText(UITheme.emoji("✅", archivoPortada.getName()));
                lblPortada.setForeground(new Color(0x4CAF50));
            }
        });
        lblPortada = new JLabel("Sin imagen seleccionada");
        lblPortada.setFont(UITheme.FONT_SMALL);
        lblPortada.setForeground(UITheme.MUTED);
        lblPortada.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel rowPortada = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rowPortada.setOpaque(false);
        rowPortada.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowPortada.add(btnPortada);
        rowPortada.add(lblPortada);

        // ── Selector Música ───────────────────────────────────────────
        JButton btnMusica = crearBotonArchivo(UITheme.emoji("📁", "Elegir MP3"));
        btnMusica.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Audio MP3", "mp3"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                archivoMusica = fc.getSelectedFile();
                lblMusica.setText(UITheme.emoji("✅", archivoMusica.getName()));
                lblMusica.setForeground(new Color(0x4CAF50));
            }
        });
        lblMusica = new JLabel("Sin archivo de audio seleccionado");
        lblMusica.setFont(UITheme.FONT_SMALL);
        lblMusica.setForeground(UITheme.MUTED);
        lblMusica.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel rowMusica = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rowMusica.setOpaque(false);
        rowMusica.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowMusica.add(btnMusica);
        rowMusica.add(lblMusica);

        // ── Selector Video ────────────────────────────────────────────
        JButton btnVideo = crearBotonArchivo(UITheme.emoji("🎬", "Elegir MP4"));
        btnVideo.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Video MP4", "mp4"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                archivoVideo = fc.getSelectedFile();
                lblVideo.setText(UITheme.emoji("✅", archivoVideo.getName()));
                lblVideo.setForeground(new Color(0x4CAF50));
            }
        });
        lblVideo = new JLabel("Sin archivo de video seleccionado (opcional)");
        lblVideo.setFont(UITheme.FONT_SMALL);
        lblVideo.setForeground(UITheme.MUTED);
        lblVideo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel rowVideo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rowVideo.setOpaque(false);
        rowVideo.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowVideo.add(btnVideo);
        rowVideo.add(lblVideo);

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
        card.add(etq("Nombre *"));               card.add(Box.createVerticalStrut(4));
        card.add(txtNombre);                      card.add(Box.createVerticalStrut(10));
        card.add(etq("Artista *"));               card.add(Box.createVerticalStrut(4));
        card.add(comboArtista);                   card.add(Box.createVerticalStrut(10));
        card.add(etq("Álbum"));                   card.add(Box.createVerticalStrut(4));
        card.add(comboAlbum);                     card.add(Box.createVerticalStrut(10));
        card.add(etq("Género *"));                card.add(Box.createVerticalStrut(4));
        card.add(txtGenero);                      card.add(Box.createVerticalStrut(10));
        card.add(etq("Emoción *"));               card.add(Box.createVerticalStrut(4));
        card.add(txtEmocion);                     card.add(Box.createVerticalStrut(10));
        card.add(etq("Duración (seg) *"));        card.add(Box.createVerticalStrut(4));
        card.add(txtDuracion);                    card.add(Box.createVerticalStrut(10));
        card.add(etq("Año *"));                   card.add(Box.createVerticalStrut(4));
        card.add(txtFecha);                       card.add(Box.createVerticalStrut(10));
        card.add(etq("Featuring (opcional)"));    card.add(Box.createVerticalStrut(4));
        card.add(txtFt);                          card.add(Box.createVerticalStrut(10));
        card.add(etq("Letra (opcional)"));        card.add(Box.createVerticalStrut(4));
        card.add(scrollLetra);                    card.add(Box.createVerticalStrut(14));
        card.add(etq("Portada *"));               card.add(Box.createVerticalStrut(4));
        card.add(rowPortada);                     card.add(Box.createVerticalStrut(10));
        card.add(etq("Archivo de audio (MP3) *")); card.add(Box.createVerticalStrut(4));
        card.add(rowMusica);                      card.add(Box.createVerticalStrut(10));
        card.add(etq("Video (MP4, opcional)"));   card.add(Box.createVerticalStrut(4));
        card.add(rowVideo);                       card.add(Box.createVerticalStrut(14));
        card.add(lblMensaje);                     card.add(Box.createVerticalStrut(8));
        card.add(botones);

        outer.add(card, BorderLayout.CENTER);
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
            msg("Completa todos los campos obligatorios.", false); return;
        }

        int duracion, fecha;
        try {
            duracion = Integer.parseInt(txtDuracion.getText().trim());
            fecha    = Integer.parseInt(txtFecha.getText().trim());
        } catch (NumberFormatException ex) {
            msg("Duración y año deben ser números enteros.", false); return;
        }

        int    idxArt   = comboArtista.getSelectedIndex();
        String idArtista = artistas.get(idxArt)[0];
        String nombreArt = artistas.get(idxArt)[1];
        int    idxAlb   = comboAlbum.getSelectedIndex();
        String idAlbum  = (idxAlb == 0) ? null : albumesList.get(idxAlb - 1)[0];

        if (archivoPortada == null) { msg("Selecciona una imagen de portada.", false); return; }
        if (archivoMusica  == null) { msg("Selecciona el archivo de audio MP3.", false); return; }

        final String        nFinal   = nombre;
        final java.io.File  fPortada = archivoPortada;
        final java.io.File  fMusica  = archivoMusica;
        final java.io.File  fVideo   = archivoVideo;

        new Thread(() -> {
            boolean ok = cancionDAO.insertar(
                nFinal, genero, nombreArt, emocion,
                duracion, fecha, ft.isEmpty() ? null : ft,
                letra.isEmpty() ? null : letra, idArtista, idAlbum,
                fPortada, fMusica, fVideo);
            SwingUtilities.invokeLater(() -> {
                if (ok) { msg("Canción \"" + nFinal + "\" creada correctamente.", true); limpiar(); }
                else    { msg("Error al guardar la canción.", false); }
            });
        }).start();
    }

    private void limpiar() {
        archivoPortada = null;
        archivoMusica  = null;
        archivoVideo   = null;
        lblPortada.setText("Sin imagen seleccionada");
        lblPortada.setForeground(UITheme.MUTED);
        lblMusica.setText("Sin archivo de audio seleccionado");
        lblMusica.setForeground(UITheme.MUTED);
        lblVideo.setText("Sin archivo de video seleccionado (opcional)");
        lblVideo.setForeground(UITheme.MUTED);
        txtNombre.setText(""); txtGenero.setText(""); txtEmocion.setText("");
        txtDuracion.setText(""); txtFecha.setText(""); txtFt.setText("");
        txtLetra.setText("");
        if (comboArtista.getItemCount() > 0) comboArtista.setSelectedIndex(0);
        if (comboAlbum.getItemCount()   > 0) comboAlbum.setSelectedIndex(0);
        lblMensaje.setText(" ");
    }

    private void msg(String t, boolean ok) {
        lblMensaje.setText(UITheme.emoji(ok ? "✅" : "❌", t));
        lblMensaje.setForeground(ok ? new Color(0x4CAF50) : new Color(0xFF4444));
    }

    private void estilizarCombo(JComboBox<String> c) {
        c.setBackground(UITheme.CARD); c.setForeground(UITheme.TEXT);
        c.setFont(UITheme.FONT_BODY);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setRenderer(new javax.swing.DefaultListCellRenderer() {
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
        b.setOpaque(true); b.setContentAreaFilled(true);
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

    /** Botón de selección de archivo — fondo CARD, texto con emoji HTML */
    private JButton crearBotonArchivo(String htmlTexto) {
        JButton b = new JButton(htmlTexto);
        b.setBackground(UITheme.CARD);
        b.setForeground(UITheme.TEXT);
        b.setFont(UITheme.FONT_BODY);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(true);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(175, 34));
        return b;
    }
}