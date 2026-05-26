package com.halconmusic.ui.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import com.halconmusic.dao.CancionDAO;
import com.halconmusic.dao.ResumenDAO;
import com.halconmusic.model.Cancion;
import com.halconmusic.ui.UITheme;
import com.halconmusic.ui.components.SongRow;

public class ResumenPanel extends JPanel {

    private final String            idUsuario;
    private final Consumer<Cancion> onPlay;
    private final Consumer<Cancion> onLike;
    private final ResumenDAO        resumenDAO = new ResumenDAO();
    private final CancionDAO        cancionDAO = new CancionDAO();

    private JTextField txtDesde;
    private JTextField txtHasta;
    private JLabel     lblGenero;
    private JLabel     lblEmocion;
    private JPanel     panelCanciones;
    private JPanel     panelHistorialGen;
    private JTabbedPane tabs;

    public ResumenPanel(Consumer<Cancion> onPlay, Consumer<Cancion> onLike, String idUsuario) {
        this.onPlay    = onPlay;
        this.onLike    = onLike;
        this.idUsuario = idUsuario;
        setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 0));
        aplicarEstiloTabs();
        construirUI();
    }

    /** Fuerza colores oscuros en el JTabbedPane vía UIManager */
    private void aplicarEstiloTabs() {
        UIManager.put("TabbedPane.background",         UITheme.SURFACE);
        UIManager.put("TabbedPane.foreground",         UITheme.TEXT);
        UIManager.put("TabbedPane.selected",           UITheme.CARD);
        UIManager.put("TabbedPane.selectedForeground", UITheme.ACCENT);
        UIManager.put("TabbedPane.contentAreaColor",   UITheme.SURFACE);
        UIManager.put("TabbedPane.light",              UITheme.BORDER);
        UIManager.put("TabbedPane.highlight",          UITheme.CARD);
        UIManager.put("TabbedPane.shadow",             UITheme.BG);
        UIManager.put("TabbedPane.darkShadow",         UITheme.BG);
        UIManager.put("TabbedPane.focus",              UITheme.ACCENT);
    }

    private void construirUI() {
        tabs = new JTabbedPane() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(UITheme.BG);
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        tabs.setBackground(UITheme.BG);
        tabs.setForeground(UITheme.TEXT);
        tabs.setFont(UITheme.FONT_BODY);
        tabs.setOpaque(true);
        tabs.addTab("Mi Resumen",       buildTabUsuario());
        tabs.addTab("Historial General", buildTabGeneral());
        tabs.setOpaque(true);
        tabs.setBackground(UITheme.BG);
        tabs.setForeground(UITheme.TEXT);
        // Forzar color de fondo en cada panel de contenido
        for (int i = 0; i < tabs.getTabCount(); i++) {
            Component comp = tabs.getComponentAt(i);
            if (comp instanceof JPanel jp) {
                jp.setBackground(UITheme.BG);
            }
        }

        // Forzar texto visible en las pestañas sin importar el Look & Feel del SO
        for (int i = 0; i < tabs.getTabCount(); i++) {
            JLabel tabLabel = new JLabel(tabs.getTitleAt(i));
            tabLabel.setFont(UITheme.FONT_BODY);
            tabLabel.setForeground(UITheme.TEXT);
            tabLabel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            tabs.setTabComponentAt(i, tabLabel);
        }

        // Resaltar pestaña seleccionada al cambiar
        tabs.addChangeListener(e -> {
            for (int i = 0; i < tabs.getTabCount(); i++) {
                java.awt.Component tc = tabs.getTabComponentAt(i);
                if (tc instanceof JLabel lbl) {
                    lbl.setForeground(i == tabs.getSelectedIndex()
                        ? UITheme.ACCENT : UITheme.TEXT);
                }
            }
        });

        add(tabs, BorderLayout.CENTER);
    }  

    // ── PESTAÑA 1 ─────────────────────────────────────────────────────
    private JPanel buildTabUsuario() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(UITheme.BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel hdr = new JLabel("Mi Resumen Musical");
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 20));
        hdr.setForeground(UITheme.TEXT);
        hdr.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        // ── Filtro de fechas ──────────────────────────────────────────
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filtros.setOpaque(false);
        filtros.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        txtDesde = campoCampo("01/01/2024");
        txtHasta = campoCampo(hoy());
        JButton btnFiltrar = crearBoton("Filtrar");
        btnFiltrar.addActionListener(e -> aplicarFiltro());
        JButton btnTodo = crearBotonSecundario("Todo");
        btnTodo.addActionListener(e -> cargarSinFiltro());

        filtros.add(etq("Desde (dd/MM/yyyy):")); filtros.add(txtDesde);
        filtros.add(Box.createHorizontalStrut(8));
        filtros.add(etq("Hasta (dd/MM/yyyy):")); filtros.add(txtHasta);
        filtros.add(Box.createHorizontalStrut(8));
        filtros.add(btnFiltrar); filtros.add(btnTodo);

        // ── Chips ─────────────────────────────────────────────────────
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        chips.setOpaque(false);
        lblGenero  = chipLabel(UITheme.emoji("🎵", "Género: —"));
        lblEmocion = chipLabel(UITheme.emoji("💭", "Emoción: —"));
        chips.add(lblGenero);
        chips.add(lblEmocion);

        // ── Lista de canciones ────────────────────────────────────────
        panelCanciones = new JPanel();
        panelCanciones.setBackground(UITheme.BG);
        panelCanciones.setLayout(new BoxLayout(panelCanciones, BoxLayout.Y_AXIS));

        JScrollPane scrollC = new JScrollPane(panelCanciones);
        scrollC.setBorder(tituloBorde("Canciones escuchadas en el período"));
        scrollC.setOpaque(false);
        scrollC.getViewport().setOpaque(false);
        scrollC.getVerticalScrollBar().setUnitIncrement(12);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(hdr,     BorderLayout.NORTH);
        top.add(filtros, BorderLayout.CENTER);
        top.add(chips,   BorderLayout.SOUTH);

        p.add(top,    BorderLayout.NORTH);
        p.add(scrollC, BorderLayout.CENTER);
        return p;
    }

    // ── PESTAÑA 2 ─────────────────────────────────────────────────────
    private JPanel buildTabGeneral() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(UITheme.BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel hdr = new JLabel("Historial General de la App");
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 20));
        hdr.setForeground(UITheme.TEXT);

        JButton btnCargar = crearBoton("Cargar historial general");
        btnCargar.addActionListener(e -> cargarHistorialGeneral());

        JPanel topGen = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        topGen.setOpaque(false);
        topGen.add(hdr);
        topGen.add(btnCargar);

        panelHistorialGen = new JPanel();
        panelHistorialGen.setBackground(UITheme.BG);
        panelHistorialGen.setLayout(new BoxLayout(panelHistorialGen, BoxLayout.Y_AXIS));

        JScrollPane scrollG = new JScrollPane(panelHistorialGen);
        scrollG.setBorder(tituloBorde("Todas las canciones reproducidas (todos los usuarios)"));
        scrollG.setOpaque(false);
        scrollG.getViewport().setOpaque(false);
        scrollG.getVerticalScrollBar().setUnitIncrement(12);

        p.add(topGen,  BorderLayout.NORTH);
        p.add(scrollG, BorderLayout.CENTER);
        return p;
    }

    // ── CARGA ──────────────────────────────────────────────────────────
    public void refrescar() { cargarSinFiltro(); }

    private void cargarSinFiltro() {
        new Thread(() -> {
            List<String[]> generos   = resumenDAO.obtenerGenerosMasEscuchadosConFecha(idUsuario, null, null);
            List<String[]> emociones = resumenDAO.obtenerEmocionesMasEscuchadas(idUsuario, null, null);
            List<Cancion>  canciones = cancionDAO.obtenerHistorialUsuario(idUsuario);
            SwingUtilities.invokeLater(() -> {
                actualizarChips(generos, emociones);
                renderizarCanciones(canciones, panelCanciones, "No hay canciones en el historial.");
            });
        }).start();
    }

    private void aplicarFiltro() {
        String desdeStr = txtDesde.getText().trim();
        String hastaStr = txtHasta.getText().trim();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date desde, hasta;
        try {
            desde = new Date(sdf.parse(desdeStr).getTime());
            hasta = new Date(sdf.parse(hastaStr).getTime());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Formato de fecha inválido. Usa dd/MM/yyyy.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final Date d = desde, h = hasta;
        new Thread(() -> {
            List<String[]> generos   = resumenDAO.obtenerGenerosMasEscuchadosConFecha(idUsuario, d, h);
            List<String[]> emociones = resumenDAO.obtenerEmocionesMasEscuchadas(idUsuario, d, h);
            List<Cancion>  canciones = resumenDAO.obtenerCancionesEnRango(idUsuario, d, h);
            SwingUtilities.invokeLater(() -> {
                actualizarChips(generos, emociones);
                renderizarCanciones(canciones, panelCanciones, "Sin canciones en ese rango de fechas.");
            });
        }).start();
    }

    private void cargarHistorialGeneral() {
        new Thread(() -> {
            List<Cancion> canciones = resumenDAO.obtenerHistorialGeneral();
            SwingUtilities.invokeLater(() ->
                renderizarCanciones(canciones, panelHistorialGen, "El historial general está vacío."));
        }).start();
    }

    // ── HELPERS ────────────────────────────────────────────────────────
    private void actualizarChips(List<String[]> generos, List<String[]> emociones) {
        String g = generos.isEmpty()   ? "—" : generos.get(0)[0]   + " (" + generos.get(0)[1]   + ")";
        String e = emociones.isEmpty() ? "—" : emociones.get(0)[0] + " (" + emociones.get(0)[1] + ")";
        lblGenero.setText(UITheme.emoji("🎵", "Género: "  + g));
        lblEmocion.setText(UITheme.emoji("💭", "Emoción: " + e));
    }

    private void renderizarCanciones(List<Cancion> lista, JPanel target, String msgVacio) {
        target.removeAll();
        if (lista.isEmpty()) {
            JLabel lbl = new JLabel(msgVacio);
            lbl.setForeground(UITheme.MUTED);
            lbl.setFont(UITheme.FONT_BODY);
            lbl.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
            target.add(lbl);
        } else {
            int[] contador = {1};
            for (Cancion c : lista) {
                int num = contador[0]++;
                target.add(new SongRow(num, c, () -> onPlay.accept(c), idUsuario, () -> onLike.accept(c)));
            }
        }
        target.revalidate();
        target.repaint();
    }

    private JLabel chipLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(UITheme.FONT_BODY);
        l.setForeground(UITheme.ACCENT);
        l.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        l.setOpaque(true);
        l.setBackground(UITheme.SURFACE);
        return l;
    }

    private TitledBorder tituloBorde(String texto) {
        TitledBorder tb = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true), texto);
        tb.setTitleColor(UITheme.TEXT);
        tb.setTitleFont(UITheme.FONT_SMALL);
        return tb;
    }

    private JTextField campoCampo(String valor) {
        JTextField f = new JTextField(valor, 10);
        f.setBackground(UITheme.CARD); f.setForeground(UITheme.TEXT);
        f.setCaretColor(UITheme.ACCENT); f.setFont(UITheme.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        f.setPreferredSize(new Dimension(120, 32));
        return f;
    }

    private JLabel etq(String t) {
        JLabel l = new JLabel(t); l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.MUTED); return l;
    }

    private JButton crearBoton(String t) {
        JButton b = new JButton(t); b.setBackground(UITheme.ACCENT);
        b.setForeground(UITheme.BG); b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(180, 32)); return b;
    }

    private JButton crearBotonSecundario(String t) {
        JButton b = new JButton(t); b.setBackground(UITheme.SURFACE);
        b.setForeground(UITheme.MUTED); b.setFont(UITheme.FONT_BODY);
        b.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(80, 32)); return b;
    }

    private String hoy() {
        return new SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date());
    }
}