package com.halconmusic.ui.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.halconmusic.dao.CancionDAO;
import com.halconmusic.model.Cancion;
import com.halconmusic.ui.UITheme;

public class CrearPlaylistPanel extends JPanel {

    private final String     idUsuario;
    private final CancionDAO cancionDAO = new CancionDAO();
    private final Runnable   onCreada;   // callback para refrescar sidebar
    private byte[]  portadaBytes = null;
    private JLabel  lblPortadaPreview;
    
    private JTextField        txtNombre;
    private JTextField        txtDescripcion;
    private DefaultListModel<Cancion> modeloDisponibles = new DefaultListModel<>();
    private DefaultListModel<Cancion> modeloSeleccionadas = new DefaultListModel<>();

    public CrearPlaylistPanel(String idUsuario, Runnable onCreada) {
        this.idUsuario = idUsuario;
        this.onCreada  = onCreada;
        setBackground(UITheme.BG);
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
        cargarCanciones();
    }

    private void build() {
        // ── Encabezado ──
        JLabel titulo = new JLabel("Nueva Playlist");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titulo.setForeground(UITheme.TEXT);
        add(titulo, BorderLayout.NORTH);

        // ── Formulario central ──
        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nombre
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblNombre = new JLabel("Nombre:");
        lblNombre.setForeground(UITheme.TEXT);
        centro.add(lblNombre, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        txtNombre = new JTextField();
        txtNombre.setBackground(UITheme.SURFACE);
        txtNombre.setForeground(UITheme.TEXT);
        txtNombre.setCaretColor(UITheme.ACCENT);
        centro.add(txtNombre, gbc);

        // Descripción
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblDesc = new JLabel("Descripción:");
        lblDesc.setForeground(UITheme.TEXT);
        centro.add(lblDesc, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        txtDescripcion = new JTextField();
        txtDescripcion.setBackground(UITheme.SURFACE);
        txtDescripcion.setForeground(UITheme.TEXT);
        txtDescripcion.setCaretColor(UITheme.ACCENT);
        centro.add(txtDescripcion, gbc);

        // DESPUÉS de la fila de descripción (gridy=1), agrega:
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblPortada = new JLabel("Portada:");
        lblPortada.setForeground(UITheme.TEXT);
        centro.add(lblPortada, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel panelPortada = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelPortada.setOpaque(false);
        JButton btnPortada = new JButton("Seleccionar imagen...");
        estilizarBtn(btnPortada);
        lblPortadaPreview = new JLabel("Sin imagen");
        lblPortadaPreview.setForeground(UITheme.MUTED);
        lblPortadaPreview.setFont(UITheme.FONT_SMALL);
        btnPortada.addActionListener(e -> seleccionarPortada());
        panelPortada.add(btnPortada);
        panelPortada.add(lblPortadaPreview);
        centro.add(panelPortada, gbc);

        // Listas de canciones
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1;
        centro.add(buildPanelListas(), gbc);

        add(centro, BorderLayout.CENTER);

        // ── Botón crear ──
        JButton btnCrear = new JButton("Crear Playlist");
        btnCrear.setBackground(UITheme.ACCENT);
        btnCrear.setForeground(UITheme.BG);
        btnCrear.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCrear.setFocusPainted(false);
        btnCrear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCrear.addActionListener(e -> crearPlaylist());

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sur.setOpaque(false);
        sur.add(btnCrear);
        add(sur, BorderLayout.SOUTH);
    }

    private void seleccionarPortada() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Imágenes (JPG, PNG)", "jpg", "jpeg", "png"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                portadaBytes = Files.readAllBytes(f.toPath());
                // Mostrar preview redimensionado
                BufferedImage img = ImageIO.read(f);
                if (img != null) {
                    Image scaled = img.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                    lblPortadaPreview.setIcon(new javax.swing.ImageIcon(scaled));
                    lblPortadaPreview.setText(" " + f.getName());
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "No se pudo leer la imagen.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel buildPanelListas() {
        JPanel p = new JPanel(new GridLayout(1, 3, 10, 0));
        p.setOpaque(false);

        // Lista disponibles
        JList<Cancion> listDisp = new JList<>(modeloDisponibles);
        listDisp.setBackground(UITheme.SURFACE);
        listDisp.setForeground(UITheme.TEXT);
        listDisp.setCellRenderer(new CancionCellRenderer());
        JScrollPane scrollDisp = new JScrollPane(listDisp);
        scrollDisp.setBorder(new TitledBorder(
            BorderFactory.createLineBorder(UITheme.BORDER), "Canciones disponibles",
            TitledBorder.LEFT, TitledBorder.TOP, UITheme.FONT_SMALL, UITheme.MUTED));
        scrollDisp.setBackground(UITheme.SURFACE);

        // Botones mover
        JPanel btnPanel = new JPanel(new GridBagLayout());
        btnPanel.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(4,4,4,4);

        JButton btnAdd = new JButton("Agregar →");
        JButton btnQuit = new JButton("← Quitar");
        estilizarBtn(btnAdd);
        estilizarBtn(btnQuit);

        btnAdd.addActionListener(e -> {
            for (Cancion c : listDisp.getSelectedValuesList()) {
                modeloDisponibles.removeElement(c);
                modeloSeleccionadas.addElement(c);
            }
        });
        btnQuit.addActionListener(e -> {
            JList<Cancion> listSel = getListSeleccionadas();
            for (Cancion c : listSel.getSelectedValuesList()) {
                modeloSeleccionadas.removeElement(c);
                modeloDisponibles.addElement(c);
            }
        });

        g.gridy = 0; btnPanel.add(btnAdd, g);
        g.gridy = 1; btnPanel.add(btnQuit, g);

        // Lista seleccionadas
        JList<Cancion> listSel = new JList<>(modeloSeleccionadas);
        listSel.setName("seleccionadas");
        listSel.setBackground(UITheme.SURFACE);
        listSel.setForeground(UITheme.TEXT);
        listSel.setCellRenderer(new CancionCellRenderer());
        JScrollPane scrollSel = new JScrollPane(listSel);
        scrollSel.setBorder(new TitledBorder(
            BorderFactory.createLineBorder(UITheme.BORDER), "En la playlist",
            TitledBorder.LEFT, TitledBorder.TOP, UITheme.FONT_SMALL, UITheme.MUTED));

        p.add(scrollDisp);
        p.add(btnPanel);
        p.add(scrollSel);
        return p;
    }

    // helper para recuperar la lista de seleccionadas desde los componentes
    private JList<Cancion> getListSeleccionadas() {
        return findList(this, "seleccionadas");
    }

    @SuppressWarnings("unchecked")
    private JList<Cancion> findList(Container c, String name) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof JList && name.equals(comp.getName()))
                return (JList<Cancion>) comp;
            if (comp instanceof Container) {
                JList<Cancion> r = findList((Container) comp, name);
                if (r != null) return r;
            }
        }
        return null;
    }

    private void estilizarBtn(JButton b) {
        b.setBackground(UITheme.CARD);
        b.setForeground(UITheme.ACCENT);
        b.setFont(UITheme.FONT_BODY);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(true);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.ACCENT, 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void cargarCanciones() {
        new Thread(() -> {
            List<Cancion> todas = cancionDAO.obtenerTodas();
            SwingUtilities.invokeLater(() -> {
                modeloDisponibles.clear();
                for (Cancion c : todas) modeloDisponibles.addElement(c);
            });
        }).start();
    }

    private void crearPlaylist() {
        String nombre = txtNombre.getText().trim();
        String desc   = txtDescripcion.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (modeloSeleccionadas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Agrega al menos una canción.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<String> ids = new ArrayList<>();
        for (int i = 0; i < modeloSeleccionadas.size(); i++)
            ids.add(modeloSeleccionadas.get(i).getIdCancion());

        new Thread(() -> {
            cancionDAO.crearPlaylist(idUsuario, nombre, desc, ids, portadaBytes);
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "¡Playlist creada exitosamente!", "Listo", JOptionPane.INFORMATION_MESSAGE);
                txtNombre.setText("");
                txtDescripcion.setText("");
                modeloSeleccionadas.clear();
                cargarCanciones();
                if (onCreada != null) onCreada.run();
            });
        }).start();
    }

    // Renderer simple para mostrar nombre - artista en la lista
    private static class CancionCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Cancion c) {
                setText(c.getNombre() + " — " + c.getArtista());
            }
            setBackground(isSelected ? UITheme.ACCENT : UITheme.SURFACE);
            setForeground(isSelected ? UITheme.BG : UITheme.TEXT);
            return this;
        }
    }
}