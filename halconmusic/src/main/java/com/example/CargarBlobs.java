package com.example;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class CargarBlobs {

    static final String BASE     = "D:\\Documentos\\TecNM\\HalconMusic\\halconmusic\\src\\recursos";
    static final String URL      = "jdbc:oracle:thin:@localhost:1521/xe";
    static final String USUARIO  = "system";
    static final String PASSWORD = "12345678";

    public static void main(String[] args) {
        try (Connection con = DriverManager.getConnection(URL, USUARIO, PASSWORD)) {
            con.setAutoCommit(false);

            //cargarPortadasArtistas(con);
            //cargarPortadasCanciones(con);
            //cargarArchivosCanciones(con);
            cargarPortadasAlbumes(con);   
            cargarPortadasPlaylists(con);

            con.commit();
            System.out.println("✅ Todos los archivos cargados correctamente");

        } catch (Exception e) {
            System.err.println("❌ Error:");
            e.printStackTrace();
        }
    }

    static void cargarPortadasArtistas(Connection con) throws Exception {
        String sql = "UPDATE ARTISTAS SET Portada = ? WHERE ID_ARTISTA = ?";

        Object[][] artistas = {
            {"pesopluma.jpg",  "ART001"},
            {"badbunny.jpg",   "ART002"},
            {"juangabriel.jpg","ART003"},
            {"shakira.jpg",    "ART004"},
            {"losbukis.jpg",   "ART005"},
            {"bandams.jpg",    "ART006"},
            {"natanael.jpg",   "ART007"},
            {"karolg.jpg",     "ART008"},
            {"vicentef.jpg",   "ART009"}
        };

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Object[] a : artistas) {
                String ruta = BASE + "\\portadas\\artistas\\" + a[0];
                ps.setBinaryStream(1, new FileInputStream(ruta));
                ps.setString(2, (String) a[1]);
                ps.executeUpdate();
                System.out.println("🖼️  Portada artista cargada: " + a[1]);
            }
        }
    }

    static void cargarPortadasCanciones(Connection con) throws Exception {
        String sql = "UPDATE CANCIONES SET Portada = ? WHERE ID_CANCION = ?";

        Object[][] canciones = {
            {"ellabaila.jpg",       "C001"},
            {"bzrp55.jpg",          "C002"},
            {"ladygaga.jpg",        "C003"},
            {"prc.jpg",             "C004"},
            {"belicon.jpg",         "C005"},
            {"titi.jpg",            "C006"},
            {"dakiti.jpg",          "C007"},
            {"meportobonito.jpg",   "C008"},
            {"callaita.jpg",        "C009"},
            {"yonaguni.jpg",        "C010"},
            {"amoreterno.jpg",      "C011"},
            {"querida.jpg",         "C012"},
            {"asifue.jpg",          "C013"},
            {"noanoa.jpg",          "C014"},
            {"telopido.jpg",        "C015"},
            {"hips.jpg",            "C016"},
            {"wakawaka.jpg",        "C017"},
            {"latortura.jpg",       "C018"},
            {"bzrp53.jpg",          "C019"},
            {"loca.jpg",            "C020"},
            {"tucarcel.jpg",        "C021"},
            {"comoyo.jpg",          "C022"},
            {"mevolvi.jpg",         "C023"},
            {"simerec.jpg",         "C024"},
            {"quieromas.jpg",       "C025"},
            {"megustas.jpg",        "C026"},
            {"estabacanora.jpg",    "C027"},
            {"soloconverte.jpg",    "C028"},
            {"alomejor.jpg",        "C029"},
            {"elcolor.jpg",         "C030"},
            {"amortumbado.jpg",     "C031"},
            {"tstrip.jpg",          "C032"},
            {"enelamor.jpg",        "C033"},
            {"pacas.jpg",           "C034"},
            {"soyeldiablo.jpg",     "C035"},
            {"provenza.jpg",        "C036"},
            {"tusa.jpg",            "C037"},
            {"mamiii.jpg",          "C038"},
            {"bichota.jpg",         "C039"},
            {"cairo.jpg",           "C040"},
            {"volver.jpg",          "C041"},
            {"elrey.jpg",           "C042"},
            {"acabetreentre.jpg",   "C043"},
            {"lastimaqueseaes.jpg", "C044"},
            {"leydelmonte.jpg",     "C045"}
        };

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Object[] c : canciones) {
                String ruta = BASE + "\\portadas\\canciones\\" + c[0];
                ps.setBinaryStream(1, new FileInputStream(ruta));
                ps.setString(2, (String) c[1]);
                ps.executeUpdate();
                System.out.println("🖼️  Portada canción cargada: " + c[1]);
            }
        }
    }

    static void cargarArchivosCanciones(Connection con) throws Exception {
        String sql = "UPDATE CANCIONES SET Musica = ? WHERE ID_CANCION = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 45; i++) {
                String id   = String.format("C%03d", i);
                String ruta = BASE + "\\canciones\\" + id + ".mp3";
                ps.setBinaryStream(1, new FileInputStream(ruta));
                ps.setString(2, id);
                ps.executeUpdate();
                System.out.println("🎵  Audio cargado: " + id);
            }
        }
    }

    static void cargarPortadasAlbumes(Connection con) throws Exception {
    String sql = "UPDATE ALBUMES SET Portada = ? WHERE ID_ALBUM = ?";

    Object[][] albumes = {
        {"genesis.jpg",       "ALB001"},
        {"verano.jpg",        "ALB002"},
        {"divoquarez.jpg",    "ALB003"},
        {"shewolf.jpg",       "ALB004"},
        {"paramipueblo.jpg",  "ALB005"},
        {"entudio.jpg",       "ALB006"},
        {"soyeldiablo.jpg",   "ALB007"},
        {"kg0516.jpg",        "ALB008"},
        {"grandesexitos.jpg", "ALB009"}
    };

    try (PreparedStatement ps = con.prepareStatement(sql)) {
        for (Object[] a : albumes) {
            String ruta = BASE + "\\portadas\\albumes\\" + a[0];
            ps.setBinaryStream(1, new FileInputStream(ruta));
            ps.setString(2, (String) a[1]);
            ps.executeUpdate();
            System.out.println("🖼️  Portada álbum cargada: " + a[1]);
        }
    }
}

static void cargarPortadasPlaylists(Connection con) throws Exception {
    String sql = "UPDATE PLAYLISTS SET Portada = ? WHERE ID_PLAYLIST = ?";

    Object[][] playlists = {
        {"corridos2024.jpg",   "PL001"},
        {"reggaeton.jpg",      "PL002"},
        {"baladas.jpg",        "PL003"},
        {"popint.jpg",         "PL004"},
        {"grupero.jpg",        "PL005"},
        {"banda.jpg",          "PL006"},
        {"tumbados.jpg",       "PL007"},
        {"reggaetonnuevo.jpg", "PL008"},
        {"rancheras.jpg",      "PL009"}
    };

    try (PreparedStatement ps = con.prepareStatement(sql)) {
        for (Object[] p : playlists) {
            String ruta = BASE + "\\portadas\\playlists\\" + p[0];
            ps.setBinaryStream(1, new FileInputStream(ruta));
            ps.setString(2, (String) p[1]);
            ps.executeUpdate();
            System.out.println("🖼️  Portada playlist cargada: " + p[1]);
        }
    }
}
}