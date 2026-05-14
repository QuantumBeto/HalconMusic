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
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.halconmusic.model.Cancion;
import com.halconmusic.ui.UITheme;

/**
 * Fila de canción.
 *
 * FIX ghost-text: usamos boolean `hovered` + siempre limpiar el rect en
 * paintComponent. Nunca llamamos setBackground()/setOpaque() desde listeners
 * porque eso deja píxeles residuales al no garantizarse repaint del padre.
 */
public class SongRow extends JPanel {

    private final Cancion  cancion;
    private       boolean  playing = false;
    private       boolean  hovered = false;   // ← estado propio, no setBackground

    public SongRow(int numero, Cancion cancion,
                   Runnable onPlay, String idUsuario, Runnable onMeGusta) {
        this.cancion = cancion;

        setOpaque(false);           // nunca cambia
        setLayout(new GridBagLayout());
        // NO setPreferredSize(0, 52) — dejamos que GridBag calcule anchos normalmente
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        setMinimumSize(new Dimension(100, 52));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // ── Número ──────────────────────────────────────
        JLabel lblNum = lbl(String.valueOf(numero), UITheme.MUTED, UITheme.FONT_BODY);
        lblNum.setPreferredSize(new Dimension(28, 36));
        lblNum.setHorizontalAlignment(SwingConstants.CENTER);

        // ── Portada ──────────────────────────────────────
        JPanel thumb = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setColor(UITheme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
                if (cancion.getPortada() != null) {
                    Shape clip = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 5, 5);
                    g2.setClip(clip);
                    drawCover(g2, cancion.getPortada(), 0, 0, getWidth(), getHeight());
                } else {
                    g2.setColor(UITheme.MUTED);
                    g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
                    g2.drawString("\u266A", 9, 23);
                }
                g2.dispose();
            }
        };
        thumb.setOpaque(false);
        thumb.setPreferredSize(new Dimension(36, 36));
        thumb.setMinimumSize(new Dimension(36, 36));
        thumb.setMaximumSize(new Dimension(36, 36));

        // ── Info ──────────────────────────────────────────
        JPanel info = new JPanel(new GridLayout(2, 1, 0, 1));
        info.setOpaque(false);
        info.add(lbl(cancion.getNombre(),                UITheme.TEXT,  UITheme.FONT_BODY));
        info.add(lbl(cancion.getNombreArtistasCompleto(), UITheme.MUTED, UITheme.FONT_SMALL));

        // ── Género ────────────────────────────────────────
        JLabel lblGenero = lbl(cancion.getGenero(),               UITheme.MUTED, UITheme.FONT_SMALL);

        // ── Año ───────────────────────────────────────────
        JLabel lblAnio = lbl(String.valueOf(cancion.getFecha()),  UITheme.MUTED, UITheme.FONT_SMALL);
        lblAnio.setHorizontalAlignment(SwingConstants.CENTER);

        // ── Duración ─────────────────────────────────────
        JLabel lblDur = lbl(cancion.getDuracionFormateada(),      UITheme.MUTED, UITheme.FONT_SMALL);
        lblDur.setHorizontalAlignment(SwingConstants.RIGHT);
        lblDur.setPreferredSize(new Dimension(42, 36));
        lblDur.setMinimumSize(new Dimension(42, 36));

        // ── ♥ Me Gusta ────────────────────────────────────
        JLabel heart = new JLabel("\u2661");
        heart.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        heart.setForeground(UITheme.MUTED);
        heart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        heart.setPreferredSize(new Dimension(30, 36));
        heart.setMinimumSize(new Dimension(30, 36));
        heart.setHorizontalAlignment(SwingConstants.CENTER);

        if (idUsuario != null && onMeGusta != null) {
            final boolean[] liked = {false};
            heart.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    e.consume();
                    if (!liked[0]) {
                        liked[0] = true;
                        heart.setText("\u2665");
                        heart.setForeground(new Color(0xFF, 0x22, 0x55));
                        onMeGusta.run();
                    }
                }
                @Override public void mouseEntered(MouseEvent e) {
                    if (!liked[0]) heart.setForeground(new Color(0xFF, 0x22, 0x55));
                }
                @Override public void mouseExited(MouseEvent e) {
                    if (!liked[0]) heart.setForeground(UITheme.MUTED);
                }
            });
        }

        // ── GridBag layout ────────────────────────────────
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.gridy   = 0;
        gbc.weighty = 1;

        gbc.gridx = 0; gbc.weightx = 0;    gbc.insets = new Insets(0,6,0,4);  add(lblNum,    gbc);
        gbc.gridx = 1; gbc.weightx = 0;    gbc.insets = new Insets(0,0,0,8);  add(thumb,     gbc);
        gbc.gridx = 2; gbc.weightx = 1;    gbc.insets = new Insets(0,0,0,8);  add(info,      gbc);
        gbc.gridx = 3; gbc.weightx = 0.25; gbc.insets = new Insets(0,0,0,8);  add(lblGenero, gbc);
        gbc.gridx = 4; gbc.weightx = 0;    gbc.insets = new Insets(0,0,0,8);  add(lblAnio,   gbc);
        gbc.gridx = 5; gbc.weightx = 0;    gbc.insets = new Insets(0,0,0,4);  add(lblDur,    gbc);
        gbc.gridx = 6; gbc.weightx = 0;    gbc.insets = new Insets(0,0,0,6);  add(heart,     gbc);

        // ── Hover — SOLO boolean, nunca setBackground() ──
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
            @Override public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getSource() == SongRow.this && onPlay != null) onPlay.run();
            }
        });
    }

    public SongRow(int numero, Cancion cancion, Runnable onPlay) {
        this(numero, cancion, onPlay, null, null);
    }

    public void setPlaying(boolean p) { this.playing = p; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        // FIX: siempre limpiar el rect completo — elimina ghost pixels del hover anterior
        g.setColor(UITheme.BG);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (playing || hovered) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(playing ? UITheme.ACCENT_SOFT : UITheme.HOVER);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.dispose();
        }
        // NO llamamos super.paintComponent — ya manejamos el fondo manualmente.
        // Los hijos los pinta paintChildren() después de este método.
    }

    /** Cover-fit: escala la imagen sin deformar, recortando si es necesario. */
    public static void drawCover(Graphics2D g2, Image img, int x, int y, int w, int h) {
        int iw = img.getWidth(null);
        int ih = img.getHeight(null);
        if (iw <= 0 || ih <= 0) return;
        double scale = Math.max((double) w / iw, (double) h / ih);
        int dw = (int) Math.ceil(iw * scale);
        int dh = (int) Math.ceil(ih * scale);
        g2.drawImage(img, x + (w - dw) / 2, y + (h - dh) / 2, dw, dh, null);
    }

    private JLabel lbl(String t, Color c, Font f) {
        JLabel l = new JLabel(t == null ? "" : t);
        l.setForeground(c); l.setFont(f);
        return l;
    }
}