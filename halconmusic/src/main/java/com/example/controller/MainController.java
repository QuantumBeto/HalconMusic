package com.example.controller;

import com.example.dao.CancionDAO;
import com.example.model.Cancion;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class MainController {

    @FXML private FlowPane gridCanciones;
    private CancionDAO cancionDAO = new CancionDAO();

    @FXML
    public void initialize() {
        // Cargar por defecto canciones "Felices" al abrir la app
        cargarMusicaPorMood("Feliz");
    }

    // Estos métodos se conectarán a los botones de "Mood" en el FXML
    @FXML public void moodEnergetico() { cargarMusicaPorMood("Energético"); }
    @FXML public void moodRelajado() { cargarMusicaPorMood("Relajado"); }
    @FXML public void moodBelicon() { cargarMusicaPorMood("Belicón"); }

    private void cargarMusicaPorMood(String emocion) {
        gridCanciones.getChildren().clear(); // Limpiamos la pantalla

        // Creamos un hilo en segundo plano para no trabar la interfaz
        Task<List<Cancion>> tareaCarga = new Task<>() {
            @Override
            protected List<Cancion> call() {
                return cancionDAO.obtenerCancionesPorEmocion(emocion);
            }
        };

        // Cuando la base de datos responda, actualizamos la interfaz
        tareaCarga.setOnSucceeded(event -> {
            List<Cancion> canciones = tareaCarga.getValue();
            for (Cancion c : canciones) {
                gridCanciones.getChildren().add(crearTarjetaCancion(c));
            }
        });

        new Thread(tareaCarga).start();
    }

    // Método que dibuja la "Tarjeta" estilo Spotify para cada canción
    private VBox crearTarjetaCancion(Cancion c) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("music-card");

        ImageView portada = new ImageView();
        if (c.getPortadaStream() != null) {
            portada.setImage(new Image(c.getPortadaStream()));
        }
        portada.setFitWidth(140);
        portada.setFitHeight(140);

        Label lblNombre = new Label(c.getNombre());
        lblNombre.getStyleClass().add("track-title");

        Label lblCompositor = new Label(c.getCompositor());
        lblCompositor.getStyleClass().add("track-artist");

        card.getChildren().addAll(portada, lblNombre, lblCompositor);

        // Efecto hover y click para reproducir
        card.setOnMouseClicked(e -> System.out.println("Reproduciendo: " + c.getNombre()));

        return card;
    }
}