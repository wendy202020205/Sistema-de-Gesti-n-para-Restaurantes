/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package restaurantesistema;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

public class ComensalWindow extends JFrame {
    private JComboBox<Integer> numPersonasCombo;
    private JComboBox<String> horaCombo, minutoCombo;
    private JTable mesasDisponiblesTable, comidasTable, comidasSeleccionadasTable;
    private JButton reservarButton, agregarComidaButton, quitarComidaButton;
    private JTextField fechaField;
    private DefaultTableModel mesasModel, comidasModel, comidasSeleccionadasModel;
    private Connection connection;
    private int usuarioId;
    
    public ComensalWindow(int usuarioId) {
        this.usuarioId = usuarioId;
        initWindow();
        initDBConnection();
        initUI();
        cargarComidas();
    }
    
    private void initWindow() {
        setTitle("Menú para Comensales - Reserva de Mesas");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }
    
    private void initDBConnection() {
        try {
            connection = ConexionBD.getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al conectar con la base de datos: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }
    
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel de parámetros
        JPanel parametrosPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        fechaField = new JTextField(LocalDate.now().toString());
        fechaField.setEditable(false);
        
        numPersonasCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
        horaCombo = new JComboBox<>();
        for (int i = 12; i <= 22; i++) horaCombo.addItem(String.format("%02d", i));
        minutoCombo = new JComboBox<>(new String[]{"00", "15", "30", "45"});
        
        parametrosPanel.add(new JLabel("Fecha de reserva:"));
        parametrosPanel.add(fechaField);
        parametrosPanel.add(new JLabel("Número de personas:"));
        parametrosPanel.add(numPersonasCombo);
        parametrosPanel.add(new JLabel("Hora de reserva:"));
        JPanel horaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        horaPanel.add(horaCombo);
        horaPanel.add(new JLabel(":"));
        horaPanel.add(minutoCombo);
        parametrosPanel.add(horaPanel);
        
        // Panel central
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        
        // Panel de mesas
        JPanel mesasPanel = new JPanel(new BorderLayout());
        mesasPanel.setBorder(BorderFactory.createTitledBorder("Mesas Disponibles"));
        mesasModel = new DefaultTableModel(new Object[]{"ID", "Número", "Capacidad", "Ubicación"}, 0);
        mesasDisponiblesTable = new JTable(mesasModel);
        mesasDisponiblesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JButton buscarMesasButton = new JButton("Buscar Mesas Disponibles");
        buscarMesasButton.addActionListener(e -> buscarMesasDisponibles());
        
        mesasPanel.add(new JScrollPane(mesasDisponiblesTable), BorderLayout.CENTER);
        mesasPanel.add(buscarMesasButton, BorderLayout.SOUTH);
        
        // Panel de comidas
        JPanel comidasPanel = new JPanel(new BorderLayout());
        comidasPanel.setBorder(BorderFactory.createTitledBorder("Menú de Comidas"));
        
        comidasModel = new DefaultTableModel(new Object[]{"ID", "Nombre", "Precio", "Categoría"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        comidasTable = new JTable(comidasModel);
        
        comidasSeleccionadasModel = new DefaultTableModel(new Object[]{"ID", "Nombre", "Precio", "Cantidad"}, 0);
        comidasSeleccionadasTable = new JTable(comidasSeleccionadasModel);
        
        agregarComidaButton = new JButton("Agregar →");
        agregarComidaButton.addActionListener(e -> agregarComida());
        quitarComidaButton = new JButton("← Quitar");
        quitarComidaButton.addActionListener(e -> quitarComida());
        
        JPanel comidasButtonsPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        comidasButtonsPanel.add(quitarComidaButton);
        comidasButtonsPanel.add(agregarComidaButton);
        
        JTabbedPane comidasTabbedPane = new JTabbedPane();
        JPanel comidasListaPanel = new JPanel(new BorderLayout());
        comidasListaPanel.add(new JScrollPane(comidasTable), BorderLayout.CENTER);
        comidasListaPanel.add(comidasButtonsPanel, BorderLayout.SOUTH);
        
        JPanel comidasSeleccionadasPanel = new JPanel(new BorderLayout());
        comidasSeleccionadasPanel.add(new JScrollPane(comidasSeleccionadasTable), BorderLayout.CENTER);
        
        comidasTabbedPane.addTab("Comidas", comidasListaPanel);
        comidasTabbedPane.addTab("Seleccionadas", comidasSeleccionadasPanel);
        comidasPanel.add(comidasTabbedPane, BorderLayout.CENTER);
        
        centerPanel.add(mesasPanel);
        centerPanel.add(comidasPanel);
        
        // Botón de reserva
        reservarButton = new JButton("Reservar Mesa");
        reservarButton.addActionListener(e -> hacerReserva());
        
        // Ensamblar ventana
        mainPanel.add(parametrosPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(reservarButton, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void cargarComidas() {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT c.id_comida, c.nombre, c.precio, cat.nombre as categoria " +
                "FROM comidas c JOIN categorias_comida cat ON c.id_categoria = cat.id_categoria " +
                "WHERE c.disponibilidad = TRUE ORDER BY cat.nombre, c.nombre");
             ResultSet rs = stmt.executeQuery()) {
            
            comidasModel.setRowCount(0);
            while (rs.next()) {
                comidasModel.addRow(new Object[]{
                    rs.getInt("id_comida"),
                    rs.getString("nombre"),
                    rs.getDouble("precio"),
                    rs.getString("categoria")
                });
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar comidas: " + e.getMessage());
        }
    }

    private void buscarMesasDisponibles() {
        int numPersonas = (Integer) numPersonasCombo.getSelectedItem();
        String hora = horaCombo.getSelectedItem() + ":" + minutoCombo.getSelectedItem();
        LocalDate fecha = LocalDate.parse(fechaField.getText());

        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT m.id_mesa, m.numero_mesa, m.capacidad, um.nombre as ubicacion " +
                        "FROM mesas m " +
                        "JOIN ubicaciones_mesa um ON m.id_ubicacion = um.id_ubicacion " +
                        "WHERE m.capacidad >= ? AND m.estado = 'disponible' " +
                        "AND m.id_mesa NOT IN (" +
                        "   SELECT r.id_mesa FROM reservas r " +
                        "   WHERE r.fecha_reserva = ? " +
                        "   AND r.estado IN ('pendiente', 'confirmada') " +
                        "   AND TIME(?) BETWEEN r.hora_reserva AND ADDTIME(r.hora_reserva, SEC_TO_TIME(r.duracion_estimada * 60))" +
                        ") ORDER BY m.capacidad")) {

            stmt.setInt(1, numPersonas);
            stmt.setDate(2, Date.valueOf(fecha));
            stmt.setTime(3, Time.valueOf(hora + ":00"));

            ResultSet rs = stmt.executeQuery();
            mesasModel.setRowCount(0);

            while (rs.next()) {
                mesasModel.addRow(new Object[]{
                        rs.getInt("id_mesa"),
                        rs.getInt("numero_mesa"),
                        rs.getInt("capacidad"),
                        rs.getString("ubicacion")
                });
            }

            if (mesasModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                        "No hay mesas disponibles para los criterios seleccionados",
                        "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            mostrarError("Error al buscar mesas: " + e.getMessage());
        }
    }
    
    private void agregarComida() {
        int selectedRow = comidasTable.getSelectedRow();
        if (selectedRow == -1) {
            mostrarAdvertencia("Seleccione una comida para agregar");
            return;
        }
        
        int idComida = (Integer) comidasModel.getValueAt(selectedRow, 0);
        String nombre = (String) comidasModel.getValueAt(selectedRow, 1);
        double precio = (Double) comidasModel.getValueAt(selectedRow, 2);
        
        for (int i = 0; i < comidasSeleccionadasModel.getRowCount(); i++) {
            if ((Integer) comidasSeleccionadasModel.getValueAt(i, 0) == idComida) {
                int cantidad = (Integer) comidasSeleccionadasModel.getValueAt(i, 3);
                comidasSeleccionadasModel.setValueAt(cantidad + 1, i, 3);
                return;
            }
        }
        
        comidasSeleccionadasModel.addRow(new Object[]{idComida, nombre, precio, 1});
    }
    
    private void quitarComida() {
        int selectedRow = comidasSeleccionadasTable.getSelectedRow();
        if (selectedRow == -1) {
            mostrarAdvertencia("Seleccione una comida para quitar");
            return;
        }
        
        int cantidad = (Integer) comidasSeleccionadasModel.getValueAt(selectedRow, 3);
        if (cantidad > 1) {
            comidasSeleccionadasModel.setValueAt(cantidad - 1, selectedRow, 3);
        } else {
            comidasSeleccionadasModel.removeRow(selectedRow);
        }
    }
    
    private void hacerReserva() {
        int selectedMesaRow = mesasDisponiblesTable.getSelectedRow();
        if (selectedMesaRow == -1) {
            mostrarAdvertencia("Seleccione una mesa para reservar");
            return;
        }
        
        int idMesa = (Integer) mesasModel.getValueAt(selectedMesaRow, 0);
        
        try {
            connection.setAutoCommit(false);
            
            // Verificar que el usuario existe
            try (PreparedStatement checkStmt = connection.prepareStatement(
                    "SELECT 1 FROM usuarios WHERE id_usuario = ?")) {
                checkStmt.setInt(1, usuarioId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        mostrarError("Usuario no válido para la reserva");
                        return;
                    }
                }
            }
            
            // Insertar reserva
            String reservaQuery = "INSERT INTO reservas (id_mesa, id_usuario, fecha_reserva, hora_reserva, " +
                                "duracion_estimada, numero_personas, estado) VALUES (?, ?, ?, ?, 90, ?, 'pendiente')";
            
            try (PreparedStatement pstmt = connection.prepareStatement(reservaQuery, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, idMesa);
                pstmt.setInt(2, usuarioId);
                pstmt.setDate(3, Date.valueOf(LocalDate.parse(fechaField.getText())));
                pstmt.setTime(4, Time.valueOf(horaCombo.getSelectedItem() + ":" + minutoCombo.getSelectedItem() + ":00"));
                pstmt.setInt(5, (Integer) numPersonasCombo.getSelectedItem());
                
                if (pstmt.executeUpdate() == 0) {
                    throw new SQLException("No se pudo crear la reserva");
                }
                
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int idReserva = generatedKeys.getInt(1);
                        
                        // Insertar comidas seleccionadas
                        if (comidasSeleccionadasModel.getRowCount() > 0) {
                            String comidaQuery = "INSERT INTO reserva_comidas (id_reserva, id_comida, cantidad) VALUES (?, ?, ?)";
                            try (PreparedStatement comidaStmt = connection.prepareStatement(comidaQuery)) {
                                for (int i = 0; i < comidasSeleccionadasModel.getRowCount(); i++) {
                                    comidaStmt.setInt(1, idReserva);
                                    comidaStmt.setInt(2, (Integer) comidasSeleccionadasModel.getValueAt(i, 0));
                                    comidaStmt.setInt(3, (Integer) comidasSeleccionadasModel.getValueAt(i, 3));
                                    comidaStmt.addBatch();
                                }
                                comidaStmt.executeBatch();
                            }
                        }
                        
                        // Actualizar estado de la mesa
                        try (PreparedStatement updateStmt = connection.prepareStatement(
                                "UPDATE mesas SET estado = 'reservada' WHERE id_mesa = ?")) {
                            updateStmt.setInt(1, idMesa);
                            updateStmt.executeUpdate();
                        }
                        
                        connection.commit();
                        
                        JOptionPane.showMessageDialog(this, 
                            "Reserva realizada con éxito. Número de reserva: " + idReserva,
                            "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Limpiar selecciones
                        mesasModel.setRowCount(0);
                        comidasSeleccionadasModel.setRowCount(0);
                    } else {
                        throw new SQLException("No se pudo obtener el ID de la reserva");
                    }
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
                mostrarError("Error al realizar reserva: " + e.getMessage());
            } catch (SQLException ex) {
                mostrarError("Error al revertir transacción: " + ex.getMessage());
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                mostrarError("Error al restaurar auto-commit: " + e.getMessage());
            }
        }
    }
    
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }
    
    @Override
    public void dispose() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        super.dispose();
    }
}