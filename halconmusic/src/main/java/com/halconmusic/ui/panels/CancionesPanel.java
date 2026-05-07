package com.halconmusic.ui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.halconmusic.dao.CancionDAO;
import com.halconmusic.model.Cancion;
import com.halconmusic.ui.UITheme;
import com.halconmusic.ui.components.SongRow;

/**
 * Panel de canciones con búsqueda por nombre, artista, género o emoción.
 * Usa UPPER + LIKE en el DAO.
 */
public class CancionesPanel extends JPanel {

    private final CancionDAO       cancionDAO;
    private final Consumer<Cancion> onPlay;
    private       JPanel           listPanel;
    private       JLabel           lblConteo;
    private final String           idUsuario;

    public CancionesPanel(Consumer<Cancion> onPlay, String idUsuario) {
        this.idUsuario = idUsuario;
        this.onPlay     = onPlay;
        this.cancionDAO = new CancionDAO();
        setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildListArea(),  BorderLayout.CENTER);

        cargarCanciones(cancionDAO.obtenerTodas());
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JLabel titulo = new JLabel("Biblioteca de canciones");
        titulo.setFont(UITheme.FONT_SECTION);
        titulo.setForeground(UITheme.TEXT);

        lblConteo = new JLabel();
        lblConteo.setFont(UITheme.FONT_SMALL);
        lblConteo.setForeground(UITheme.MUTED);

        // Barra de búsqueda
        JPanel searchBox = new JPanel(new BorderLayout(6, 0));
        searchBox.setBackground(UITheme.SURFACE);
        searchBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        searchBox.setPreferredSize(new Dimension(280, 34));

        JLabel lupa = new JLabel("⌕");
        lupa.setFont(UITheme.FONT_BODY);
        lupa.setForeground(UITheme.MUTED);

        JTextField searchField = new JTextField();
        searchField.setBackground(UITheme.SURFACE);
        searchField.setForeground(UITheme.TEXT);
        searchField.setFont(UITheme.FONT_BODY);
        searchField.setBorder(null);
        searchField.setCaretColor(UITheme.ACCENT);
        searchField.putClientProperty("JTextField.placeholderText", "Buscar canciones...");

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate (javax.swing.event.DocumentEvent e) { buscar(searchField.getText()); }
            public void removeUpdate (javax.swing.event.DocumentEvent e) { buscar(searchField.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { buscar(searchField.getText()); }
        });

        searchBox.add(lupa,        BorderLayout.WEST);
        searchBox.add(searchField, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(lblConteo);
        right.add(Box.createHorizontalStrut(12));
        right.add(searchBox);

        p.add(titulo, BorderLayout.WEST);
        p.add(right,  BorderLayout.EAST);
        return p;
    }

    private JScrollPane buildListArea() {
        // Header de columnas
        JPanel header = new JPanel(new GridBagLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0; gbc.fill = GridBagConstraints.BOTH; gbc.insets = new Insets(4, 4, 4, 4);

        gbc.gridx = 0; gbc.weightx = 0;   header.add(colLabel("#"),            gbc);
        gbc.gridx = 1; gbc.weightx = 0;   header.add(colLabel(""),             gbc); // thumb
        gbc.gridx = 2; gbc.weightx = 1;   header.add(colLabel("Título"),       gbc);
        gbc.gridx = 3; gbc.weightx = 0.3; header.add(colLabel("Género"),       gbc);
        gbc.gridx = 4; gbc.weightx = 0.1; header.add(colLabel("Año"),          gbc);
        gbc.gridx = 5; gbc.weightx = 0;   header.add(colLabel("Duración"),     gbc);

        listPanel = new JPanel();
        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(header,    BorderLayout.NORTH);
        wrapper.add(listPanel, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        return scroll;
    }

    private void buscar(String termino) {
        List<Cancion> resultados = termino.isBlank()
            ? cancionDAO.obtenerTodas()
            : cancionDAO.buscar(termino);
        cargarCanciones(resultados);
    }

    private void cargarCanciones(List<Cancion> canciones) {
        listPanel.removeAll();
        lblConteo.setText(canciones.size() + " canciones");

        int i = 1;
        for (Cancion c : canciones) {
            final Cancion cancion = c;
            listPanel.add(new SongRow(i++, c, () -> onPlay.accept(cancion)));
            listPanel.add(Box.createVerticalStrut(2));
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JLabel colLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(UITheme.FONT_LABEL);
        l.setForeground(UITheme.MUTED);
        return l;
    }
}
