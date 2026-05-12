package com.halconmusic.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    // ── Cambia estos 3 valores ────────────────────────────
    private static final String WALLET_PATH = "C:/SQLDeveloper/Wallet_HalconMusic"; // Ruta de tu wallet
    private static final String SERVICIO = "halconmusic_high"; // Del tnsnames.ora
    private static final String PASSWORD = "Halcones2026"; // Password de ADMIN
    // ─────────────────────────────────────────────────────

    private static final String USUARIO = "ADMIN";
    private static final String URL = "jdbc:oracle:thin:@" + SERVICIO;

    private static ConexionDB instancia;
    private Connection conexion;

    private ConexionDB() {
        try {
            // Apunta al wallet para la conexión segura
            System.setProperty("oracle.net.tns_admin", WALLET_PATH);

            this.conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
            System.out.println("✅ Conexión a Oracle Cloud establecida.");
        } catch (SQLException e) {
            System.err.println("❌ Error al conectar con Oracle Cloud:");
            e.printStackTrace();
        }
    }

    public static ConexionDB getInstance() {
        if (instancia == null || !instancia.isConexionActiva()) {
            instancia = new ConexionDB();
        }
        return instancia;
    }

    public Connection getConexion() {
        return conexion;
    }

    private boolean isConexionActiva() {
        try {
            return conexion != null && !conexion.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("Conexión cerrada.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}