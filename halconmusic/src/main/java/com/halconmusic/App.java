package com.halconmusic;

import com.halconmusic.dao.HistorialDAO;
import com.halconmusic.db.ConexionDB;
import com.halconmusic.model.Cancion;
import com.halconmusic.ui.UITheme;
import com.halconmusic.ui.components.PlayerBar;
import com.halconmusic.ui.components.Sidebar;
import com.halconmusic.ui.panels.*;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal de HalconMusic.
 * Arquitectura: Sidebar → ContentArea → PlayerBar
 */
public class App extends JFrame {

    private static final String ID_USUARIO = "US001";
    private static final String NOMBRE_USR = "Alejandro";
    private static final String TIPO_USR   = "Premium ✦";

    private JPanel        contentArea;
    private CardLayout    cardLayout;
    private PlayerBar     playerBar;
    private HistorialDAO  historialDAO;

    // Referencias para poder hacer refresh
    private ResumenPanel  resumenPanel;
    private MeGustasPanel meGustasPanel;

    public App() {
        super("HalconMusic");
        historialDAO = new HistorialDAO();
        configurarVentana();
        construirUI();
        setVisible(true);
    }

    private void configurarVentana() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650));
        setPreferredSize(new Dimension(1280, 750));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG);
        setBackground(UITheme.BG);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Panel.background",          UITheme.BG);
            UIManager.put("ScrollPane.background",     UITheme.BG);
            UIManager.put("Viewport.background",       UITheme.BG);
            UIManager.put("ScrollBar.thumb",           UITheme.SURFACE);
            UIManager.put("ScrollBar.thumbDarkShadow", UITheme.SURFACE);
            UIManager.put("TextField.background",      UITheme.SURFACE);
            UIManager.put("TextField.foreground",      UITheme.TEXT);
            UIManager.put("TextField.caretForeground", UITheme.ACCENT);
        } catch (Exception ignored) {}
    }

    private void construirUI() {
        setLayout(new BorderLayout());

        // Sidebar
        Sidebar sidebar = new Sidebar(this::navegar, NOMBRE_USR, TIPO_USR);

        // ContentArea
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(UITheme.BG);

        // Paneles — ResumenPanel y MeGustasPanel guardados para refresh
        contentArea.add(wrapScroll(new HomePanel(this::reproducir, ID_USUARIO)), "home");
        contentArea.add(new BuscarPanel(this::reproducir, this::agregarMeGusta, ID_USUARIO), "buscar");

        resumenPanel = new ResumenPanel(this::reproducir, this::agregarMeGusta, ID_USUARIO);
        contentArea.add(wrapScroll(resumenPanel), "historial");

        meGustasPanel = new MeGustasPanel(this::reproducir, this::agregarMeGusta, ID_USUARIO);
        contentArea.add(meGustasPanel, "megustas");

        contentArea.add(new ArtistasPanel(), "artistas");
        contentArea.add(new AlbumesPanel(this::reproducir), "albumes");
        contentArea.add(new CancionesPanel(this::reproducir, this::agregarMeGusta, ID_USUARIO), "canciones");

        // PlayerBar
        playerBar = new PlayerBar();

        // Estructura principal
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, contentArea);
        split.setDividerSize(0);
        split.setEnabled(false);
        split.setBorder(null);

        add(split,     BorderLayout.CENTER);
        add(playerBar, BorderLayout.SOUTH);

        cardLayout.show(contentArea, "home");
        pack();
    }

    private void navegar(String vista) {
        cardLayout.show(contentArea, vista);
    }

    /**
     * Reproduce la canción y guarda automáticamente en historial.
     * Luego refresca el panel de historial.
     */
    private void reproducir(Cancion cancion) {
        playerBar.reproducir(cancion);
        new Thread(() -> {
            historialDAO.registrarEnHistorial(ID_USUARIO, cancion.getIdCancion());
            SwingUtilities.invokeLater(() -> resumenPanel.refrescar());
        }).start();
    }

    /**
     * Agrega la canción a Me Gusta y refresca el panel de Me Gusta.
     */
    private void agregarMeGusta(Cancion cancion) {
        new Thread(() -> {
            historialDAO.agregarMeGusta(ID_USUARIO, cancion.getIdCancion());
            SwingUtilities.invokeLater(() -> meGustasPanel.refrescar());
        }).start();
    }

    private JScrollPane wrapScroll(JPanel panel) {
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        return scroll;
    }

    public static void main(String[] args) {
        ConexionDB.getInstance();
        SwingUtilities.invokeLater(() -> new App());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> ConexionDB.getInstance().cerrar()));
    }
}