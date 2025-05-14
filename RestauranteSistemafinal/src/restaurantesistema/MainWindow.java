package restaurantesistema;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

public class MainWindow extends JFrame {

    public MainWindow() {
        setTitle("Sistema de Restaurante");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        JButton comensalesBtn = new JButton("Comensales");
        comensalesBtn.setPreferredSize(new Dimension(150, 50));
        JButton loginBtn = new JButton("Login Personal");
        loginBtn.setPreferredSize(new Dimension(150, 50));

        comensalesBtn.addActionListener(e -> {
            int comensalId = obtenerIdComensal();
            if (comensalId != -1) {
                new ComensalWindow(comensalId).setVisible(true);
            }
        });

        loginBtn.addActionListener(e -> mostrarLogin());

        panel.add(comensalesBtn, gbc);
        panel.add(loginBtn, gbc);

        add(panel);
    }

    private int obtenerIdComensal() {
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id_usuario FROM usuarios WHERE username = 'comensal' AND activo = TRUE");
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("id_usuario");
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se encontró el usuario comensal activo en la base de datos",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return -1;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al obtener usuario comensal: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }

    private void mostrarLogin() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField txtUser = new JTextField();
        JPasswordField txtPass = new JPasswordField();

        panel.add(new JLabel("Usuario:"));
        panel.add(txtUser);
        panel.add(new JLabel("Contraseña:"));
        panel.add(txtPass);

        int option = JOptionPane.showConfirmDialog(this, panel, "Login",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String username = txtUser.getText().trim();
            char[] password = txtPass.getPassword();

            if (username.isEmpty() || password.length == 0) {
                JOptionPane.showMessageDialog(this, "Usuario y contraseña son requeridos",
                        "Error", JOptionPane.ERROR_MESSAGE);
                Arrays.fill(password, ' '); // Limpiar el array de contraseña
                return;
            }

            String rol = autenticarUsuario(username, password);
            Arrays.fill(password, ' '); // Limpiar el array de contraseña siempre

            if (rol != null) {
                abrirPanel(rol);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Credenciales incorrectas o usuario inactivo",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String autenticarUsuario(String usuario, char[] contrasena) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.getConnection();
            // Consulta para obtener el hash almacenado
            String sql = "SELECT r.nombre_rol, u.password FROM usuarios u "
                    + "JOIN roles r ON u.id_rol = r.id_rol "
                    + "WHERE u.username = ? AND u.activo = TRUE";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, usuario);
            rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                String inputHash = hashPassword(new String(contrasena));

                // Comparación segura de hashes
                if (inputHash != null && inputHash.equals(storedHash)) {
                    return rs.getString("nombre_rol");
                }
            }
            return null;

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error de autenticación. Por favor intente nuevamente.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void abrirPanel(String rol) {
        switch (rol) {
            case "Administrador":
                new AdminWindow().setVisible(true);
                break;
            case "Camarero":
                new CamareroWindow().setVisible(true);
                break;
            case "Chef":
                new ChefWindow().setVisible(true);
                break;
            default:
                JOptionPane.showMessageDialog(this,
                        "No tiene permisos para acceder a ningún panel o su rol no está configurado",
                        "Acceso denegado", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Establecer el look and feel del sistema para mejor apariencia
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error al establecer el look and feel: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            try {
                // Verificar conexión con la base de datos al iniciar
                if (!ConexionBD.testConnection()) {
                    JOptionPane.showMessageDialog(null,
                            "No se pudo establecer conexión con la base de datos",
                            "Error crítico", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }

                // Mostrar ventana principal
                new MainWindow().setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                        "Error al iniciar la aplicación: " + ex.getMessage(),
                        "Error crítico", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
                System.exit(1);
            }
        });
    }
}