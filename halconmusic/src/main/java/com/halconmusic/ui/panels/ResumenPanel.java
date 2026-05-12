package com.halconmusic.ui.panels;

import com.halconmusic.dao.CancionDAO;
import com.halconmusic.dao.ResumenDAO;
import com.halconmusic.model.Cancion;
import com.halconmusic.model.ResumenSemanal;
import com.halconmusic.ui.UITheme;
import com.halconmusic.ui.components.SongRow;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel de resumen semanal e historial del usuario.
 */
public class ResumenPanel extends JPanel {

    private final ResumenDAO        resumenDAO;
    private final CancionDAO        cancionDAO;
    private final Consumer<Cancion> onPlay;
    private final Consumer<Cancion> onMeGusta;
    private final String            idUsuario;

    public ResumenPanel(Consumer<Cancion> onPlay, Consumer<Cancion> onMeGusta, String idUsuario) {
        this.onPlay     = onPlay;
        this.onMeGusta  = onMeGusta;
        this.idUsuario  = idUsuario;
        this.resumenDAO = new ResumenDAO();
        this.cancionDAO = new CancionDAO();

        setBackground(UITheme.BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        cargar();
    }

    /** Limpia y recarga todo el panel — llamar tras reproducir para actualizar historial */
    public void refrescar() {
        removeAll();
        cargar();
        revalidate();
        repaint();
    }

    private void cargar() {
        ResumenSemanal resumen = resumenDAO.obtenerUltimoDeUsuario(idUsuario);

        JLabel titulo = lbl("Tu resumen semanal", UITheme.TEXT, UITheme.FONT_SECTION);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(titulo);
        add(Box.createVerticalStrut(14));

        if (resumen != null) {
            add(buildResumenCard(resumen));
        } else {
            add(lbl("No hay resumen disponible todavía.", UITheme.MUTED, UITheme.FONT_BODY));
        }

        add(Box.createVerticalStrut(28));

        JLabel lblGeneros = lbl("Géneros más escuchados", UITheme.TEXT, UITheme.FONT_SECTION);
        lblGeneros.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(lblGeneros);
        add(Box.createVerticalStrut(12));
        add(buildGenerosChart());
        add(Box.createVerticalStrut(28));

        JLabel lblHistorial = lbl("Escuchado recientemente", UITheme.TEXT, UITheme.FONT_SECTION);
        lblHistorial.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(lblHistorial);
        add(Box.createVerticalStrut(12));
        add(buildHistorial());
    }

    private JPanel buildResumenCard(ResumenSemanal r) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(0x1C, 0x16, 0x00),
                    getWidth(), getHeight(), new Color(0x10, 0x10, 0x10)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.RADIUS, UITheme.RADIUS);
                g2.setColor(UITheme.withAlpha(UITheme.ACCENT, 60));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, UITheme.RADIUS, UITheme.RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new GridLayout(2, 3, 12, 12));
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(stat("Género principal",  r.getGeneroPrincipal()));
        card.add(stat("Artista principal", r.getArtistaPrincipal()));
        card.add(stat("Emoción dominante", r.getEmocion()));
        card.add(stat("Canciones top",     acortar(r.getCancionesPrincipales(), 30)));
        card.add(stat("Fecha del resumen", r.getFecha() != null ? r.getFecha().toString() : "-"));
        card.add(stat("ID resumen",        r.getIdResumen()));

        return card;
    }

    private JPanel stat(String etiqueta, String valor) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel lblEtq = new JLabel(etiqueta.toUpperCase());
        lblEtq.setFont(UITheme.FONT_LABEL);
        lblEtq.setForeground(UITheme.ACCENT);

        JLabel lblVal = new JLabel(valor == null ? "-" : valor);
        lblVal.setFont(UITheme.FONT_BODY);
        lblVal.setForeground(UITheme.TEXT);

        p.add(lblEtq);
        p.add(Box.createVerticalStrut(3));
        p.add(lblVal);
        return p;
    }

    private JPanel buildGenerosChart() {
        List<String[]> generos = resumenDAO.obtenerGenerosMasEscuchados(idUsuario);
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (generos.isEmpty()) {
            p.add(lbl("Sin datos de historial.", UITheme.MUTED, UITheme.FONT_SMALL));
            return p;
        }

        int maxVal = Integer.parseInt(generos.get(0)[1]);

        for (String[] g : generos) {
            String genero = g[0];
            int    total  = Integer.parseInt(g[1]);
            double pct    = (double) total / maxVal;

            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

            JLabel lblGenero = new JLabel(genero);
            lblGenero.setFont(UITheme.FONT_BODY);
            lblGenero.setForeground(UITheme.TEXT);
            lblGenero.setPreferredSize(new Dimension(180, 22));

            JPanel barTrack = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(UITheme.SURFACE);
                    g2.fillRoundRect(0, getHeight()/2 - 4, getWidth(), 8, 4, 4);
                    g2.setColor(UITheme.ACCENT);
                    g2.fillRoundRect(0, getHeight()/2 - 4, (int)(getWidth() * pct), 8, 4, 4);
                    g2.dispose();
                }
            };
            barTrack.setOpaque(false);

            JLabel lblTotal = new JLabel(total + " esc.");
            lblTotal.setFont(UITheme.FONT_SMALL);
            lblTotal.setForeground(UITheme.MUTED);
            lblTotal.setPreferredSize(new Dimension(60, 22));

            row.add(lblGenero, BorderLayout.WEST);
            row.add(barTrack,  BorderLayout.CENTER);
            row.add(lblTotal,  BorderLayout.EAST);

            p.add(row);
            p.add(Box.createVerticalStrut(6));
        }
        return p;
    }

    private JPanel buildHistorial() {
        List<Cancion> historial = cancionDAO.obtenerHistorialUsuario(idUsuario);
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (historial.isEmpty()) {
            p.add(lbl("Tu historial está vacío.", UITheme.MUTED, UITheme.FONT_BODY));
            return p;
        }

        int i = 1;
        for (Cancion c : historial) {
            final Cancion cancion = c;
            SongRow row = new SongRow(i++, c, () -> onPlay.accept(cancion), idUsuario, () -> onMeGusta.accept(cancion));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(row);
            p.add(Box.createVerticalStrut(2));
        }
        return p;
    }

    private JLabel lbl(String text, Color color, Font font) {
        JLabel l = new JLabel(text == null ? "" : text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    private String acortar(String text, int max) {
        if (text == null) return "-";
        return text.length() > max ? text.substring(0, max) + "..." : text;
    }
}