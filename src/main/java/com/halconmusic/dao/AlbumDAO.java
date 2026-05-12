package com.halconmusic.dao;

import com.halconmusic.db.ConexionDB;
import com.halconmusic.model.Album;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de Álbumes.
 * Usa JOIN con ARTISTAS, LIKE, UPPER/LOWER.
 */
public class AlbumDAO {

    private final Connection con;

    public AlbumDAO() {
        this.con = ConexionDB.getInstance().getConexion();
    }

    /**
     * Obtiene todos los álbumes con nombre del artista.
     * SQL: JOIN ALBUMES + ARTISTAS
     */
    public List<Album> obtenerTodos() {
        List<Album> lista = new ArrayList<>();

        String sql = """
            SELECT AL.ID_ALBUM,
                   AL.TITULO,
                   AL.PORTADA,
                   AL.NUMERODECANCIONES,
                   AL.GENERO,
                   AL.FECHA,
                   AL.DURACION_SEG,
                   AL.COMPOSITORES,
                   AL.ID_ARTISTA,
                   A.NOMBRE AS NOMBRE_ARTISTA
            FROM ALBUMES AL
            JOIN ARTISTAS A ON AL.ID_ARTISTA = A.ID_ARTISTA
            ORDER BY AL.FECHA DESC, UPPER(AL.TITULO)
            """;

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearAlbum(rs));
        } catch (SQLException e) {
            System.err.println("Error en AlbumDAO.obtenerTodos: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Busca álbumes por título o artista.
     * SQL: UPPER + LIKE
     */
    public List<Album> buscar(String termino) {
        List<Album> lista = new ArrayList<>();

        String sql = """
            SELECT AL.ID_ALBUM,
                   AL.TITULO,
                   AL.PORTADA,
                   AL.NUMERODECANCIONES,
                   AL.GENERO,
                   AL.FECHA,
                   AL.DURACION_SEG,
                   AL.COMPOSITORES,
                   AL.ID_ARTISTA,
                   A.NOMBRE AS NOMBRE_ARTISTA
            FROM ALBUMES AL
            JOIN ARTISTAS A ON AL.ID_ARTISTA = A.ID_ARTISTA
            WHERE UPPER(AL.TITULO)  LIKE UPPER(?)
               OR UPPER(A.NOMBRE)   LIKE UPPER(?)
               OR UPPER(AL.GENERO)  LIKE UPPER(?)
            ORDER BY AL.FECHA DESC
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String like = "%" + termino + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearAlbum(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en AlbumDAO.buscar: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Obtiene álbumes de un artista específico.
     * SQL: WHERE + JOIN
     */
    public List<Album> obtenerPorArtista(String idArtista) {
        List<Album> lista = new ArrayList<>();

        String sql = """
            SELECT AL.ID_ALBUM,
                   AL.TITULO,
                   AL.PORTADA,
                   AL.NUMERODECANCIONES,
                   AL.GENERO,
                   AL.FECHA,
                   AL.DURACION_SEG,
                   AL.COMPOSITORES,
                   AL.ID_ARTISTA,
                   A.NOMBRE AS NOMBRE_ARTISTA
            FROM ALBUMES AL
            JOIN ARTISTAS A ON AL.ID_ARTISTA = A.ID_ARTISTA
            WHERE AL.ID_ARTISTA = ?
            ORDER BY AL.FECHA DESC
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idArtista);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearAlbum(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en AlbumDAO.obtenerPorArtista: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Mapea un ResultSet a un objeto Album.
     */
    private Album mapearAlbum(ResultSet rs) throws SQLException {
        Image portada = null;
        try {
            Blob blob = rs.getBlob("PORTADA");
            if (blob != null) {
                InputStream is = blob.getBinaryStream();
                portada = ImageIO.read(is);
            }
        } catch (Exception e) {
            // Portada vacía
        }

        return new Album(
            rs.getString("ID_ALBUM"),
            rs.getString("TITULO"),
            portada,
            rs.getInt("NUMERODECANCIONES"),
            rs.getString("GENERO"),
            rs.getInt("FECHA"),
            rs.getInt("DURACION_SEG"),
            rs.getString("COMPOSITORES"),
            rs.getString("ID_ARTISTA"),
            rs.getString("NOMBRE_ARTISTA")
        );
    }
}
