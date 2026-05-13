package com.halconmusic.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.halconmusic.dao.AlbumDAO;
import com.halconmusic.dao.ArtistaDAO;
import com.halconmusic.dao.CancionDAO;
import com.halconmusic.model.Album;
import com.halconmusic.model.Artista;
import com.halconmusic.model.Cancion;
import com.halconmusic.ui.UITheme;
import com.halconmusic.ui.components.SongRow;

/**
 * Panel de búsqueda global — busca en canciones, artistas y álbumes.
 */
public class BuscarPanel extends JPanel {

    private final CancionDAO        cancionDAO;
    private final ArtistaDAO        artistaDAO;
    private final AlbumDAO          albumDAO;
    private final Consumer<Cancion> onPlay;
    private final Consumer<Cancion> onMeGusta;
    private final String            idUsuario;
    private       JPanel            resultsArea;

    public BuscarPanel(Consumer<Cancion> onPlay, Consumer<Cancion> onMeGusta, String idUsuario) {
        this.onPlay     = onPlay;
        this.onMeGusta  = onMeGusta;
        this.idUsuario  = idUsuario;
        this.cancionDAO = new CancionDAO();
        this.artistaDAO = new ArtistaDAO();
        this.albumDAO   = new AlbumDAO();

        setBackground(UITheme.BG);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(buildSearchBar(), BorderLayout.NORTH);

        resultsArea = new JPanel();
        resultsArea.setOpaque(false);
        resultsArea.setLayout(new BoxLayout(resultsArea, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(resultsArea);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        add(scroll, BorderLayout.CENTER);

        mostrarEstadoInicial();
    }

    // ── Barra de búsqueda ─────────────────────────────────
    private JPanel buildSearchBar() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JLabel titulo = new JLabel("Buscar");
        titulo.setFont(UITheme.FONT_SECTION);
        titulo.setForeground(UITheme.TEXT);

        JPanel searchBox = new JPanel(new BorderLayout(10, 0));
        searchBox.setBackground(UITheme.SURFACE);
        searchBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        JLabel lupa = new JLabel("⌕");
        lupa.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        lupa.setForeground(UITheme.MUTED);

        JTextField field = new JTextField();
        field.setBackground(UITheme.SURFACE);
        field.setForeground(UITheme.TEXT);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setBorder(null);
        field.setCaretColor(UITheme.ACCENT);
        field.putClientProperty("JTextField.placeholderText", "Empieza a escribir...");

        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate (javax.swing.event.DocumentEvent e) { buscar(field.getText()); }
            public void removeUpdate (javax.swing.event.DocumentEvent e) {
                if (field.getText().isBlank()) mostrarEstadoInicial();
                else buscar(field.getText());
            }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { buscar(field.getText()); }
        });

        searchBox.add(lupa,  BorderLayout.WEST);
        searchBox.add(field, BorderLayout.CENTER);

        p.add(searchBox,  BorderLayout.CENTER);
        return p;
    }

    // ── Req. 1 & 2: Búsqueda por inicio de palabra ────────
    private void buscar(String termino) {
        if (termino.isBlank()) { mostrarEstadoInicial(); return; }

        List<Cancion> canciones = cancionDAO.buscar(termino);
        List<Artista> artistas  = artistaDAO.buscarPorNombre(termino);
        List<Album>   albumes   = albumDAO.buscar(termino);

        resultsArea.removeAll();

        int total = canciones.size() + artistas.size() + albumes.size();
        JLabel lblTotal = new JLabel("Se encontraron " + total + " resultados para \"" + termino + "\"");
        lblTotal.setFont(UITheme.FONT_SMALL);
        lblTotal.setForeground(UITheme.MUTED);
        lblTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultsArea.add(lblTotal);
        resultsArea.add(Box.createVerticalStrut(18));

        if (!canciones.isEmpty()) {
            resultsArea.add(seccion("Canciones (" + canciones.size() + ")"));
            int i = 1;
            for (Cancion c : canciones) {
                final Cancion cancion = c;
                SongRow row = new SongRow(i++, c, () -> onPlay.accept(cancion), idUsuario, () -> onMeGusta.accept(cancion));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
                resultsArea.add(row);
                resultsArea.add(Box.createVerticalStrut(2));
            }
            resultsArea.add(Box.createVerticalStrut(18));
        }

        if (!artistas.isEmpty()) {
            resultsArea.add(seccion("Artistas (" + artistas.size() + ")"));
            JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            grid.setOpaque(false);
            grid.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (Artista a : artistas) grid.add(buildArtistaPill(a));
            resultsArea.add(grid);
            resultsArea.add(Box.createVerticalStrut(18));
        }

        if (!albumes.isEmpty()) {
            resultsArea.add(seccion("Álbumes (" + albumes.size() + ")"));
            JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            grid.setOpaque(false);
            grid.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (Album al : albumes) grid.add(buildAlbumPill(al));
            resultsArea.add(grid);
        }

        if (total == 0) {
            JLabel lblVacio = new JLabel("No se encontraron canciones que inicien con \"" + termino + "\".");
            lblVacio.setFont(UITheme.FONT_BODY);
            lblVacio.setForeground(UITheme.MUTED);
            lblVacio.setAlignmentX(Component.LEFT_ALIGNMENT);
            resultsArea.add(lblVacio);
        }

        resultsArea.revalidate();
        resultsArea.repaint();
    }

    // ── Estado inicial con chips de género ────────────────
    private void mostrarEstadoInicial() {
        resultsArea.removeAll();

        JLabel hint = new JLabel("Escribe algo para buscar canciones...");
        hint.setFont(UITheme.FONT_BODY);
        hint.setForeground(UITheme.MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblGeneros = new JLabel("Explorar por género");
        lblGeneros.setFont(UITheme.FONT_SECTION);
        lblGeneros.setForeground(UITheme.TEXT);
        lblGeneros.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        chips.setOpaque(false);
        chips.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] generos = {"Reggaeton","Corridos Tumbados","Balada","Ranchera","Banda","Grupero","Pop"};
        Color[]  colores = {
            new Color(0x1A, 0x5C, 0x96), new Color(0x6A, 0x1A, 0x1A),
            new Color(0x1A, 0x6A, 0x3A), new Color(0x6A, 0x4A, 0x1A),
            new Color(0x4A, 0x1A, 0x6A), new Color(0x1A, 0x4A, 0x6A),
            new Color(0x6A, 0x1A, 0x4A)
        };

        for (int i = 0; i < generos.length; i++) {
            final String g = generos[i];
            final Color  c = colores[i % colores.length];
            JPanel chip = buildGeneroChip(g, c);
            chip.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { buscar(g); }
            });
            chips.add(chip);
        }

        resultsArea.add(hint);
        resultsArea.add(Box.createVerticalStrut(24));
        resultsArea.add(lblGeneros);
        resultsArea.add(Box.createVerticalStrut(10));
        resultsArea.add(chips);
        resultsArea.revalidate();
        resultsArea.repaint();
    }

    private JPanel buildGeneroChip(String genero, Color color) {
        JPanel p = new JPanel() {
            boolean hover = false;
            { addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                @Override public void mouseExited (MouseEvent e) { hover = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? color.brighter() : color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 8));
        p.setPreferredSize(new Dimension(140, 46));
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel l = new JLabel(genero);
        l.setFont(UITheme.FONT_BODY);
        l.setForeground(UITheme.TEXT);
        p.add(l);
        return p;
    }

    private JPanel buildArtistaPill(Artista a) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.RADIUS, UITheme.RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        p.setPreferredSize(new Dimension(200, 52));

        JPanel av = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.SURFACE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                if (a.getPortada() != null) {
                    g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, getWidth(), getHeight()));
                    g2.drawImage(a.getPortada(), 0, 0, getWidth(), getHeight(), null);
                }
                g2.dispose();
            }
        };
        av.setOpaque(false);
        av.setPreferredSize(new Dimension(34, 34));

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 1));
        info.setOpaque(false);
        JLabel n = new JLabel(a.getNombre());          n.setFont(UITheme.FONT_BODY);  n.setForeground(UITheme.TEXT);
        JLabel g = new JLabel(a.getGeneroPrincipal()); g.setFont(UITheme.FONT_SMALL); g.setForeground(UITheme.MUTED);
        info.add(n); info.add(g);

        p.add(av); p.add(info);
        return p;
    }

    private JPanel buildAlbumPill(Album al) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.RADIUS, UITheme.RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        p.setPreferredSize(new Dimension(200, 52));

        JPanel img = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UITheme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
                if (al.getPortada() != null) {
                    g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 5, 5));
                    g2.drawImage(al.getPortada(), 0, 0, getWidth(), getHeight(), null);
                }
                g2.dispose();
            }
        };
        img.setOpaque(false);
        img.setPreferredSize(new Dimension(34, 34));

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 1));
        info.setOpaque(false);
        JLabel t = new JLabel(al.getTitulo());        t.setFont(UITheme.FONT_BODY);  t.setForeground(UITheme.TEXT);
        JLabel a = new JLabel(al.getNombreArtista()); a.setFont(UITheme.FONT_SMALL); a.setForeground(UITheme.MUTED);
        info.add(t); info.add(a);

        p.add(img); p.add(info);
        return p;
    }

    private JLabel seccion(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(UITheme.FONT_SECTION);
        l.setForeground(UITheme.TEXT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        return l;
    }
}