package com.halconmusic.dao;

import java.awt.Image;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.halconmusic.db.ConexionDB;
import com.halconmusic.model.Cancion;
import com.halconmusic.model.ResumenSemanal;

public class ResumenDAO {

    private final Connection con;

    public ResumenDAO() {
        this.con = ConexionDB.getInstance().getConexion();
    }

    // ── RESUMEN USUARIO ───────────────────────────────────

    public ResumenSemanal obtenerUltimoDeUsuario(String idUsuario) {
        String sql = """
            SELECT R.ID_RESUMEN, R.FECHA, R.GENEROPRINCIPAL,
                   R.CANCIONESPRINCIPALES, R.ARTISTAPRINCIPAL,
                   R.EMOCION, R.ID_USUARIO, R.ID_HISTORIAL
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
            System.err.println("Error en obtenerUltimoDeUsuario: " + e.getMessage());
        }
        return null;
    }

    public List<ResumenSemanal> obtenerTodosDeUsuario(String idUsuario) {
        List<ResumenSemanal> lista = new ArrayList<>();
        String sql = """
            SELECT R.ID_RESUMEN, R.FECHA, R.GENEROPRINCIPAL,
                   R.CANCIONESPRINCIPALES, R.ARTISTAPRINCIPAL,
                   R.EMOCION, R.ID_USUARIO, R.ID_HISTORIAL
            FROM RESUMENES_SEMANALES R
            WHERE R.ID_USUARIO = ?
            ORDER BY R.FECHA DESC
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearResumen(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en obtenerTodosDeUsuario: " + e.getMessage());
        }
        return lista;
    }

    // ── REQ. 9 — RESUMEN USUARIO CON RANGO DE FECHAS ─────

    /** Género más escuchado por el usuario en el rango de fechas dado. */
    public List<String[]> obtenerGenerosMasEscuchados(String idUsuario) {
        return obtenerGenerosMasEscuchadosConFecha(idUsuario, null, null);
    }

    public List<String[]> obtenerGenerosMasEscuchadosConFecha(
            String idUsuario, Date desde, Date hasta) {
        List<String[]> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT C.GENERO, COUNT(C.ID_CANCION) AS TOTAL
            FROM CANCIONES C
            JOIN HISTORIAL_CANCIONES HC ON C.ID_CANCION   = HC.ID_CANCION
            JOIN HISTORIAL H            ON HC.ID_HISTORIAL = H.ID_HISTORIAL
            WHERE H.ID_USUARIO = ?
            """);
        if (desde != null) sql.append(" AND H.ROWID IN (SELECT ROWID FROM HISTORIAL WHERE ID_USUARIO = ?) ");
        // Nota: HISTORIAL no tiene fecha; filtramos por RESUMENES_SEMANALES vinculado
        // Usamos fecha de RESUMENES_SEMANALES si existe
        sql.append(" GROUP BY C.GENERO ORDER BY TOTAL DESC");

        // Versión con fecha: filtra canciones del historial cuyo resumen cae en el rango
        if (desde != null && hasta != null) {
            lista.clear();
            String sqlFecha = """
                SELECT C.GENERO, COUNT(C.ID_CANCION) AS TOTAL
                FROM CANCIONES C
                JOIN HISTORIAL_CANCIONES HC ON C.ID_CANCION   = HC.ID_CANCION
                JOIN HISTORIAL H            ON HC.ID_HISTORIAL = H.ID_HISTORIAL
                JOIN RESUMENES_SEMANALES RS ON RS.ID_HISTORIAL = H.ID_HISTORIAL
                WHERE H.ID_USUARIO = ?
                  AND RS.FECHA BETWEEN ? AND ?
                GROUP BY C.GENERO
                ORDER BY TOTAL DESC
                """;
            try (PreparedStatement ps = con.prepareStatement(sqlFecha)) {
                ps.setString(1, idUsuario);
                ps.setDate  (2, desde);
                ps.setDate  (3, hasta);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        lista.add(new String[]{ rs.getString("GENERO"), rs.getString("TOTAL") });
                }
            } catch (SQLException e) {
                System.err.println("Error en obtenerGenerosFecha: " + e.getMessage());
            }
            return lista;
        }

        // Sin filtro de fecha
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT C.GENERO, COUNT(C.ID_CANCION) AS TOTAL " +
                "FROM CANCIONES C " +
                "JOIN HISTORIAL_CANCIONES HC ON C.ID_CANCION = HC.ID_CANCION " +
                "JOIN HISTORIAL H ON HC.ID_HISTORIAL = H.ID_HISTORIAL " +
                "WHERE H.ID_USUARIO = ? " +
                "GROUP BY C.GENERO ORDER BY TOTAL DESC")) {
            ps.setString(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    lista.add(new String[]{ rs.getString("GENERO"), rs.getString("TOTAL") });
            }
        } catch (SQLException e) {
            System.err.println("Error en obtenerGenerosMasEscuchados: " + e.getMessage());
        }
        return lista;
    }

    /** Emoción más escuchada por el usuario, con rango de fechas opcional. */
    public List<String[]> obtenerEmocionesMasEscuchadas(
            String idUsuario, Date desde, Date hasta) {
        List<String[]> lista = new ArrayList<>();
        String sqlBase;
        if (desde != null && hasta != null) {
            sqlBase = """
                SELECT C.EMOCION, COUNT(C.ID_CANCION) AS TOTAL
                FROM CANCIONES C
                JOIN HISTORIAL_CANCIONES HC ON C.ID_CANCION   = HC.ID_CANCION
                JOIN HISTORIAL H            ON HC.ID_HISTORIAL = H.ID_HISTORIAL
                JOIN RESUMENES_SEMANALES RS ON RS.ID_HISTORIAL = H.ID_HISTORIAL
                WHERE H.ID_USUARIO = ?
                  AND RS.FECHA BETWEEN ? AND ?
                GROUP BY C.EMOCION
                ORDER BY TOTAL DESC
                """;
            try (PreparedStatement ps = con.prepareStatement(sqlBase)) {
                ps.setString(1, idUsuario);
                ps.setDate  (2, desde);
                ps.setDate  (3, hasta);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        lista.add(new String[]{ rs.getString("EMOCION"), rs.getString("TOTAL") });
                }
            } catch (SQLException e) {
                System.err.println("Error en obtenerEmociones: " + e.getMessage());
            }
        } else {
            sqlBase = """
                SELECT C.EMOCION, COUNT(C.ID_CANCION) AS TOTAL
                FROM CANCIONES C
                JOIN HISTORIAL_CANCIONES HC ON C.ID_CANCION   = HC.ID_CANCION
                JOIN HISTORIAL H            ON HC.ID_HISTORIAL = H.ID_HISTORIAL
                WHERE H.ID_USUARIO = ?
                GROUP BY C.EMOCION
                ORDER BY TOTAL DESC
                """;
            try (PreparedStatement ps = con.prepareStatement(sqlBase)) {
                ps.setString(1, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        lista.add(new String[]{ rs.getString("EMOCION"), rs.getString("TOTAL") });
                }
            } catch (SQLException e) {
                System.err.println("Error en obtenerEmociones: " + e.getMessage());
            }
        }
        return lista;
    }

    /** Canciones escuchadas por el usuario en el rango dado. */
    public List<Cancion> obtenerCancionesEnRango(
            String idUsuario, Date desde, Date hasta) {
        List<Cancion> lista = new ArrayList<>();
        String sql = """
        SELECT ID_CANCION, NOMBRE, GENERO, ARTISTA, FT, PORTADA, EMOCION, DURACION_SEG, FECHA
        FROM (
            SELECT DISTINCT C.ID_CANCION, C.NOMBRE, C.GENERO,
                A.NOMBRE AS ARTISTA, C.FT, C.PORTADA,
                C.EMOCION, C.DURACION_SEG, C.FECHA
            FROM CANCIONES C
            JOIN HISTORIAL_CANCIONES HC ON C.ID_CANCION   = HC.ID_CANCION
            JOIN HISTORIAL H            ON HC.ID_HISTORIAL = H.ID_HISTORIAL
            JOIN ARTISTAS_CANCIONES AC  ON C.ID_CANCION   = AC.ID_CANCION
            JOIN ARTISTAS A             ON AC.ID_ARTISTA  = A.ID_ARTISTA
            JOIN RESUMENES_SEMANALES RS ON RS.ID_HISTORIAL = H.ID_HISTORIAL
            WHERE H.ID_USUARIO = ?
            AND RS.FECHA BETWEEN ? AND ?
        )
        ORDER BY UPPER(NOMBRE)
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idUsuario);
            ps.setDate  (2, desde);
            ps.setDate  (3, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCancion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en obtenerCancionesEnRango: " + e.getMessage());
        }
        return lista;
    }

    // ── REQ. 10 — RESUMEN GLOBAL (TODOS LOS USUARIOS) ────

    public List<String[]> obtenerGenerosMasEscuchadosGlobal(Date desde, Date hasta) {
        List<String[]> lista = new ArrayList<>();
        String sql = (desde != null && hasta != null) ? """
            SELECT C.GENERO, COUNT(C.ID_CANCION) AS TOTAL
            FROM CANCIONES C
            JOIN HISTORIAL_CANCIONES HC ON C.ID_CANCION   = HC.ID_CANCION
            JOIN HISTORIAL H            ON HC.ID_HISTORIAL = H.ID_HISTORIAL
            JOIN RESUMENES_SEMANALES RS ON RS.ID_HISTORIAL = H.ID_HISTORIAL
            WHERE RS.FECHA BETWEEN ? AND ?
            GROUP BY C.GENERO
            ORDER BY TOTAL DESC
            """ : """
            SELECT C.GENERO, COUNT(C.ID_CANCION) AS TOTAL
            FROM CANCIONES C
            JOIN HISTORIAL_CANCIONES HC ON C.ID_CANCION   = HC.ID_CANCION
            JOIN HISTORIAL H            ON HC.ID_HISTORIAL = H.ID_HISTORIAL
            GROUP BY C.GENERO
            ORDER BY TOTAL DESC
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (desde != null && hasta != null) { ps.setDate(1, desde); ps.setDate(2, hasta); }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    lista.add(new String[]{ rs.getString("GENERO"), rs.getString("TOTAL") });
            }
        } catch (SQLException e) {
            System.err.println("Error en generoGlobal: " + e.getMessage());
        }
        return lista;
    }

    public List<String[]> obtenerEmocionesMasEscuchadasGlobal(Date desde, Date hasta) {
        List<String[]> lista = new ArrayList<>();
        String sql = (desde != null && hasta != null) ? """
            SELECT C.EMOCION, COUNT(C.ID_CANCION) AS TOTAL
            FROM CANCIONES C
            JOIN HISTORIAL_CANCIONES HC ON C.ID_CANCION   = HC.ID_CANCION
            JOIN HISTORIAL H            ON HC.ID_HISTORIAL = H.ID_HISTORIAL
            JOIN RESUMENES_SEMANALES RS ON RS.ID_HISTORIAL = H.ID_HISTORIAL
            WHERE RS.FECHA BETWEEN ? AND ?
            GROUP BY C.EMOCION
            ORDER BY TOTAL DESC
            """ : """
            SELECT C.EMOCION, COUNT(C.ID_CANCION) AS TOTAL
            FROM CANCIONES C
            JOIN HISTORIAL_CANCIONES HC ON C.ID_CANCION   = HC.ID_CANCION
            JOIN HISTORIAL H            ON HC.ID_HISTORIAL = H.ID_HISTORIAL
            GROUP BY C.EMOCION
            ORDER BY TOTAL DESC
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (desde != null && hasta != null) { ps.setDate(1, desde); ps.setDate(2, hasta); }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    lista.add(new String[]{ rs.getString("EMOCION"), rs.getString("TOTAL") });
            }
        } catch (SQLException e) {
            System.err.println("Error en emocionGlobal: " + e.getMessage());
        }
        return lista;
    }

    public List<Cancion> obtenerCancionesGlobalEnRango(Date desde, Date hasta) {
        List<Cancion> lista = new ArrayList<>();
        String sql = (desde != null && hasta != null) ? """
            SELECT ID_CANCION, NOMBRE, GENERO, ARTISTA, FT, PORTADA, EMOCION, DURACION_SEG, FECHA
            FROM (
                SELECT DISTINCT C.ID_CANCION, C.NOMBRE, C.GENERO,
                    A.NOMBRE AS ARTISTA, C.FT, C.PORTADA,
                    C.EMOCION, C.DURACION_SEG, C.FECHA
                FROM CANCIONES C
                JOIN HISTORIAL_CANCIONES HC ON C.ID_CANCION   = HC.ID_CANCION
                JOIN HISTORIAL H            ON HC.ID_HISTORIAL = H.ID_HISTORIAL
                JOIN ARTISTAS_CANCIONES AC  ON C.ID_CANCION   = AC.ID_CANCION
                JOIN ARTISTAS A             ON AC.ID_ARTISTA  = A.ID_ARTISTA
                JOIN RESUMENES_SEMANALES RS ON RS.ID_HISTORIAL = H.ID_HISTORIAL
                WHERE RS.FECHA BETWEEN ? AND ?
            )
            ORDER BY UPPER(NOMBRE)
            """ : """
            SELECT ID_CANCION, NOMBRE, GENERO, ARTISTA, FT, PORTADA, EMOCION, DURACION_SEG, FECHA
            FROM (
                SELECT DISTINCT C.ID_CANCION, C.NOMBRE, C.GENERO,
                    A.NOMBRE AS ARTISTA, C.FT, C.PORTADA,
                    C.EMOCION, C.DURACION_SEG, C.FECHA
                FROM CANCIONES C
                JOIN HISTORIAL_CANCIONES HC ON C.ID_CANCION   = HC.ID_CANCION
                JOIN HISTORIAL H            ON HC.ID_HISTORIAL = H.ID_HISTORIAL
                JOIN ARTISTAS_CANCIONES AC  ON C.ID_CANCION   = AC.ID_CANCION
                JOIN ARTISTAS A             ON AC.ID_ARTISTA  = A.ID_ARTISTA
            )
            ORDER BY UPPER(NOMBRE)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (desde != null && hasta != null) { ps.setDate(1, desde); ps.setDate(2, hasta); }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCancion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en cancionesGlobal: " + e.getMessage());
        }
        return lista;
    }

    // ── HISTORIAL GENERAL (REQ. 8) ────────────────────────
    /** Retorna canciones del historial de TODOS los usuarios. */
    public List<Cancion> obtenerHistorialGeneral() {
        List<Cancion> lista = new ArrayList<>();
        String sql = """
            SELECT ID_CANCION, NOMBRE, GENERO, ARTISTA, FT, PORTADA, EMOCION, DURACION_SEG, FECHA
            FROM (
                SELECT DISTINCT C.ID_CANCION, C.NOMBRE, C.GENERO,
                    A.NOMBRE AS ARTISTA, C.FT, C.PORTADA,
                    C.EMOCION, C.DURACION_SEG, C.FECHA
                FROM CANCIONES C
                JOIN HISTORIAL_CANCIONES HC ON C.ID_CANCION   = HC.ID_CANCION
                JOIN ARTISTAS_CANCIONES AC  ON C.ID_CANCION   = AC.ID_CANCION
                JOIN ARTISTAS A             ON AC.ID_ARTISTA  = A.ID_ARTISTA
            )
             BY UPPER(NOMBRE)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearCancion(rs));
        } catch (SQLException e) {
            System.err.println("Error en obtenerHistorialGeneral: " + e.getMessage());
        }
        return lista;
    }

    // ── MAPPERS ───────────────────────────────────────────
    private ResumenSemanal mapearResumen(ResultSet rs) throws SQLException {
        return new ResumenSemanal(
            rs.getString("ID_RESUMEN"), rs.getDate("FECHA"),
            rs.getString("GENEROPRINCIPAL"), rs.getString("CANCIONESPRINCIPALES"),
            rs.getString("ARTISTAPRINCIPAL"), rs.getString("EMOCION"),
            rs.getString("ID_USUARIO"), rs.getString("ID_HISTORIAL"));
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