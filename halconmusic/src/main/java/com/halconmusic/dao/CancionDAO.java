package com.halconmusic.dao;

import java.awt.Image;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.halconmusic.db.ConexionDB;
import com.halconmusic.model.Cancion;

public class CancionDAO {

    private final Connection con;

    public CancionDAO() {
        this.con = ConexionDB.getInstance().getConexion();
    }

    // ── REQ. 2 — CREAR CANCIÓN ────────────────────────────
    /**
     * Inserta una nueva canción en CANCIONES y la vincula al artista en
     * ARTISTAS_CANCIONES y al álbum en ALBUMES_CANCIONES (si se indica).
     * @return true si se insertó correctamente.
     */
    public boolean insertar(String nombre, String genero, String artista,
                            String emocion, int duracionSeg, int fecha,
                            String ft, String letra,
                            String idArtista, String idAlbum,
                            java.io.File archivoPortada,
                            java.io.File archivoMusica,
                            java.io.File archivoVideo) {  // ← nuevo (opcional)

        String idNuevo = generarNuevoId();
        
        try {
            con.setAutoCommit(false);   // ← AQUÍ, antes de todo
        } catch (SQLException e) {
            System.err.println("Error setAutoCommit: " + e.getMessage());
            return false;
        }

        // Paso 1: INSERT con EMPTY_BLOB() igual que antes
        String sqlCan = """
            INSERT INTO CANCIONES
              (ID_CANCION, NOMBRE, GENERO, ARTISTA, PORTADA, MUSICA, VIDEO,
               EMOCION, DURACION_SEG, FECHA, FT, LETRA)
            VALUES (?, ?, ?, ?, EMPTY_BLOB(), EMPTY_BLOB(), EMPTY_BLOB(), ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = con.prepareStatement(sqlCan)) {
            ps.setString(1, idNuevo);
            ps.setString(2, nombre.trim());
            ps.setString(3, genero.trim());
            ps.setString(4, artista.trim());
            ps.setString(5, emocion.trim());
            ps.setInt   (6, duracionSeg);
            ps.setInt   (7, fecha);
            ps.setString(8, (ft    == null || ft.isBlank())    ? null : ft.trim());
            ps.setString(9, (letra == null || letra.isBlank())  ? null : letra.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al insertar canción: " + e.getMessage());
            try { con.rollback(); con.setAutoCommit(true); } catch (Exception ignored) {}
            return false;
        }

        // Paso 2: escribir PORTADA via locator
        String sqlBlob = "SELECT PORTADA, MUSICA, VIDEO FROM CANCIONES WHERE ID_CANCION = ? FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(sqlBlob)) {
            ps.setString(1, idNuevo);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Portada
                    oracle.sql.BLOB blobPortada = (oracle.sql.BLOB) rs.getBlob("PORTADA");
                    try (java.io.FileInputStream fis = new java.io.FileInputStream(archivoPortada);
                         java.io.OutputStream    os  = blobPortada.getBinaryOutputStream()) {
                        byte[] buf = new byte[blobPortada.getBufferSize()];
                        int n;
                        while ((n = fis.read(buf)) != -1) os.write(buf, 0, n);
                    }
                    // Música
                    oracle.sql.BLOB blobMusica = (oracle.sql.BLOB) rs.getBlob("MUSICA");
                    try (java.io.FileInputStream fis = new java.io.FileInputStream(archivoMusica);
                         java.io.OutputStream    os  = blobMusica.getBinaryOutputStream()) {
                        byte[] buf = new byte[blobMusica.getBufferSize()];
                        int n;
                        while ((n = fis.read(buf)) != -1) os.write(buf, 0, n);
                    }
                    // Video (opcional)
                    if (archivoVideo != null) {
                        oracle.sql.BLOB blobVideo = (oracle.sql.BLOB) rs.getBlob("VIDEO");
                        try (java.io.FileInputStream fis = new java.io.FileInputStream(archivoVideo);
                             java.io.OutputStream    os  = blobVideo.getBinaryOutputStream()) {
                            byte[] buf = new byte[blobVideo.getBufferSize()];
                            int n;
                            while ((n = fis.read(buf)) != -1) os.write(buf, 0, n);
                        }
                    }
                }
    }
            con.commit();
            con.setAutoCommit(true);
        } catch (Exception e) {
            System.err.println("Error al cargar BLOBs: " + e.getMessage());
            try { con.rollback(); con.setAutoCommit(true); } catch (Exception ignored) {}
            return false;
        }

        // Paso 3: vincular artista y álbum
        String sqlAC = "INSERT INTO ARTISTAS_CANCIONES (ID_ARTISTA, ID_CANCION) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sqlAC)) {
            ps.setString(1, idArtista);
            ps.setString(2, idNuevo);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al vincular artista-canción: " + e.getMessage());
            return false;
        }

        if (idAlbum != null && !idAlbum.isBlank()) {
            String sqlAL = "INSERT INTO ALBUMES_CANCIONES (ID_ALBUM, ID_CANCION) VALUES (?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlAL)) {
                ps.setString(1, idAlbum);
                ps.setString(2, idNuevo);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error al vincular álbum-canción: " + e.getMessage());
                return false;
            }
        }

        return true;
    }

    private String generarNuevoId() {
        String sql = "SELECT NVL(MAX(TO_NUMBER(SUBSTR(ID_CANCION,2))),0) + 1 AS NEXT_ID FROM CANCIONES";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return String.format("C%03d", rs.getInt("NEXT_ID"));
        } catch (SQLException e) {
            System.err.println("Error generando ID canción: " + e.getMessage());
        }
        return "C" + System.currentTimeMillis();
    }

    // ── REQ. 11 — OBTENER LETRA DE CANCIÓN ───────────────
    /**
     * Retorna la letra (CLOB) de una canción, o null si no tiene.
     */
    // ── REQ. 11 — OBTENER VIDEO DE CANCIÓN ───────────────
    /**
    * Retorna los bytes del VIDEO (BLOB) de una canción, o null si no tiene.
     */ 
    public byte[] obtenerVideo(String idCancion) {
        String sql = "SELECT VIDEO FROM CANCIONES WHERE ID_CANCION = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idCancion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Blob blob = rs.getBlob("VIDEO");
                    if (blob != null && blob.length() > 0) {
                        return blob.getBytes(1, (int) blob.length());
                    }
                }
            }
        } catch (SQLException e) {
        System.err.println("Error al obtener video: " + e.getMessage());
        }
        return null;
    }
    
    public String obtenerLetra(String idCancion) {
        String sql = "SELECT LETRA FROM CANCIONES WHERE ID_CANCION = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idCancion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Clob clob = rs.getClob("LETRA");
                    if (clob != null) {
                        return clob.getSubString(1, (int) clob.length());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener letra: " + e.getMessage());
        }
        return null;
    }

    // ── CONSULTAS EXISTENTES ──────────────────────────────
    public List<Cancion> obtenerTodas() {
        List<Cancion> lista = new ArrayList<>();
        String sql = """
            SELECT C.ID_CANCION, C.NOMBRE, C.GENERO,
                   A.NOMBRE AS ARTISTA, C.FT, C.PORTADA,
                   C.EMOCION, C.DURACION_SEG, C.FECHA
            FROM CANCIONES C
            JOIN ARTISTAS_CANCIONES AC ON C.ID_CANCION = AC.ID_CANCION
            JOIN ARTISTAS A            ON AC.ID_ARTISTA = A.ID_ARTISTA
            ORDER BY UPPER(C.NOMBRE)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearCancion(rs));
        } catch (SQLException e) {
            System.err.println("Error en CancionDAO.obtenerTodas: " + e.getMessage());
        }
        return lista;
    }

    public List<Cancion> buscar(String termino) {
        List<Cancion> lista = new ArrayList<>();
        String sql = """
            SELECT C.ID_CANCION, C.NOMBRE, C.GENERO,
                   A.NOMBRE AS ARTISTA, C.FT, C.PORTADA,
                   C.EMOCION, C.DURACION_SEG, C.FECHA
            FROM CANCIONES C
            JOIN ARTISTAS_CANCIONES AC ON C.ID_CANCION = AC.ID_CANCION
            JOIN ARTISTAS A            ON AC.ID_ARTISTA = A.ID_ARTISTA
            WHERE UPPER(C.NOMBRE)  LIKE UPPER(?)
               OR UPPER(A.NOMBRE)  LIKE UPPER(?)
               OR UPPER(C.GENERO)  LIKE UPPER(?)
               OR UPPER(C.EMOCION) LIKE UPPER(?)
            ORDER BY UPPER(C.NOMBRE)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String like = "%" + termino + "%";
            ps.setString(1,like); ps.setString(2,like);
            ps.setString(3,like); ps.setString(4,like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCancion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en CancionDAO.buscar: " + e.getMessage());
        }
        return lista;
    }

    public List<Cancion> buscarPorInicio(String termino) {
        List<Cancion> lista = new ArrayList<>();
        String sql = """
            SELECT C.ID_CANCION, C.NOMBRE, C.GENERO,
                   A.NOMBRE AS ARTISTA, C.FT, C.PORTADA,
                   C.EMOCION, C.DURACION_SEG, C.FECHA
            FROM CANCIONES C
            JOIN ARTISTAS_CANCIONES AC ON C.ID_CANCION = AC.ID_CANCION
            JOIN ARTISTAS A            ON AC.ID_ARTISTA = A.ID_ARTISTA
            WHERE UPPER(C.NOMBRE) LIKE UPPER(?)
            ORDER BY UPPER(C.NOMBRE)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, termino + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCancion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en CancionDAO.buscarPorInicio: " + e.getMessage());
        }
        return lista;
    }

    public List<Cancion> obtenerPorAlbum(String idAlbum) {
        List<Cancion> lista = new ArrayList<>();
        String sql = """
            SELECT C.ID_CANCION, C.NOMBRE, C.GENERO,
                   A.NOMBRE AS ARTISTA, C.FT, C.PORTADA,
                   C.EMOCION, C.DURACION_SEG, C.FECHA
            FROM CANCIONES C
            JOIN ALBUMES_CANCIONES  AL ON C.ID_CANCION  = AL.ID_CANCION
            JOIN ARTISTAS_CANCIONES AC ON C.ID_CANCION  = AC.ID_CANCION
            JOIN ARTISTAS A            ON AC.ID_ARTISTA = A.ID_ARTISTA
            WHERE AL.ID_ALBUM = ?
            ORDER BY C.FECHA, UPPER(C.NOMBRE)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idAlbum);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCancion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en CancionDAO.obtenerPorAlbum: " + e.getMessage());
        }
        return lista;
    }

    public List<Cancion> obtenerHistorialUsuario(String idUsuario) {
        List<Cancion> lista = new ArrayList<>();
        String sql = """
            SELECT ID_CANCION, NOMBRE, GENERO, ARTISTA, FT, PORTADA, EMOCION, DURACION_SEG, FECHA
            FROM (
                SELECT C.ID_CANCION, C.NOMBRE, C.GENERO,
                    A.NOMBRE AS ARTISTA, C.FT, C.PORTADA,
                    C.EMOCION, C.DURACION_SEG, C.FECHA,
                    MAX(HC.FECHA_REPRODUCCION) OVER (PARTITION BY C.ID_CANCION) AS ULTIMA_VEZ,
                    ROW_NUMBER() OVER (PARTITION BY C.ID_CANCION ORDER BY HC.FECHA_REPRODUCCION DESC) AS RN
                FROM CANCIONES C
                JOIN HISTORIAL_CANCIONES HC ON C.ID_CANCION   = HC.ID_CANCION
                JOIN HISTORIAL H            ON HC.ID_HISTORIAL = H.ID_HISTORIAL
                JOIN ARTISTAS_CANCIONES AC  ON C.ID_CANCION   = AC.ID_CANCION
                JOIN ARTISTAS A             ON AC.ID_ARTISTA  = A.ID_ARTISTA
                WHERE H.ID_USUARIO = ?
            )
            WHERE RN = 1
            ORDER BY ULTIMA_VEZ DESC
            """;            
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCancion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en CancionDAO.obtenerHistorialUsuario: " + e.getMessage());
        }
        return lista;
    }

    public List<Cancion> obtenerMeGustasUsuario(String idUsuario) {
        List<Cancion> lista = new ArrayList<>();
        String sql = """
            SELECT C.ID_CANCION, C.NOMBRE, C.GENERO,
                   A.NOMBRE AS ARTISTA, C.FT, C.PORTADA,
                   C.EMOCION, C.DURACION_SEG, C.FECHA
            FROM CANCIONES C
            JOIN MEGUSTAS_CANCIONES MC ON C.ID_CANCION   = MC.ID_CANCION
            JOIN MEGUSTAS M            ON MC.ID_MEGUSTAS  = M.ID_MEGUSTAS
            JOIN ARTISTAS_CANCIONES AC ON C.ID_CANCION   = AC.ID_CANCION
            JOIN ARTISTAS A            ON AC.ID_ARTISTA  = A.ID_ARTISTA
            WHERE M.ID_USUARIO = ?
            ORDER BY UPPER(C.NOMBRE)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCancion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en CancionDAO.obtenerMeGustasUsuario: " + e.getMessage());
        }
        return lista;
    }

    public int contarTotal() {
        String sql = "SELECT COUNT(*) FROM CANCIONES";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error en contarTotal: " + e.getMessage());
        }
        return 0;
    }

    public List<String[]> contarPorGenero() {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT GENERO, COUNT(*) AS TOTAL FROM CANCIONES GROUP BY GENERO ORDER BY TOTAL DESC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new String[]{ rs.getString("GENERO"), rs.getString("TOTAL") });
        } catch (SQLException e) {
            System.err.println("Error en contarPorGenero: " + e.getMessage());
        }
        return lista;
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

    public List<Cancion> obtenerPorPlaylist(String idPlaylist) {
        List<Cancion> lista = new ArrayList<>();
        String sql = """
            SELECT C.ID_CANCION, C.NOMBRE, C.GENERO,
                   A.NOMBRE AS ARTISTA, C.FT, C.PORTADA,
                   C.EMOCION, C.DURACION_SEG, C.FECHA
            FROM CANCIONES C
            JOIN PLAYLISTS_CANCIONES PC ON C.ID_CANCION = PC.ID_CANCION
            JOIN ARTISTAS_CANCIONES AC  ON C.ID_CANCION = AC.ID_CANCION
            JOIN ARTISTAS A             ON AC.ID_ARTISTA = A.ID_ARTISTA
            WHERE PC.ID_PLAYLIST = ?
            ORDER BY C.NOMBRE
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idPlaylist);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCancion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en obtenerPorPlaylist: " + e.getMessage());
        }
        return lista;
    }

    public void crearPlaylist(String idUsuario, String nombre, String descripcion,
                              List<String> idCanciones, byte[] portadaBytes) {
        try {
            con.setAutoCommit(false);

            // Generar nuevo ID
            String nuevoId;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT NVL(MAX(TO_NUMBER(SUBSTR(ID_PLAYLIST,3))),0)+1 AS NEXT_ID FROM PLAYLISTS");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                nuevoId = String.format("PL%03d", rs.getInt("NEXT_ID"));
            }

            // Insertar en PLAYLISTS
            String sqlPL = "INSERT INTO PLAYLISTS (ID_PLAYLIST, NOMBRE, PORTADA, CREADOR, DESCRIPCION, NUMERODECANCIONES) VALUES (?, ?, EMPTY_BLOB(), ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlPL)) {
                ps.setString(1, nuevoId);
                ps.setString(2, nombre);
                ps.setString(3, idUsuario);
                ps.setString(4, descripcion);
                ps.setInt(5, idCanciones.size());
                ps.executeUpdate();
            }

            // Escribir BLOB de portada usando el mismo patrón que CancionDAO
            if (portadaBytes != null && portadaBytes.length > 0) {
                String sqlBlob = "SELECT PORTADA FROM PLAYLISTS WHERE ID_PLAYLIST = ? FOR UPDATE";
                try (PreparedStatement ps = con.prepareStatement(sqlBlob)) {
                    ps.setString(1, nuevoId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            oracle.sql.BLOB blob = (oracle.sql.BLOB) rs.getBlob("PORTADA");
                            java.io.OutputStream os = blob.getBinaryOutputStream();
                            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(portadaBytes);
                            byte[] buf = new byte[blob.getBufferSize()];
                            int n;
                            while ((n = bis.read(buf)) != -1) os.write(buf, 0, n);
                            os.close();
                            bis.close();
                        }
                    }
                }
            }

            // Insertar en USUARIOS_PLAYLISTS
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO USUARIOS_PLAYLISTS (ID_USUARIO, ID_PLAYLIST) VALUES (?, ?)")) {
                ps.setString(1, idUsuario);
                ps.setString(2, nuevoId);
                ps.executeUpdate();
            }

            // Insertar canciones en PLAYLISTS_CANCIONES
            String sqlPC = "INSERT INTO PLAYLISTS_CANCIONES (ID_PLAYLIST, ID_CANCION) VALUES (?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlPC)) {
                for (String idC : idCanciones) {
                    ps.setString(1, nuevoId);
                    ps.setString(2, idC);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            con.commit();
            con.setAutoCommit(true);
            System.out.println("✅ Playlist creada: " + nuevoId);

        } catch (Exception e) {
            System.err.println("Error en crearPlaylist: " + e.getMessage());
            try { con.rollback(); con.setAutoCommit(true); } catch (Exception ignored) {}
        }
    }
}