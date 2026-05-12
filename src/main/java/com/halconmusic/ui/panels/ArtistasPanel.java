package com.halconmusic.ui.panels;

import com.halconmusic.dao.ArtistaDAO;
import com.halconmusic.model.Artista;
import com.halconmusic.ui.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Panel de artistas con estadísticas y grid de tarjetas.
 * Usa COUNT + GROUP BY + DISTINCT en el DAO.
 */
public class ArtistasPanel extends JPanel {

    private final ArtistaDAO artistaDAO;
    private       JPanel     grid;

    public ArtistasPanel() {
        this.artistaDAO = new ArtistaDAO();

        setBackground(UITheme.BG);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(buildStats());
        content.add(Box.createVerticalStrut(22));
        content.add(buildSearchBar());
        content.add(Box.createVerticalStrut(14));
        content.add(buildGrid());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        add(scroll, BorderLayout.CENTER);

        cargarGrid(artistaDAO.obtenerTodos());
    }

    private JPanel buildStats() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        int totalArtistas = artistaDAO.obtenerTodos().size();
        int totalGeneros  = artistaDAO.contarGenerosDistintos();
        int totalPaises   = artistaDAO.contarPaisesDistintos();

        p.add(statPill(String.valueOf(totalArtistas), "Artistas"));
        p.add(statPill(String.valueOf(totalGeneros),  "Géneros distintos"));
        p.add(statPill(String.valueOf(totalPaises),   "Países de origen"));
        return p;
    }

    private JPanel statPill(String numero, String etiqueta) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.RADIUS, UITheme.RADIUS);
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, UITheme.RADIUS, UITheme.RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        p.setPreferredSize(new Dimension(140, 68));

        JLabel lblNum = new JLabel(numero);
        lblNum.setFont(UITheme.FONT_NUM);
        lblNum.setForeground(UITheme.ACCENT);

        JLabel lblEtq = new JLabel(etiqueta);
        lblEtq.setFont(UITheme.FONT_SMALL);
        lblEtq.setForeground(UITheme.MUTED);

        p.add(lblNum);
        p.add(lblEtq);
        return p;
    }

    private JPanel buildSearchBar() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel titulo = new JLabel("Tus artistas");
        titulo.setFont(UITheme.FONT_SECTION);
        titulo.setForeground(UITheme.TEXT);

        JTextField search = new JTextField();
        search.setBackground(UITheme.SURFACE);
        search.setForeground(UITheme.TEXT);
        search.setFont(UITheme.FONT_BODY);
        search.setCaretColor(UITheme.ACCENT);
        search.setPreferredSize(new Dimension(220, 32));
        search.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        search.putClientProperty("JTextField.placeholderText", "⌕  Buscar artista...");

        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate (javax.swing.event.DocumentEvent e) { filtrar(search.getText()); }
            public void removeUpdate (javax.swing.event.DocumentEvent e) { filtrar(search.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(search.getText()); }
        });

        p.add(titulo, BorderLayout.WEST);
        p.add(search, BorderLayout.EAST);
        return p;
    }

    private JPanel buildGrid() {
        grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 14));
        grid.setOpaque(false);
        return grid;
    }

    private void filtrar(String termino) {
        List<Artista> lista = termino.isBlank()
            ? artistaDAO.obtenerTodos()
            : artistaDAO.buscarPorNombre(termino);
        cargarGrid(lista);
    }

    private void cargarGrid(List<Artista> artistas) {
        grid.removeAll();
        for (Artista a : artistas) grid.add(buildCard(a));
        grid.revalidate();
        grid.repaint();
    }

    private JPanel buildCard(Artista a) {
        JPanel card = new JPanel() {
            boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                    @Override public void mouseExited (MouseEvent e) { hover = false; repaint(); }
                });
            }
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
        card.setPreferredSize(new Dimension(150, 200));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Imagen circular
        JPanel img = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,    RenderingHints.VALUE_RENDER_QUALITY);
                g2.setColor(UITheme.SURFACE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                if (a.getPortada() != null) {
                    Shape clip = new java.awt.geom.Ellipse2D.Float(0, 0, getWidth(), getHeight());
                    g2.setClip(clip);
                    g2.drawImage(a.getPortada(), 0, 0, getWidth(), getHeight(), null);
                } else {
                    g2.setColor(UITheme.MUTED);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 36));
                    FontMetrics fm = g2.getFontMetrics();
                    String icon = "♪";
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

        JLabel lblNombre = lbl(a.getNombre(),          UITheme.TEXT,  UITheme.FONT_BODY);
        JLabel lblGenero = lbl(a.getGeneroPrincipal(), UITheme.MUTED, UITheme.FONT_SMALL);
        JLabel lblPais   = lbl(a.getPaisDeOrigen(),    UITheme.MUTED, UITheme.FONT_SMALL);
        JLabel lblCanc   = lbl(a.getTotalCanciones() + " canciones", UITheme.ACCENT, UITheme.FONT_SMALL);

        card.add(img);
        card.add(Box.createVerticalStrut(10));
        card.add(lblNombre);
        card.add(lblGenero);
        card.add(lblPais);
        card.add(Box.createVerticalStrut(4));
        card.add(lblCanc);
        return card;
    }

    private JLabel lbl(String text, Color color, Font font) {
        JLabel l = new JLabel(text == null ? "" : text);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }
}
