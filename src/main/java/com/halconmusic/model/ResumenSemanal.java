package com.halconmusic.model;

import java.util.Date;

public class ResumenSemanal {
    private String idResumen;
    private Date   fecha;
    private String generoPrincipal;
    private String cancionesPrincipales;
    private String artistaPrincipal;
    private String emocion;
    private String idUsuario;
    private String idHistorial;

    public ResumenSemanal(String idResumen, Date fecha, String generoPrincipal,
                          String cancionesPrincipales, String artistaPrincipal,
                          String emocion, String idUsuario, String idHistorial) {
        this.idResumen           = idResumen;
        this.fecha               = fecha;
        this.generoPrincipal     = generoPrincipal;
        this.cancionesPrincipales = cancionesPrincipales;
        this.artistaPrincipal    = artistaPrincipal;
        this.emocion             = emocion;
        this.idUsuario           = idUsuario;
        this.idHistorial         = idHistorial;
    }

    public String getIdResumen()             { return idResumen; }
    public Date   getFecha()                 { return fecha; }
    public String getGeneroPrincipal()       { return generoPrincipal; }
    public String getCancionesPrincipales()  { return cancionesPrincipales; }
    public String getArtistaPrincipal()      { return artistaPrincipal; }
    public String getEmocion()               { return emocion; }
    public String getIdUsuario()             { return idUsuario; }
    public String getIdHistorial()           { return idHistorial; }
}
