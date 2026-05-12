package com.halconmusic.ui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.halconmusic.model.Cancion;
import com.halconmusic.ui.UITheme;

/**
 * Fila de canción reutilizable con número, portada, título, artista, género, año, duración y botón ♥.
 */
public class SongRow extends JPanel {

    private final Cancion  cancion;
    private final int      numero;
    private       boolean  playing = false;

    public SongRow(int numero, Cancion cancion, Runnable onPlay, String idUsuario, Runnable onMeGusta) {
        this.numero  = numero;
        this.cancion = cancion;

        setOpaque(false);
        setLayout(new GridBagLayout());
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        setPreferredSize(new Dimension(0, 52));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Número
        JLabel lblNum = label(String.valueOf(numero), UITheme.MUTED, UITheme.FONT_BODY);
        lblNum.setPreferredSize(new Dimension(30, 36));
        lblNum.setHorizontalAlignment(SwingConstants.CENTER);

        // Thumb (portada pequeña)
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

        // Info (título + artista)
        JPanel info = new JPanel(new GridLayout(2, 1, 0, 1));
        info.setOpaque(false);
        JLabel lblTitle  = label(cancion.getNombre(), UITheme.TEXT, UITheme.FONT_BODY);
        JLabel lblArtist = label(cancion.getNombreArtistasCompleto(), UITheme.MUTED, UITheme.FONT_SMALL);
        info.add(lblTitle);
        info.add(lblArtist);

        // Género
        JLabel lblGenero = label(cancion.getGenero(), UITheme.MUTED, UITheme.FONT_SMALL);

        // Año
        JLabel lblAnio = label(String.valueOf(cancion.getFecha()), UITheme.MUTED, UITheme.FONT_SMALL);

        // Duración
        JLabel lblDur = label(cancion.getDuracionFormateada(), UITheme.MUTED, UITheme.FONT_SMALL);
        lblDur.setHorizontalAlignment(SwingConstants.RIGHT);

        // Botón ♥ Me Gusta
        JLabel btnHeart = new JLabel("♡");
        btnHeart.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnHeart.setForeground(UITheme.MUTED);
        btnHeart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnHeart.setPreferredSize(new Dimension(30, 36));
        btnHeart.setHorizontalAlignment(SwingConstants.CENTER);

        if (idUsuario != null && onMeGusta != null) {
            btnHeart.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    e.consume(); // evita que también dispare el onPlay del panel
                    btnHeart.setText("♥");
                    btnHeart.setForeground(new Color(0xFF, 0x22, 0x55));
                    onMeGusta.run();
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (btnHeart.getText().equals("♡")) {
                        btnHeart.setForeground(UITheme.TEXT);
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (btnHeart.getText().equals("♡")) {
                        btnHeart.setForeground(UITheme.MUTED);
                    }
                }
            });
        }

        // Layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 4, 0, 4);
        gbc.fill   = GridBagConstraints.BOTH;
        gbc.gridy  = 0;
        gbc.weighty = 1;

        gbc.gridx = 0; gbc.weightx = 0;   add(lblNum,    gbc);
        gbc.gridx = 1; gbc.weightx = 0;   add(thumb,     gbc);
        gbc.gridx = 2; gbc.weightx = 1;   add(info,      gbc);
        gbc.gridx = 3; gbc.weightx = 0.3; add(lblGenero, gbc);
        gbc.gridx = 4; gbc.weightx = 0.1; add(lblAnio,   gbc);
        gbc.gridx = 5; gbc.weightx = 0;   add(lblDur,    gbc);
        gbc.gridx = 6; gbc.weightx = 0;   add(btnHeart,  gbc);

        // Hover + click para reproducir
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { setBackground(UITheme.HOVER); repaint(); }
            @Override public void mouseExited (MouseEvent e) { setBackground(null);          repaint(); }
            @Override public void mouseClicked(MouseEvent e) { if (onPlay != null) onPlay.run(); }
        });
    }

    /** Constructor sin Me Gusta (compatibilidad con paneles que no lo necesitan) */
    public SongRow(int numero, Cancion cancion, Runnable onPlay) {
        this(numero, cancion, onPlay, null, null);
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