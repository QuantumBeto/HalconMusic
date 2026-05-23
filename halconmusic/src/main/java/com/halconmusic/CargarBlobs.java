package com.halconmusic;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class CargarBlobs {

    static final String BASE     = "D:\\Documentos\\TecNM\\HalconMusic\\halconmusic\\src\\recursos";

    // ── Conexión Oracle Cloud (igual que ConexionDB.java) ──
    static final String WALLET_PATH = "C:/SQLDeveloper/Wallet_HalconMusic";
    static final String SERVICIO    = "halconmusic_high";
    static final String URL         = "jdbc:oracle:thin:@" + SERVICIO;
    static final String USUARIO     = "ADMIN";
    static final String PASSWORD    = "Halcones2026";
    // ───────────────────────────────────────────────────────

    public static void main(String[] args) {
        // Apunta al wallet ANTES de abrir la conexión
        System.setProperty("oracle.net.tns_admin", WALLET_PATH);

        try (Connection con = DriverManager.getConnection(URL, USUARIO, PASSWORD)) {
            con.setAutoCommit(false);

            cargarVideosCanciones(con);
            // Agrega esto ANTES de con.commit()
            PreparedStatement check = con.prepareStatement(
                "SELECT ID_CANCION, DBMS_LOB.GETLENGTH(VIDEO) FROM CANCIONES WHERE ID_CANCION = 'C001'"
            );
            java.sql.ResultSet rs = check.executeQuery();
            if (rs.next()) {
                System.out.println("🔍 Verificación C001 en esta conexión: " + rs.getLong(2) + " bytes");
            }

            con.commit();
            System.out.println("✅ Todos los archivos cargados correctamente");

        } catch (Exception e) {
            System.err.println("❌ Error:");
            e.printStackTrace();
        }
    }

    static void cargarVideosCanciones(Connection con) throws Exception {
        // Paso 1: inicializar el BLOB en la fila
        String sqlInit = "UPDATE CANCIONES SET VIDEO = EMPTY_BLOB() WHERE ID_CANCION = ?";
        // Paso 2: bloquear la fila y obtener el BLOB para escribir
        String sqlSelect = "SELECT VIDEO FROM CANCIONES WHERE ID_CANCION = ? FOR UPDATE";

        try (PreparedStatement psInit   = con.prepareStatement(sqlInit);
            PreparedStatement psSelect = con.prepareStatement(sqlSelect)) {

            for (int i = 1; i <= 45; i++) { // cambia a 45 cuando funcione

                String idCancion = String.format("C%03d", i); // para el WHERE en la BD
                String idVideo   = String.format("V%03d", i); // para el nombre del archivo
                File   file      = new File(BASE + "\\videos\\" + idVideo + ".mp4");
                // Paso 1
                psInit.setString(1, idCancion);
                psInit.executeUpdate();
                // Paso 2
                psSelect.setString(1, idCancion);
                try (java.sql.ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        oracle.sql.BLOB blob = (oracle.sql.BLOB) rs.getBlob(1);
                        try (FileInputStream fis = new FileInputStream(file);
                            java.io.OutputStream os = blob.getBinaryOutputStream()) {
                            byte[] buffer = new byte[blob.getBufferSize()];
                            int bytesRead;
                            while ((bytesRead = fis.read(buffer)) != -1) {
                                os.write(buffer, 0, bytesRead);
                            }
                        }
                        System.out.println("🎬  Video cargado: " + idCancion + " ← " + idVideo + ".mp4 (" + file.length() + " bytes)");
                    }
                }
            }
        }
    }
}