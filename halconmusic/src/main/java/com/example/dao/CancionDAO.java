package com.example.dao;

import com.example.model.Cancion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CancionDAO {
    // Asegúrate de tener tu clase de conexión configurada
    private static final String URL = "jdbc:oracle:thin:@localhost:1521/xe";
    private static final String USER = "system";
    private static final String PASS = "12345678";

    // Innovación: Filtramos por el estado de ánimo (Emocion)
    public List<Cancion> obtenerCancionesPorEmocion(String emocion) {
        List<Cancion> lista = new ArrayList<>();
        // No traemos el archivo de audio aún, solo la metadata y la imagen para no saturar la RAM
        String sql = "SELECT ID_CANCION, Nombre, Compositor, Emocion, Portada FROM CANCIONES WHERE Emocion = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, emocion);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Cancion c = new Cancion(
                        rs.getString("ID_CANCION"),
                        rs.getString("Nombre"),
                        rs.getString("Compositor"),
                        rs.getString("Emocion"),
                        rs.getBlob("Portada").getBinaryStream() // Convertimos el BLOB a un Stream para JavaFX
                );
                lista.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}