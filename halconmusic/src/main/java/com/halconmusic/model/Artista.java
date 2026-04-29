package com.halconmusic.model;

import java.awt.Image;

public class Artista {
    private String idArtista;
    private String nombre;
    private Image  portada;
    private String descripcion;
    private String generoPrincipal;
    private String paisDeOrigen;
    private int    totalCanciones; // calculado con COUNT

    public Artista(String idArtista, String nombre, Image portada,
                   String descripcion, String generoPrincipal,
                   String paisDeOrigen, int totalCanciones) {
        this.idArtista      = idArtista;
        this.nombre         = nombre;
        this.portada        = portada;
        this.descripcion    = descripcion;
        this.generoPrincipal = generoPrincipal;
        this.paisDeOrigen   = paisDeOrigen;
        this.totalCanciones = totalCanciones;
    }

    public String getIdArtista()      { return idArtista; }
    public String getNombre()          { return nombre; }
    public Image  getPortada()         { return portada; }
    public String getDescripcion()     { return descripcion; }
    public String getGeneroPrincipal() { return generoPrincipal; }
    public String getPaisDeOrigen()    { return paisDeOrigen; }
    public int    getTotalCanciones()  { return totalCanciones; }
}
