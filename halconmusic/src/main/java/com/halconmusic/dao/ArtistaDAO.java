package com.halconmusic.dao;

import com.halconmusic.db.ConexionDB;
import com.halconmusic.model.Artista;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArtistaDAO {

    private final Connection con;

    public ArtistaDAO() {
        this.con = ConexionDB.getInstance().getConexion();
    }

    // ── REQ. 2 — CREAR ARTISTA ────────────────────────────
    /**
     * Inserta un nuevo artista en la tabla ARTISTAS.
     * @return true si se insertó correctamente.
     */
    public boolean insertar(String nombre, String descripcion,
                            String generoPrincipal, String paisDeOrigen) {
        // Genera ID secuencial basado en el máximo actual
        String idNuevo = generarNuevoId();
        String sql = """
            INSERT INTO ARTISTAS (ID_ARTISTA, NOMBRE, DESCRIPCION, GENEROPRINCIPAL, PAISDEORIGEN)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idNuevo);
            ps.setString(2, nombre.trim());
            ps.setString(3, descripcion.trim());
            ps.setString(4, generoPrincipal.trim());
            ps.setString(5, paisDeOrigen.trim());
            ps.executeUpdate();
            System.out.println("✅ Artista creado: " + idNuevo);
            return true;
        } catch (SQLException e) {
            System.err.println("Error al insertar artista: " + e.getMessage());
            return false;
        }
    }

    /** Genera ID tipo ART010, ART011, ... basado en el máximo existente. */
    private String generarNuevoId() {
        String sql = "SELECT NVL(MAX(TO_NUMBER(SUBSTR(ID_ARTISTA,4))),0) + 1 AS NEXT_ID FROM ARTISTAS";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return String.format("ART%03d", rs.getInt("NEXT_ID"));
            }
        } catch (SQLException e) {
            System.err.println("Error generando ID artista: " + e.getMessage());
        }
        return "ART" + System.currentTimeMillis();
    }

    // ── CONSULTAS EXISTENTES ──────────────────────────────
    public List<Artista> obtenerTodos() {
        List<Artista> lista = new ArrayList<>();
        String sql = """
            SELECT A.ID_ARTISTA, A.NOMBRE, A.DESCRIPCION,
                   A.GENEROPRINCIPAL, A.PAISDEORIGEN,
                   COUNT(AC.ID_CANCION) AS TOTAL_CANCIONES
            FROM ARTISTAS A
            LEFT JOIN ARTISTAS_CANCIONES AC ON A.ID_ARTISTA = AC.ID_ARTISTA
            GROUP BY A.ID_ARTISTA, A.NOMBRE, A.DESCRIPCION,
                     A.GENEROPRINCIPAL, A.PAISDEORIGEN
            ORDER BY UPPER(A.NOMBRE)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Image portada = obtenerPortada(rs.getString("ID_ARTISTA"));
                lista.add(new Artista(
                    rs.getString("ID_ARTISTA"), rs.getString("NOMBRE"), portada,
                    rs.getString("DESCRIPCION"), rs.getString("GENEROPRINCIPAL"),
                    rs.getString("PAISDEORIGEN"), rs.getInt("TOTAL_CANCIONES")));
            }
        } catch (SQLException e) {
            System.err.println("Error en ArtistaDAO.obtenerTodos: " + e.getMessage());
        }
        return lista;
    }

    public List<Artista> buscarPorNombre(String termino) {
        List<Artista> lista = new ArrayList<>();
        String sql = """
            SELECT A.ID_ARTISTA, A.NOMBRE, A.DESCRIPCION,
                   A.GENEROPRINCIPAL, A.PAISDEORIGEN,
                   COUNT(AC.ID_CANCION) AS TOTAL_CANCIONES
            FROM ARTISTAS A
            LEFT JOIN ARTISTAS_CANCIONES AC ON A.ID_ARTISTA = AC.ID_ARTISTA
            WHERE UPPER(A.NOMBRE) LIKE UPPER(?)
            GROUP BY A.ID_ARTISTA, A.NOMBRE, A.DESCRIPCION,
                     A.GENEROPRINCIPAL, A.PAISDEORIGEN
            ORDER BY UPPER(A.NOMBRE)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + termino + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Image portada = obtenerPortada(rs.getString("ID_ARTISTA"));
                    lista.add(new Artista(
                        rs.getString("ID_ARTISTA"), rs.getString("NOMBRE"), portada,
                        rs.getString("DESCRIPCION"), rs.getString("GENEROPRINCIPAL"),
                        rs.getString("PAISDEORIGEN"), rs.getInt("TOTAL_CANCIONES")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en ArtistaDAO.buscarPorNombre: " + e.getMessage());
        }
        return lista;
    }

    /** Retorna lista de IDs y nombres para el combo de CrearAlbumPanel / CrearCancionPanel. */
    public List<String[]> obtenerIdsYNombres() {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT ID_ARTISTA, NOMBRE FROM ARTISTAS ORDER BY UPPER(NOMBRE)";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{ rs.getString("ID_ARTISTA"), rs.getString("NOMBRE") });
            }
        } catch (SQLException e) {
            System.err.println("Error en obtenerIdsYNombres: " + e.getMessage());
        }
        return lista;
    }

    public int contarGenerosDistintos() {
        String sql = "SELECT COUNT(DISTINCT UPPER(GENEROPRINCIPAL)) FROM ARTISTAS";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error en contarGenerosDistintos: " + e.getMessage());
        }
        return 0;
    }

    public int contarPaisesDistintos() {
        String sql = "SELECT COUNT(DISTINCT UPPER(PAISDEORIGEN)) FROM ARTISTAS";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error en contarPaisesDistintos: " + e.getMessage());
        }
        return 0;
    }

    private Image obtenerPortada(String idArtista) {
        String sql = "SELECT PORTADA FROM ARTISTAS WHERE ID_ARTISTA = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idArtista);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Blob blob = rs.getBlob("PORTADA");
                    if (blob != null) return ImageIO.read(blob.getBinaryStream());
                }
            }
        } catch (Exception e) { /* retorna null */ }
        return null;
    }
}