package com.halconmusic;

import com.halconmusic.dao.HistorialDAO; // ← NUEVO
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
 * Navegación controlada por CardLayout — sin instanciar paneles
 * innecesariamente.
 */
public class App extends JFrame {

    // Usuario activo — en una app real vendría del login
    private static final String ID_USUARIO = "US001";
    private static final String NOMBRE_USR = "Alejandro";
    private static final String TIPO_USR = "Premium ✦";

    private JPanel contentArea;
    private CardLayout cardLayout;
    private PlayerBar playerBar;
    private HistorialDAO historialDAO; // ← NUEVO

    public App() {
        super("HalconMusic");
        historialDAO = new HistorialDAO(); // ← NUEVO
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
            UIManager.put("Panel.background", UITheme.BG);
            UIManager.put("ScrollPane.background", UITheme.BG);
            UIManager.put("Viewport.background", UITheme.BG);
            UIManager.put("ScrollBar.thumb", UITheme.SURFACE);
            UIManager.put("ScrollBar.thumbDarkShadow", UITheme.SURFACE);
            UIManager.put("TextField.background", UITheme.SURFACE);
            UIManager.put("TextField.foreground", UITheme.TEXT);
            UIManager.put("TextField.caretForeground", UITheme.ACCENT);
        } catch (Exception ignored) {
        }
    }

    private void construirUI() {
        setLayout(new BorderLayout());

        // ── Sidebar ───────────────────────────────────────
        Sidebar sidebar = new Sidebar(this::navegar, NOMBRE_USR, TIPO_USR);

        // ── ContentArea (CardLayout) ──────────────────────
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(UITheme.BG);

        // Registrar todos los paneles
        // ↓ SongRow ahora recibe idUsuario para el botón ♥
        contentArea.add(wrapScroll(new HomePanel(this::reproducir, ID_USUARIO)), "home");
        contentArea.add(new BuscarPanel(this::reproducir, ID_USUARIO), "buscar"); // ← pasa ID_USUARIO
        contentArea.add(wrapScroll(new ResumenPanel(this::reproducir, ID_USUARIO)), "historial");
        contentArea.add(new MeGustasPanel(this::reproducir, ID_USUARIO), "megustas");
        contentArea.add(new ArtistasPanel(), "artistas");
        contentArea.add(new AlbumesPanel(this::reproducir), "albumes");
        contentArea.add(new CancionesPanel(this::reproducir, ID_USUARIO), "canciones"); // ← pasa ID_USUARIO

        // ── PlayerBar (inferior) ──────────────────────────
        playerBar = new PlayerBar();

        // ── Estructura principal ──────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, contentArea);
        split.setDividerSize(0);
        split.setEnabled(false);
        split.setBorder(null);

        add(split, BorderLayout.CENTER);
        add(playerBar, BorderLayout.SOUTH);

        cardLayout.show(contentArea, "home");
        pack();
    }

    private void navegar(String vista) {
        cardLayout.show(contentArea, vista);
    }

    /**
     * Reproduce la canción en el PlayerBar Y la guarda automáticamente
     * en la tabla HISTORIAL_CANCIONES. — Req. 4 cumplido.
     */
    private void reproducir(Cancion cancion) {
        playerBar.reproducir(cancion);

        // ✅ NUEVO: guardar en historial automáticamente al reproducir
        new Thread(() -> historialDAO.registrarEnHistorial(ID_USUARIO, cancion.getIdCancion())).start();
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