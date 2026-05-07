package com.halconmusic.ui.components;

import com.halconmusic.dao.HistorialDAO;
import com.halconmusic.model.Cancion;
import com.halconmusic.ui.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Fila de canción reutilizable con número, portada, título, artista,
 * género, año, duración y botón de Me Gusta (♥).
 */
public class SongRow extends JPanel {

    private final Cancion  cancion;
    private final int      numero;
    private       boolean  playing = false;
    private       boolean  liked   = false;

    /**
     * Constructor con soporte de Me Gusta.
     *
     * @param numero    Número de fila
     * @param cancion   Objeto canción a mostrar
     * @param onPlay    Callback al hacer clic para reproducir
     * @param idUsuario ID del usuario activo (para guardar Me Gusta en BD)
     */
    public SongRow(int numero, Cancion cancion, Runnable onPlay, String idUsuario) {
        this.numero  = numero;
        this.cancion = cancion;

        setOpaque(false);
        setLayout(new GridBagLayout());
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        setPreferredSize(new Dimension(0, 52));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // ── Número ──────────────────────────────────────
        JLabel lblNum = label(String.valueOf(numero), UITheme.MUTED, UITheme.FONT_BODY);
        lblNum.setPreferredSize(new Dimension(30, 36));
        lblNum.setHorizontalAlignment(SwingConstants.CENTER);

        // ── Portada (thumb) ──────────────────────────────
        JPanel thumb = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
                if (cancion.getPortada() != null) {
                    g2.drawImage(cancion.getPortada(), 0, 0, getWidth(), getHeight(), null);
                } else {
                    g2.setColor(UITheme.MUTED);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    g2.drawString("♪", 9, 23);
                }
                g2.dispose();
            }
        };
        thumb.setOpaque(false);
        thumb.setPreferredSize(new Dimension(36, 36));

        // ── Info (título + artista) ──────────────────────
        JPanel info = new JPanel(new GridLayout(2, 1, 0, 1));
        info.setOpaque(false);
        JLabel lblTitle  = label(cancion.getNombre(), UITheme.TEXT, UITheme.FONT_BODY);
        JLabel lblArtist = label(cancion.getNombreArtistasCompleto(), UITheme.MUTED, UITheme.FONT_SMALL);
        info.add(lblTitle);
        info.add(lblArtist);

        // ── Género ───────────────────────────────────────
        JLabel lblGenero = label(cancion.getGenero(), UITheme.MUTED, UITheme.FONT_SMALL);

        // ── Año ──────────────────────────────────────────
        JLabel lblAnio = label(String.valueOf(cancion.getFecha()), UITheme.MUTED, UITheme.FONT_SMALL);

        // ── Duración ─────────────────────────────────────
        JLabel lblDur = label(cancion.getDuracionFormateada(), UITheme.MUTED, UITheme.FONT_SMALL);
        lblDur.setHorizontalAlignment(SwingConstants.RIGHT);

        // ── Botón Me Gusta ♥ ─────────────────────────────
        JLabel lblHeart = new JLabel("♡");
        lblHeart.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblHeart.setForeground(UITheme.MUTED);
        lblHeart.setPreferredSize(new Dimension(34, 36));
        lblHeart.setHorizontalAlignment(SwingConstants.CENTER);
        lblHeart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblHeart.setToolTipText("Agregar a Me Gusta");

        lblHeart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume(); // evita que dispare el onPlay de la fila
                if (idUsuario == null || idUsuario.isBlank()) return;

                new Thread(() -> {
                    try {
                        HistorialDAO dao = new HistorialDAO();
                        boolean added = dao.agregarMeGusta(idUsuario, cancion.getIdCancion());
                        SwingUtilities.invokeLater(() -> {
                            if (added) {
                                liked = true;
                                lblHeart.setText("♥");
                                lblHeart.setForeground(new Color(0xFF, 0x22, 0x55));
                                lblHeart.setToolTipText("Ya está en Me Gusta");
                            } else {
                                // Ya existía — igualmente colorear para el usuario
                                lblHeart.setText("♥");
                                lblHeart.setForeground(new Color(0xFF, 0x22, 0x55));
                                lblHeart.setToolTipText("Ya está en Me Gusta");
                            }
                        });
                    } catch (Exception ex) {
                        System.err.println("Error al agregar Me Gusta: " + ex.getMessage());
                    }
                }).start();
            }

            @Override public void mouseEntered(MouseEvent e) {
                if (!liked) lblHeart.setForeground(new Color(0xFF, 0x22, 0x55));
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!liked) lblHeart.setForeground(UITheme.MUTED);
            }
        });

        // ── Layout ───────────────────────────────────────
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(0, 4, 0, 4);
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.gridy   = 0;
        gbc.weighty = 1;

        gbc.gridx = 0; gbc.weightx = 0;   add(lblNum,   gbc);
        gbc.gridx = 1; gbc.weightx = 0;   add(thumb,    gbc);
        gbc.gridx = 2; gbc.weightx = 1;   add(info,     gbc);
        gbc.gridx = 3; gbc.weightx = 0.3; add(lblGenero,gbc);
        gbc.gridx = 4; gbc.weightx = 0.1; add(lblAnio,  gbc);
        gbc.gridx = 5; gbc.weightx = 0;   add(lblDur,   gbc);
        gbc.gridx = 6; gbc.weightx = 0;   add(lblHeart, gbc);

        // ── Hover + clic en la fila ──────────────────────
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { setBackground(UITheme.HOVER); repaint(); }
            @Override public void mouseExited (MouseEvent e) { setBackground(null);          repaint(); }
            @Override public void mouseClicked(MouseEvent e) {
                // Solo reproducir si el clic no fue en el corazón
                if (e.getSource() == SongRow.this && onPlay != null) onPlay.run();
            }
        });
    }

    // ── Constructor de compatibilidad sin Me Gusta ───────
    public SongRow(int numero, Cancion cancion, Runnable onPlay) {
        this(numero, cancion, onPlay, null);
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (playing) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(UITheme.ACCENT_SOFT);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.dispose();
        }
        super.paintComponent(g);
    }

    private JLabel label(String text, Color color, Font font) {
        JLabel l = new JLabel(text == null ? "" : text);
        l.setForeground(color);
        l.setFont(font);
        return l;
    }
}
