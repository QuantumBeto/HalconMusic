package com.halconmusic.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.halconmusic.db.ConexionDB;

/**
 * DAO para autenticar usuarios contra la tabla USUARIOS de Oracle Cloud.
 *
 * Columnas esperadas en USUARIOS:
 *   ID_USUARIO       VARCHAR2  — clave primaria (ej. "US001")
 *   NOMBRE           VARCHAR2  — nombre para mostrar
 *   CONTRASENA       VARCHAR2  — contraseña en texto plano / hash
 *   TIPO_SUSCRIPCION VARCHAR2  — "PREMIUM" | "NORMAL"
 *
 * Si tu tabla usa otros nombres de columna cambia solo las constantes de abajo.
 */
public class UsuarioDAO {

    // ── Ajusta estos nombres si difieren en tu BD ─────────
    private static final String COL_ID    = "ID_USUARIO";
    private static final String COL_PASS  = "CONTRASENA";   // o "PASSWORD"
    private static final String COL_NOM   = "NOMBRE";
    private static final String COL_TIPO  = "TIPODEUSUARIO";
    // ─────────────────────────────────────────────────────

    private final Connection con;

    public UsuarioDAO() {
        this.con = ConexionDB.getInstance().getConexion();
    }

    /**
     * Valida credenciales.
     * @return String[] { ID_USUARIO, NOMBRE, TIPO_SUSCRIPCION } o null si falla.
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
                    String tipo = rs.getString(COL_TIPO);
                    // Normaliza para mostrarlo con símbolo en la UI
                    String tipoDisplay = (tipo != null && tipo.toUpperCase().contains("PREMIUM"))
                            ? "Premium \u2756"   // ✦
                            : "Normal";
                    return new String[]{
                        rs.getString(COL_ID),
                        rs.getString(COL_NOM),
                        tipoDisplay
                    };
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en UsuarioDAO.autenticar: " + e.getMessage());
        }
        return null;
    }
}