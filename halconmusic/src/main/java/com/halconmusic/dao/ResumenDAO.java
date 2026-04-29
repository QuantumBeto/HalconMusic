package com.halconmusic.dao;

import com.halconmusic.db.ConexionDB;
import com.halconmusic.model.ResumenSemanal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de Resúmenes Semanales.
 * Usa JOIN con USUARIOS, ORDER BY fecha.
 */
public class ResumenDAO {

    private final Connection con;

    public ResumenDAO() {
        this.con = ConexionDB.getInstance().getConexion();
    }

    /**
     * Obtiene el resumen más reciente de un usuario.
     * SQL: JOIN RESUMENES + USUARIOS + ORDER BY + ROWNUM
     */
    public ResumenSemanal obtenerUltimoDeUsuario(String idUsuario) {
        String sql = """
            SELECT R.ID_RESUMEN,
                   R.FECHA,
                   R.GENEROPRINCIPAL,
                   R.CANCIONESPPRINCIPALES,
                   R.ARTISTAPRINCIPAL,
                   R.EMOCION,
                   R.ID_USUARIO,
                   R.ID_HISTORIAL
            FROM RESUMENES_SEMANALES R
            WHERE R.ID_USUARIO = ?
            ORDER BY R.FECHA DESC
            FETCH FIRST 1 ROWS ONLY
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearResumen(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error en ResumenDAO.obtenerUltimoDeUsuario: " + e.getMessage());
        }

        return null;
    }

    /**
     * Obtiene todos los resúmenes de un usuario.
     * SQL: WHERE + ORDER BY fecha DESC
     */
    public List<ResumenSemanal> obtenerTodosDeUsuario(String idUsuario) {
        List<ResumenSemanal> lista = new ArrayList<>();

        String sql = """
            SELECT R.ID_RESUMEN,
                   R.FECHA,
                   R.GENEROPRINCIPAL,
                   R.CANCIONESPPRINCIPALES,
                   R.ARTISTAPRINCIPAL,
                   R.EMOCION,
                   R.ID_USUARIO,
                   R.ID_HISTORIAL
            FROM RESUMENES_SEMANALES R
            JOIN USUARIOS U ON R.ID_USUARIO = U.ID_USUARIO
            WHERE R.ID_USUARIO = ?
            ORDER BY R.FECHA DESC
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearResumen(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en ResumenDAO.obtenerTodosDeUsuario: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Obtiene estadísticas de géneros más escuchados.
     * SQL: GROUP BY + COUNT + ORDER BY
     */
    public List<String[]> obtenerGenerosMasEscuchados(String idUsuario) {
        List<String[]> lista = new ArrayList<>();

        String sql = """
            SELECT C.GENERO,
                   COUNT(C.ID_CANCION) AS TOTAL
            FROM CANCIONES C
            JOIN HISTORIAL_CANCIONES HC ON C.ID_CANCION   = HC.ID_CANCION
            JOIN HISTORIAL H            ON HC.ID_HISTORIAL = H.ID_HISTORIAL
            WHERE H.ID_USUARIO = ?
            GROUP BY C.GENERO
            ORDER BY TOTAL DESC
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new String[]{ rs.getString("GENERO"), rs.getString("TOTAL") });
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en obtenerGenerosMasEscuchados: " + e.getMessage());
        }

        return lista;
    }

    private ResumenSemanal mapearResumen(ResultSet rs) throws SQLException {
        return new ResumenSemanal(
            rs.getString("ID_RESUMEN"),
            rs.getDate("FECHA"),
            rs.getString("GENEROPRINCIPAL"),
            rs.getString("CANCIONESPPRINCIPALES"),
            rs.getString("ARTISTAPRINCIPAL"),
            rs.getString("EMOCION"),
            rs.getString("ID_USUARIO"),
            rs.getString("ID_HISTORIAL")
        );
    }
}
