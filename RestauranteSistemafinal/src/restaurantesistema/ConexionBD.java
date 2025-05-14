package restaurantesistema;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class ConexionBD {
    // Configuración para MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/restaurante_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Bloque estático para registrar el driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            mostrarErrorCritico(
                    "Driver de MySQL no encontrado.\n\n"
                            + "Soluciones:\n"
                            + "1. Descarga el conector MySQL (Connector/J) desde:\n"
                            + "   https://dev.mysql.com/downloads/connector/j/\n"
                            + "2. Selecciona la opción 'Platform Independent'\n"
                            + "3. Descarga el archivo ZIP y extrae el .jar\n"
                            + "4. Colócalo en la carpeta 'lib' de tu proyecto\n"
                            + "5. Agrégalo a las bibliotecas del proyecto en tu IDE",
                    ex
            );
        }
    }

    /**
     * Obtiene una conexión a la base de datos
     * @return Connection objeto de conexión
     * @throws SQLException si ocurre un error
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Verifica si la conexión a la BD está activa
     * @return true si la conexión es exitosa
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException ex) {
            mostrarErrorConexion(ex);
            return false;
        }
    }

    // Métodos para cerrar recursos (sobrecargados)
    public static void close(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.err.println("Error al cerrar Connection: " + ex.getMessage());
            }
        }
    }

    public static void close(PreparedStatement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ex) {
                System.err.println("Error al cerrar PreparedStatement: " + ex.getMessage());
            }
        }
    }

    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ex) {
                System.err.println("Error al cerrar ResultSet: " + ex.getMessage());
            }
        }
    }

    // Métodos para mostrar errores
    private static void mostrarErrorCritico(String mensaje, Exception ex) {
        System.err.println(mensaje);
        ex.printStackTrace();
        JOptionPane.showMessageDialog(
                null,
                mensaje + "\n\nDetalle técnico: " + ex.getMessage(),
                "Error crítico - Driver no encontrado",
                JOptionPane.ERROR_MESSAGE
        );
        System.exit(1);
    }

    private static void mostrarErrorConexion(SQLException ex) {
        String mensaje = "Error al conectar con la base de datos:\n\n"
                + "1. Verifica que MySQL esté en ejecución\n"
                + "2. Confirma que la BD 'restaurante_db' existe\n"
                + "3. Revisa usuario/contraseña\n"
                + "4. Verifica el puerto (3306 por defecto)\n\n"
                + "Detalle técnico: " + ex.getMessage();

        JOptionPane.showMessageDialog(
                null,
                mensaje,
                "Error de conexión",
                JOptionPane.ERROR_MESSAGE
        );
    }
}