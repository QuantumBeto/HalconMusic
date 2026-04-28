package com.example.model;

import java.io.InputStream;

public class Cancion {
    private String idCancion;
    private String nombre;
    private String compositor;
    private String emocion;
    private InputStream portadaStream; // Para manejar el BLOB visual

    public Cancion(String idCancion, String nombre, String compositor, String emocion, InputStream portadaStream) {
        this.idCancion = idCancion;
        this.nombre = nombre;
        this.compositor = compositor;
        this.emocion = emocion;
        this.portadaStream = portadaStream;
    }

    // Getters
    public String getIdCancion() { return idCancion; }
    public String getNombre() { return nombre; }
    public String getCompositor() { return compositor; }
    public String getEmocion() { return emocion; }
    public InputStream getPortadaStream() { return portadaStream; }
}