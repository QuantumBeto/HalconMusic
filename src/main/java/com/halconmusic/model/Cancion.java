package com.halconmusic.model;

import java.awt.Image;

public class Cancion {
    private String idCancion;
    private String nombre;
    private String genero;
    private String artista;
    private String ft;
    private Image  portada;
    private String emocion;
    private int    duracionSeg;
    private int    fecha;

    public Cancion(String idCancion, String nombre, String genero,
                   String artista, String ft, Image portada,
                   String emocion, int duracionSeg, int fecha) {
        this.idCancion   = idCancion;
        this.nombre      = nombre;
        this.genero      = genero;
        this.artista     = artista;
        this.ft          = ft;
        this.portada     = portada;
        this.emocion     = emocion;
        this.duracionSeg = duracionSeg;
        this.fecha       = fecha;
    }

    /** Formatea los segundos a mm:ss */
    public String getDuracionFormateada() {
        int min = duracionSeg / 60;
        int seg = duracionSeg % 60;
        return String.format("%d:%02d", min, seg);
    }

    /** Nombre completo con featuring si aplica */
    public String getNombreArtistasCompleto() {
        if (ft != null && !ft.isBlank()) {
            return artista + " ft. " + ft;
        }
        return artista;
    }

    public String getIdCancion()   { return idCancion; }
    public String getNombre()       { return nombre; }
    public String getGenero()       { return genero; }
    public String getArtista()      { return artista; }
    public String getFt()           { return ft; }
    public Image  getPortada()      { return portada; }
    public String getEmocion()      { return emocion; }
    public int    getDuracionSeg()  { return duracionSeg; }
    public int    getFecha()        { return fecha; }
}
