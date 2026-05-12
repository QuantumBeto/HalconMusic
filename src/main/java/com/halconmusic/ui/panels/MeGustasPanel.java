package com.halconmusic.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.halconmusic.dao.CancionDAO;
import com.halconmusic.model.Cancion;
import com.halconmusic.ui.UITheme;
import com.halconmusic.ui.components.SongRow;

/**
 * Panel de canciones que le gustan al usuario.
 */
public class MeGustasPanel extends JPanel {

    private final CancionDAO        cancionDAO;
    private final Consumer<Cancion> onPlay;
    private final Consumer<Cancion> onMeGusta;
    private final String            idUsuario;

    public MeGustasPanel(Consumer<Cancion> onPlay, Consumer<Cancion> onMeGusta, String idUsuario) {
        this.onPlay    = onPlay;
        this.onMeGusta = onMeGusta;
        this.idUsuario = idUsuario;
        this.cancionDAO = new CancionDAO();

        setBackground(UITheme.BG);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        cargar();
    }

    /** Limpia y recarga el panel — llamar tras agregar Me Gusta */
    public void refrescar() {
        removeAll();
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        cargar();
        revalidate();
        repaint();
    }

    private void cargar() {
        add(buildHeader(), BorderLayout.NORTH);
        List<Cancion> canciones = cancionDAO.obtenerMeGustasUsuario(idUsuario);
        add(buildLista(canciones), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel hero = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x1A, 0x00, 0x14),
                                                     getWidth(), getHeight(), UITheme.BG);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.RADIUS, UITheme.RADIUS);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 56));
                g2.setColor(new Color(0xFF, 0x00, 0x50, 30));
                g2.drawString("♥", getWidth() - 90, 70);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        hero.setOpaque(false);
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblIcon  = new JLabel("♥");
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        lblIcon.setForeground(new Color(0xFF, 0x22, 0x55));

        JLabel lblTitle = new JLabel("Canciones que me gustan");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(UITheme.TEXT);

        int total = cancionDAO.obtenerMeGustasUsuario(idUsuario).size();
        JLabel lblSub = new JLabel(total + " canciones");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.MUTED);

        hero.add(lblIcon);
        hero.add(Box.createVerticalStrut(4));
        hero.add(lblTitle);
        hero.add(lblSub);

        p.add(hero);
        return p;
    }

    private JScrollPane buildLista(List<Cancion> canciones) {
        JPanel lista = new JPanel();
        lista.setOpaque(false);
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));

        if (canciones.isEmpty()) {
            JLabel vacio = new JLabel("Aún no tienes canciones marcadas como favoritas.");
            vacio.setFont(UITheme.FONT_BODY);
            vacio.setForeground(UITheme.MUTED);
            lista.add(vacio);
        } else {
            int i = 1;
            for (Cancion c : canciones) {
                final Cancion cancion = c;
                SongRow row = new SongRow(i++, c, () -> onPlay.accept(cancion), idUsuario, () -> onMeGusta.accept(cancion));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                lista.add(row);
                lista.add(Box.createVerticalStrut(2));
            }
        }

        JScrollPane scroll = new JScrollPane(lista);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        return scroll;
    }
}