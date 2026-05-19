package com.halconmusic.dao;

import com.halconmusic.db.ConexionDB;
import com.halconmusic.model.Cancion;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CancionDAO {

    private final Connection con;

    public CancionDAO() {
        this.con = ConexionDB.getInstance().getConexion();
    }

    // ── REQ. 2 — CREAR CANCIÓN ────────────────────────────
    /**
     * Inserta una nueva canción en CANCIONES y la vincula al artista en
     * ARTISTAS_CANCIONES y al álbum en ALBUMES_CANCIONES (si se indica).
     * @return true si se insertó correctamente.
     */
    public boolean insertar(String nombre, String genero, String artista,
                            String emocion, int duracionSeg, int fecha,
                            String ft, String letra,
                            String idArtista, String idAlbum) {
        String idNuevo = generarNuevoId();

        String sqlCan = """
            INSERT INTO CANCIONES
              (ID_CANCION, NOMBRE, GENERO, ARTISTA, PORTADA, MUSICA,
               EMOCION, DURACION_SEG, FECHA, FT, LETRA)
            VALUES (?, ?, ?, ?, EMPTY_BLOB(), EMPTY_BLOB(), ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = con.prepareStatement(sqlCan)) {
            ps.setString(1, idNuevo);
            ps.setString(2, nombre.trim());
            ps.setString(3, genero.trim());
            ps.setString(4, artista.trim());
            ps.setString(5, emocion.trim());
            ps.setInt   (6, duracionSeg);
            ps.setInt   (7, fecha);
            ps.setString(8, (ft == null || ft.isBlank()) ? null : ft.trim());
            ps.setString(9, (letra == null || letra.isBlank()) ? null : letra.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al insertar canción: " + e.getMessage());
            return false;
        }

        // Vincula a artista
        String sqlArt = "INSERT INTO ARTISTAS_CANCIONES (ID_ARTISTA, ID_CANCION) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sqlArt)) {
            ps.setString(1, idArtista);
            ps.setString(2, idNuevo);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al vincular artista-canción: " + e.getMessage());
        }

        // Vincula a álbum si se seleccionó uno
        if (idAlbum != null && !idAlbum.isBlank()) {
            String sqlAlb = "INSERT INTO ALBUMES_CANCIONES (ID_ALBUM, ID_CANCION) VALUES (?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlAlb)) {
                ps.setString(1, idAlbum);
                ps.setString(2, idNuevo);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error al vincular álbum-canción: " + e.getMessage());
            }
        }

        System.out.println("✅ Canción creada: " + idNuevo);
        return true;
    }

    private String generarNuevoId() {
        String sql = "SELECT NVL(MAX(TO_NUMBER(SUBSTR(ID_CANCION,2))),0) + 1 AS NEXT_ID FROM CANCIONES";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return String.format("C%03d", rs.getInt("NEXT_ID"));
        } catch (SQLException e) {
            System.err.println("Error generando ID canción: " + e.getMessage());
        }
        return "C" + System.currentTimeMillis();
    }

    // ── REQ. 11 — OBTENER LETRA DE CANCIÓN ───────────────
    /**
     * Retorna la letra (CLOB) de una canción, o null si no tiene.
     */
    public String obtenerLetra(String idCancion) {
        String sql = "SELECT LETRA FROM CANCIONES WHERE ID_CANCION = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idCancion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Clob clob = rs.getClob("LETRA");
                    if (clob != null) {
                        return clob.getSubString(1, (int) clob.length());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener letra: " + e.getMessage());
        }
        return null;
    }

    // ── CONSULTAS EXISTENTES ──────────────────────────────
    public List<Cancion> obtenerTodas() {
        List<Cancion> lista = new ArrayList<>();
        String sql = """
            SELECT C.ID_CANCION, C.NOMBRE, C.GENERO,
                   A.NOMBRE AS ARTISTA, C.FT, C.PORTADA,
                   C.EMOCION, C.DURACION_SEG, C.FECHA
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

    public List<Cancion> buscar(String termino) {
        List<Cancion> lista = new ArrayList<>();
        String sql = """
            SELECT C.ID_CANCION, C.NOMBRE, C.GENERO,
                   A.NOMBRE AS ARTISTA, C.FT, C.PORTADA,
                   C.EMOCION, C.DURACION_SEG, C.FECHA
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
            ps.setString(1,like); ps.setString(2,like);
            ps.setString(3,like); ps.setString(4,like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCancion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en CancionDAO.buscar: " + e.getMessage());
        }
        return lista;
    }

    public List<Cancion> buscarPorInicio(String termino) {
        List<Cancion> lista = new ArrayList<>();
        String sql = """
            SELECT C.ID_CANCION, C.NOMBRE, C.GENERO,
                   A.NOMBRE AS ARTISTA, C.FT, C.PORTADA,
                   C.EMOCION, C.DURACION_SEG, C.FECHA
            FROM CANCIONES C
            JOIN ARTISTAS_CANCIONES AC ON C.ID_CANCION = AC.ID_CANCION
            JOIN ARTISTAS A            ON AC.ID_ARTISTA = A.ID_ARTISTA
            WHERE UPPER(C.NOMBRE) LIKE UPPER(?)
            ORDER BY UPPER(C.NOMBRE)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, termino + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCancion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en CancionDAO.buscarPorInicio: " + e.getMessage());
        }
        return lista;
    }

    public List<Cancion> obtenerPorAlbum(String idAlbum) {
        List<Cancion> lista = new ArrayList<>();
        String sql = """
            SELECT C.ID_CANCION, C.NOMBRE, C.GENERO,
                   A.NOMBRE AS ARTISTA, C.FT, C.PORTADA,
                   C.EMOCION, C.DURACION_SEG, C.FECHA
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

    public List<Cancion> obtenerHistorialUsuario(String idUsuario) {
        List<Cancion> lista = new ArrayList<>();
        String sql = """
            SELECT C.ID_CANCION, C.NOMBRE, C.GENERO,
                   A.NOMBRE AS ARTISTA, C.FT, C.PORTADA,
                   C.EMOCION, C.DURACION_SEG, C.FECHA
            FROM CANCIONES C
            JOIN HISTORIAL_CANCIONES HC ON C.ID_CANCION   = HC.ID_CANCION
            JOIN HISTORIAL H            ON HC.ID_HISTORIAL = H.ID_HISTORIAL
            JOIN ARTISTAS_CANCIONES AC  ON C.ID_CANCION   = AC.ID_CANCION
            JOIN ARTISTAS A             ON AC.ID_ARTISTA  = A.ID_ARTISTA
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

    public List<Cancion> obtenerMeGustasUsuario(String idUsuario) {
        List<Cancion> lista = new ArrayList<>();
        String sql = """
            SELECT C.ID_CANCION, C.NOMBRE, C.GENERO,
                   A.NOMBRE AS ARTISTA, C.FT, C.PORTADA,
                   C.EMOCION, C.DURACION_SEG, C.FECHA
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

    public List<String[]> contarPorGenero() {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT GENERO, COUNT(*) AS TOTAL FROM CANCIONES GROUP BY GENERO ORDER BY TOTAL DESC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new String[]{ rs.getString("GENERO"), rs.getString("TOTAL") });
        } catch (SQLException e) {
            System.err.println("Error en contarPorGenero: " + e.getMessage());
        }
        return lista;
    }

    private Cancion mapearCancion(ResultSet rs) throws SQLException {
        Image portada = null;
        try {
            Blob blob = rs.getBlob("PORTADA");
            if (blob != null) portada = ImageIO.read(blob.getBinaryStream());
        } catch (Exception ignored) {}
        return new Cancion(
            rs.getString("ID_CANCION"), rs.getString("NOMBRE"),
            rs.getString("GENERO"),     rs.getString("ARTISTA"),
            rs.getString("FT"),         portada,
            rs.getString("EMOCION"),    rs.getInt("DURACION_SEG"),
            rs.getInt("FECHA"));
    }
}