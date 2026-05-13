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
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

import com.halconmusic.model.Cancion;
import com.halconmusic.ui.AudioService;
import com.halconmusic.ui.UITheme;

/**
 * Barra de reproducción inferior.
 * El botón ♥ llama a onMeGusta(cancion) para guardar en BD.
 */
public class PlayerBar extends JPanel {

    // ── Cola ─────────────────────────────────────────────
    private final List<Cancion> cola      = new ArrayList<>();
    private       int           colaIndex = -1;
    private       Cancion       cancionActual;

    // ── Callback externo ─────────────────────────────────
    private final Consumer<Cancion> onMeGusta;

    // ── UI ───────────────────────────────────────────────
    private JLabel  lblTitle;
    private JLabel  lblArtist;
    private JButton btnPlay;
    private JButton btnPrev;
    private JButton btnNext;
    private JLabel  btnHeart;
    private JLabel  lblCurrent;
    private JLabel  lblTotal;
    private JPanel  progTrack;

    // ── Estado ───────────────────────────────────────────
    private boolean isPlaying   = false;
    private boolean liked       = false;
    private int     duracionSeg = 0;
    private int     progSeg     = 0;

    private static final Font FONT_CTRL  = new Font("Segoe UI Symbol", Font.PLAIN, 14);
    private static final Font FONT_PLAY  = new Font("Segoe UI Symbol", Font.PLAIN, 13);
    private static final Font FONT_HEART = new Font("Segoe UI Symbol", Font.PLAIN, 17);

    public PlayerBar(Consumer<Cancion> onMeGusta) {
        this.onMeGusta = onMeGusta;

        setPreferredSize(new Dimension(0, UITheme.PLAYER_H));
        setBackground(UITheme.PLAYER);
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));
        setLayout(new BorderLayout(0, 0));

        add(buildTrackPanel(),   BorderLayout.WEST);
        add(buildControlPanel(), BorderLayout.CENTER);
        add(buildExtrasPanel(),  BorderLayout.EAST);
    }

    // ── Track info (izquierda) ────────────────────────────
    private JPanel buildTrackPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(UITheme.SIDEBAR_W + 20, UITheme.PLAYER_H));

        // Miniatura
        JPanel thumb = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7);
                if (cancionActual != null && cancionActual.getPortada() != null) {
                    Shape clip = new java.awt.geom.RoundRectangle2D.Float(0,0,getWidth(),getHeight(),7,7);
                    g2.setClip(clip);
                    drawCover(g2, cancionActual.getPortada(), 0, 0, getWidth(), getHeight());
                } else {
                    g2.setColor(UITheme.MUTED);
                    g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
                    g2.drawString("\u266A", 10, 28);
                }
                g2.dispose();
            }
        };
        thumb.setOpaque(false);
        thumb.setPreferredSize(new Dimension(44, 44));

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 1));
        info.setOpaque(false);
        lblTitle  = lbl("Sin reproducción", UITheme.TEXT,  UITheme.FONT_BODY);
        lblArtist = lbl("",                  UITheme.MUTED, UITheme.FONT_SMALL);
        info.add(lblTitle);
        info.add(lblArtist);

        // Botón ♥ — llama agregarMeGusta al activarse
        btnHeart = new JLabel("\u2661"); // ♡ vacío
        btnHeart.setFont(FONT_HEART);
        btnHeart.setForeground(UITheme.MUTED);
        btnHeart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnHeart.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        btnHeart.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { toggleLike(); }
            @Override public void mouseEntered(MouseEvent e) {
                if (!liked) btnHeart.setForeground(UITheme.TEXT);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!liked) btnHeart.setForeground(UITheme.MUTED);
            }
        });

        p.add(thumb);
        p.add(info);
        p.add(btnHeart);
        return p;
    }

    // ── Controls (centro) ─────────────────────────────────
    private JPanel buildControlPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btns.setOpaque(false);

        btnPrev = ctrlBtn("\u23EE"); // ⏮
        btnPrev.addActionListener(e -> skipAnterior());

        btnNext = ctrlBtn("\u23ED"); // ⏭
        btnNext.addActionListener(e -> skipSiguiente());

        btnPlay = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isPlaying ? UITheme.ACCENT2 : UITheme.ACCENT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.BG);
                g2.setFont(FONT_PLAY);
                FontMetrics fm = g2.getFontMetrics();
                String t = isPlaying ? "\u23F8" : "\u25B6";
                g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2 + 1,
                              (getHeight() + fm.getAscent()) / 2 - 3);
                g2.dispose();
            }
        };
        btnPlay.setPreferredSize(new Dimension(34, 34));
        btnPlay.setBorderPainted(false);
        btnPlay.setContentAreaFilled(false);
        btnPlay.setFocusPainted(false);
        btnPlay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPlay.addActionListener(e -> togglePlay());

        btns.add(btnPrev);
        btns.add(btnPlay);
        btns.add(btnNext);

        lblCurrent = lbl("0:00", UITheme.MUTED, UITheme.FONT_SMALL);
        lblTotal   = lbl("0:00", UITheme.MUTED, UITheme.FONT_SMALL);

        progTrack = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRoundRect(0, getHeight()/2 - 3, getWidth(), 6, 3, 3);
                if (duracionSeg > 0) {
                    int w = (int)((double) progSeg / duracionSeg * getWidth());
                    g2.setColor(UITheme.ACCENT);
                    g2.fillRoundRect(0, getHeight()/2 - 3, w, 6, 3, 3);
                    g2.setColor(UITheme.ACCENT2);
                    g2.fillOval(w - 6, getHeight()/2 - 6, 12, 12);
                }
                g2.dispose();
            }
        };
        progTrack.setOpaque(false);
        progTrack.setPreferredSize(new Dimension(360, 18));
        progTrack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        MouseAdapter seekListener = new MouseAdapter() {
            private void seek(MouseEvent e) {
                if (duracionSeg <= 0) return;
                double pct = Math.max(0, Math.min(1, (double) e.getX() / progTrack.getWidth()));
                progSeg = (int)(pct * duracionSeg);
                actualizarProgreso();
                AudioService.getInstance().buscarPosicion(progSeg);
            }
            @Override public void mousePressed(MouseEvent e)  { seek(e); }
            @Override public void mouseDragged(MouseEvent e)  { seek(e); }
        };
        progTrack.addMouseListener(seekListener);
        progTrack.addMouseMotionListener(seekListener);

        JPanel progRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        progRow.setOpaque(false);
        progRow.add(lblCurrent);
        progRow.add(progTrack);
        progRow.add(lblTotal);

        p.add(Box.createVerticalGlue());
        p.add(btns);
        p.add(Box.createVerticalStrut(4));
        p.add(progRow);
        p.add(Box.createVerticalGlue());
        return p;
    }

    // ── Extras (derecha) ──────────────────────────────────
    private JPanel buildExtrasPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(180, UITheme.PLAYER_H));

        JLabel lblVol = new JLabel("\u25B7\u25B7");
        lblVol.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 13));
        lblVol.setForeground(UITheme.MUTED);

        JSlider volSlider = new JSlider(0, 100, 65);
        volSlider.setOpaque(false);
        volSlider.setPreferredSize(new Dimension(80, 20));
        volSlider.setForeground(UITheme.ACCENT);
        volSlider.setPaintTicks(false);
        volSlider.setPaintLabels(false);
        volSlider.addChangeListener(e -> {
            double vol = volSlider.getValue() / 100.0;
            AudioService.getInstance().setVolumen(vol);
            lblVol.setText(vol == 0 ? "\u25A1\u25A1" : vol < 0.5 ? "\u25B7\u25B6" : "\u25B6\u25B6");
        });

        p.add(lblVol);
        p.add(volSlider);
        return p;
    }

    // ── API pública ───────────────────────────────────────
    public void reproducir(Cancion c) {
        cancionActual = c;
        if (cola.isEmpty() || !cola.get(cola.size()-1).getIdCancion().equals(c.getIdCancion())) {
            cola.add(c);
        }
        colaIndex = cola.indexOf(c);

        lblTitle.setText(c.getNombre());
        lblArtist.setText(c.getNombreArtistasCompleto());
        duracionSeg = c.getDuracionSeg();
        progSeg     = 0;
        lblTotal.setText(c.getDuracionFormateada());
        isPlaying   = true;
        liked       = false;
        btnHeart.setText("\u2661");
        btnHeart.setForeground(UITheme.MUTED);

        // Refresca miniatura
        SwingUtilities.invokeLater(() -> {
            if (getComponent(0) instanceof JPanel trackPanel) {
                trackPanel.repaint();
            }
            repaint();
        });

        actualizarProgreso();
        btnPlay.repaint();

        AudioService.getInstance().reproducir(
            c.getIdCancion(),
            () -> SwingUtilities.invokeLater(() -> {
                isPlaying = false;
                progSeg   = 0;
                actualizarProgreso();
                btnPlay.repaint();
                skipSiguiente();
            }),
            (seg) -> SwingUtilities.invokeLater(() -> {
                progSeg = (int) Math.floor(seg);
                actualizarProgreso();
            })
        );
    }

    // ── Acciones internas ─────────────────────────────────
    private void togglePlay() {
        AudioService audio = AudioService.getInstance();
        if (isPlaying) { audio.pausar();   isPlaying = false; }
        else           { audio.reanudar(); isPlaying = true; }
        btnPlay.repaint();
    }

    /**
     * Alterna el corazón y, si pasa a "gustado",
     * llama al callback que persiste en BD.
     */
    private void toggleLike() {
        liked = !liked;
        if (liked) {
            btnHeart.setText("\u2665");  // ♥ relleno
            btnHeart.setForeground(new Color(0xFF, 0x22, 0x55));
            // Guardar en BD si hay canción reproduciendo y callback registrado
            if (cancionActual != null && onMeGusta != null) {
                onMeGusta.accept(cancionActual);
            }
        } else {
            btnHeart.setText("\u2661");  // ♡ vacío
            btnHeart.setForeground(UITheme.MUTED);
        }
    }

    private void skipAnterior() {
        if (cola.isEmpty()) return;
        if (progSeg > 3) {
            progSeg = 0;
            AudioService.getInstance().buscarPosicion(0);
            actualizarProgreso();
            return;
        }
        if (colaIndex > 0) { colaIndex--; reproducir(cola.get(colaIndex)); }
    }

    private void skipSiguiente() {
        if (cola.isEmpty()) return;
        if (colaIndex < cola.size() - 1) { colaIndex++; reproducir(cola.get(colaIndex)); }
        else { isPlaying = false; progSeg = 0; actualizarProgreso(); btnPlay.repaint(); }
    }

    private void actualizarProgreso() {
        if (duracionSeg > 0)
            lblCurrent.setText(String.format("%d:%02d", progSeg/60, progSeg%60));
        SwingUtilities.invokeLater(() -> { if (progTrack != null) progTrack.repaint(); });
    }

    // ── Helpers ───────────────────────────────────────────
    private JButton ctrlBtn(String text) {
        JButton b = new JButton(text);
        b.setForeground(UITheme.MUTED);
        b.setFont(FONT_CTRL);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setForeground(UITheme.TEXT); }
            @Override public void mouseExited (MouseEvent e) { b.setForeground(UITheme.MUTED); }
        });
        return b;
    }

    private JLabel lbl(String t, Color c, Font f) {
        JLabel l = new JLabel(t);
        l.setForeground(c);
        l.setFont(f);
        return l;
    }

    /**
     * Dibuja una imagen manteniendo proporción (cover — rellena sin deformar).
     */
    public static void drawCover(Graphics2D g2, java.awt.Image img,
                                  int x, int y, int w, int h) {
        int iw = img.getWidth(null);
        int ih = img.getHeight(null);
        if (iw <= 0 || ih <= 0) return;
        double scale = Math.max((double) w / iw, (double) h / ih);
        int dw = (int) Math.ceil(iw * scale);
        int dh = (int) Math.ceil(ih * scale);
        g2.drawImage(img, x + (w - dw) / 2, y + (h - dh) / 2, dw, dh, null);
    }
}