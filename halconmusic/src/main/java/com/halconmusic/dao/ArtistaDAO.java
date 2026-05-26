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
import com.halconmusic.model.Artista;

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
                            String generoPrincipal, String paisDeOrigen,
                            java.io.File archivoPortada) {
        String idNuevo = generarNuevoId();
        try {
            con.setAutoCommit(false);

            // Paso 1: INSERT con EMPTY_BLOB()
            String sql = """
                INSERT INTO ARTISTAS (ID_ARTISTA, NOMBRE, PORTADA, DESCRIPCION, GENEROPRINCIPAL, PAISDEORIGEN)
                VALUES (?, ?, EMPTY_BLOB(), ?, ?, ?)
                """;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, idNuevo);
                ps.setString(2, nombre.trim());
                ps.setString(3, descripcion.trim());
                ps.setString(4, generoPrincipal.trim());
                ps.setString(5, paisDeOrigen.trim());
                ps.executeUpdate();
            }

            // Paso 2: escribir BLOB portada si se proporcionó
            if (archivoPortada != null) {
                String sqlBlob = "SELECT PORTADA FROM ARTISTAS WHERE ID_ARTISTA = ? FOR UPDATE";
                try (PreparedStatement ps = con.prepareStatement(sqlBlob)) {
                    ps.setString(1, idNuevo);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            oracle.sql.BLOB blob = (oracle.sql.BLOB) rs.getBlob("PORTADA");
                            java.io.OutputStream os = blob.getBinaryOutputStream();
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
            System.out.println("✅ Artista creado: " + idNuevo);
            return true;
        } catch (Exception e) {
            System.err.println("Error al insertar artista: " + e.getMessage());
            try { con.rollback(); con.setAutoCommit(true); } catch (Exception ignored) {}
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