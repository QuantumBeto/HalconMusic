package com.halconmusic.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import com.halconmusic.ui.UITheme;

/**
 * Sidebar izquierdo con navegación y lista de playlists.
 */
public class Sidebar extends JPanel {

    private final Consumer<String> onNavigate;
    private       String           currentView = "home";

    public Sidebar(Consumer<String> onNavigate, String usuarioNombre, String usuarioTipo) {
        this.onNavigate = onNavigate;
        setPreferredSize(new Dimension(UITheme.SIDEBAR_W, 0));
        setBackground(UITheme.SIDEBAR);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER));
        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(buildLogo());
        top.add(buildNavSection());
        top.add(buildDivider());
        top.add(buildLibrarySection());
        top.add(buildDivider());
        top.add(buildPlaylistLabel());

        JScrollPane scrollPlaylists = new JScrollPane(buildPlaylistPanel());
        scrollPlaylists.setBorder(null);
        scrollPlaylists.setOpaque(false);
        scrollPlaylists.getViewport().setOpaque(false);
        scrollPlaylists.getVerticalScrollBar().setPreferredSize(new Dimension(4, 0));

        add(top,              BorderLayout.NORTH);
        add(scrollPlaylists,  BorderLayout.CENTER);
        add(buildUserBadge(usuarioNombre, usuarioTipo), BorderLayout.SOUTH);
    }

    private JPanel buildLogo() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 14));
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

        JLabel logoText = new JLabel("<html>Halcon<span style='color:#C8A84B'>Music</span></html>");
        logoText.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        logoText.setForeground(UITheme.TEXT);

        p.add(icon);
        p.add(logoText);
        return p;
    }

    private JPanel buildNavSection() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(navLabel("Menú"));
        p.add(navItem("home",      "⌂", "Inicio"));
        p.add(navItem("buscar",    "⌕", "Buscar"));
        p.add(navItem("historial", "◷", "Historial"));
        p.add(navItem("megustas",  "♥", "Me gusta"));
        return p;
    }

    private JPanel buildLibrarySection() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(navLabel("Biblioteca"));
        p.add(navItem("artistas",  "◎", "Artistas"));
        p.add(navItem("albumes",   "▣", "Álbumes"));
        p.add(navItem("canciones", "♫", "Canciones"));
        return p;
    }

    private JPanel buildPlaylistLabel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 4));
        p.setOpaque(false);
        JLabel l = new JLabel("MIS PLAYLISTS");
        l.setFont(UITheme.FONT_LABEL);
        l.setForeground(UITheme.MUTED);
        p.add(l);
        return p;
    }

    private JPanel buildPlaylistPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        // Playlists estáticas de ejemplo — se reemplazarán dinámicamente
        String[] playlists = {"Corridos 2024","Reggaeton Hits","Baladas Clásicas",
                              "Pop Internacional","Grupero Mix","Banda Sinaloense",
                              "Corridos Tumbados","Reggaeton Nuevo","Rancheras Eternas"};
        for (String pl : playlists) {
            JLabel item = new JLabel(pl);
            item.setFont(UITheme.FONT_SMALL);
            item.setForeground(UITheme.MUTED);
            item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            item.setBorder(BorderFactory.createEmptyBorder(5, 18, 5, 18));
            item.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { item.setForeground(UITheme.TEXT); }
                @Override public void mouseExited (MouseEvent e) { item.setForeground(UITheme.MUTED); }
            });
            p.add(item);
        }
        return p;
    }

    private JPanel buildUserBadge(String nombre, String tipo) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        // Avatar con iniciales
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UITheme.ACCENT, getWidth(), getHeight(), UITheme.ACCENT2);
                g2.setPaint(gp);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.BG);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String ini = nombre.length() >= 2 ? nombre.substring(0, 2).toUpperCase() : "HM";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(ini, (getWidth() - fm.stringWidth(ini)) / 2,
                              (getHeight() + fm.getAscent()) / 2 - 2);
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(28, 28));

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 0));
        info.setOpaque(false);
        JLabel lblNombre = new JLabel(nombre);
        lblNombre.setFont(UITheme.FONT_SMALL);
        lblNombre.setForeground(UITheme.TEXT);
        JLabel lblTipo = new JLabel(tipo);
        lblTipo.setFont(UITheme.FONT_LABEL);
        lblTipo.setForeground(UITheme.ACCENT);
        info.add(lblNombre);
        info.add(lblTipo);

        p.add(avatar);
        p.add(info);
        return p;
    }

    private JPanel navItem(String view, String icon, String label) {
        // Usamos un panel que siempre pinta su fondo — evita el "fantasma" en hover
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setBackground(UITheme.SIDEBAR);  // fondo base = mismo que sidebar
        p.setOpaque(false);                 // dejamos que nuestro paintComponent maneje el fondo
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLbl  = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
        JLabel labelLbl = new JLabel(label);
        labelLbl.setFont(UITheme.FONT_BODY);

        boolean isActive = view.equals(currentView);
        iconLbl.setForeground(isActive  ? UITheme.ACCENT : UITheme.MUTED);
        labelLbl.setForeground(isActive ? UITheme.ACCENT : UITheme.MUTED);

        p.add(iconLbl);
        p.add(labelLbl);

        p.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!view.equals(currentView)) {
                    iconLbl.setForeground(UITheme.TEXT);
                    labelLbl.setForeground(UITheme.TEXT);
                    p.setBackground(new Color(
                        UITheme.SIDEBAR.getRed(),
                        UITheme.SIDEBAR.getGreen(),
                        UITheme.SIDEBAR.getBlue()
                    ).brighter());
                    p.repaint();
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!view.equals(currentView)) {
                    iconLbl.setForeground(UITheme.MUTED);
                    labelLbl.setForeground(UITheme.MUTED);
                    p.setBackground(UITheme.SIDEBAR);
                    p.repaint();
                }
            }
            @Override public void mouseClicked(MouseEvent e) {
                currentView = view;
                onNavigate.accept(view);
            }
        });

        return p;
    }

    private JLabel navLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(UITheme.FONT_LABEL);
        l.setForeground(UITheme.MUTED);
        l.setBorder(BorderFactory.createEmptyBorder(8, 18, 4, 18));
        return l;
    }

    private JSeparator buildDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }
}