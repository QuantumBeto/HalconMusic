package com.example.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    public static void main(String[] args) {
        
        String url = "jdbc:oracle:thin:@localhost:1521/xe";
        String usuario = "system";
        String password = "12345678";

        System.out.println("Intentando conectar a Oracle XE local...");

        try {
            Connection con = DriverManager.getConnection(url, usuario, password);
            System.out.println("¡CONEXIÓN EXITOSA!");
            con.close();
        } catch (SQLException e) {
            System.out.println("¡CONEXIÓN FALLIDA!");
        }
    }
}