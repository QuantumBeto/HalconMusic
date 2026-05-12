package com.halconmusic.ui.components;

import com.halconmusic.ui.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Panel con bordes redondeados reutilizable.
 */
public class RoundedPanel extends JPanel {

    private final int   radius;
    private       Color background;

    public RoundedPanel(int radius, Color background) {
        this.radius     = radius;
        this.background = background;
        setOpaque(false);
    }

    public RoundedPanel(int radius) {
        this(radius, UITheme.CARD);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(background);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
        g2.dispose();
        super.paintComponent(g);
    }

    public void setBackground(Color bg) {
        this.background = bg;
        repaint();
    }
}

// ─────────────────────────────────────────────────────────────────

/**
 * Tarjeta clickeable de artista/álbum con hover effect y botón de play.
 */
class CardComponent extends RoundedPanel {

    private final String titulo;
    private final String subtitulo;
    private final Image  imagen;
    private       boolean circular; // true para artistas (imagen circular)

    public CardComponent(String titulo, String subtitulo, Image imagen, boolean circular, Runnable onPlay) {
        super(UITheme.RADIUS, UITheme.CARD);
        this.titulo    = titulo;
        this.subtitulo = subtitulo;
        this.imagen    = imagen;
        this.circular  = circular;

        setPreferredSize(new Dimension(UITheme.CARD_SIZE, UITheme.CARD_SIZE + 50));
        setLayout(new BorderLayout());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                setBackground(UITheme.SURFACE);
                setBorder(BorderFactory.createLineBorder(UITheme.withAlpha(UITheme.ACCENT, 50), 1, true));
                repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                setBackground(UITheme.CARD);
                setBorder(null);
                repaint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                if (onPlay != null) onPlay.run();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,        RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,           RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,       RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int imgSize = w - 24;
        int imgY    = 12;

        // Fondo de imagen
        g2.setColor(UITheme.SURFACE);
        if (circular) {
            g2.fillOval(12, imgY, imgSize, imgSize);
        } else {
            g2.fillRoundRect(12, imgY, imgSize, imgSize, 8, 8);
        }

        // Imagen
        if (imagen != null) {
            if (circular) {
                Shape clip = new java.awt.geom.Ellipse2D.Float(12, imgY, imgSize, imgSize);
                g2.setClip(clip);
                g2.drawImage(imagen, 12, imgY, imgSize, imgSize, null);
                g2.setClip(null);
            } else {
                Shape clip = new RoundRectangle2D.Float(12, imgY, imgSize, imgSize, 8, 8);
                g2.setClip(clip);
                g2.drawImage(imagen, 12, imgY, imgSize, imgSize, null);
                g2.setClip(null);
            }
        } else {
            // Placeholder — icono de nota musical
            g2.setColor(UITheme.MUTED);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 28));
            FontMetrics fm = g2.getFontMetrics();
            String icon = "♪";
            g2.drawString(icon, 12 + (imgSize - fm.stringWidth(icon)) / 2,
                          imgY + (imgSize + fm.getAscent()) / 2 - 4);
        }

        // Título
        int textY = imgY + imgSize + 14;
        g2.setColor(UITheme.TEXT);
        g2.setFont(UITheme.FONT_BODY);
        drawTruncated(g2, titulo, 12, textY, w - 24);

        // Subtítulo
        g2.setColor(UITheme.MUTED);
        g2.setFont(UITheme.FONT_SMALL);
        drawTruncated(g2, subtitulo, 12, textY + 16, w - 24);

        g2.dispose();
    }

    private void drawTruncated(Graphics2D g2, String text, int x, int y, int maxW) {
        if (text == null) return;
        FontMetrics fm = g2.getFontMetrics();
        String t = text;
        while (fm.stringWidth(t) > maxW && t.length() > 3) {
            t = t.substring(0, t.length() - 4) + "...";
        }
        g2.drawString(t, x, y);
    }
}
