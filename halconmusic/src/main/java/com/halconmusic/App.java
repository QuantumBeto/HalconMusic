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

    private CardLayout rootLayout;
    private JPanel     rootPanel;

    private String idUsuario;
    private String nombreUsuario;
    private String tipoDisplay;   // "Artista ✦" | "Oyente"
    private String tipoRaw;       // "Premium"   | "Gratis"

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

        rootLayout = new CardLayout();
        rootPanel  = new JPanel(rootLayout);
        rootPanel.setBackground(UITheme.BG);
        setContentPane(rootPanel);
    }

    private void mostrarLogin() {
        LoginPanel login = new LoginPanel(this::onLoginExitoso);
        rootPanel.add(login, "login");
        rootLayout.show(rootPanel, "login");
        pack();
    }

    private void onLoginExitoso(String[] datos) {
        // datos = { ID_USUARIO, NOMBRE, TIPO_DISPLAY, TIPO_RAW }
        idUsuario    = datos[0];
        nombreUsuario = datos[1];
        tipoDisplay  = datos[2];
        tipoRaw      = datos[3];

        historialDAO = new HistorialDAO();
        construirUI();
        rootLayout.show(rootPanel, "app");
    }

    private void construirUI() {
        JPanel appPanel = new JPanel(new BorderLayout());
        appPanel.setBackground(UITheme.BG);

        // Sidebar recibe tipoRaw para mostrar/ocultar sección Artista
        sidebar = new Sidebar(this::navegar, nombreUsuario, tipoDisplay, tipoRaw);

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

        // Panel de resumen global (req. 10) — solo artistas lo ven
        contentArea.add(wrapScroll(new ResumenGlobalPanel()), "resumenGlobal");

        // Paneles de creación (req. 2) — solo para tipo Artista
        contentArea.add(new CrearArtistaPanel(), "crearArtista");
        contentArea.add(new CrearAlbumPanel(),   "crearAlbum");
        contentArea.add(new CrearCancionPanel(), "crearCancion");

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
        if ("historial".equals(vista))     resumenPanel.refrescar();
        if ("megustas".equals(vista))      meGustasPanel.refrescar();
        if ("resumenGlobal".equals(vista)) { /* ResumenGlobalPanel carga en su constructor */ }
    }

    private void reproducir(Cancion cancion) {
        playerBar.reproducir(cancion);
        new Thread(() -> {
            historialDAO.registrarEnHistorial(idUsuario, cancion.getIdCancion());
            SwingUtilities.invokeLater(() -> resumenPanel.refrescar());
        }).start();
    }

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