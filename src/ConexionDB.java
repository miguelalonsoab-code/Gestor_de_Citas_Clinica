import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * CLASE: ConexionDB
 * ─────────────────────────────────────────────
 * Gestiona la conexión con MySQL usando JDBC.
 *
 * PATRÓN SINGLETON: solo existe UNA conexión activa en toda
 * la aplicación. Si ya hay una abierta, la reutiliza.
 *
 * CONFIGURACIÓN: cambia URL, USUARIO y PASSWORD según tu instalación.
 *
 * USO:
 *   Connection con = ConexionDB.getConexion();
 *   // usar la conexión...
 *   ConexionDB.cerrarConexion();
 */
public class ConexionDB {

    // ── Datos de conexión ─────────────────────────────────────────
    // ⚠ Cambia estos valores según tu instalación de MySQL:
    private static final String URL      = "jdbc:mysql://localhost:3306/clinica_upn"
                                         + "?useSSL=false"
                                         + "&allowPublicKeyRetrieval=true"
                                         + "&serverTimezone=America/Lima";
    private static final String USUARIO  = "root";
    private static final String PASSWORD = "121312Miki+"; 

    // Instancia única de la conexión (patrón Singleton)
    private static Connection conexion = null;

    /**
     * Retorna la conexión activa o crea una nueva si no existe.
     *
     * DriverManager.getConnection() es el método JDBC que establece
     * la comunicación física con el servidor MySQL. Requiere:
     *  - URL con el formato: jdbc:mysql://host:puerto/baseDatos
     *  - Usuario y contraseña del servidor
     *
     * @return Connection activa con la base de datos
     * @throws SQLException si no puede conectarse (MySQL no está corriendo,
     *                      contraseña incorrecta, BD no existe, etc.)
     */
    public static Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            try {
                // Cargar el driver JDBC de MySQL
                // En versiones modernas del conector esto es automático,
                // pero lo incluimos por compatibilidad con Java 8.
                Class.forName("com.mysql.cj.jdbc.Driver");

                conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
                System.out.println("✔ Conexión con MySQL establecida.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver MySQL no encontrado. "
                    + "¿Agregaste mysql-connector-j al Build Path?", e);
            }
        }
        return conexion;
    }

    /**
     * Cierra la conexión activa con MySQL.
     * Debe llamarse al cerrar la aplicación (en windowClosing de VentanaPrincipal).
     */
    public static void cerrarConexion() {
        if (conexion != null) {
            try {
                conexion.close();
                conexion = null;
                System.out.println("✔ Conexión con MySQL cerrada.");
            } catch (SQLException e) {
                System.out.println("⚠ Error al cerrar conexión: " + e.getMessage());
            }
        }
    }

    /**
     * Verifica si la conexión está activa.
     * Útil para mostrar el estado en la barra de la ventana.
     *
     * @return true si hay conexión abierta, false si no
     */
    public static boolean estaConectado() {
        try {
            return conexion != null && !conexion.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
