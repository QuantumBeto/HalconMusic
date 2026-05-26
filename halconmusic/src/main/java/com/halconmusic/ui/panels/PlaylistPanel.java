package com.halconmusic.ui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.halconmusic.dao.CancionDAO;
import com.halconmusic.model.Cancion;
import com.halconmusic.ui.UITheme;
import com.halconmusic.ui.components.SongRow;

public class PlaylistPanel extends JPanel {

    private final CancionDAO cancionDAO = new CancionDAO();
    private final Consumer<Cancion> onPlay;
    private final Consumer<Cancion> onLike;
    private final String idUsuario;

    private JLabel  lblNombre;
    private JLabel  lblInfo;
    private JPanel  panelCover;
    private JPanel  panelCanciones;
    private String  idPlaylistActual;

    public PlaylistPanel(Consumer<Cancion> onPlay, Consumer<Cancion> onLike, String idUsuario) {
        this.onPlay    = onPlay;
        this.onLike    = onLike;
        this.idUsuario = idUsuario;
        setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
    }

    private void build() {
        // ── Header ──
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        // Portada
        panelCover = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panelCover.setPreferredSize(new Dimension(120, 120));
        panelCover.setOpaque(false);

        // Info
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel lblTipo = new JLabel("PLAYLIST");
        lblTipo.setFont(UITheme.FONT_LABEL);
        lblTipo.setForeground(UITheme.MUTED);

        lblNombre = new JLabel("—");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblNombre.setForeground(UITheme.TEXT);

        lblInfo = new JLabel(" ");
        lblInfo.setFont(UITheme.FONT_SMALL);
        lblInfo.setForeground(UITheme.MUTED);

        info.add(lblTipo);
        info.add(Box.createVerticalStrut(6));
        info.add(lblNombre);
        info.add(Box.createVerticalStrut(4));
        info.add(lblInfo);

        header.add(panelCover, BorderLayout.WEST);
        header.add(info,       BorderLayout.CENTER);

        // ── Lista de canciones ──
        panelCanciones = new JPanel();
        panelCanciones.setBackground(UITheme.BG);
        panelCanciones.setLayout(new BoxLayout(panelCanciones, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(panelCanciones);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void cargar(String idPlaylist, String nombrePlaylist,
                       String descripcion, Image portada) {
        this.idPlaylistActual = idPlaylist;
        lblNombre.setText(nombrePlaylist);
        lblInfo.setText(descripcion != null && !descripcion.isBlank() ? descripcion : " ");

        // Portada
        panelCover.removeAll();
        if (portada != null) {
            JLabel imgLbl = new JLabel(new ImageIcon(
                portada.getScaledInstance(120, 120, Image.SCALE_SMOOTH)));
            panelCover.setLayout(new BorderLayout());
            panelCover.add(imgLbl, BorderLayout.CENTER);
        }
        panelCover.revalidate();

        // Canciones en hilo aparte
        new Thread(() -> {
            List<Cancion> canciones = cancionDAO.obtenerPorPlaylist(idPlaylist);
            SwingUtilities.invokeLater(() -> {
                panelCanciones.removeAll();
                if (canciones.isEmpty()) {
                    JLabel vacio = new JLabel("Esta playlist no tiene canciones.");
                    vacio.setForeground(UITheme.MUTED);
                    vacio.setFont(UITheme.FONT_BODY);
                    vacio.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
                    panelCanciones.add(vacio);
                } else {
                    int[] n = {1};
                    for (Cancion c : canciones)
                        panelCanciones.add(new SongRow(n[0]++, c,
                            () -> onPlay.accept(c), idUsuario, () -> onLike.accept(c)));
                }
                panelCanciones.revalidate();
                panelCanciones.repaint();
            });
        }).start();
    }
}