package com.halconmusic.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.halconmusic.db.ConexionDB;

public class UsuarioDAO {

    private static final String COL_ID   = "ID_USUARIO";
    private static final String COL_PASS = "CONTRASENA";
    private static final String COL_NOM  = "NOMBRE";
    private static final String COL_TIPO = "TIPODEUSUARIO";

    private final Connection con;

    public UsuarioDAO() {
        this.con = ConexionDB.getInstance().getConexion();
    }

    /**
     * Valida credenciales.
     * Retorna String[] { ID_USUARIO, NOMBRE, TIPO_DISPLAY, TIPO_RAW }
     *   TIPO_RAW = "Premium" | "Gratis"  — se usa para lógica de rol en App y Sidebar.
     *   TIPO_DISPLAY = texto con símbolo para mostrar en el badge.
     */
    public String[] autenticar(String idUsuario, String contrasena) {
        String sql = "SELECT " + COL_ID + ", " + COL_NOM + ", " + COL_TIPO
                   + " FROM USUARIOS"
                   + " WHERE UPPER(" + COL_ID + ") = UPPER(?)"
                   + "   AND " + COL_PASS + " = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idUsuario.trim());
            ps.setString(2, contrasena);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String tipoRaw = rs.getString(COL_TIPO); // "Premium" o "Gratis"
                    String tipoDisplay = "Premium".equalsIgnoreCase(tipoRaw)
                            ? "Artista \u2756"   // ✦  — usuario creador
                            : "Oyente";
                    return new String[]{
                        rs.getString(COL_ID),
                        rs.getString(COL_NOM),
                        tipoDisplay,
                        tipoRaw          // [3] = tipo raw para lógica
                    };
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en UsuarioDAO.autenticar: " + e.getMessage());
        }
        return null;
    }
}