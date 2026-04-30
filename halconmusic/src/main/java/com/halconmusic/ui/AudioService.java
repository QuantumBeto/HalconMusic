package com.halconmusic.ui;

import com.halconmusic.db.ConexionDB;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.function.Consumer;

/**
 * Servicio de audio con JavaFX MediaPlayer.
 * Soporta: reproducir, pausar, reanudar, detener y seekbar.
 *
 * Flujo: BLOB de Oracle → archivo temporal → JavaFX Media → MediaPlayer
 */
public class AudioService {

    private static AudioService instancia;
    private        MediaPlayer  player;
    private        Path         archivoTemporal;
    private        boolean      inicializado = false;

    // Callbacks para la UI
    private Consumer<Double>  onProgreso;  // Progreso en segundos
    private Runnable          onFinish;
    private Runnable          onListo;     // Cuando el media está listo para reproducir

    private AudioService() {
        inicializarJavaFX();
    }

    public static AudioService getInstance() {
        if (instancia == null) instancia = new AudioService();
        return instancia;
    }

    /**
     * JavaFX necesita inicializarse aunque no uses ventana JavaFX.
     * JFXPanel hace el truco — inicializa el toolkit sin abrir ventana.
     */
    private void inicializarJavaFX() {
        if (!inicializado) {
            new JFXPanel(); // Inicializa JavaFX toolkit
            Platform.setImplicitExit(false);
            inicializado = true;
        }
    }

    /**
     * Carga el BLOB de Oracle, lo guarda en un archivo temporal
     * y lo reproduce con JavaFX MediaPlayer.
     */
    public void reproducir(String idCancion,
                           Runnable       alTerminar,
                           Consumer<Double> alProgresar) {
        this.onFinish   = alTerminar;
        this.onProgreso = alProgresar;

        // Carga el BLOB en hilo separado para no bloquear la UI
        Thread cargador = new Thread(() -> {
            try {
                byte[] audioBytes = obtenerBlobDeOracle(idCancion);
                if (audioBytes == null) {
                    System.err.println("No se encontró audio para: " + idCancion);
                    return;
                }

                // Guarda en archivo temporal (JavaFX necesita URI, no InputStream)
                limpiarTemporal();
                archivoTemporal = Files.createTempFile("halcon_", ".mp3");
                archivoTemporal.toFile().deleteOnExit();
                Files.write(archivoTemporal, audioBytes);

                // Reproduce en el hilo de JavaFX
                String uri = archivoTemporal.toUri().toString();
                Platform.runLater(() -> iniciarPlayer(uri));

            } catch (Exception e) {
                System.err.println("Error cargando audio: " + e.getMessage());
            }
        });
        cargador.setDaemon(true);
        cargador.start();
    }

    /**
     * Inicializa y configura el MediaPlayer en el hilo de JavaFX.
     */
    private void iniciarPlayer(String uri) {
        // Detiene el player anterior si existe
        if (player != null) {
            player.stop();
            player.dispose();
        }

        Media media = new Media(uri);
        player = new MediaPlayer(media);

        // Al terminar la canción
        player.setOnEndOfMedia(() -> {
            if (onFinish != null) onFinish.run();
        });

        // Progreso en tiempo real (cada 500ms)
        player.currentTimeProperty().addListener((obs, anterior, actual) -> {
            if (onProgreso != null) {
                onProgreso.accept(actual.toSeconds());
            }
        });

        player.setOnReady(() -> {
            if (onListo != null) onListo.run();
            player.play();
        });

        player.setOnError(() -> {
            System.err.println("Error en MediaPlayer: " + player.getError());
        });
    }

    /** Pausa la reproducción — guarda la posición exacta */
    public void pausar() {
        if (player != null && player.getStatus() == MediaPlayer.Status.PLAYING) {
            player.pause();
        }
    }

    /** Reanuda desde donde se pausó */
    public void reanudar() {
        if (player != null && player.getStatus() == MediaPlayer.Status.PAUSED) {
            player.play();
        }
    }

    /** Detiene completamente y limpia recursos */
    public void detener() {
        if (player != null) {
            player.stop();
            player.dispose();
            player = null;
        }
        limpiarTemporal();
    }

    /** Salta a un momento específico de la canción (seekbar) */
    public void buscarPosicion(double segundos) {
        if (player != null) {
            Platform.runLater(() ->
                player.seek(Duration.seconds(segundos))
            );
        }
    }

    /** Ajusta el volumen (0.0 a 1.0) */
    public void setVolumen(double volumen) {
        if (player != null) {
            Platform.runLater(() -> player.setVolume(volumen));
        }
    }

    public boolean isReproduciendo() {
        return player != null && player.getStatus() == MediaPlayer.Status.PLAYING;
    }

    public boolean isPausado() {
        return player != null && player.getStatus() == MediaPlayer.Status.PAUSED;
    }

    public double getDuracionTotal() {
        if (player != null && player.getTotalDuration() != null) {
            return player.getTotalDuration().toSeconds();
        }
        return 0;
    }

    public void setOnListo(Runnable onListo) {
        this.onListo = onListo;
    }

    /** Obtiene el BLOB de Oracle como arreglo de bytes */
    private byte[] obtenerBlobDeOracle(String idCancion) {
        String sql = "SELECT MUSICA FROM CANCIONES WHERE ID_CANCION = ?";
        try (PreparedStatement ps = ConexionDB.getInstance().getConexion().prepareStatement(sql)) {
            ps.setString(1, idCancion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Blob blob = rs.getBlob("MUSICA");
                    if (blob != null) {
                        try (InputStream is = blob.getBinaryStream()) {
                            return is.readAllBytes();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo BLOB: " + e.getMessage());
        }
        return null;
    }

    /** Elimina el archivo temporal si existe */
    private void limpiarTemporal() {
        try {
            if (archivoTemporal != null && Files.exists(archivoTemporal)) {
                Files.delete(archivoTemporal);
                archivoTemporal = null;
            }
        } catch (Exception ignored) {}
    }
}