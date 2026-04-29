package com.halconmusic;

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
 *
 * Navegación controlada por CardLayout — sin instanciar paneles innecesariamente.
 */
public class App extends JFrame {

    // Usuario activo — en una app real vendría del login
    private static final String ID_USUARIO    = "US001";
    private static final String NOMBRE_USR    = "Alejandro";
    private static final String TIPO_USR      = "Premium ✦";

    private JPanel     contentArea;
    private CardLayout cardLayout;
    private PlayerBar  playerBar;

    public App() {
        super("HalconMusic");
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

        // Aplicar look and feel oscuro
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

        // ── Sidebar ───────────────────────────────────────
        Sidebar sidebar = new Sidebar(this::navegar, NOMBRE_USR, TIPO_USR);

        // ── ContentArea (CardLayout) ──────────────────────
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(UITheme.BG);

        // Registrar todos los paneles
        contentArea.add(wrapScroll(new HomePanel(this::reproducir, ID_USUARIO)), "home");
        contentArea.add(new BuscarPanel(this::reproducir),                        "buscar");
        contentArea.add(wrapScroll(new ResumenPanel(this::reproducir, ID_USUARIO)), "historial");
        contentArea.add(new MeGustasPanel(this::reproducir, ID_USUARIO),           "megustas");
        contentArea.add(new ArtistasPanel(),                                        "artistas");
        contentArea.add(new AlbumesPanel(this::reproducir),                         "albumes");
        contentArea.add(new CancionesPanel(this::reproducir),                        "canciones");

        // ── PlayerBar (inferior) ──────────────────────────
        playerBar = new PlayerBar();

        // ── Estructura principal ──────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, contentArea);
        split.setDividerSize(0);
        split.setEnabled(false);
        split.setBorder(null);

        add(split,     BorderLayout.CENTER);
        add(playerBar, BorderLayout.SOUTH);

        // Mostrar Home por defecto
        cardLayout.show(contentArea, "home");

        pack();
    }

    /** Cambia el panel visible según la vista seleccionada en el Sidebar */
    private void navegar(String vista) {
        cardLayout.show(contentArea, vista);
    }

    /** Indica al PlayerBar que reproduzca la canción */
    private void reproducir(Cancion cancion) {
        playerBar.reproducir(cancion);
    }

    /** Envuelve un panel en un JScrollPane cuando el contenido puede ser largo */
    private JScrollPane wrapScroll(JPanel panel) {
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        return scroll;
    }

    // ── Entry point ───────────────────────────────────────
    public static void main(String[] args) {
        // Inicializar conexión antes de abrir la UI
        ConexionDB.getInstance();

        SwingUtilities.invokeLater(() -> {
            new App();
        });

        // Cerrar conexión al terminar
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ConexionDB.getInstance().cerrar();
        }));
    }
}
