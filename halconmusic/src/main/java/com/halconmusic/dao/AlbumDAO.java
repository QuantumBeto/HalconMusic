package com.halconmusic.dao;

import java.awt.Image;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.halconmusic.db.ConexionDB;
import com.halconmusic.model.Album;

public class AlbumDAO {

    private final Connection con;

    public AlbumDAO() {
        this.con = ConexionDB.getInstance().getConexion();
    }

    // ── REQ. 2 — CREAR ÁLBUM ─────────────────────────────
    /**
     * Inserta un nuevo álbum en la tabla ALBUMES.
     * @return true si se insertó correctamente.
     */
    public boolean insertar(String titulo, int numeroCanciones, String genero,
                            int fecha, int duracionSeg, String compositores,
                            String idArtista, java.io.File archivoPortada) {
        String idNuevo = generarNuevoId();
        try {
            con.setAutoCommit(false);

            String sql = """
                INSERT INTO ALBUMES
                  (ID_ALBUM, TITULO, PORTADA, NUMERODECANCIONES, GENERO,
                   FECHA, DURACION_SEG, COMPOSITORES, ID_ARTISTA)
                VALUES (?, ?, EMPTY_BLOB(), ?, ?, ?, ?, ?, ?)
                """;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, idNuevo);
                ps.setString(2, titulo.trim());
                ps.setInt   (3, numeroCanciones);
                ps.setString(4, genero.trim());
                ps.setInt   (5, fecha);
                ps.setInt   (6, duracionSeg);
                ps.setString(7, compositores.trim());
                ps.setString(8, idArtista);
                ps.executeUpdate();
            }

            if (archivoPortada != null) {
                String sqlBlob = "SELECT PORTADA FROM ALBUMES WHERE ID_ALBUM = ? FOR UPDATE";
                try (PreparedStatement ps = con.prepareStatement(sqlBlob)) {
                    ps.setString(1, idNuevo);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            oracle.sql.BLOB blob = (oracle.sql.BLOB) rs.getBlob("PORTADA");
                            java.io.OutputStream  os  = blob.getBinaryOutputStream();
                            java.io.FileInputStream fis = new java.io.FileInputStream(archivoPortada);
                            byte[] buf = new byte[blob.getBufferSize()];
                            int n;
                            while ((n = fis.read(buf)) != -1) os.write(buf, 0, n);
                            os.close();
                            fis.close();
                        }
                    }
                }
            }

            con.commit();
            con.setAutoCommit(true);
            System.out.println("✅ Álbum creado: " + idNuevo);
            return true;
        } catch (Exception e) {
            System.err.println("Error al insertar álbum: " + e.getMessage());
            try { con.rollback(); con.setAutoCommit(true); } catch (Exception ignored) {}
            return false;
        }
    }

    private String generarNuevoId() {
        String sql = "SELECT NVL(MAX(TO_NUMBER(SUBSTR(ID_ALBUM,4))),0) + 1 AS NEXT_ID FROM ALBUMES";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return String.format("ALB%03d", rs.getInt("NEXT_ID"));
        } catch (SQLException e) {
            System.err.println("Error generando ID álbum: " + e.getMessage());
        }
        return "ALB" + System.currentTimeMillis();
    }

    // ── CONSULTAS EXISTENTES ──────────────────────────────
    public List<Album> obtenerTodos() {
        List<Album> lista = new ArrayList<>();
        String sql = """
            SELECT AL.ID_ALBUM, AL.TITULO, AL.PORTADA,
                   AL.NUMERODECANCIONES, AL.GENERO, AL.FECHA,
                   AL.DURACION_SEG, AL.COMPOSITORES, AL.ID_ARTISTA,
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

    public List<Album> buscar(String termino) {
        List<Album> lista = new ArrayList<>();
        String sql = """
            SELECT AL.ID_ALBUM, AL.TITULO, AL.PORTADA,
                   AL.NUMERODECANCIONES, AL.GENERO, AL.FECHA,
                   AL.DURACION_SEG, AL.COMPOSITORES, AL.ID_ARTISTA,
                   A.NOMBRE AS NOMBRE_ARTISTA
            FROM ALBUMES AL
            JOIN ARTISTAS A ON AL.ID_ARTISTA = A.ID_ARTISTA
            WHERE UPPER(AL.TITULO) LIKE UPPER(?)
               OR UPPER(A.NOMBRE)  LIKE UPPER(?)
               OR UPPER(AL.GENERO) LIKE UPPER(?)
            ORDER BY AL.FECHA DESC
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String like = "%" + termino + "%";
            ps.setString(1, like); ps.setString(2, like); ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearAlbum(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en AlbumDAO.buscar: " + e.getMessage());
        }
        return lista;
    }

    public List<Album> obtenerPorArtista(String idArtista) {
        List<Album> lista = new ArrayList<>();
        String sql = """
            SELECT AL.ID_ALBUM, AL.TITULO, AL.PORTADA,
                   AL.NUMERODECANCIONES, AL.GENERO, AL.FECHA,
                   AL.DURACION_SEG, AL.COMPOSITORES, AL.ID_ARTISTA,
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

    private Album mapearAlbum(ResultSet rs) throws SQLException {
        Image portada = null;
        try {
            Blob blob = rs.getBlob("PORTADA");
            if (blob != null) portada = ImageIO.read(blob.getBinaryStream());
        } catch (Exception ignored) {}
        return new Album(
            rs.getString("ID_ALBUM"), rs.getString("TITULO"), portada,
            rs.getInt("NUMERODECANCIONES"), rs.getString("GENERO"),
            rs.getInt("FECHA"), rs.getInt("DURACION_SEG"),
            rs.getString("COMPOSITORES"), rs.getString("ID_ARTISTA"),
            rs.getString("NOMBRE_ARTISTA"));
    }
}