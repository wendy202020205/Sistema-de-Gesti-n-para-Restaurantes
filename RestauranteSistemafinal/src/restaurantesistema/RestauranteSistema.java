/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */



package restaurantesistema;

import javax.swing.*;

public class RestauranteSistema {
    public static void main(String[] args) {
        try {
            // Verificar driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Verificar conexión
            if (!ConexionBD.testConnection()) {
                System.exit(1);
            }
            
            // Iniciar interfaz
            SwingUtilities.invokeLater(() -> {
                MainWindow ventana = new MainWindow();
                ventana.setVisible(true);
            });
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                "Error: Driver Mysql no encontrado\n" + e.getMessage(),
                "Error crítico",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}