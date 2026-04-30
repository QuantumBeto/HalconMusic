package com.halconmusic.dao;

import java.awt.Image;
import java.io.InputStream;
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

/**
 * DAO de Artistas — Todas las operaciones SQL relacionadas con ARTISTAS.
 * Usa JOIN, COUNT, GROUP BY, LIKE, UPPER/LOWER según sea necesario.
 */
public class ArtistaDAO {

    private final Connection con;

    public ArtistaDAO() {
        this.con = ConexionDB.getInstance().getConexion();
    }

    public List<Artista> obtenerTodos() {
        List<Artista> lista = new ArrayList<>();

        // ✅ Sin PORTADA en el SELECT principal — se obtiene aparte
        String sql = """
            SELECT A.ID_ARTISTA,
                A.NOMBRE,
                A.DESCRIPCION,
                A.GENEROPRINCIPAL,
                A.PAISDEORIGEN,
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
                // Obtiene el BLOB en query separado por ID
                Image portada = obtenerPortada(rs.getString("ID_ARTISTA"));
                lista.add(new Artista(
                    rs.getString("ID_ARTISTA"),
                    rs.getString("NOMBRE"),
                    portada,
                    rs.getString("DESCRIPCION"),
                    rs.getString("GENEROPRINCIPAL"),
                    rs.getString("PAISDEORIGEN"),
                    rs.getInt("TOTAL_CANCIONES")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error en ArtistaDAO.obtenerTodos: " + e.getMessage());
        }
        return lista;
    }

    public List<Artista> buscarPorNombre(String termino) {
        List<Artista> lista = new ArrayList<>();

        String sql = """
            SELECT A.ID_ARTISTA,
                A.NOMBRE,
                A.DESCRIPCION,
                A.GENEROPRINCIPAL,
                A.PAISDEORIGEN,
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
                        rs.getString("ID_ARTISTA"),
                        rs.getString("NOMBRE"),
                        portada,
                        rs.getString("DESCRIPCION"),
                        rs.getString("GENEROPRINCIPAL"),
                        rs.getString("PAISDEORIGEN"),
                        rs.getInt("TOTAL_CANCIONES")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en ArtistaDAO.buscarPorNombre: " + e.getMessage());
        }
        return lista;
    }

// ✅ Método auxiliar — obtiene solo el BLOB por ID
private Image obtenerPortada(String idArtista) {
    String sql = "SELECT PORTADA FROM ARTISTAS WHERE ID_ARTISTA = ?";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, idArtista);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                Blob blob = rs.getBlob("PORTADA");
                if (blob != null) {
                    return ImageIO.read(blob.getBinaryStream());
                }
            }
        }
    } catch (Exception e) {
        // Retorna null — la UI mostrará placeholder
    }
    return null;
}

    /**
     * Obtiene el conteo de géneros únicos entre todos los artistas.
     * SQL: COUNT + DISTINCT
     */
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

    /**
     * Obtiene el conteo de países únicos entre todos los artistas.
     * SQL: COUNT + DISTINCT
     */
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

    /**
     * Mapea un ResultSet a un objeto Artista.
     */
    private Artista mapearArtista(ResultSet rs) throws SQLException {
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

        return new Artista(
            rs.getString("ID_ARTISTA"),
            rs.getString("NOMBRE"),
            portada,
            rs.getString("DESCRIPCION"),
            rs.getString("GENEROPRINCIPAL"),
            rs.getString("PAISDEORIGEN"),
            rs.getInt("TOTAL_CANCIONES")
        );
    }
}
