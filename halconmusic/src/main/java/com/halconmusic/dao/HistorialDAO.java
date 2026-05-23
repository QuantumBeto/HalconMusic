package com.halconmusic.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.halconmusic.db.ConexionDB;

/**
 * DAO para registrar canciones en el Historial y en MeGustas automáticamente.
 *
 * Tablas involucradas:
 *   HISTORIAL (ID_HISTORIAL, ID_USUARIO)
 *   HISTORIAL_CANCIONES (ID_HISTORIAL, ID_CANCION)
 *   MEGUSTAS (ID_MEGUSTAS, ID_USUARIO)
 *   MEGUSTAS_CANCIONES (ID_MEGUSTAS, ID_CANCION)
 */
public class HistorialDAO {

    private final Connection con;

    public HistorialDAO() {
        this.con = ConexionDB.getInstance().getConexion();
    }

    // ──────────────────────────────────────────────────────
    //  HISTORIAL
    // ──────────────────────────────────────────────────────

    /**
     * Registra una canción reproducida en el historial del usuario.
     * Si el usuario aún no tiene un registro en HISTORIAL, lo crea primero.
     * Luego inserta la canción en HISTORIAL_CANCIONES.
     *
     * Llamar desde App.reproducir() cada vez que el usuario inicia una canción.
     */
    public void registrarEnHistorial(String idUsuario, String idCancion) {
        String idHistorial = obtenerOCrearHistorial(idUsuario);
        if (idHistorial == null) return;

        // Evita duplicados en la misma sesión (opcional: quitar si se quieren repetidos)
        if (yaExisteEnHistorial(idHistorial, idCancion)) return;

        String sql = "INSERT INTO HISTORIAL_CANCIONES (ID_HISTORIAL, ID_CANCION, FECHA_REPRODUCCION) VALUES (?, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idHistorial);
            ps.setString(2, idCancion);
            ps.executeUpdate();
            System.out.println("✅ Canción agregada al historial: " + idCancion);
        } catch (SQLException e) {
            System.err.println("Error al registrar en historial: " + e.getMessage());
        }
    }

    /**
     * Obtiene el ID_HISTORIAL del usuario. Si no existe, crea uno nuevo.
     */
    private String obtenerOCrearHistorial(String idUsuario) {
        // Buscar historial existente
        String sqlSelect = "SELECT ID_HISTORIAL FROM HISTORIAL WHERE ID_USUARIO = ? FETCH FIRST 1 ROWS ONLY";
        try (PreparedStatement ps = con.prepareStatement(sqlSelect)) {
            ps.setString(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("ID_HISTORIAL");
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar historial: " + e.getMessage());
            return null;
        }

        // No existe → crear uno nuevo con ID generado
        String nuevoId = "HIS_" + idUsuario + "_" + System.currentTimeMillis();
        String sqlInsert = "INSERT INTO HISTORIAL (ID_HISTORIAL, ID_USUARIO) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
            ps.setString(1, nuevoId);
            ps.setString(2, idUsuario);
            ps.executeUpdate();
            System.out.println("✅ Historial creado para usuario: " + idUsuario);
            return nuevoId;
        } catch (SQLException e) {
            System.err.println("Error al crear historial: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verifica si la canción ya está en el historial para no duplicar.
     */
    private boolean yaExisteEnHistorial(String idHistorial, String idCancion) {
        String sql = "SELECT 1 FROM HISTORIAL_CANCIONES WHERE ID_HISTORIAL = ? AND ID_CANCION = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idHistorial);
            ps.setString(2, idCancion);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // ──────────────────────────────────────────────────────
    //  ME GUSTA
    // ──────────────────────────────────────────────────────

    /**
     * Agrega una canción a la tabla ME_GUSTA del usuario.
     * Si el usuario no tiene registro en MEGUSTAS, lo crea primero.
     * No agrega duplicados.
     *
     * Llamar desde SongRow cuando el usuario presiona el botón ♥.
     *
     * @return true si se agregó, false si ya existía (ya era favorita).
     */
    public boolean agregarMeGusta(String idUsuario, String idCancion) {
        String idMeGustas = obtenerOCrearMeGustas(idUsuario);
        if (idMeGustas == null) return false;

        if (yaExisteEnMeGustas(idMeGustas, idCancion)) {
            System.out.println("ℹ️ La canción ya estaba en Me Gusta: " + idCancion);
            return false;
        }

        String sql = "INSERT INTO MEGUSTAS_CANCIONES (ID_MEGUSTAS, ID_CANCION) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idMeGustas);
            ps.setString(2, idCancion);
            ps.executeUpdate();
            System.out.println("✅ Canción agregada a Me Gusta: " + idCancion);
            return true;
        } catch (SQLException e) {
            System.err.println("Error al agregar Me Gusta: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si la canción ya está en Me Gusta del usuario.
     */
    public boolean esMeGusta(String idUsuario, String idCancion) {
        String idMeGustas = obtenerIdMeGustas(idUsuario);
        if (idMeGustas == null) return false;
        return yaExisteEnMeGustas(idMeGustas, idCancion);
    }

    /**
     * Obtiene el ID_MEGUSTAS del usuario. Si no existe, lo crea.
     */
    private String obtenerOCrearMeGustas(String idUsuario) {
        String id = obtenerIdMeGustas(idUsuario);
        if (id != null) return id;

        String nuevoId = "MG_" + idUsuario + "_" + System.currentTimeMillis();
        String sqlInsert = "INSERT INTO MEGUSTAS (ID_MEGUSTAS, ID_USUARIO) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
            ps.setString(1, nuevoId);
            ps.setString(2, idUsuario);
            ps.executeUpdate();
            System.out.println("✅ Registro MeGustas creado para usuario: " + idUsuario);
            return nuevoId;
        } catch (SQLException e) {
            System.err.println("Error al crear MeGustas: " + e.getMessage());
            return null;
        }
    }

    /**
     * Busca el ID_MEGUSTAS existente del usuario (sin crear).
     */
    private String obtenerIdMeGustas(String idUsuario) {
        String sql = "SELECT ID_MEGUSTAS FROM MEGUSTAS WHERE ID_USUARIO = ? FETCH FIRST 1 ROWS ONLY";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("ID_MEGUSTAS");
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar MeGustas: " + e.getMessage());
        }
        return null;
    }

    /**
     * Verifica si la canción ya está en la lista de Me Gusta.
     */
    private boolean yaExisteEnMeGustas(String idMeGustas, String idCancion) {
        String sql = "SELECT 1 FROM MEGUSTAS_CANCIONES WHERE ID_MEGUSTAS = ? AND ID_CANCION = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idMeGustas);
            ps.setString(2, idCancion);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}