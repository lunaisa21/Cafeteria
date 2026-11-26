package Operaciones;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class GestorConexion {
    private static GestorConexion instancia;
    private final String url;
    private final String usuario;
    private final String clave;
    private GestorConexion(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }
    public static synchronized GestorConexion getInstancia(String url, String usuario, String clave) {
        if (instancia == null) {
            instancia = new GestorConexion(url, usuario, clave);
        }
        return instancia;
    }
    public static synchronized GestorConexion getInstancia() {
        if (instancia == null) {
            throw new IllegalStateException("GestorConexion no ha sido inicializado. "
                    + "Llame a getInstancia(url, usuario, clave) primero.");
        }
        return instancia;
    }
    public static synchronized boolean estaInicializado() {
        return instancia != null;
    }
    public Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }
}