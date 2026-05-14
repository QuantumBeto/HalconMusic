package com.halconmusic.ui.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import com.halconmusic.dao.AlbumDAO;
import com.halconmusic.dao.ArtistaDAO;
import com.halconmusic.dao.CancionDAO;
import com.halconmusic.model.Album;
import com.halconmusic.model.Artista;
import com.halconmusic.model.Cancion;
import com.halconmusic.ui.UITheme;
import com.halconmusic.ui.components.RoundedPanel;
import com.halconmusic.ui.components.SongRow;

public class HomePanel extends JPanel {

    private final ArtistaDAO        artistaDAO;
    private final AlbumDAO          albumDAO;
    private final CancionDAO        cancionDAO;
    private final Consumer<Cancion> onPlay;
    private final Consumer<Cancion> onMeGusta;
    private final String            idUsuario;

    public HomePanel(Consumer<Cancion> onPlay, Consumer<Cancion> onMeGusta, String idUsuario) {
        this.onPlay    = onPlay;
        this.onMeGusta = onMeGusta;
        this.idUsuario = idUsuario;
        artistaDAO = new ArtistaDAO();
        albumDAO   = new AlbumDAO();
        cancionDAO = new CancionDAO();

        setBackground(UITheme.BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        cargar();
    }

    private void cargar() {
        add(aligned(buildHero()));
        add(Box.createVerticalStrut(24));
        add(aligned(secHeader("Artistas")));
        add(Box.createVerticalStrut(10));
        add(buildCarrusel(artistaDAO.obtenerTodos(), 190));   // horizontal scroll
        add(Box.createVerticalStrut(24));
        add(aligned(secHeader("Álbumes recientes")));
        add(Box.createVerticalStrut(10));
        add(buildCarruselAlbumes(albumDAO.obtenerTodos(), 205));
        add(Box.createVerticalStrut(24));
        add(aligned(secHeader("Escuchado recientemente")));
        add(Box.createVerticalStrut(10));
        add(buildHistorial());
    }

    // ── Hero ─────────────────────────────────────────────
    private RoundedPanel buildHero() {
        RoundedPanel hero = new RoundedPanel(12, new Color(0x1C, 0x16, 0x00));
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 155));
        hero.add(lbl("DESTACADO DE LA SEMANA",                              UITheme.ACCENT, UITheme.FONT_LABEL));
        hero.add(Box.createVerticalStrut(6));
        hero.add(lbl("Tu resumen semanal está listo",                       UITheme.TEXT,   UITheme.FONT_TITLE));
        hero.add(Box.createVerticalStrut(4));
        hero.add(lbl("Descubre tu género favorito y tus artistas más escuchados.", UITheme.MUTED, UITheme.FONT_BODY));
        return hero;
    }

    // ── Carrusel horizontal genérico para artistas ───────
    private Component buildCarrusel(List<Artista> artistas, int cardH) {
        JPanel track = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        track.setOpaque(false);
        for (Artista a : artistas) track.add(buildArtistCard(a));

        JScrollPane carousel = new JScrollPane(track,
            JScrollPane.VERTICAL_SCROLLBAR_NEVER,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        carousel.setBorder(null);
        carousel.setOpaque(false);
        carousel.getViewport().setOpaque(false);
        // Scroll horizontal con rueda del ratón (shift+scroll o scroll horizontal)
        carousel.getViewport().addMouseWheelListener(e -> {
            JScrollBar bar = carousel.getHorizontalScrollBar();
            bar.setValue(bar.getValue() + (int)(e.getUnitsToScroll() * 20));
        });
        carousel.setMaximumSize(new Dimension(Integer.MAX_VALUE, cardH));
        carousel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return carousel;
    }

    private Component buildCarruselAlbumes(List<Album> albumes, int cardH) {
        JPanel track = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        track.setOpaque(false);
        for (Album al : albumes) track.add(buildAlbumCard(al));

        JScrollPane carousel = new JScrollPane(track,
            JScrollPane.VERTICAL_SCROLLBAR_NEVER,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        carousel.setBorder(null);
        carousel.setOpaque(false);
        carousel.getViewport().setOpaque(false);
        carousel.getViewport().addMouseWheelListener(e -> {
            JScrollBar bar = carousel.getHorizontalScrollBar();
            bar.setValue(bar.getValue() + (int)(e.getUnitsToScroll() * 20));
        });
        carousel.setMaximumSize(new Dimension(Integer.MAX_VALUE, cardH));
        carousel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return carousel;
    }

    // ── Tarjeta de artista ───────────────────────────────
    private JPanel buildArtistCard(Artista a) {
        JPanel card = roundCard();
        card.setPreferredSize(new Dimension(148, 188));

        // Imagen circular — siempre cuadrada con clip elíptico
        int IMG = 124;
        JPanel img = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,  RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setColor(UITheme.SURFACE);
                g2.fillOval(0, 0, IMG, IMG);
                if (a.getPortada() != null) {
                    g2.setClip(new Ellipse2D.Float(0, 0, IMG, IMG));
                    SongRow.drawCover(g2, a.getPortada(), 0, 0, IMG, IMG);
                } else {
                    g2.setColor(UITheme.MUTED);
                    g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 32));
                    g2.drawString("\u266A", 40, 72);
                }
                g2.dispose();
            }
        };
        img.setOpaque(false);
        img.setPreferredSize(new Dimension(IMG, IMG));
        img.setMinimumSize(new Dimension(IMG, IMG));
        img.setMaximumSize(new Dimension(IMG, IMG));
        img.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(img);
        card.add(Box.createVerticalStrut(8));
        card.add(cardLbl(a.getNombre(),           UITheme.TEXT,   UITheme.FONT_BODY));
        card.add(cardLbl(a.getGeneroPrincipal(),  UITheme.MUTED,  UITheme.FONT_SMALL));
        card.add(cardLbl(a.getTotalCanciones() + " canciones", UITheme.ACCENT, UITheme.FONT_SMALL));
        return card;
    }

    // ── Tarjeta de álbum ─────────────────────────────────
    private JPanel buildAlbumCard(Album al) {
        JPanel card = roundCard();
        card.setPreferredSize(new Dimension(148, 202));

        int IMG = 124;
        JPanel img = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setColor(UITheme.SURFACE);
                g2.fillRoundRect(0, 0, IMG, IMG, 8, 8);
                if (al.getPortada() != null) {
                    g2.setClip(new RoundRectangle2D.Float(0, 0, IMG, IMG, 8, 8));
                    SongRow.drawCover(g2, al.getPortada(), 0, 0, IMG, IMG);
                } else {
                    g2.setColor(UITheme.MUTED);
                    g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 32));
                    g2.drawString("\u25C9", 40, 72);
                }
                g2.dispose();
            }
        };
        img.setOpaque(false);
        img.setPreferredSize(new Dimension(IMG, IMG));
        img.setMinimumSize(new Dimension(IMG, IMG));
        img.setMaximumSize(new Dimension(IMG, IMG));
        img.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(img);
        card.add(Box.createVerticalStrut(8));
        card.add(cardLbl(al.getTitulo(),               UITheme.TEXT,   UITheme.FONT_BODY));
        card.add(cardLbl(al.getNombreArtista(),         UITheme.MUTED,  UITheme.FONT_SMALL));
        card.add(cardLbl(String.valueOf(al.getFecha()), UITheme.ACCENT, UITheme.FONT_SMALL));
        return card;
    }

    // ── Historial reciente — full-width con SongRows ─────
    private JPanel buildHistorial() {
        List<Cancion> historial = cancionDAO.obtenerHistorialUsuario(idUsuario);
        // Panel que reporta su ancho = ancho del contenedor (evita colapso en BoxLayout)
        JPanel p = new JPanel() {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                Container parent = getParent();
                if (parent != null) d.width = parent.getWidth() - 48; // insets del HomePanel
                return d;
            }
        };
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (historial.isEmpty()) {
            JLabel v = new JLabel("Aún no hay canciones en tu historial.");
            v.setFont(UITheme.FONT_BODY);
            v.setForeground(UITheme.MUTED);
            p.add(v);
        } else {
            int i = 1;
            for (Cancion c : historial) {
                final Cancion cancion = c;
                SongRow row = new SongRow(i++, c,
                    () -> onPlay.accept(cancion),
                    idUsuario,
                    () -> onMeGusta.accept(cancion));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                p.add(row);
                p.add(Box.createVerticalStrut(2));
            }
        }
        return p;
    }

    // ── Helpers ───────────────────────────────────────────
    private JPanel roundCard() {
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
        return card;
    }

    private JLabel secHeader(String t) {
        JLabel l = new JLabel(t);
        l.setFont(UITheme.FONT_SECTION);
        l.setForeground(UITheme.TEXT);
        return l;
    }

    private JLabel lbl(String t, Color c, Font f) {
        JLabel l = new JLabel(t == null ? "" : t);
        l.setForeground(c); l.setFont(f);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel cardLbl(String t, Color c, Font f) {
        JLabel l = new JLabel(t == null ? "" : t);
        l.setForeground(c); l.setFont(f);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    /** Fuerza LEFT_ALIGNMENT sin crear subclase adicional */
    private Component aligned(Component comp) {
        if (comp instanceof JComponent jc) jc.setAlignmentX(Component.LEFT_ALIGNMENT);
        return comp;
    }
}