package com.halconmusic.dao;

import com.halconmusic.db.ConexionDB;
import com.halconmusic.model.Cancion;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de Canciones.
 * Usa JOIN, LIKE, UPPER/LOWER, COUNT, GROUP BY, ORDER BY.
 */
public class CancionDAO {

    private final Connection con;

    public CancionDAO() {
        this.con = ConexionDB.getInstance().getConexion();
    }

    /**
     * Obtiene todas las canciones con el nombre del artista.
     * SQL: JOIN CANCIONES + ARTISTAS_CANCIONES + ARTISTAS
     */
    public List<Cancion> obtenerTodas() {
        List<Cancion> lista = new ArrayList<>();

        String sql = """
            SELECT C.ID_CANCION,
                   C.NOMBRE,
                   C.GENERO,
                   A.NOMBRE   AS ARTISTA,
                   C.FT,
                   C.PORTADA,
                   C.EMOCION,
                   C.DURACION_SEG,
                   C.FECHA
            FROM CANCIONES C
            JOIN ARTISTAS_CANCIONES AC ON C.ID_CANCION = AC.ID_CANCION
            JOIN ARTISTAS A            ON AC.ID_ARTISTA = A.ID_ARTISTA
            ORDER BY UPPER(C.NOMBRE)
            """;

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearCancion(rs));
        } catch (SQLException e) {
            System.err.println("Error en CancionDAO.obtenerTodas: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Búsqueda de canciones por nombre, artista o género.
     * SQL: UPPER + LOWER + LIKE múltiple
     */
    public List<Cancion> buscar(String termino) {
        List<Cancion> lista = new ArrayList<>();

        String sql = """
            SELECT C.ID_CANCION,
                   C.NOMBRE,
                   C.GENERO,
                   A.NOMBRE   AS ARTISTA,
                   C.FT,
                   C.PORTADA,
                   C.EMOCION,
                   C.DURACION_SEG,
                   C.FECHA
            FROM CANCIONES C
            JOIN ARTISTAS_CANCIONES AC ON C.ID_CANCION = AC.ID_CANCION
            JOIN ARTISTAS A            ON AC.ID_ARTISTA = A.ID_ARTISTA
            WHERE UPPER(C.NOMBRE)  LIKE UPPER(?)
               OR UPPER(A.NOMBRE)  LIKE UPPER(?)
               OR UPPER(C.GENERO)  LIKE UPPER(?)
               OR UPPER(C.EMOCION) LIKE UPPER(?)
            ORDER BY UPPER(C.NOMBRE)
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String like = "%" + termino + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCancion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en CancionDAO.buscar: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Obtiene canciones de un álbum específico.
     * SQL: JOIN ALBUMES_CANCIONES
     */
    public List<Cancion> obtenerPorAlbum(String idAlbum) {
        List<Cancion> lista = new ArrayList<>();

        String sql = """
            SELECT C.ID_CANCION,
                   C.NOMBRE,
                   C.GENERO,
                   A.NOMBRE   AS ARTISTA,
                   C.FT,
                   C.PORTADA,
                   C.EMOCION,
                   C.DURACION_SEG,
                   C.FECHA
            FROM CANCIONES C
            JOIN ALBUMES_CANCIONES  AL ON C.ID_CANCION  = AL.ID_CANCION
            JOIN ARTISTAS_CANCIONES AC ON C.ID_CANCION  = AC.ID_CANCION
            JOIN ARTISTAS A            ON AC.ID_ARTISTA = A.ID_ARTISTA
            WHERE AL.ID_ALBUM = ?
            ORDER BY C.FECHA, UPPER(C.NOMBRE)
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idAlbum);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCancion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en CancionDAO.obtenerPorAlbum: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Obtiene canciones del historial de un usuario.
     * SQL: JOIN HISTORIAL + HISTORIAL_CANCIONES
     */
    public List<Cancion> obtenerHistorialUsuario(String idUsuario) {
        List<Cancion> lista = new ArrayList<>();

        String sql = """
            SELECT C.ID_CANCION,
                   C.NOMBRE,
                   C.GENERO,
                   A.NOMBRE   AS ARTISTA,
                   C.FT,
                   C.PORTADA,
                   C.EMOCION,
                   C.DURACION_SEG,
                   C.FECHA
            FROM CANCIONES C
            JOIN HISTORIAL_CANCIONES HC ON C.ID_CANCION  = HC.ID_CANCION
            JOIN HISTORIAL H            ON HC.ID_HISTORIAL = H.ID_HISTORIAL
            JOIN ARTISTAS_CANCIONES AC  ON C.ID_CANCION  = AC.ID_CANCION
            JOIN ARTISTAS A             ON AC.ID_ARTISTA = A.ID_ARTISTA
            WHERE H.ID_USUARIO = ?
            ORDER BY UPPER(C.NOMBRE)
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCancion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en CancionDAO.obtenerHistorialUsuario: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Obtiene canciones que le gustan a un usuario.
     * SQL: JOIN MEGUSTAS + MEGUSTAS_CANCIONES
     */
    public List<Cancion> obtenerMeGustasUsuario(String idUsuario) {
        List<Cancion> lista = new ArrayList<>();

        String sql = """
            SELECT C.ID_CANCION,
                   C.NOMBRE,
                   C.GENERO,
                   A.NOMBRE   AS ARTISTA,
                   C.FT,
                   C.PORTADA,
                   C.EMOCION,
                   C.DURACION_SEG,
                   C.FECHA
            FROM CANCIONES C
            JOIN MEGUSTAS_CANCIONES MC ON C.ID_CANCION   = MC.ID_CANCION
            JOIN MEGUSTAS M            ON MC.ID_MEGUSTAS  = M.ID_MEGUSTAS
            JOIN ARTISTAS_CANCIONES AC ON C.ID_CANCION   = AC.ID_CANCION
            JOIN ARTISTAS A            ON AC.ID_ARTISTA  = A.ID_ARTISTA
            WHERE M.ID_USUARIO = ?
            ORDER BY UPPER(C.NOMBRE)
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCancion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en CancionDAO.obtenerMeGustasUsuario: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Cuenta el total de canciones en la BD.
     * SQL: COUNT
     */
    public int contarTotal() {
        String sql = "SELECT COUNT(*) FROM CANCIONES";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error en contarTotal: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Cuenta canciones agrupadas por género.
     * SQL: COUNT + GROUP BY
     */
    public List<String[]> contarPorGenero() {
        List<String[]> lista = new ArrayList<>();

        String sql = """
            SELECT GENERO, COUNT(*) AS TOTAL
            FROM CANCIONES
            GROUP BY GENERO
            ORDER BY TOTAL DESC
            """;

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{ rs.getString("GENERO"), rs.getString("TOTAL") });
            }
        } catch (SQLException e) {
            System.err.println("Error en contarPorGenero: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Mapea un ResultSet a un objeto Cancion.
     */
    private Cancion mapearCancion(ResultSet rs) throws SQLException {
        Image portada = null;
        try {
            Blob blob = rs.getBlob("PORTADA");
            if (blob != null) {
                InputStream is = blob.getBinaryStream();
                portada = ImageIO.read(is);
            }
        } catch (Exception e) {
            // Portada vacía — se mostrará placeholder en la UI
        }

        return new Cancion(
            rs.getString("ID_CANCION"),
            rs.getString("NOMBRE"),
            rs.getString("GENERO"),
            rs.getString("ARTISTA"),
            rs.getString("FT"),
            portada,
            rs.getString("EMOCION"),
            rs.getInt("DURACION_SEG"),
            rs.getInt("FECHA")
        );
    }
}
