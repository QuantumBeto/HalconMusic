package com.halconmusic.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton para manejar la conexión a Oracle XE.
 * Solo existe UNA instancia de conexión en toda la aplicación.
 */
public class ConexionDB {

    private static final String URL      = "jdbc:oracle:thin:@localhost:1521/XE";
    private static final String USUARIO  = "system";
    private static final String PASSWORD = "12345678"; // Cambia tu password aquí

    private static ConexionDB instancia;
    private Connection conexion;

    // Constructor privado — nadie puede crear instancias desde fuera
    private ConexionDB() {
        try {
            this.conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
            System.out.println("✅ Conexión a Oracle XE establecida.");
        } catch (SQLException e) {
            System.err.println("❌ Error al conectar con Oracle XE:");
            e.printStackTrace();
        }
    }

    /** Retorna la única instancia del singleton */
    public static ConexionDB getInstance() {
        if (instancia == null || !instancia.isConexionActiva()) {
            instancia = new ConexionDB();
        }
        return instancia;
    }

    /** Retorna el objeto Connection para ejecutar queries */
    public Connection getConexion() {
        return conexion;
    }

    /** Verifica si la conexión sigue activa */
    private boolean isConexionActiva() {
        try {
            return conexion != null && !conexion.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /** Cierra la conexión al terminar la app */
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
