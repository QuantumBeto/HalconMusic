package com.halconmusic.ui.panels;

import com.halconmusic.dao.AlbumDAO;
import com.halconmusic.dao.CancionDAO;
import com.halconmusic.model.Album;
import com.halconmusic.model.Cancion;
import com.halconmusic.ui.UITheme;
import com.halconmusic.ui.components.SongRow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel de álbumes con grid de tarjetas y detalle de canciones al hacer clic.
 * Usa JOIN ALBUMES + ARTISTAS + ALBUMES_CANCIONES en el DAO.
 */
public class AlbumesPanel extends JPanel {

    private final AlbumDAO         albumDAO;
    private final CancionDAO       cancionDAO;
    private final Consumer<Cancion> onPlay;
    private       JPanel           mainArea;

    public AlbumesPanel(Consumer<Cancion> onPlay) {
        this.onPlay     = onPlay;
        this.albumDAO   = new AlbumDAO();
        this.cancionDAO = new CancionDAO();

        setBackground(UITheme.BG);
        setLayout(new BorderLayout());

        mainArea = new JPanel(new BorderLayout());
        mainArea.setOpaque(false);

        add(buildTopBar(), BorderLayout.NORTH);
        add(new JScrollPane(mainArea) {{
            setBorder(null);
            setOpaque(false);
            getViewport().setOpaque(false);
            getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        }}, BorderLayout.CENTER);

        mostrarGrid(albumDAO.obtenerTodos());
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(24, 24, 14, 24));

        JLabel titulo = new JLabel("Álbumes");
        titulo.setFont(UITheme.FONT_SECTION);
        titulo.setForeground(UITheme.TEXT);

        JTextField search = new JTextField();
        search.setBackground(UITheme.SURFACE);
        search.setForeground(UITheme.TEXT);
        search.setFont(UITheme.FONT_BODY);
        search.setCaretColor(UITheme.ACCENT);
        search.setPreferredSize(new Dimension(240, 32));
        search.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        search.putClientProperty("JTextField.placeholderText", "⌕  Buscar álbum o artista...");

        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate (javax.swing.event.DocumentEvent e) { filtrar(search.getText()); }
            public void removeUpdate (javax.swing.event.DocumentEvent e) { filtrar(search.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(search.getText()); }
        });

        p.add(titulo, BorderLayout.WEST);
        p.add(search, BorderLayout.EAST);
        return p;
    }

    private void filtrar(String termino) {
        List<Album> lista = termino.isBlank()
            ? albumDAO.obtenerTodos()
            : albumDAO.buscar(termino);
        mostrarGrid(lista);
    }

    private void mostrarGrid(List<Album> albumes) {
        mainArea.removeAll();
        JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 14));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

        for (Album al : albumes) grid.add(buildCard(al));

        mainArea.add(grid, BorderLayout.NORTH);
        mainArea.revalidate();
        mainArea.repaint();
    }

    private void mostrarDetalle(Album al) {
        mainArea.removeAll();

        JPanel detalle = new JPanel();
        detalle.setOpaque(false);
        detalle.setLayout(new BoxLayout(detalle, BoxLayout.Y_AXIS));
        detalle.setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

        // Botón volver
        JButton btnVolver = new JButton("← Volver a álbumes");
        btnVolver.setFont(UITheme.FONT_SMALL);
        btnVolver.setForeground(UITheme.ACCENT);
        btnVolver.setBackground(UITheme.SURFACE);
        btnVolver.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btnVolver.setFocusPainted(false);
        btnVolver.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVolver.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnVolver.addActionListener(e -> mostrarGrid(albumDAO.obtenerTodos()));

        // Header del álbum
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        // Portada grande
        JPanel portada = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);
                g2.setColor(UITheme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                if (al.getPortada() != null) {
                    Shape clip = new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setClip(clip);
                    g2.drawImage(al.getPortada(), 0, 0, getWidth(), getHeight(), null);
                } else {
                    g2.setColor(UITheme.MUTED);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 40));
                    g2.drawString("◉", 36, 90);
                }
                g2.dispose();
            }
        };
        portada.setOpaque(false);
        portada.setPreferredSize(new Dimension(140, 140));

        // Info del álbum
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        info.add(lbl("ÁLBUM",                       UITheme.ACCENT, UITheme.FONT_LABEL));
        info.add(Box.createVerticalStrut(6));
        info.add(lbl(al.getTitulo(),                UITheme.TEXT,   new Font("Segoe UI", Font.BOLD, 22)));
        info.add(Box.createVerticalStrut(6));
        info.add(lbl(al.getNombreArtista(),          UITheme.MUTED,  UITheme.FONT_BODY));
        info.add(lbl(al.getGenero() + " • " + al.getFecha(), UITheme.MUTED, UITheme.FONT_SMALL));
        info.add(lbl(al.getNumeroDeCanciones() + " canciones • " + al.getDuracionFormateada(), UITheme.MUTED, UITheme.FONT_SMALL));

        header.add(portada);
        header.add(info);

        // Lista de canciones del álbum
        List<Cancion> canciones = cancionDAO.obtenerPorAlbum(al.getIdAlbum());
        JPanel lista = new JPanel();
        lista.setOpaque(false);
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));

        JLabel lblCanciones = lbl("Canciones", UITheme.TEXT, UITheme.FONT_SECTION);
        lblCanciones.setAlignmentX(Component.LEFT_ALIGNMENT);
        lista.add(lblCanciones);
        lista.add(Box.createVerticalStrut(10));

        int i = 1;
        for (Cancion c : canciones) {
            final Cancion cancion = c;
            SongRow row = new SongRow(i++, c, () -> onPlay.accept(cancion));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            lista.add(row);
            lista.add(Box.createVerticalStrut(2));
        }

        detalle.add(btnVolver);
        detalle.add(Box.createVerticalStrut(14));
        detalle.add(header);
        detalle.add(lista);

        mainArea.add(detalle, BorderLayout.NORTH);
        mainArea.revalidate();
        mainArea.repaint();
    }

    private JPanel buildCard(Album al) {
        JPanel card = new JPanel() {
            boolean hover = false;
            { addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                @Override public void mouseExited (MouseEvent e) { hover = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { mostrarDetalle(al); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? UITheme.SURFACE : UITheme.CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.RADIUS, UITheme.RADIUS);
                if (hover) {
                    g2.setColor(UITheme.withAlpha(UITheme.ACCENT, 50));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, UITheme.RADIUS, UITheme.RADIUS);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        card.setPreferredSize(new Dimension(150, 205));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel img = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                if (al.getPortada() != null) {
                    Shape clip = new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setClip(clip);
                    g2.drawImage(al.getPortada(), 0, 0, getWidth(), getHeight(), null);
                } else {
                    g2.setColor(UITheme.MUTED);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 32));
                    FontMetrics fm = g2.getFontMetrics();
                    String icon = "◉";
                    g2.drawString(icon, (getWidth() - fm.stringWidth(icon)) / 2,
                                  (getHeight() + fm.getAscent()) / 2 - 4);
                }
                g2.dispose();
            }
        };
        img.setOpaque(false);
        img.setAlignmentX(Component.CENTER_ALIGNMENT);
        img.setPreferredSize(new Dimension(126, 126));
        img.setMaximumSize(new Dimension(126, 126));

        card.add(img);
        card.add(Box.createVerticalStrut(10));
        card.add(lbl(al.getTitulo(),         UITheme.TEXT,  UITheme.FONT_BODY));
        card.add(lbl(al.getNombreArtista(),  UITheme.MUTED, UITheme.FONT_SMALL));
        card.add(lbl(String.valueOf(al.getFecha()), UITheme.ACCENT, UITheme.FONT_SMALL));
        return card;
    }

    private JLabel lbl(String text, Color color, Font font) {
        JLabel l = new JLabel(text == null ? "" : text);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
}
