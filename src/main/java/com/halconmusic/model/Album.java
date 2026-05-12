package com.halconmusic.model;

import java.awt.Image;

public class Album {
    private String idAlbum;
    private String titulo;
    private Image  portada;
    private int    numeroDeCanciones;
    private String genero;
    private int    fecha;
    private int    duracionSeg;
    private String compositores;
    private String idArtista;
    private String nombreArtista; // JOIN con ARTISTAS

    public Album(String idAlbum, String titulo, Image portada,
                 int numeroDeCanciones, String genero, int fecha,
                 int duracionSeg, String compositores,
                 String idArtista, String nombreArtista) {
        this.idAlbum          = idAlbum;
        this.titulo           = titulo;
        this.portada          = portada;
        this.numeroDeCanciones = numeroDeCanciones;
        this.genero           = genero;
        this.fecha            = fecha;
        this.duracionSeg      = duracionSeg;
        this.compositores     = compositores;
        this.idArtista        = idArtista;
        this.nombreArtista    = nombreArtista;
    }

    public String getDuracionFormateada() {
        int min = duracionSeg / 60;
        int seg = duracionSeg % 60;
        return String.format("%d:%02d", min, seg);
    }

    public String getIdAlbum()           { return idAlbum; }
    public String getTitulo()             { return titulo; }
    public Image  getPortada()            { return portada; }
    public int    getNumeroDeCanciones()  { return numeroDeCanciones; }
    public String getGenero()             { return genero; }
    public int    getFecha()              { return fecha; }
    public int    getDuracionSeg()        { return duracionSeg; }
    public String getCompositores()       { return compositores; }
    public String getIdArtista()          { return idArtista; }
    public String getNombreArtista()      { return nombreArtista; }
}
