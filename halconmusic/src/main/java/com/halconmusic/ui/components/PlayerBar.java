package com.halconmusic.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.halconmusic.dao.CancionDAO;
import com.halconmusic.model.Cancion;
import com.halconmusic.ui.AudioService;
import com.halconmusic.ui.UITheme;

/**
 * Barra de reproducción inferior.
 *
 * REQ. 11 — Muestra: imagen (portada), audio, letra.
 *   · Portada: miniatura izquierda.
 *   · Audio:   AudioService (ya existía).
 *   · Letra:   botón "♩ Letra" abre JDialog con el texto desde BD (CLOB).
 *
 * Nota sobre VIDEO: la tabla CANCIONES almacena solo MUSICA (BLOB audio).
 *   No existe columna VIDEO. Si en el futuro se agrega, sustituir EMPTY_BLOB()
 *   por el stream del archivo de video y renderizarlo con JavaFX MediaView.
 */
public class PlayerBar extends JPanel {

    // ── Cola ──────────────────────────────────────────────
    private final List<Cancion> cola      = new ArrayList<>();
    private       int           colaIndex = -1;
    private       Cancion       cancionActual;

    // ── Callbacks ─────────────────────────────────────────
    private final Consumer<Cancion> onMeGusta;
    private final CancionDAO        cancionDAO = new CancionDAO();

    // ── UI ────────────────────────────────────────────────
    private JLabel  lblTitle;
    private JLabel  lblArtist;
    private JButton btnPlay;
    private JButton btnPrev;
    private JButton btnNext;
    private JLabel  btnHeart;
    private JButton btnLetra;       // ← REQ. 11
    private JButton btnVideo;       // ← REQ. 11 video
    private JLabel  lblCurrent;
    private JLabel  lblTotal;
    private JPanel  progTrack;
    private JPanel  thumbPanel;     // para repaint de portada

    // ── Estado ────────────────────────────────────────────
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

        // Portada (REQ. 11 — imagen de la canción)
        thumbPanel = new JPanel() {
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
        thumbPanel.setOpaque(false);
        thumbPanel.setPreferredSize(new Dimension(44, 44));

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 1));
        info.setOpaque(false);
        lblTitle  = lbl("Sin reproducción", UITheme.TEXT,  UITheme.FONT_BODY);
        lblArtist = lbl("",                  UITheme.MUTED, UITheme.FONT_SMALL);
        info.add(lblTitle);
        info.add(lblArtist);

        // Botón ♥
        btnHeart = new JLabel("\u2661");
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

        // Botón Letra (REQ. 11)
        btnLetra = new JButton(UITheme.emoji("🎵", " Letra"));
        btnLetra.setFont(UITheme.FONT_SMALL);
        btnLetra.setForeground(UITheme.MUTED);
        btnLetra.setBackground(UITheme.SURFACE);
        btnLetra.setOpaque(true);         
        btnLetra.setContentAreaFilled(true); 
        btnLetra.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        btnLetra.setFocusPainted(false);
        btnLetra.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLetra.setEnabled(false);
        btnLetra.addActionListener(e -> mostrarLetra());

        // Botón Video (REQ. 11)
        btnVideo = new JButton(UITheme.emoji("🎬", " Video"));
        btnVideo.setFont(UITheme.FONT_SMALL);
        btnVideo.setForeground(UITheme.MUTED);
        btnVideo.setBackground(UITheme.SURFACE);
        btnVideo.setOpaque(true);        
        btnVideo.setContentAreaFilled(true);  
        btnVideo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        btnVideo.setFocusPainted(false);
        btnVideo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVideo.setEnabled(false);
        btnVideo.addActionListener(e -> mostrarVideo());

        p.add(thumbPanel);
        p.add(info);
        p.add(btnHeart);
        p.add(btnLetra);
        p.add(btnVideo);
        return p;
    }

    // ── Controls (centro) ─────────────────────────────────
    private JPanel buildControlPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btns.setOpaque(false);

        btnPrev = ctrlBtn("\u23EE");
        btnPrev.addActionListener(e -> skipAnterior());

        btnNext = ctrlBtn("\u23ED");
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

    // ── Extras (derecha) — volumen ────────────────────────
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

    public void detener() {
        AudioService.getInstance().detener();
        cancionActual = null;
        isPlaying     = false;
        progSeg       = 0;
        lblTitle.setText("Sin reproducción");
        lblArtist.setText("");
        btnLetra.setEnabled(false);
        btnVideo.setEnabled(false);
        if (thumbPanel != null) thumbPanel.repaint();
        repaint();
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
        btnLetra.setEnabled(true);   // activa botón letra cuando hay canción
        btnVideo.setEnabled(true);   // activa botón video cuando hay canción

        SwingUtilities.invokeLater(() -> {
            if (thumbPanel != null) thumbPanel.repaint();
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

    // ── REQ. 11 — Mostrar video ───────────────────────────
    /**
     * Pausa el audio, obtiene el VIDEO (BLOB) en hilo secundario
     * y lo reproduce con sonido en un JDialog usando JavaFX MediaView.
     */
    private void mostrarVideo() {
        if (cancionActual == null) return;

        AudioService.getInstance().pausar();
        isPlaying = false;
        btnPlay.repaint();

        new Thread(() -> {
            byte[] videoBytes = cancionDAO.obtenerVideo(cancionActual.getIdCancion());

            SwingUtilities.invokeLater(() -> {
                if (videoBytes == null || videoBytes.length == 0) {
                    javax.swing.JOptionPane.showMessageDialog(
                        SwingUtilities.getWindowAncestor(PlayerBar.this),
                        "Esta canción no tiene video registrado.",
                        "Sin video", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                try {
                    java.io.File tempFile = java.io.File.createTempFile("halcon_video_", ".mp4");
                    tempFile.deleteOnExit();
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                        fos.write(videoBytes);
                    }

                    javafx.application.Platform.runLater(() -> {
                        javafx.stage.Stage stage = new javafx.stage.Stage();
                        stage.setTitle("▶  " + cancionActual.getNombre()
                                     + "  —  " + cancionActual.getNombreArtistasCompleto());
                        stage.initModality(javafx.stage.Modality.NONE);
                        stage.setResizable(true);

                        // ── Media ──────────────────────────────────────────────
                        javafx.scene.media.Media       media  =
                            new javafx.scene.media.Media(tempFile.toURI().toString());
                        javafx.scene.media.MediaPlayer player =
                            new javafx.scene.media.MediaPlayer(media);
                        javafx.scene.media.MediaView   view   =
                            new javafx.scene.media.MediaView(player);

                        view.setFitWidth(854);
                        view.setFitHeight(480);
                        view.setPreserveRatio(true);

                        // ── Barra de progreso ──────────────────────────────────
                        javafx.scene.control.Slider sliderProgreso =
                            new javafx.scene.control.Slider(0, 1, 0);
                        sliderProgreso.setPrefWidth(620);
                        sliderProgreso.setStyle("-fx-accent: #1DB954;");

                        javafx.scene.control.Label lblActual  = new javafx.scene.control.Label("0:00");
                        javafx.scene.control.Label lblTotal   = new javafx.scene.control.Label("0:00");
                        lblActual.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12px;");
                        lblTotal.setStyle( "-fx-text-fill: #aaaaaa; -fx-font-size: 12px;");

                        // ── Botones de control ─────────────────────────────────
                        javafx.scene.control.Button btnPlayPause =
                            new javafx.scene.control.Button("⏸");
                        btnPlayPause.setStyle(
                            "-fx-background-color: #1DB954; -fx-text-fill: black;" +
                            "-fx-font-size: 14px; -fx-background-radius: 50%;" +
                            "-fx-min-width: 36px; -fx-min-height: 36px;");

                        javafx.scene.control.Button btnRetroceder =
                            new javafx.scene.control.Button("⏮");
                        javafx.scene.control.Button btnAdelantar =
                            new javafx.scene.control.Button("⏭");
                        String estiloCtrl =
                            "-fx-background-color: transparent; -fx-text-fill: #cccccc;" +
                            "-fx-font-size: 16px; -fx-cursor: hand;";
                        btnRetroceder.setStyle(estiloCtrl);
                        btnAdelantar.setStyle(estiloCtrl);

                        // ── Volumen ────────────────────────────────────────────
                        javafx.scene.control.Label lblVolIcon =
                            new javafx.scene.control.Label("🔊");
                        lblVolIcon.setStyle("-fx-font-size: 14px;");

                        javafx.scene.control.Slider sliderVol =
                            new javafx.scene.control.Slider(0, 1, 0.8);
                        sliderVol.setPrefWidth(90);
                        sliderVol.setStyle("-fx-accent: #ffffff;");
                        player.setVolume(0.8);

                        sliderVol.valueProperty().addListener((obs, oldV, newV) ->
                            player.setVolume(newV.doubleValue()));

                        // ── Tiempo actual mientras reproduce ───────────────────
                        player.currentTimeProperty().addListener((obs, oldT, newT) -> {
                            if (!sliderProgreso.isValueChanging()) {
                                javafx.util.Duration dur = player.getTotalDuration();
                                if (dur != null && dur.greaterThan(javafx.util.Duration.ZERO)) {
                                    sliderProgreso.setValue(newT.toSeconds() / dur.toSeconds());
                                }
                            }
                            int seg = (int) newT.toSeconds();
                            lblActual.setText(String.format("%d:%02d", seg / 60, seg % 60));
                        });

                        // Duración total cuando el media esté listo
                        player.setOnReady(() -> {
                            int tot = (int) player.getTotalDuration().toSeconds();
                            lblTotal.setText(String.format("%d:%02d", tot / 60, tot % 60));
                        });

                        // ── Seek al mover slider ───────────────────────────────
                        sliderProgreso.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
                            if (!isChanging) {
                                javafx.util.Duration dur = player.getTotalDuration();
                                if (dur != null)
                                    player.seek(dur.multiply(sliderProgreso.getValue()));
                            }
                        });
                        sliderProgreso.setOnMouseClicked(e -> {
                            javafx.util.Duration dur = player.getTotalDuration();
                            if (dur != null)
                                player.seek(dur.multiply(sliderProgreso.getValue()));
                        });

                        // ── Acciones botones ───────────────────────────────────
                        final boolean[] playing = {true};

                        btnPlayPause.setOnAction(e -> {
                            if (playing[0]) {
                                player.pause();
                                btnPlayPause.setText("▶");
                            } else {
                                player.play();
                                btnPlayPause.setText("⏸");
                            }
                            playing[0] = !playing[0];
                        });

                        btnRetroceder.setOnAction(e -> {
                            javafx.util.Duration actual = player.getCurrentTime();
                            player.seek(actual.subtract(javafx.util.Duration.seconds(10)));
                        });

                        btnAdelantar.setOnAction(e -> {
                            javafx.util.Duration actual = player.getCurrentTime();
                            player.seek(actual.add(javafx.util.Duration.seconds(10)));
                        });

                        player.setOnEndOfMedia(() -> {
                            btnPlayPause.setText("▶");
                            playing[0] = false;
                        });

                        // ── Layout ─────────────────────────────────────────────
                        javafx.scene.layout.HBox rowProgreso = new javafx.scene.layout.HBox(8,
                            lblActual, sliderProgreso, lblTotal);
                        rowProgreso.setAlignment(javafx.geometry.Pos.CENTER);

                        javafx.scene.layout.HBox rowControles = new javafx.scene.layout.HBox(16,
                            btnRetroceder, btnPlayPause, btnAdelantar);
                        rowControles.setAlignment(javafx.geometry.Pos.CENTER);

                        javafx.scene.layout.HBox rowVolumen = new javafx.scene.layout.HBox(8,
                            lblVolIcon, sliderVol);
                        rowVolumen.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

                        javafx.scene.layout.BorderPane rowBottom =
                            new javafx.scene.layout.BorderPane();
                        rowBottom.setCenter(rowControles);
                        rowBottom.setRight(rowVolumen);
                        javafx.scene.layout.BorderPane.setAlignment(
                            rowVolumen, javafx.geometry.Pos.CENTER_RIGHT);

                        javafx.scene.layout.VBox playbar = new javafx.scene.layout.VBox(10,
                            rowProgreso, rowBottom);
                        playbar.setStyle(
                            "-fx-background-color: #181818;" +
                            "-fx-padding: 12 20 12 20;");

                        javafx.scene.layout.StackPane videoArea =
                            new javafx.scene.layout.StackPane(view);
                        videoArea.setStyle("-fx-background-color: black;");

                        javafx.scene.layout.BorderPane root =
                            new javafx.scene.layout.BorderPane();
                        root.setCenter(videoArea);
                        root.setBottom(playbar);
                        root.setStyle("-fx-background-color: black;");

                        stage.setScene(new javafx.scene.Scene(root, 854, 560));
                        stage.show();
                        player.play();

                        stage.setOnCloseRequest(ev -> player.stop());
                    });

                } catch (Exception ex) {
                    System.err.println("Error al preparar video: " + ex.getMessage());
                }
            });
        }).start();
    }

    // ── REQ. 11 — Mostrar letra ───────────────────────────
    /**
     * Consulta la letra (CLOB) en BD y la muestra en un JDialog.
     * Si la canción no tiene letra registrada, informa al usuario.
     */
    private void mostrarLetra() {
        if (cancionActual == null) return;

        // Busca la letra en BD en hilo secundario
        new Thread(() -> {
            String letra = cancionDAO.obtenerLetra(cancionActual.getIdCancion());
            SwingUtilities.invokeLater(() -> {
                JDialog dialogo = new JDialog(
                    SwingUtilities.getWindowAncestor(PlayerBar.this),
                    "Letra — " + cancionActual.getNombre(),
                    Dialog.ModalityType.MODELESS);
                dialogo.setSize(500, 560);
                dialogo.setLocationRelativeTo(PlayerBar.this);

                JPanel contenido = new JPanel(new BorderLayout(0, 12));
                contenido.setBackground(UITheme.SURFACE);
                contenido.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

                // Cabecera
                JLabel titulo = new JLabel(cancionActual.getNombre());
                titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
                titulo.setForeground(UITheme.TEXT);
                JLabel artista = new JLabel(cancionActual.getNombreArtistasCompleto());
                artista.setFont(UITheme.FONT_SMALL);
                artista.setForeground(UITheme.MUTED);

                JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
                header.setOpaque(false);
                header.add(titulo);
                header.add(artista);

                // Cuerpo — letra
                JTextArea areaLetra = new JTextArea();
                areaLetra.setEditable(false);
                areaLetra.setLineWrap(true);
                areaLetra.setWrapStyleWord(true);
                areaLetra.setBackground(UITheme.SURFACE);
                areaLetra.setForeground(UITheme.TEXT);
                areaLetra.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                areaLetra.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
                areaLetra.setText(
                    (letra == null || letra.isBlank())
                        ? "Esta canción no tiene letra registrada en la base de datos."
                        : letra);

                JScrollPane scroll = new JScrollPane(areaLetra);
                scroll.setBorder(null);
                scroll.setOpaque(false);
                scroll.getViewport().setOpaque(false);
                scroll.getVerticalScrollBar().setUnitIncrement(12);

                contenido.add(header, BorderLayout.NORTH);
                contenido.add(scroll, BorderLayout.CENTER);

                dialogo.setContentPane(contenido);
                dialogo.setVisible(true);
            });
        }).start();
    }

    // ── Acciones internas ─────────────────────────────────
    private void togglePlay() {
        AudioService audio = AudioService.getInstance();
        if (isPlaying) { audio.pausar();   isPlaying = false; }
        else           { audio.reanudar(); isPlaying = true; }
        btnPlay.repaint();
    }

    private void toggleLike() {
        liked = !liked;
        if (liked) {
            btnHeart.setText("\u2665");
            btnHeart.setForeground(new Color(0xFF, 0x22, 0x55));
            if (cancionActual != null && onMeGusta != null) {
                onMeGusta.accept(cancionActual);
            }
        } else {
            btnHeart.setText("\u2661");
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