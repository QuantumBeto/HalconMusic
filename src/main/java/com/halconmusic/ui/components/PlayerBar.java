package com.halconmusic.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.halconmusic.model.Cancion;
import com.halconmusic.ui.AudioService;
import com.halconmusic.ui.UITheme;

/**
 * Barra de reproducción inferior (siempre visible).
 * Simula la UI del reproductor tipo Spotify.
 */
public class PlayerBar extends JPanel {

    private JLabel  lblTitle;
    private JLabel  lblArtist;
    private JButton btnPlay;
    private JPanel  progFill;
    private JLabel  lblCurrent;
    private JLabel  lblTotal;

    private boolean isPlaying   = false;
    private int     duracionSeg = 0;
    private int     progSeg     = 0;
    private Timer   timer;

    public PlayerBar() {
        setPreferredSize(new Dimension(0, UITheme.PLAYER_H));
        setBackground(UITheme.PLAYER);
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));
        setLayout(new BorderLayout(0, 0));

        add(buildTrackPanel(),    BorderLayout.WEST);
        add(buildControlPanel(),  BorderLayout.CENTER);
        add(buildExtrasPanel(),   BorderLayout.EAST);
    }

    // ── Track info (izquierda) ────────────────────────────
    private JPanel buildTrackPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(UITheme.SIDEBAR_W, UITheme.PLAYER_H));

        // Thumbnail
        JPanel thumb = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7);
                g2.setColor(UITheme.MUTED);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 18));
                g2.drawString("♪", 10, 28);
                g2.dispose();
            }
        };
        thumb.setOpaque(false);
        thumb.setPreferredSize(new Dimension(44, 44));

        // Info
        JPanel info = new JPanel(new GridLayout(2, 1, 0, 1));
        info.setOpaque(false);
        lblTitle  = makeLabel("Sin reproducción", UITheme.TEXT,  UITheme.FONT_BODY);
        lblArtist = makeLabel("",                  UITheme.MUTED, UITheme.FONT_SMALL);
        info.add(lblTitle);
        info.add(lblArtist);

        p.add(thumb);
        p.add(info);
        return p;
    }

    // ── Controls (centro) ─────────────────────────────────
    private JPanel buildControlPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        // Botones
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btns.setOpaque(false);
        btns.add(ctrlBtn("⏮"));
        btns.add(ctrlBtn("⏭"));

        btnPlay = new JButton("▶") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isPlaying ? UITheme.ACCENT2 : UITheme.ACCENT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.BG);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                String t = isPlaying ? "⏸" : "▶";
                g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2 + 1,
                              (getHeight() + fm.getAscent()) / 2 - 3);
                g2.dispose();
            }
        };
        btnPlay.setPreferredSize(new Dimension(34, 34));
        btnPlay.setBorderPainted(false);
        btnPlay.setContentAreaFilled(false);
        btnPlay.setFocusPainted(false);
        btnPlay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnPlay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPlay.addActionListener(e -> togglePlay());
        btns.add(btnPlay);

        // Labels de tiempo
        lblCurrent = makeLabel("0:00", UITheme.MUTED, UITheme.FONT_SMALL);
        lblTotal   = makeLabel("0:00", UITheme.MUTED, UITheme.FONT_SMALL);

        // ── Barra de progreso deslizable ──────────────────────
        JPanel progTrack = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fondo gris
                g2.setColor(new Color(0xFF, 0xFF, 0xFF, 25));
                g2.fillRoundRect(0, getHeight()/2 - 3, getWidth(), 6, 3, 3);
                // Progreso dorado
                if (duracionSeg > 0) {
                    int w = (int)((double) progSeg / duracionSeg * getWidth());
                    g2.setColor(UITheme.ACCENT);
                    g2.fillRoundRect(0, getHeight()/2 - 3, w, 6, 3, 3);
                    // Bolita indicadora
                    g2.setColor(UITheme.ACCENT2);
                    g2.fillOval(w - 6, getHeight()/2 - 6, 12, 12);
                }
                g2.dispose();
            }
        };
        progTrack.setOpaque(false);
        progTrack.setPreferredSize(new Dimension(360, 18));
        progTrack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // ── Listeners para clic y arrastre ───────────────────
        MouseAdapter seekListener = new MouseAdapter() {

            // Calcula la posición en segundos según el clic en la barra
            private void seek(MouseEvent e) {
                if (duracionSeg <= 0) return;

                double pct     = (double) e.getX() / progTrack.getWidth();
                pct            = Math.max(0, Math.min(1, pct)); // clamp entre 0 y 1
                int nuevosSeg  = (int)(pct * duracionSeg);

                // Actualiza la UI
                progSeg = nuevosSeg;
                actualizarProgreso();

                // Salta al momento exacto en el audio
                AudioService.getInstance().buscarPosicion(nuevosSeg);
            }

            @Override
            public void mousePressed(MouseEvent e)  { seek(e); }

            @Override
            public void mouseDragged(MouseEvent e)  {
                seek(e);
                // Mientras arrastra muestra el tiempo en tiempo real
                if (duracionSeg > 0) {
                    double pct = (double) e.getX() / progTrack.getWidth();
                    pct = Math.max(0, Math.min(1, pct));
                    int seg = (int)(pct * duracionSeg);
                    int min = seg / 60;
                    int s   = seg % 60;
                    lblCurrent.setText(String.format("%d:%02d", min, s));
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // Agranda la barra al pasar el mouse (efecto hover)
                progTrack.setPreferredSize(new Dimension(360, 22));
                progTrack.getParent().revalidate();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Regresa al tamaño normal
                progTrack.setPreferredSize(new Dimension(360, 18));
                progTrack.getParent().revalidate();
            }
        };

        progTrack.addMouseListener(seekListener);
        progTrack.addMouseMotionListener(seekListener);

        // ── Fila de controles ────────────────────────────────
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

        JLabel volIcon = makeLabel("🔊", UITheme.MUTED, UITheme.FONT_SMALL);

        // Slider de volumen interactivo
        JSlider volSlider = new JSlider(0, 100, 65);
        volSlider.setOpaque(false);
        volSlider.setPreferredSize(new Dimension(80, 20));
        volSlider.setForeground(UITheme.ACCENT);
        volSlider.setPaintTrack(true);
        volSlider.setPaintTicks(false);
        volSlider.setPaintLabels(false);

        volSlider.addChangeListener(e -> {
            double volumen = volSlider.getValue() / 100.0;
            AudioService.getInstance().setVolumen(volumen);
            volIcon.setText(volumen == 0 ? "🔇" : volumen < 0.5 ? "🔉" : "🔊");
        });

        p.add(volIcon);
        p.add(volSlider);
        return p;
    }

    // ── API pública ───────────────────────────────────────
    public void reproducir(Cancion c) {
        lblTitle.setText(c.getNombre());
        lblArtist.setText(c.getNombreArtistasCompleto());
        duracionSeg = c.getDuracionSeg();
        progSeg     = 0;
        lblTotal.setText(c.getDuracionFormateada());
        isPlaying   = true;
        if (timer != null) timer.stop();
        actualizarProgreso();
        btnPlay.repaint();

        // ✅ Reproduce con pausa real
        AudioService.getInstance().reproducir(
            c.getIdCancion(),
            // Al terminar
            () -> SwingUtilities.invokeLater(() -> {
                isPlaying = false;
                progSeg   = 0;
                actualizarProgreso();
                btnPlay.repaint();
            }),
            // Al progresar — actualiza la barra en tiempo real
            (segundosActuales) -> SwingUtilities.invokeLater(() -> {
                progSeg = (int) Math.floor(segundosActuales);
                actualizarProgreso();
            })
        );
    }

    private void togglePlay() {
        AudioService audio = AudioService.getInstance();

        if (isPlaying) {
            // Pausar
            audio.pausar();
            if (timer != null) timer.stop();
            isPlaying = false;
        } else {
            // Reanudar
            audio.reanudar();
            isPlaying = true;
        }
        btnPlay.repaint();
    }

    private void startTimer() {
        if (timer != null) timer.stop();
        timer = new Timer(1000, e -> {
            if (progSeg < duracionSeg) {
                progSeg++;
                actualizarProgreso();
            } else {
                timer.stop();
                isPlaying = false;
                btnPlay.repaint();
            }
        });
        timer.start();
    }

    private void actualizarProgreso() {
        if (duracionSeg > 0) {
            int min = progSeg / 60;
            int seg = progSeg % 60;
            lblCurrent.setText(String.format("%d:%02d", min, seg));
        }
        // ✅ Repinta la barra directamente en lugar de cambiar el tamaño de progFill
        SwingUtilities.invokeLater(() -> {
        // Busca progTrack y lo repinta
            Container parent = btnPlay.getParent();
            if (parent != null) parent.getParent().repaint();
        });
    }

    private JButton ctrlBtn(String text) {
        JButton b = new JButton(text);
        b.setForeground(UITheme.MUTED);
        b.setFont(UITheme.FONT_BODY);
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

    private JLabel makeLabel(String text, Color color, Font font) {
        JLabel l = new JLabel(text);
        l.setForeground(color);
        l.setFont(font);
        return l;
    }
}
