package com.halconmusic.ui.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import com.halconmusic.dao.ResumenDAO;
import com.halconmusic.model.Cancion;
import com.halconmusic.ui.UITheme;
import com.halconmusic.ui.components.SongRow;

/**
 * REQ. 10 — Resumen musical de toda la app (todos los usuarios) con rango de fechas.
 *  · Género más escuchado
 *  · Emoción más escuchada
 *  · Canciones escuchadas en ese período
 *
 * Solo accesible para el usuario tipo Artista/Premium.
 */
public class ResumenGlobalPanel extends JPanel {

    private final ResumenDAO resumenDAO = new ResumenDAO();

    private JTextField txtDesde;
    private JTextField txtHasta;
    private JLabel     lblGenero;
    private JLabel     lblEmocion;
    private JPanel     panelCanciones;

    public ResumenGlobalPanel() {
        setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        construirUI();
        cargarTodo(); // carga inicial sin filtro
    }

    private void construirUI() {
        // ── Encabezado ────────────────────────────────────────────────
        JLabel hdr = new JLabel("Resumen Global de la App");
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 22));
        hdr.setForeground(UITheme.TEXT);

        JLabel sub = new JLabel("Estadísticas de TODOS los usuarios · Solo visible para Artistas");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.MUTED);

        // ── Filtro de fechas ──────────────────────────────────────────
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filtros.setOpaque(false);
        filtros.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        txtDesde = campoFecha("01/01/2024");
        txtHasta = campoFecha(hoy());
        JButton btnFiltrar = crearBoton("Filtrar");
        btnFiltrar.addActionListener(e -> aplicarFiltro());
        JButton btnTodo = crearBotonSec("Todo");
        btnTodo.addActionListener(e -> cargarTodo());

        filtros.add(etq("Desde (dd/MM/yyyy):")); filtros.add(txtDesde);
        filtros.add(Box.createHorizontalStrut(8));
        filtros.add(etq("Hasta (dd/MM/yyyy):")); filtros.add(txtHasta);
        filtros.add(Box.createHorizontalStrut(8));
        filtros.add(btnFiltrar); filtros.add(btnTodo);

        // ── Chips de estadísticas ─────────────────────────────────────
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        chips.setOpaque(false);
        lblGenero  = chip("🎵 Género: —");
        lblEmocion = chip("💭 Emoción: —");
        chips.add(lblGenero);
        chips.add(lblEmocion);

        // ── Lista de canciones ────────────────────────────────────────
        panelCanciones = new JPanel();
        panelCanciones.setBackground(UITheme.BG);
        panelCanciones.setLayout(new BoxLayout(panelCanciones, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(panelCanciones);
        scroll.setBorder(tituloBorde("Canciones escuchadas en el período (todos los usuarios)"));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        // ── Ensamble ──────────────────────────────────────────────────
        JPanel norte = new JPanel();
        norte.setOpaque(false);
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        hdr.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        filtros.setAlignmentX(Component.LEFT_ALIGNMENT);
        chips.setAlignmentX(Component.LEFT_ALIGNMENT);
        norte.add(hdr);
        norte.add(Box.createVerticalStrut(4));
        norte.add(sub);
        norte.add(Box.createVerticalStrut(16));
        norte.add(filtros);
        norte.add(Box.createVerticalStrut(12));
        norte.add(chips);

        add(norte,  BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    // ── Carga sin filtro ──────────────────────────────────────────────
    private void cargarTodo() {
        new Thread(() -> {
            List<String[]> gens = resumenDAO.obtenerGenerosMasEscuchadosGlobal(null, null);
            List<String[]> ems  = resumenDAO.obtenerEmocionesMasEscuchadasGlobal(null, null);
            List<Cancion>  cans = resumenDAO.obtenerCancionesGlobalEnRango(null, null);
            SwingUtilities.invokeLater(() -> { actualizarChips(gens, ems); renderizar(cans); });
        }).start();
    }

    // ── Carga con filtro de fechas ────────────────────────────────────
    private void aplicarFiltro() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date desde, hasta;
        try {
            desde = new Date(sdf.parse(txtDesde.getText().trim()).getTime());
            hasta = new Date(sdf.parse(txtHasta.getText().trim()).getTime());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Formato de fecha inválido. Usa dd/MM/yyyy.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final Date d = desde, h = hasta;
        new Thread(() -> {
            List<String[]> gens = resumenDAO.obtenerGenerosMasEscuchadosGlobal(d, h);
            List<String[]> ems  = resumenDAO.obtenerEmocionesMasEscuchadasGlobal(d, h);
            List<Cancion>  cans = resumenDAO.obtenerCancionesGlobalEnRango(d, h);
            SwingUtilities.invokeLater(() -> { actualizarChips(gens, ems); renderizar(cans); });
        }).start();
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private void actualizarChips(List<String[]> gens, List<String[]> ems) {
        String g = gens.isEmpty() ? "—" : gens.get(0)[0] + " (" + gens.get(0)[1] + ")";
        String e = ems.isEmpty()  ? "—" : ems.get(0)[0]  + " (" + ems.get(0)[1]  + ")";
        lblGenero.setText("🎵 Género: " + g);
        lblEmocion.setText("💭 Emoción: " + e);
    }

    private void renderizar(List<Cancion> lista) {
        panelCanciones.removeAll();
        if (lista.isEmpty()) {
            JLabel l = new JLabel("Sin canciones en ese rango de fechas.");
            l.setForeground(UITheme.MUTED); l.setFont(UITheme.FONT_BODY);
            l.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
            panelCanciones.add(l);
        } else {
            int[] contador = {1};
            for (Cancion c : lista) {
                int num = contador[0]++;
                panelCanciones.add(new SongRow(num, c, () -> {}, null, () -> {}));
            }
        }
        panelCanciones.revalidate();
        panelCanciones.repaint();
    }

    private JLabel chip(String t) {
        JLabel l = new JLabel(t);
        l.setFont(UITheme.FONT_BODY); l.setForeground(UITheme.ACCENT);
        l.setOpaque(true); l.setBackground(UITheme.SURFACE);
        l.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        return l;
    }

    private TitledBorder tituloBorde(String t) {
        TitledBorder tb = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true), t);
        tb.setTitleColor(UITheme.MUTED); tb.setTitleFont(UITheme.FONT_SMALL);
        return tb;
    }

    private JTextField campoFecha(String val) {
        JTextField f = new JTextField(val, 10);
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
        b.setPreferredSize(new Dimension(100, 32)); return b;
    }
    private JButton crearBotonSec(String t) {
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