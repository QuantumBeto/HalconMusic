package com.halconmusic.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import com.halconmusic.ui.UITheme;

public class Sidebar extends JPanel {

    private final Consumer<String> onNavigate;
    private final Runnable         onCerrarSesion;
    private       String           currentView = "home";
    private final String           tipoRaw;

    public Sidebar(Consumer<String> onNavigate, Runnable onCerrarSesion,
                   String usuarioNombre, String usuarioTipo,
                   String tipoRaw, List<String[]> playlists) {

        this.onNavigate     = onNavigate;
        this.tipoRaw        = tipoRaw;
        this.onCerrarSesion = onCerrarSesion;

        setPreferredSize(new Dimension(UITheme.SIDEBAR_W, 0));
        setMinimumSize(new Dimension(UITheme.SIDEBAR_W, 0));
        setMaximumSize(new Dimension(UITheme.SIDEBAR_W, Integer.MAX_VALUE));
        setBackground(UITheme.SIDEBAR);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER));
        setLayout(new BorderLayout());

        // ── Contenido del scroll ──────────────────────────────────────
        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.add(buildLogo());
        contenido.add(buildNavSection());
        contenido.add(buildDivider());
        contenido.add(buildLibrarySection());

        if ("Premium".equalsIgnoreCase(tipoRaw)) {
            contenido.add(buildDivider());
            contenido.add(buildArtistSection());
        }

        contenido.add(buildDivider());
        contenido.add(buildPlaylistLabel());
        contenido.add(buildPlaylistPanel(playlists));

        // ── Scroll — sin barra horizontal, barra vertical delgada ─────
        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(4, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        add(scroll,                                    BorderLayout.CENTER);
        add(buildUserBadge(usuarioNombre, usuarioTipo), BorderLayout.SOUTH);
    }

    // ── LOGO ──────────────────────────────────────────────────────────
    private JPanel buildLogo() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 14));
        p.setOpaque(false);

        JPanel icon = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.ACCENT);
                g2.fillRoundRect(0, 0, 32, 32, 8, 8);
                g2.setColor(UITheme.BG);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                g2.drawString("H", 9, 22);
                g2.dispose();
            }
        };
        icon.setOpaque(false);
        icon.setPreferredSize(new Dimension(32, 32));

        JLabel logoText = new JLabel("Halcon<html><span style='color:#C8A84B'>Music</span></html>");
        logoText.setFont(new Font("Segoe UI", Font.BOLD, 15));
        logoText.setForeground(UITheme.TEXT);
        // HTML correcto para el logo
        logoText.setText("<html>Halcon<font color='#C8A84B'>Music</font></html>");

        p.add(icon);
        p.add(logoText);
        return p;
    }

    // ── SECCIONES DE NAV ─────────────────────────────────────────────
    private JPanel buildNavSection() {
        JPanel p = sectionPanel();
        p.add(navLabel("Menú"));
        p.add(navItem("home",      "⌂",  "Inicio"));
        p.add(navItem("buscar",    "◎",  "Buscar"));
        p.add(navItem("historial", "◷",  "Historial"));
        p.add(navItem("megustas",  "♥",  "Me gusta"));
        return p;
    }

    private JPanel buildLibrarySection() {
        JPanel p = sectionPanel();
        p.add(navLabel("Biblioteca"));
        p.add(navItem("artistas",  "◎",  "Artistas"));
        p.add(navItem("albumes",   "▣",  "Álbumes"));
        p.add(navItem("canciones", "♫",  "Canciones"));
        return p;
    }

    private JPanel buildArtistSection() {
        JPanel p = sectionPanel();
        p.add(navLabel("Gestión Artista"));
        p.add(navItem("crearArtista",  "+",  "Nuevo Artista"));
        p.add(navItem("crearAlbum",    "+",  "Nuevo Álbum"));
        p.add(navItem("crearCancion",  "+",  "Nueva Canción"));
        p.add(navItem("resumenGlobal", "★",  "Resumen Global"));
        return p;
    }

    // ── PLAYLISTS ─────────────────────────────────────────────────────
    private JPanel buildPlaylistLabel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        p.setOpaque(false);

        JLabel l = new JLabel("MIS PLAYLISTS");
        l.setFont(UITheme.FONT_LABEL);
        l.setForeground(UITheme.MUTED);

        JLabel btnNueva = new JLabel("+");
        btnNueva.setForeground(UITheme.ACCENT);
        btnNueva.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnNueva.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNueva.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onNavigate.accept("crearPlaylist"); }
        });

        p.add(l);
        p.add(btnNueva);
        return p;
    }

    private JPanel buildPlaylistPanel(List<String[]> playlists) {
        JPanel p = sectionPanel();
        for (String[] pl : playlists) {
            String id     = pl[0];
            String nombre = pl[1];

            JLabel item = new JLabel(nombre);
            item.setFont(UITheme.FONT_SMALL);
            item.setForeground(UITheme.MUTED);
            item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            item.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
            item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            item.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { item.setForeground(UITheme.TEXT); }
                @Override public void mouseExited (MouseEvent e) { item.setForeground(UITheme.MUTED); }
                @Override public void mouseClicked(MouseEvent e) { onNavigate.accept("playlist:" + id); }
            });
            p.add(item);
        }
        return p;
    }

    // ── USER BADGE ────────────────────────────────────────────────────
    private JPanel buildUserBadge(String nombre, String tipo) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        // Avatar circular con iniciales
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.ACCENT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.BG);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String ini = nombre.length() >= 2 ? nombre.substring(0, 2).toUpperCase() : "HM";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(ini,
                    (getWidth()  - fm.stringWidth(ini)) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(30, 30));
        avatar.setMinimumSize(new Dimension(30, 30));
        avatar.setMaximumSize(new Dimension(30, 30));

        // Columna de texto: nombre / tipo / cerrar sesión
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel lblNombre = new JLabel(nombre);
        lblNombre.setFont(UITheme.FONT_SMALL);
        lblNombre.setForeground(UITheme.TEXT);

        JLabel lblTipo = new JLabel(tipo);
        lblTipo.setFont(UITheme.FONT_LABEL);
        lblTipo.setForeground(UITheme.ACCENT);

        JLabel btnCerrar = new JLabel("Cerrar sesión");
        btnCerrar.setFont(UITheme.FONT_LABEL);
        btnCerrar.setForeground(UITheme.MUTED);
        btnCerrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCerrar.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnCerrar.setForeground(UITheme.ACCENT); }
            @Override public void mouseExited (MouseEvent e) { btnCerrar.setForeground(UITheme.MUTED);  }
            @Override public void mouseClicked(MouseEvent e) {
                int ok = JOptionPane.showConfirmDialog(
                    null, "¿Cerrar sesión?", "HalconMusic",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (ok == JOptionPane.YES_OPTION) onCerrarSesion.run();
            }
        });

        info.add(lblNombre);
        info.add(lblTipo);
        info.add(btnCerrar);

        p.add(avatar);
        p.add(info);
        return p;
    }

    // ── NAV ITEM ──────────────────────────────────────────────────────
    /**
     * Cada ítem: [ icono (42px) ][ label (resto del ancho) ]
     * El icono usa Font.DIALOG para máxima compatibilidad con símbolos Unicode.
     * El ancho total está fijado al SIDEBAR_W para que nunca desborde.
     */
    private JPanel navItem(String view, String icon, String label) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(UITheme.SIDEBAR_W, 38));
        p.setPreferredSize(new Dimension(UITheme.SIDEBAR_W, 38));
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // ── Icono ────────────────────────────────────────────────────
        JLabel iconLbl = new JLabel(icon, SwingConstants.CENTER);
        iconLbl.setFont(new Font(Font.DIALOG, Font.PLAIN, 15));
        iconLbl.setPreferredSize(new Dimension(44, 38));
        iconLbl.setMinimumSize(new Dimension(44, 38));
        iconLbl.setMaximumSize(new Dimension(44, 38));

        // ── Texto ─────────────────────────────────────────────────────
        JLabel labelLbl = new JLabel(label);
        labelLbl.setFont(UITheme.FONT_BODY);
        labelLbl.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 8));

        // ── Color según estado activo ─────────────────────────────────
        boolean active = view.equals(currentView);
        Color normalColor = active ? UITheme.ACCENT : UITheme.MUTED;
        iconLbl.setForeground(normalColor);
        labelLbl.setForeground(normalColor);

        // ── Barra de acento a la izquierda si está activo ─────────────
        if (active) {
            p.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, UITheme.ACCENT));
        }

        p.add(iconLbl,  BorderLayout.WEST);
        p.add(labelLbl, BorderLayout.CENTER);

        p.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!view.equals(currentView)) {
                    iconLbl.setForeground(UITheme.TEXT);
                    labelLbl.setForeground(UITheme.TEXT);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!view.equals(currentView)) {
                    iconLbl.setForeground(UITheme.MUTED);
                    labelLbl.setForeground(UITheme.MUTED);
                }
            }
            @Override public void mouseClicked(MouseEvent e) {
                currentView = view;
                onNavigate.accept(view);
            }
        });

        return p;
    }

    // ── HELPERS ───────────────────────────────────────────────────────
    private JLabel navLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(UITheme.FONT_LABEL);
        l.setForeground(UITheme.MUTED);
        l.setBorder(BorderFactory.createEmptyBorder(10, 18, 4, 18));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        return l;
    }

    private JSeparator buildDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    /** Panel base para secciones de nav con BoxLayout vertical */
    private JPanel sectionPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }
}