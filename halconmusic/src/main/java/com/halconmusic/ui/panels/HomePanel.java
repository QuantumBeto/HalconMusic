package com.halconmusic.ui.panels;

import com.halconmusic.dao.AlbumDAO;
import com.halconmusic.dao.ArtistaDAO;
import com.halconmusic.dao.CancionDAO;
import com.halconmusic.model.Album;
import com.halconmusic.model.Artista;
import com.halconmusic.model.Cancion;
import com.halconmusic.ui.UITheme;
import com.halconmusic.ui.components.RoundedPanel;
import com.halconmusic.ui.components.SongRow;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel de inicio — Muestra artistas seguidos, álbumes recientes y escuchado recientemente.
 */
public class HomePanel extends JPanel {

    private final ArtistaDAO        artistaDAO;
    private final AlbumDAO          albumDAO;
    private final CancionDAO        cancionDAO;
    private final Consumer<Cancion> onPlay;
    private final String            idUsuario;

    public HomePanel(Consumer<Cancion> onPlay, String idUsuario) {
        this.onPlay     = onPlay;
        this.idUsuario  = idUsuario;
        this.artistaDAO = new ArtistaDAO();
        this.albumDAO   = new AlbumDAO();
        this.cancionDAO = new CancionDAO();

        setBackground(UITheme.BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        cargar();
    }

    private void cargar() {
        add(buildHero());
        add(Box.createVerticalStrut(28));

        add(sectionHeader("Artistas"));
        add(Box.createVerticalStrut(12));
        add(buildArtistasGrid());
        add(Box.createVerticalStrut(28));

        add(sectionHeader("Álbumes recientes"));
        add(Box.createVerticalStrut(12));
        add(buildAlbumesGrid());
        add(Box.createVerticalStrut(28));

        add(sectionHeader("Escuchado recientemente"));
        add(Box.createVerticalStrut(12));
        add(buildHistorialReciente());
    }

    private JPanel buildHero() {
        RoundedPanel hero = new RoundedPanel(12, new Color(0x1C, 0x16, 0x00));
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JLabel lblLabel = label("DESTACADO DE LA SEMANA", UITheme.ACCENT, UITheme.FONT_LABEL);
        JLabel lblTitle = label("Tu resumen semanal está listo",  UITheme.TEXT,  UITheme.FONT_TITLE);
        JLabel lblSub   = label("Descubre tu género favorito y tus artistas más escuchados.", UITheme.MUTED, UITheme.FONT_BODY);

        hero.add(lblLabel);
        hero.add(Box.createVerticalStrut(6));
        hero.add(lblTitle);
        hero.add(Box.createVerticalStrut(4));
        hero.add(lblSub);

        return hero;
    }

    private JPanel buildArtistasGrid() {
        List<Artista> artistas = artistaDAO.obtenerTodos();
        JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        for (Artista a : artistas) grid.add(buildArtistCard(a));
        return grid;
    }

    private JPanel buildArtistCard(Artista a) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.RADIUS, UITheme.RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.setPreferredSize(new Dimension(140, 185));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel img = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.SURFACE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                if (a.getPortada() != null) {
                    Shape clip = new java.awt.geom.Ellipse2D.Float(0, 0, getWidth(), getHeight());
                    g2.setClip(clip);
                    g2.drawImage(a.getPortada(), 0, 0, getWidth(), getHeight(), null);
                } else {
                    g2.setColor(UITheme.MUTED);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 32));
                    g2.drawString("♪", 32, 72);
                }
                g2.dispose();
            }
        };
        img.setOpaque(false);
        img.setPreferredSize(new Dimension(120, 120));
        img.setMaximumSize(new Dimension(120, 120));

        card.add(img);
        card.add(Box.createVerticalStrut(8));
        card.add(label(a.getNombre(),           UITheme.TEXT,  UITheme.FONT_BODY));
        card.add(label(a.getGeneroPrincipal(),  UITheme.MUTED, UITheme.FONT_SMALL));
        card.add(label(a.getTotalCanciones() + " canciones", UITheme.ACCENT, UITheme.FONT_SMALL));

        return card;
    }

    private JPanel buildAlbumesGrid() {
        List<Album> albumes = albumDAO.obtenerTodos();
        JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        for (Album al : albumes) grid.add(buildAlbumCard(al));
        return grid;
    }

    private JPanel buildAlbumCard(Album al) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.RADIUS, UITheme.RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.setPreferredSize(new Dimension(140, 185));

        JPanel img = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                if (al.getPortada() != null) {
                    Shape clip = new java.awt.geom.RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8);
                    g2.setClip(clip);
                    g2.drawImage(al.getPortada(), 0, 0, getWidth(), getHeight(), null);
                } else {
                    g2.setColor(UITheme.MUTED);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 32));
                    g2.drawString("◉", 40, 72);
                }
                g2.dispose();
            }
        };
        img.setOpaque(false);
        img.setPreferredSize(new Dimension(120, 120));
        img.setMaximumSize(new Dimension(120, 120));

        card.add(img);
        card.add(Box.createVerticalStrut(8));
        card.add(label(al.getTitulo(),               UITheme.TEXT,  UITheme.FONT_BODY));
        card.add(label(al.getNombreArtista(),         UITheme.MUTED, UITheme.FONT_SMALL));
        card.add(label(String.valueOf(al.getFecha()), UITheme.ACCENT, UITheme.FONT_SMALL));

        return card;
    }

    /** Req. 4 + 5: historial reciente con botón ♥ en cada fila */
    private JPanel buildHistorialReciente() {
        List<Cancion> historial = cancionDAO.obtenerHistorialUsuario(idUsuario);
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        int i = 1;
        for (Cancion c : historial) {
            final Cancion cancion = c;
            // Pasa idUsuario → SongRow mostrará el botón ♥ para Me Gusta (Req. 5)
            SongRow row = new SongRow(i++, c, () -> onPlay.accept(cancion), idUsuario);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(row);
            p.add(Box.createVerticalStrut(2));
        }

        if (historial.isEmpty()) {
            JLabel vacio = new JLabel("Aún no hay canciones en tu historial.");
            vacio.setFont(UITheme.FONT_BODY);
            vacio.setForeground(UITheme.MUTED);
            p.add(vacio);
        }

        return p;
    }

    // ── Helpers ───────────────────────────────────────────
    private JLabel label(String text, Color color, Font font) {
        JLabel l = new JLabel(text == null ? "" : text);
        l.setForeground(color);
        l.setFont(font);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel sectionHeader(String title) {
        JLabel l = new JLabel(title);
        l.setFont(UITheme.FONT_SECTION);
        l.setForeground(UITheme.TEXT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
}
