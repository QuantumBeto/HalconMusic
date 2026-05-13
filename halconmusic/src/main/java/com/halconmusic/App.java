package com.halconmusic;

import java.awt.*;
import javax.swing.*;

import com.halconmusic.dao.HistorialDAO;
import com.halconmusic.db.ConexionDB;
import com.halconmusic.model.Cancion;
import com.halconmusic.ui.UITheme;
import com.halconmusic.ui.components.PlayerBar;
import com.halconmusic.ui.components.Sidebar;
import com.halconmusic.ui.panels.*;

public class App extends JFrame {

    private CardLayout    rootLayout;
    private JPanel        rootPanel;

    // Datos del usuario autenticado
    private String        idUsuario;
    private String        nombreUsuario;
    private String        tipoUsuario;

    // Estructura principal (construida tras el login)
    private JPanel        contentArea;
    private CardLayout    cardLayout;
    private PlayerBar     playerBar;
    private HistorialDAO  historialDAO;
    private ResumenPanel  resumenPanel;
    private MeGustasPanel meGustasPanel;
    private Sidebar       sidebar;

    public App() {
        super("HalconMusic");
        configurarVentana();
        mostrarLogin();
        setVisible(true);
    }

    // ── Configuración de ventana ──────────────────────────
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
            UIManager.put("TextField.background",      UITheme.SURFACE);
            UIManager.put("TextField.foreground",      UITheme.TEXT);
            UIManager.put("TextField.caretForeground", UITheme.ACCENT);
        } catch (Exception ignored) {}

        // Root card: alterna entre "login" y "app"
        rootLayout = new CardLayout();
        rootPanel  = new JPanel(rootLayout);
        rootPanel.setBackground(UITheme.BG);
        setContentPane(rootPanel);
    }

    // ── Pantalla de login ─────────────────────────────────
    private void mostrarLogin() {
        LoginPanel login = new LoginPanel(this::onLoginExitoso);
        rootPanel.add(login, "login");
        rootLayout.show(rootPanel, "login");
        pack();
    }

    private void onLoginExitoso(String[] datos) {
        // datos = { ID_USUARIO, NOMBRE, TIPO_SUSCRIPCION }
        idUsuario    = datos[0];
        nombreUsuario = datos[1];
        tipoUsuario  = datos[2];

        historialDAO = new HistorialDAO();
        construirUI();

        rootLayout.show(rootPanel, "app");
    }

    // ── UI principal ──────────────────────────────────────
    private void construirUI() {
        JPanel appPanel = new JPanel(new BorderLayout());
        appPanel.setBackground(UITheme.BG);

        // Sidebar con el nombre/tipo real del usuario
        sidebar = new Sidebar(this::navegar, nombreUsuario, tipoUsuario);

        // Content area
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(UITheme.BG);

        contentArea.add(wrapScroll(new HomePanel(this::reproducir, this::agregarMeGusta, idUsuario)), "home");
        contentArea.add(new BuscarPanel(this::reproducir, this::agregarMeGusta, idUsuario), "buscar");

        resumenPanel = new ResumenPanel(this::reproducir, this::agregarMeGusta, idUsuario);
        contentArea.add(wrapScroll(resumenPanel), "historial");

        meGustasPanel = new MeGustasPanel(this::reproducir, this::agregarMeGusta, idUsuario);
        contentArea.add(meGustasPanel, "megustas");

        contentArea.add(new ArtistasPanel(), "artistas");
        contentArea.add(new AlbumesPanel(this::reproducir, this::agregarMeGusta, idUsuario), "albumes");
        contentArea.add(new CancionesPanel(this::reproducir, this::agregarMeGusta, idUsuario), "canciones");

        // PlayerBar — recibe callback Me Gusta para el botón ♥ de la barra
        playerBar = new PlayerBar(this::agregarMeGusta);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, contentArea);
        split.setDividerSize(0);
        split.setEnabled(false);
        split.setBorder(null);

        appPanel.add(split,     BorderLayout.CENTER);
        appPanel.add(playerBar, BorderLayout.SOUTH);

        rootPanel.add(appPanel, "app");
        cardLayout.show(contentArea, "home");
    }

    private void navegar(String vista) {
        cardLayout.show(contentArea, vista);
        // Refresca paneles dinámicos al navegar a ellos
        if ("historial".equals(vista)) resumenPanel.refrescar();
        if ("megustas".equals(vista))  meGustasPanel.refrescar();
    }

    private void reproducir(Cancion cancion) {
        playerBar.reproducir(cancion);
        new Thread(() -> {
            historialDAO.registrarEnHistorial(idUsuario, cancion.getIdCancion());
            SwingUtilities.invokeLater(() -> resumenPanel.refrescar());
        }).start();
    }

    /** Agrega Me Gusta en BD y refresca el panel */
    private void agregarMeGusta(Cancion cancion) {
        new Thread(() -> {
            historialDAO.agregarMeGusta(idUsuario, cancion.getIdCancion());
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
        // Evita scroll horizontal — todo debe caber en el ancho
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    public static void main(String[] args) {
        ConexionDB.getInstance();
        SwingUtilities.invokeLater(App::new);
        Runtime.getRuntime().addShutdownHook(
            new Thread(() -> ConexionDB.getInstance().cerrar()));
    }
}