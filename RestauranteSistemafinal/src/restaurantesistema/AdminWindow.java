package restaurantesistema;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminWindow extends JFrame {
    private JTabbedPane tabbedPane;
    private Connection connection;

    // Modelos de tablas
    private DefaultTableModel usuariosModel;
    private DefaultTableModel mesasModel;
    private DefaultTableModel comidasModel;
    private DefaultTableModel reservasModel;
    private DefaultTableModel categoriasModel;
    private DefaultTableModel ubicacionesModel;

    public AdminWindow() {
        setTitle("Panel Administrador");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            connection = ConexionBD.getConnection();
            initUI();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al conectar con la base de datos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void initUI() {
        tabbedPane = new JTabbedPane();

        // Pestaña de Usuarios
        tabbedPane.addTab("Usuarios", createUsuariosPanel());

        // Pestaña de Mesas
        tabbedPane.addTab("Mesas", createMesasPanel());

        // Pestaña de Comidas
        tabbedPane.addTab("Comidas", createComidasPanel());

        // Pestaña de Reservas
        tabbedPane.addTab("Reservas", createReservasPanel());

        // Pestaña de Categorías
        tabbedPane.addTab("Categorías", createCategoriasPanel());

        // Pestaña de Ubicaciones
        tabbedPane.addTab("Ubicaciones", createUbicacionesPanel());

        add(tabbedPane);
        cargarTodosLosDatos();
    }

    private JPanel createUsuariosPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Modelo de tabla
        usuariosModel = new DefaultTableModel(new Object[]{"ID", "Usuario", "Nombre", "Apellido", "Rol", "Activo"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) return Boolean.class;
                return super.getColumnClass(columnIndex);
            }
        };

        JTable usuariosTable = new JTable(usuariosModel);
        usuariosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Botones
        JButton agregarBtn = new JButton("Agregar");
        JButton editarBtn = new JButton("Editar");
        JButton eliminarBtn = new JButton("Eliminar");
        JButton actualizarBtn = new JButton("Actualizar");

        agregarBtn.addActionListener(e -> agregarUsuario());
        editarBtn.addActionListener(e -> editarUsuario(usuariosTable.getSelectedRow()));
        eliminarBtn.addActionListener(e -> eliminarUsuario(usuariosTable.getSelectedRow()));
        actualizarBtn.addActionListener(e -> cargarUsuarios());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(agregarBtn);
        buttonPanel.add(editarBtn);
        buttonPanel.add(eliminarBtn);
        buttonPanel.add(actualizarBtn);

        panel.add(new JScrollPane(usuariosTable), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMesasPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        mesasModel = new DefaultTableModel(new Object[]{"ID", "Número", "Capacidad", "Ubicación", "Estado"}, 0);
        JTable mesasTable = new JTable(mesasModel);

        JButton agregarBtn = new JButton("Agregar");
        JButton editarBtn = new JButton("Editar");
        JButton eliminarBtn = new JButton("Eliminar");
        JButton actualizarBtn = new JButton("Actualizar");

        agregarBtn.addActionListener(e -> agregarMesa());
        editarBtn.addActionListener(e -> editarMesa(mesasTable.getSelectedRow()));
        eliminarBtn.addActionListener(e -> eliminarMesa(mesasTable.getSelectedRow()));
        actualizarBtn.addActionListener(e -> cargarMesas());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(agregarBtn);
        buttonPanel.add(editarBtn);
        buttonPanel.add(eliminarBtn);
        buttonPanel.add(actualizarBtn);

        panel.add(new JScrollPane(mesasTable), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createComidasPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        comidasModel = new DefaultTableModel(new Object[]{"ID", "Nombre", "Descripción", "Precio", "Categoría", "Disponible"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) return Boolean.class;
                return super.getColumnClass(columnIndex);
            }
        };

        JTable comidasTable = new JTable(comidasModel);

        JButton agregarBtn = new JButton("Agregar");
        JButton editarBtn = new JButton("Editar");
        JButton eliminarBtn = new JButton("Eliminar");
        JButton actualizarBtn = new JButton("Actualizar");

        agregarBtn.addActionListener(e -> agregarComida());
        editarBtn.addActionListener(e -> editarComida(comidasTable.getSelectedRow()));
        eliminarBtn.addActionListener(e -> eliminarComida(comidasTable.getSelectedRow()));
        actualizarBtn.addActionListener(e -> cargarComidas());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(agregarBtn);
        buttonPanel.add(editarBtn);
        buttonPanel.add(eliminarBtn);
        buttonPanel.add(actualizarBtn);

        panel.add(new JScrollPane(comidasTable), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createReservasPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        reservasModel = new DefaultTableModel(new Object[]{
                "ID", "Mesa", "Usuario", "Fecha", "Hora",
                "Personas", "Estado", "Comidas"
        }, 0);

        JTable reservasTable = new JTable(reservasModel);

        JButton actualizarBtn = new JButton("Actualizar");
        JButton cancelarBtn = new JButton("Cancelar Reserva");
        JButton detallesBtn = new JButton("Ver Detalles");

        actualizarBtn.addActionListener(e -> cargarReservas());
        cancelarBtn.addActionListener(e -> cancelarReserva(reservasTable.getSelectedRow()));
        detallesBtn.addActionListener(e -> verDetallesReserva(reservasTable.getSelectedRow()));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(actualizarBtn);
        buttonPanel.add(cancelarBtn);
        buttonPanel.add(detallesBtn);

        panel.add(new JScrollPane(reservasTable), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCategoriasPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        categoriasModel = new DefaultTableModel(new Object[]{"ID", "Nombre", "Descripción"}, 0);
        JTable categoriasTable = new JTable(categoriasModel);

        JButton agregarBtn = new JButton("Agregar");
        JButton editarBtn = new JButton("Editar");
        JButton eliminarBtn = new JButton("Eliminar");
        JButton actualizarBtn = new JButton("Actualizar");

        agregarBtn.addActionListener(e -> agregarCategoria());
        editarBtn.addActionListener(e -> editarCategoria(categoriasTable.getSelectedRow()));
        eliminarBtn.addActionListener(e -> eliminarCategoria(categoriasTable.getSelectedRow()));
        actualizarBtn.addActionListener(e -> cargarCategorias());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(agregarBtn);
        buttonPanel.add(editarBtn);
        buttonPanel.add(eliminarBtn);
        buttonPanel.add(actualizarBtn);

        panel.add(new JScrollPane(categoriasTable), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createUbicacionesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        ubicacionesModel = new DefaultTableModel(new Object[]{"ID", "Nombre", "Descripción"}, 0);
        JTable ubicacionesTable = new JTable(ubicacionesModel);

        JButton agregarBtn = new JButton("Agregar");
        JButton editarBtn = new JButton("Editar");
        JButton eliminarBtn = new JButton("Eliminar");
        JButton actualizarBtn = new JButton("Actualizar");

        agregarBtn.addActionListener(e -> agregarUbicacion());
        editarBtn.addActionListener(e -> editarUbicacion(ubicacionesTable.getSelectedRow()));
        eliminarBtn.addActionListener(e -> eliminarUbicacion(ubicacionesTable.getSelectedRow()));
        actualizarBtn.addActionListener(e -> cargarUbicaciones());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(agregarBtn);
        buttonPanel.add(editarBtn);
        buttonPanel.add(eliminarBtn);
        buttonPanel.add(actualizarBtn);

        panel.add(new JScrollPane(ubicacionesTable), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void cargarTodosLosDatos() {
        cargarUsuarios();
        cargarMesas();
        cargarComidas();
        cargarReservas();
        cargarCategorias();
        cargarUbicaciones();
    }

    private void cargarUsuarios() {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT u.id_usuario, u.username, u.nombre, u.apellido, r.nombre_rol, u.activo " +
                        "FROM usuarios u JOIN roles r ON u.id_rol = r.id_rol")) {

            ResultSet rs = stmt.executeQuery();
            usuariosModel.setRowCount(0);

            while (rs.next()) {
                usuariosModel.addRow(new Object[]{
                        rs.getInt("id_usuario"),
                        rs.getString("username"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("nombre_rol"),
                        rs.getBoolean("activo")
                });
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar usuarios: " + e.getMessage());
        }
    }

    private void cargarMesas() {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT m.id_mesa, m.numero_mesa, m.capacidad, um.nombre as ubicacion, m.estado " +
                        "FROM mesas m JOIN ubicaciones_mesa um ON m.id_ubicacion = um.id_ubicacion")) {

            ResultSet rs = stmt.executeQuery();
            mesasModel.setRowCount(0);

            while (rs.next()) {
                mesasModel.addRow(new Object[]{
                        rs.getInt("id_mesa"),
                        rs.getInt("numero_mesa"),
                        rs.getInt("capacidad"),
                        rs.getString("ubicacion"),
                        rs.getString("estado")
                });
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar mesas: " + e.getMessage());
        }
    }

    private void editarMesa(int row) {
        if (row == -1) {
            mostrarAdvertencia("Seleccione una mesa para editar");
            return;
        }

        int idMesa = (Integer) mesasModel.getValueAt(row, 0);
        int numeroMesa = (Integer) mesasModel.getValueAt(row, 1);
        int capacidad = (Integer) mesasModel.getValueAt(row, 2);
        String ubicacionActual = (String) mesasModel.getValueAt(row, 3);
        String estadoActual = (String) mesasModel.getValueAt(row, 4);

        JDialog dialog = new JDialog(this, "Editar Mesa", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new GridLayout(0, 2, 10, 10));

        JTextField numeroField = new JTextField(String.valueOf(numeroMesa));
        JSpinner capacidadSpinner = new JSpinner(new SpinnerNumberModel(capacidad, 1, 20, 1));

        // Combo de ubicaciones
        JComboBox<String> ubicacionCombo = new JComboBox<>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT nombre FROM ubicaciones_mesa");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ubicacionCombo.addItem(rs.getString("nombre"));
            }
            ubicacionCombo.setSelectedItem(ubicacionActual);
        } catch (SQLException e) {
            mostrarError("Error al cargar ubicaciones: " + e.getMessage());
        }

        JComboBox<String> estadoCombo = new JComboBox<>(new String[]{"disponible", "ocupada", "reservada", "mantenimiento"});
        estadoCombo.setSelectedItem(estadoActual);

        dialog.add(new JLabel("Número de mesa:"));
        dialog.add(numeroField);
        dialog.add(new JLabel("Capacidad:"));
        dialog.add(capacidadSpinner);
        dialog.add(new JLabel("Ubicación:"));
        dialog.add(ubicacionCombo);
        dialog.add(new JLabel("Estado:"));
        dialog.add(estadoCombo);

        JButton guardarBtn = new JButton("Guardar");
        guardarBtn.addActionListener(e -> {
            try {
                // Validar campos
                if (numeroField.getText().isEmpty()) {
                    mostrarAdvertencia("El número de mesa es obligatorio");
                    return;
                }

                // Obtener ID de ubicación
                int idUbicacion;
                try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT id_ubicacion FROM ubicaciones_mesa WHERE nombre = ?")) {
                    stmt.setString(1, (String) ubicacionCombo.getSelectedItem());
                    ResultSet rs = stmt.executeQuery();
                    if (!rs.next()) throw new SQLException("Ubicación no encontrada");
                    idUbicacion = rs.getInt("id_ubicacion");
                }

                // Actualizar mesa
                try (PreparedStatement stmt = connection.prepareStatement(
                        "UPDATE mesas SET numero_mesa = ?, capacidad = ?, id_ubicacion = ?, estado = ? " +
                                "WHERE id_mesa = ?")) {

                    stmt.setInt(1, Integer.parseInt(numeroField.getText()));
                    stmt.setInt(2, (Integer) capacidadSpinner.getValue());
                    stmt.setInt(3, idUbicacion);
                    stmt.setString(4, (String) estadoCombo.getSelectedItem());
                    stmt.setInt(5, idMesa);

                    int affected = stmt.executeUpdate();
                    if (affected > 0) {
                        cargarMesas();
                        dialog.dispose();
                        JOptionPane.showMessageDialog(this, "Mesa actualizada exitosamente");
                    } else {
                        mostrarError("No se pudo actualizar la mesa");
                    }
                }
            } catch (NumberFormatException ex) {
                mostrarAdvertencia("El número de mesa debe ser un valor numérico");
            } catch (SQLException ex) {
                mostrarError("Error al actualizar mesa: " + ex.getMessage());
            }
        });

        JButton cancelarBtn = new JButton("Cancelar");
        cancelarBtn.addActionListener(e -> dialog.dispose());

        dialog.add(guardarBtn);
        dialog.add(cancelarBtn);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void eliminarMesa(int row) {
        if (row == -1) {
            mostrarAdvertencia("Seleccione una mesa para eliminar");
            return;
        }

        int idMesa = (Integer) mesasModel.getValueAt(row, 0);
        int numeroMesa = (Integer) mesasModel.getValueAt(row, 1);

        // Verificar si la mesa tiene reservas activas
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT COUNT(*) FROM reservas WHERE id_mesa = ? AND estado IN ('pendiente', 'confirmada')")) {

            stmt.setInt(1, idMesa);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                mostrarError("No se puede eliminar la mesa porque tiene reservas activas");
                return;
            }
        } catch (SQLException e) {
            mostrarError("Error al verificar reservas: " + e.getMessage());
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar la mesa #" + numeroMesa + "?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM mesas WHERE id_mesa = ?")) {

                stmt.setInt(1, idMesa);
                int affected = stmt.executeUpdate();

                if (affected > 0) {
                    cargarMesas();
                    JOptionPane.showMessageDialog(this, "Mesa eliminada exitosamente");
                }
            } catch (SQLException e) {
                mostrarError("Error al eliminar mesa: " + e.getMessage());
            }
        }
    }

    private void cargarComidas() {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT c.id_comida, c.nombre, c.descripcion, c.precio, cat.nombre as categoria, c.disponibilidad " +
                        "FROM comidas c JOIN categorias_comida cat ON c.id_categoria = cat.id_categoria")) {

            ResultSet rs = stmt.executeQuery();
            comidasModel.setRowCount(0);

            while (rs.next()) {
                comidasModel.addRow(new Object[]{
                        rs.getInt("id_comida"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio"),
                        rs.getString("categoria"),
                        rs.getBoolean("disponibilidad")
                });
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar comidas: " + e.getMessage());
        }
    }

    private void editarComida(int row) {
        if (row == -1) {
            mostrarAdvertencia("Seleccione una comida para editar");
            return;
        }

        int idComida = (Integer) comidasModel.getValueAt(row, 0);
        String nombreActual = (String) comidasModel.getValueAt(row, 1);
        String descripcionActual = (String) comidasModel.getValueAt(row, 2);
        double precioActual = (Double) comidasModel.getValueAt(row, 3);
        String categoriaActual = (String) comidasModel.getValueAt(row, 4);
        boolean disponibleActual = (Boolean) comidasModel.getValueAt(row, 5);

        JDialog dialog = new JDialog(this, "Editar Comida", true);
        dialog.setSize(500, 400);
        dialog.setLayout(new GridLayout(0, 2, 10, 10));

        JTextField nombreField = new JTextField(nombreActual);
        JTextArea descripcionArea = new JTextArea(descripcionActual != null ? descripcionActual : "");
        JScrollPane descScroll = new JScrollPane(descripcionArea);
        JSpinner precioSpinner = new JSpinner(new SpinnerNumberModel(precioActual, 0.0, 1000.0, 0.5));

        // Combo de categorías
        JComboBox<String> categoriaCombo = new JComboBox<>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT nombre FROM categorias_comida");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categoriaCombo.addItem(rs.getString("nombre"));
            }
            categoriaCombo.setSelectedItem(categoriaActual);
        } catch (SQLException e) {
            mostrarError("Error al cargar categorías: " + e.getMessage());
        }

        JCheckBox disponibleCheck = new JCheckBox("Disponible", disponibleActual);

        dialog.add(new JLabel("Nombre:"));
        dialog.add(nombreField);
        dialog.add(new JLabel("Descripción:"));
        dialog.add(descScroll);
        dialog.add(new JLabel("Precio:"));
        dialog.add(precioSpinner);
        dialog.add(new JLabel("Categoría:"));
        dialog.add(categoriaCombo);
        dialog.add(new JLabel("Disponible:"));
        dialog.add(disponibleCheck);

        JButton guardarBtn = new JButton("Guardar");
        guardarBtn.addActionListener(e -> {
            try {
                // Validar campos
                if (nombreField.getText().isEmpty()) {
                    mostrarAdvertencia("El nombre es obligatorio");
                    return;
                }

                // Obtener ID de categoría
                int idCategoria;
                try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT id_categoria FROM categorias_comida WHERE nombre = ?")) {
                    stmt.setString(1, (String) categoriaCombo.getSelectedItem());
                    ResultSet rs = stmt.executeQuery();
                    if (!rs.next()) throw new SQLException("Categoría no encontrada");
                    idCategoria = rs.getInt("id_categoria");
                }

                // Actualizar comida
                try (PreparedStatement stmt = connection.prepareStatement(
                        "UPDATE comidas SET nombre = ?, descripcion = ?, precio = ?, " +
                                "id_categoria = ?, disponibilidad = ? WHERE id_comida = ?")) {

                    stmt.setString(1, nombreField.getText());
                    stmt.setString(2, descripcionArea.getText().isEmpty() ? null : descripcionArea.getText());
                    stmt.setDouble(3, (Double) precioSpinner.getValue());
                    stmt.setInt(4, idCategoria);
                    stmt.setBoolean(5, disponibleCheck.isSelected());
                    stmt.setInt(6, idComida);

                    int affected = stmt.executeUpdate();
                    if (affected > 0) {
                        cargarComidas();
                        dialog.dispose();
                        JOptionPane.showMessageDialog(this, "Comida actualizada exitosamente");
                    } else {
                        mostrarError("No se pudo actualizar la comida");
                    }
                }
            } catch (SQLException ex) {
                mostrarError("Error al actualizar comida: " + ex.getMessage());
            }
        });

        JButton cancelarBtn = new JButton("Cancelar");
        cancelarBtn.addActionListener(e -> dialog.dispose());

        dialog.add(guardarBtn);
        dialog.add(cancelarBtn);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void eliminarComida(int row) {
        if (row == -1) {
            mostrarAdvertencia("Seleccione una comida para eliminar");
            return;
        }

        int idComida = (Integer) comidasModel.getValueAt(row, 0);
        String nombreComida = (String) comidasModel.getValueAt(row, 1);

        // Verificar si la comida está en alguna reserva activa
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT COUNT(*) FROM reserva_comidas rc " +
                        "JOIN reservas r ON rc.id_reserva = r.id_reserva " +
                        "WHERE rc.id_comida = ? AND r.estado IN ('pendiente', 'confirmada')")) {

            stmt.setInt(1, idComida);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                mostrarError("No se puede eliminar la comida porque está incluida en reservas activas");
                return;
            }
        } catch (SQLException e) {
            mostrarError("Error al verificar reservas: " + e.getMessage());
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar la comida '" + nombreComida + "'?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                connection.setAutoCommit(false);

                // Primero eliminar de reserva_comidas (si existe en reservas pasadas)
                try (PreparedStatement stmt = connection.prepareStatement(
                        "DELETE FROM reserva_comidas WHERE id_comida = ?")) {
                    stmt.setInt(1, idComida);
                    stmt.executeUpdate();
                }

                // Luego eliminar la comida
                try (PreparedStatement stmt = connection.prepareStatement(
                        "DELETE FROM comidas WHERE id_comida = ?")) {
                    stmt.setInt(1, idComida);
                    int affected = stmt.executeUpdate();

                    if (affected > 0) {
                        connection.commit();
                        cargarComidas();
                        JOptionPane.showMessageDialog(this, "Comida eliminada exitosamente");
                    } else {
                        connection.rollback();
                        mostrarError("No se pudo eliminar la comida");
                    }
                }
            } catch (SQLException e) {
                try {
                    connection.rollback();
                    mostrarError("Error al eliminar comida: " + e.getMessage());
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
    }

    private void cargarReservas() {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT r.id_reserva, m.numero_mesa, CONCAT(u.nombre, ' ', u.apellido) as usuario, " +
                        "r.fecha_reserva, r.hora_reserva, r.numero_personas, r.estado, " +
                        "(SELECT COUNT(*) FROM reserva_comidas rc WHERE rc.id_reserva = r.id_reserva) as num_comidas " +
                        "FROM reservas r " +
                        "JOIN mesas m ON r.id_mesa = m.id_mesa " +
                        "JOIN usuarios u ON r.id_usuario = u.id_usuario " +
                        "ORDER BY r.fecha_reserva DESC, r.hora_reserva DESC")) {

            ResultSet rs = stmt.executeQuery();
            reservasModel.setRowCount(0);

            while (rs.next()) {
                reservasModel.addRow(new Object[]{
                        rs.getInt("id_reserva"),
                        rs.getInt("numero_mesa"),
                        rs.getString("usuario"),
                        rs.getDate("fecha_reserva"),
                        rs.getTime("hora_reserva"),
                        rs.getInt("numero_personas"),
                        rs.getString("estado"),
                        rs.getInt("num_comidas")
                });
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar reservas: " + e.getMessage());
        }
    }

    private void cargarCategorias() {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id_categoria, nombre, descripcion FROM categorias_comida")) {

            ResultSet rs = stmt.executeQuery();
            categoriasModel.setRowCount(0);

            while (rs.next()) {
                categoriasModel.addRow(new Object[]{
                        rs.getInt("id_categoria"),
                        rs.getString("nombre"),
                        rs.getString("descripcion")
                });
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar categorías: " + e.getMessage());
        }
    }

    private void editarCategoria(int row) {
        if (row == -1) {
            mostrarAdvertencia("Seleccione una categoría para editar");
            return;
        }

        int idCategoria = (Integer) categoriasModel.getValueAt(row, 0);
        String nombreActual = (String) categoriasModel.getValueAt(row, 1);
        String descripcionActual = (String) categoriasModel.getValueAt(row, 2);

        JDialog dialog = new JDialog(this, "Editar Categoría", true);
        dialog.setSize(400, 200);
        dialog.setLayout(new GridLayout(0, 2, 10, 10));

        JTextField nombreField = new JTextField(nombreActual);
        JTextArea descripcionArea = new JTextArea(descripcionActual != null ? descripcionActual : "");
        JScrollPane descScroll = new JScrollPane(descripcionArea);

        dialog.add(new JLabel("Nombre:"));
        dialog.add(nombreField);
        dialog.add(new JLabel("Descripción:"));
        dialog.add(descScroll);

        JButton guardarBtn = new JButton("Guardar");
        guardarBtn.addActionListener(e -> {
            if (nombreField.getText().isEmpty()) {
                mostrarAdvertencia("El nombre es obligatorio");
                return;
            }

            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE categorias_comida SET nombre = ?, descripcion = ? WHERE id_categoria = ?")) {

                stmt.setString(1, nombreField.getText());
                stmt.setString(2, descripcionArea.getText().isEmpty() ? null : descripcionArea.getText());
                stmt.setInt(3, idCategoria);

                int affected = stmt.executeUpdate();
                if (affected > 0) {
                    cargarCategorias();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Categoría actualizada exitosamente");
                } else {
                    mostrarError("No se pudo actualizar la categoría");
                }
            } catch (SQLException ex) {
                mostrarError("Error al actualizar categoría: " + ex.getMessage());
            }
        });

        JButton cancelarBtn = new JButton("Cancelar");
        cancelarBtn.addActionListener(e -> dialog.dispose());

        dialog.add(guardarBtn);
        dialog.add(cancelarBtn);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void eliminarCategoria(int row) {
        if (row == -1) {
            mostrarAdvertencia("Seleccione una categoría para eliminar");
            return;
        }

        int idCategoria = (Integer) categoriasModel.getValueAt(row, 0);
        String nombreCategoria = (String) categoriasModel.getValueAt(row, 1);

        // Verificar si la categoría tiene comidas asociadas
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT COUNT(*) FROM comidas WHERE id_categoria = ?")) {

            stmt.setInt(1, idCategoria);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                mostrarError("No se puede eliminar la categoría porque tiene comidas asociadas");
                return;
            }
        } catch (SQLException e) {
            mostrarError("Error al verificar comidas asociadas: " + e.getMessage());
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar la categoría '" + nombreCategoria + "'?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM categorias_comida WHERE id_categoria = ?")) {

                stmt.setInt(1, idCategoria);
                int affected = stmt.executeUpdate();

                if (affected > 0) {
                    cargarCategorias();
                    JOptionPane.showMessageDialog(this, "Categoría eliminada exitosamente");
                }
            } catch (SQLException e) {
                mostrarError("Error al eliminar categoría: " + e.getMessage());
            }
        }
    }

    private void cargarUbicaciones() {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id_ubicacion, nombre, descripcion FROM ubicaciones_mesa")) {

            ResultSet rs = stmt.executeQuery();
            ubicacionesModel.setRowCount(0);

            while (rs.next()) {
                ubicacionesModel.addRow(new Object[]{
                        rs.getInt("id_ubicacion"),
                        rs.getString("nombre"),
                        rs.getString("descripcion")
                });
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar ubicaciones: " + e.getMessage());
        }
    }

    private void editarUbicacion(int row) {
        if (row == -1) {
            mostrarAdvertencia("Seleccione una ubicación para editar");
            return;
        }

        int idUbicacion = (Integer) ubicacionesModel.getValueAt(row, 0);
        String nombreActual = (String) ubicacionesModel.getValueAt(row, 1);
        String descripcionActual = (String) ubicacionesModel.getValueAt(row, 2);

        JDialog dialog = new JDialog(this, "Editar Ubicación", true);
        dialog.setSize(400, 200);
        dialog.setLayout(new GridLayout(0, 2, 10, 10));

        JTextField nombreField = new JTextField(nombreActual);
        JTextArea descripcionArea = new JTextArea(descripcionActual != null ? descripcionActual : "");
        JScrollPane descScroll = new JScrollPane(descripcionArea);

        dialog.add(new JLabel("Nombre:"));
        dialog.add(nombreField);
        dialog.add(new JLabel("Descripción:"));
        dialog.add(descScroll);

        JButton guardarBtn = new JButton("Guardar");
        guardarBtn.addActionListener(e -> {
            if (nombreField.getText().isEmpty()) {
                mostrarAdvertencia("El nombre es obligatorio");
                return;
            }

            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE ubicaciones_mesa SET nombre = ?, descripcion = ? WHERE id_ubicacion = ?")) {

                stmt.setString(1, nombreField.getText());
                stmt.setString(2, descripcionArea.getText().isEmpty() ? null : descripcionArea.getText());
                stmt.setInt(3, idUbicacion);

                int affected = stmt.executeUpdate();
                if (affected > 0) {
                    cargarUbicaciones();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Ubicación actualizada exitosamente");
                } else {
                    mostrarError("No se pudo actualizar la ubicación");
                }
            } catch (SQLException ex) {
                mostrarError("Error al actualizar ubicación: " + ex.getMessage());
            }
        });

        JButton cancelarBtn = new JButton("Cancelar");
        cancelarBtn.addActionListener(e -> dialog.dispose());

        dialog.add(guardarBtn);
        dialog.add(cancelarBtn);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void eliminarUbicacion(int row) {
        if (row == -1) {
            mostrarAdvertencia("Seleccione una ubicación para eliminar");
            return;
        }

        int idUbicacion = (Integer) ubicacionesModel.getValueAt(row, 0);
        String nombreUbicacion = (String) ubicacionesModel.getValueAt(row, 1);

        // Verificar si la ubicación tiene mesas asociadas
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT COUNT(*) FROM mesas WHERE id_ubicacion = ?")) {

            stmt.setInt(1, idUbicacion);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                mostrarError("No se puede eliminar la ubicación porque tiene mesas asociadas");
                return;
            }
        } catch (SQLException e) {
            mostrarError("Error al verificar mesas asociadas: " + e.getMessage());
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar la ubicación '" + nombreUbicacion + "'?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM ubicaciones_mesa WHERE id_ubicacion = ?")) {

                stmt.setInt(1, idUbicacion);
                int affected = stmt.executeUpdate();

                if (affected > 0) {
                    cargarUbicaciones();
                    JOptionPane.showMessageDialog(this, "Ubicación eliminada exitosamente");
                }
            } catch (SQLException e) {
                mostrarError("Error al eliminar ubicación: " + e.getMessage());
            }
        }
    }

    private void agregarUsuario() {
        JDialog dialog = new JDialog(this, "Agregar Usuario", true);
        dialog.setSize(400, 350);
        dialog.setLayout(new GridLayout(0, 2, 10, 10));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField nombreField = new JTextField();
        JTextField apellidoField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField telefonoField = new JTextField();

        // Obtener roles para el combo
        JComboBox<String> rolCombo = new JComboBox<>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT nombre_rol FROM roles");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rolCombo.addItem(rs.getString("nombre_rol"));
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar roles: " + e.getMessage());
        }

        JCheckBox activoCheck = new JCheckBox("Activo", true);

        dialog.add(new JLabel("Usuario:"));
        dialog.add(usernameField);
        dialog.add(new JLabel("Contraseña:"));
        dialog.add(passwordField);
        dialog.add(new JLabel("Nombre:"));
        dialog.add(nombreField);
        dialog.add(new JLabel("Apellido:"));
        dialog.add(apellidoField);
        dialog.add(new JLabel("Email:"));
        dialog.add(emailField);
        dialog.add(new JLabel("Teléfono:"));
        dialog.add(telefonoField);
        dialog.add(new JLabel("Rol:"));
        dialog.add(rolCombo);
        dialog.add(new JLabel("Activo:"));
        dialog.add(activoCheck);

        JButton guardarBtn = new JButton("Guardar");
        guardarBtn.addActionListener(e -> {
            // Validar campos
            if (usernameField.getText().isEmpty() || passwordField.getPassword().length == 0 ||
                    nombreField.getText().isEmpty() || apellidoField.getText().isEmpty()) {
                mostrarAdvertencia("Usuario, contraseña, nombre y apellido son obligatorios");
                return;
            }

            try {
                // Obtener ID del rol seleccionado
                int idRol;
                try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT id_rol FROM roles WHERE nombre_rol = ?")) {
                    stmt.setString(1, (String) rolCombo.getSelectedItem());
                    ResultSet rs = stmt.executeQuery();
                    if (!rs.next()) throw new SQLException("Rol no encontrado");
                    idRol = rs.getInt("id_rol");
                }

                // Insertar usuario
                try (PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO usuarios (username, password, nombre, apellido, email, telefono, id_rol, activo) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {

                    stmt.setString(1, usernameField.getText());
                    stmt.setString(2, hashSHA256(new String(passwordField.getPassword())));
                    stmt.setString(3, nombreField.getText());
                    stmt.setString(4, apellidoField.getText());
                    stmt.setString(5, emailField.getText().isEmpty() ? null : emailField.getText());
                    stmt.setString(6, telefonoField.getText().isEmpty() ? null : telefonoField.getText());
                    stmt.setInt(7, idRol);
                    stmt.setBoolean(8, activoCheck.isSelected());

                    stmt.executeUpdate();
                    cargarUsuarios();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Usuario agregado exitosamente");
                }
            } catch (SQLException ex) {
                mostrarError("Error al agregar usuario: " + ex.getMessage());
            }
        });

        JButton cancelarBtn = new JButton("Cancelar");
        cancelarBtn.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        buttonPanel.add(guardarBtn);
        buttonPanel.add(cancelarBtn);

        dialog.add(new JLabel());
        dialog.add(buttonPanel);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String hashSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            mostrarError("Error en el sistema de seguridad");
            return null;
        }
    }

    private void editarUsuario(int row) {
        if (row == -1) {
            mostrarAdvertencia("Seleccione un usuario para editar");
            return;
        }

        int idUsuario = (Integer) usuariosModel.getValueAt(row, 0);

        // Obtener los datos actuales del usuario
        String username = "";
        String nombre = "";
        String apellido = "";
        String email = "";
        String telefono = "";
        String rolActual = "";
        boolean activo = false;

        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT u.username, u.nombre, u.apellido, u.email, u.telefono, r.nombre_rol, u.activo " +
                        "FROM usuarios u JOIN roles r ON u.id_rol = r.id_rol " +
                        "WHERE u.id_usuario = ?")) {

            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                username = rs.getString("username");
                nombre = rs.getString("nombre");
                apellido = rs.getString("apellido");
                email = rs.getString("email");
                telefono = rs.getString("telefono");
                rolActual = rs.getString("nombre_rol");
                activo = rs.getBoolean("activo");
            } else {
                mostrarError("Usuario no encontrado");
                return;
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar datos del usuario: " + e.getMessage());
            return;
        }

        // Crear el diálogo de edición
        JDialog dialog = new JDialog(this, "Editar Usuario", true);
        dialog.setSize(400, 400);
        dialog.setLayout(new GridLayout(0, 2, 10, 10));

        JTextField usernameField = new JTextField(username);
        JPasswordField passwordField = new JPasswordField();
        JTextField nombreField = new JTextField(nombre);
        JTextField apellidoField = new JTextField(apellido);
        JTextField emailField = new JTextField(email != null ? email : "");
        JTextField telefonoField = new JTextField(telefono != null ? telefono : "");

        // Combo de roles
        JComboBox<String> rolCombo = new JComboBox<>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT nombre_rol FROM roles");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rolCombo.addItem(rs.getString("nombre_rol"));
            }
            rolCombo.setSelectedItem(rolActual);
        } catch (SQLException e) {
            mostrarError("Error al cargar roles: " + e.getMessage());
        }

        JCheckBox activoCheck = new JCheckBox("Activo", activo);
        JCheckBox cambiarPassCheck = new JCheckBox("Cambiar contraseña");

        // Campos iniciales
        usernameField.setEnabled(false); // No permitir cambiar el username
        passwordField.setEnabled(false);

        // Listener para el checkbox de cambiar contraseña
        cambiarPassCheck.addActionListener(e -> {
            passwordField.setEnabled(cambiarPassCheck.isSelected());
            if (!cambiarPassCheck.isSelected()) {
                passwordField.setText("");
            }
        });

        // Agregar componentes al diálogo
        dialog.add(new JLabel("Usuario:"));
        dialog.add(usernameField);
        dialog.add(new JLabel("Contraseña:"));
        dialog.add(passwordField);
        dialog.add(new JLabel("Cambiar contraseña:"));
        dialog.add(cambiarPassCheck);
        dialog.add(new JLabel("Nombre:"));
        dialog.add(nombreField);
        dialog.add(new JLabel("Apellido:"));
        dialog.add(apellidoField);
        dialog.add(new JLabel("Email:"));
        dialog.add(emailField);
        dialog.add(new JLabel("Teléfono:"));
        dialog.add(telefonoField);
        dialog.add(new JLabel("Rol:"));
        dialog.add(rolCombo);
        dialog.add(new JLabel("Activo:"));
        dialog.add(activoCheck);

        // Botones
        JButton guardarBtn = new JButton("Guardar");
        guardarBtn.addActionListener(e -> {
            // Validar campos obligatorios
            if (nombreField.getText().isEmpty() || apellidoField.getText().isEmpty()) {
                mostrarAdvertencia("Nombre y apellido son obligatorios");
                return;
            }

            // Validar contraseña si se va a cambiar
            if (cambiarPassCheck.isSelected() && passwordField.getPassword().length == 0) {
                mostrarAdvertencia("Debe ingresar una nueva contraseña");
                return;
            }

            try {
                // Obtener ID del rol seleccionado
                int idRol;
                try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT id_rol FROM roles WHERE nombre_rol = ?")) {
                    stmt.setString(1, (String) rolCombo.getSelectedItem());
                    ResultSet rs = stmt.executeQuery();
                    if (!rs.next()) throw new SQLException("Rol no encontrado");
                    idRol = rs.getInt("id_rol");
                }

                // Construir la consulta SQL según si se cambia la contraseña o no
                String sql;
                if (cambiarPassCheck.isSelected()) {
                    sql = "UPDATE usuarios SET nombre = ?, apellido = ?, email = ?, telefono = ?, " +
                            "id_rol = ?, activo = ?, password = ? WHERE id_usuario = ?";
                } else {
                    sql = "UPDATE usuarios SET nombre = ?, apellido = ?, email = ?, telefono = ?, " +
                            "id_rol = ?, activo = ? WHERE id_usuario = ?";
                }

                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    int paramIndex = 1;
                    stmt.setString(paramIndex++, nombreField.getText());
                    stmt.setString(paramIndex++, apellidoField.getText());
                    stmt.setString(paramIndex++, emailField.getText().isEmpty() ? null : emailField.getText());
                    stmt.setString(paramIndex++, telefonoField.getText().isEmpty() ? null : telefonoField.getText());
                    stmt.setInt(paramIndex++, idRol);
                    stmt.setBoolean(paramIndex++, activoCheck.isSelected());

                    if (cambiarPassCheck.isSelected()) {
                        stmt.setString(paramIndex++, hashSHA256(new String(passwordField.getPassword())));
                    }

                    stmt.setInt(paramIndex++, idUsuario);

                    int affected = stmt.executeUpdate();
                    if (affected > 0) {
                        cargarUsuarios();
                        dialog.dispose();
                        JOptionPane.showMessageDialog(this, "Usuario actualizado exitosamente");
                    } else {
                        mostrarError("No se pudo actualizar el usuario");
                    }
                }
            } catch (SQLException ex) {
                mostrarError("Error al actualizar usuario: " + ex.getMessage());
            }
        });

        JButton cancelarBtn = new JButton("Cancelar");
        cancelarBtn.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        buttonPanel.add(guardarBtn);
        buttonPanel.add(cancelarBtn);

        dialog.add(new JLabel());
        dialog.add(buttonPanel);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void eliminarUsuario(int row) {
        if (row == -1) {
            mostrarAdvertencia("Seleccione un usuario para eliminar");
            return;
        }

        int idUsuario = (Integer) usuariosModel.getValueAt(row, 0);
        String username = (String) usuariosModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar al usuario '" + username + "'?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM usuarios WHERE id_usuario = ?")) {

                stmt.setInt(1, idUsuario);
                int affected = stmt.executeUpdate();

                if (affected > 0) {
                    cargarUsuarios();
                    JOptionPane.showMessageDialog(this, "Usuario eliminado exitosamente");
                }
            } catch (SQLException e) {
                mostrarError("Error al eliminar usuario: " + e.getMessage());
            }
        }
    }
    private void agregarMesa() {
        JDialog dialog = new JDialog(this, "Agregar Mesa", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new GridLayout(0, 2, 10, 10));

        JTextField numeroField = new JTextField();
        JSpinner capacidadSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 20, 1));

        // Combo de ubicaciones
        JComboBox<String> ubicacionCombo = new JComboBox<>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT nombre FROM ubicaciones_mesa");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ubicacionCombo.addItem(rs.getString("nombre"));
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar ubicaciones: " + e.getMessage());
        }

        JComboBox<String> estadoCombo = new JComboBox<>(new String[]{"disponible", "mantenimiento"});

        dialog.add(new JLabel("Número de mesa:"));
        dialog.add(numeroField);
        dialog.add(new JLabel("Capacidad:"));
        dialog.add(capacidadSpinner);
        dialog.add(new JLabel("Ubicación:"));
        dialog.add(ubicacionCombo);
        dialog.add(new JLabel("Estado inicial:"));
        dialog.add(estadoCombo);

        JButton guardarBtn = new JButton("Guardar");
        guardarBtn.addActionListener(e -> {
            try {
                // Validar campos
                if (numeroField.getText().isEmpty()) {
                    mostrarAdvertencia("El número de mesa es obligatorio");
                    return;
                }

                // Obtener ID de ubicación
                int idUbicacion;
                try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT id_ubicacion FROM ubicaciones_mesa WHERE nombre = ?")) {
                    stmt.setString(1, (String) ubicacionCombo.getSelectedItem());
                    ResultSet rs = stmt.executeQuery();
                    if (!rs.next()) throw new SQLException("Ubicación no encontrada");
                    idUbicacion = rs.getInt("id_ubicacion");
                }

                // Insertar mesa
                try (PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO mesas (numero_mesa, capacidad, id_ubicacion, estado) " +
                                "VALUES (?, ?, ?, ?)")) {

                    stmt.setInt(1, Integer.parseInt(numeroField.getText()));
                    stmt.setInt(2, (Integer) capacidadSpinner.getValue());
                    stmt.setInt(3, idUbicacion);
                    stmt.setString(4, (String) estadoCombo.getSelectedItem());

                    stmt.executeUpdate();
                    cargarMesas();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Mesa agregada exitosamente");
                }
            } catch (NumberFormatException ex) {
                mostrarAdvertencia("El número de mesa debe ser un valor numérico");
            } catch (SQLException ex) {
                mostrarError("Error al agregar mesa: " + ex.getMessage());
            }
        });

        JButton cancelarBtn = new JButton("Cancelar");
        cancelarBtn.addActionListener(e -> dialog.dispose());

        dialog.add(guardarBtn);
        dialog.add(cancelarBtn);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void agregarComida() {
        JDialog dialog = new JDialog(this, "Agregar Comida", true);
        dialog.setSize(500, 400);
        dialog.setLayout(new GridLayout(0, 2, 10, 10));

        JTextField nombreField = new JTextField();
        JTextArea descripcionArea = new JTextArea(3, 20);
        JScrollPane descScroll = new JScrollPane(descripcionArea);
        JSpinner precioSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000.0, 0.5));

        // Combo de categorías
        JComboBox<String> categoriaCombo = new JComboBox<>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT nombre FROM categorias_comida");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categoriaCombo.addItem(rs.getString("nombre"));
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar categorías: " + e.getMessage());
        }

        JCheckBox disponibleCheck = new JCheckBox("Disponible", true);

        dialog.add(new JLabel("Nombre:"));
        dialog.add(nombreField);
        dialog.add(new JLabel("Descripción:"));
        dialog.add(descScroll);
        dialog.add(new JLabel("Precio:"));
        dialog.add(precioSpinner);
        dialog.add(new JLabel("Categoría:"));
        dialog.add(categoriaCombo);
        dialog.add(new JLabel("Disponible:"));
        dialog.add(disponibleCheck);

        JButton guardarBtn = new JButton("Guardar");
        guardarBtn.addActionListener(e -> {
            try {
                // Validar campos
                if (nombreField.getText().isEmpty()) {
                    mostrarAdvertencia("El nombre es obligatorio");
                    return;
                }

                // Obtener ID de categoría
                int idCategoria;
                try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT id_categoria FROM categorias_comida WHERE nombre = ?")) {
                    stmt.setString(1, (String) categoriaCombo.getSelectedItem());
                    ResultSet rs = stmt.executeQuery();
                    if (!rs.next()) throw new SQLException("Categoría no encontrada");
                    idCategoria = rs.getInt("id_categoria");
                }

                // Insertar comida
                try (PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO comidas (nombre, descripcion, precio, id_categoria, disponibilidad) " +
                                "VALUES (?, ?, ?, ?, ?)")) {

                    stmt.setString(1, nombreField.getText());
                    stmt.setString(2, descripcionArea.getText().isEmpty() ? null : descripcionArea.getText());
                    stmt.setDouble(3, (Double) precioSpinner.getValue());
                    stmt.setInt(4, idCategoria);
                    stmt.setBoolean(5, disponibleCheck.isSelected());

                    stmt.executeUpdate();
                    cargarComidas();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Comida agregada exitosamente");
                }
            } catch (SQLException ex) {
                mostrarError("Error al agregar comida: " + ex.getMessage());
            }
        });

        JButton cancelarBtn = new JButton("Cancelar");
        cancelarBtn.addActionListener(e -> dialog.dispose());

        dialog.add(guardarBtn);
        dialog.add(cancelarBtn);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void agregarCategoria() {
        JDialog dialog = new JDialog(this, "Agregar Categoría", true);
        dialog.setSize(400, 200);
        dialog.setLayout(new GridLayout(0, 2, 10, 10));

        JTextField nombreField = new JTextField();
        JTextArea descripcionArea = new JTextArea(3, 20);
        JScrollPane descScroll = new JScrollPane(descripcionArea);

        dialog.add(new JLabel("Nombre:"));
        dialog.add(nombreField);
        dialog.add(new JLabel("Descripción:"));
        dialog.add(descScroll);

        JButton guardarBtn = new JButton("Guardar");
        guardarBtn.addActionListener(e -> {
            if (nombreField.getText().isEmpty()) {
                mostrarAdvertencia("El nombre es obligatorio");
                return;
            }

            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO categorias_comida (nombre, descripcion) VALUES (?, ?)")) {

                stmt.setString(1, nombreField.getText());
                stmt.setString(2, descripcionArea.getText().isEmpty() ? null : descripcionArea.getText());

                stmt.executeUpdate();
                cargarCategorias();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Categoría agregada exitosamente");
            } catch (SQLException ex) {
                mostrarError("Error al agregar categoría: " + ex.getMessage());
            }
        });

        JButton cancelarBtn = new JButton("Cancelar");
        cancelarBtn.addActionListener(e -> dialog.dispose());

        dialog.add(guardarBtn);
        dialog.add(cancelarBtn);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void agregarUbicacion() {
        JDialog dialog = new JDialog(this, "Agregar Ubicación", true);
        dialog.setSize(400, 200);
        dialog.setLayout(new GridLayout(0, 2, 10, 10));

        JTextField nombreField = new JTextField();
        JTextArea descripcionArea = new JTextArea(3, 20);
        JScrollPane descScroll = new JScrollPane(descripcionArea);

        dialog.add(new JLabel("Nombre:"));
        dialog.add(nombreField);
        dialog.add(new JLabel("Descripción:"));
        dialog.add(descScroll);

        JButton guardarBtn = new JButton("Guardar");
        guardarBtn.addActionListener(e -> {
            if (nombreField.getText().isEmpty()) {
                mostrarAdvertencia("El nombre es obligatorio");
                return;
            }

            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO ubicaciones_mesa (nombre, descripcion) VALUES (?, ?)")) {

                stmt.setString(1, nombreField.getText());
                stmt.setString(2, descripcionArea.getText().isEmpty() ? null : descripcionArea.getText());

                stmt.executeUpdate();
                cargarUbicaciones();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Ubicación agregada exitosamente");
            } catch (SQLException ex) {
                mostrarError("Error al agregar ubicación: " + ex.getMessage());
            }
        });

        JButton cancelarBtn = new JButton("Cancelar");
        cancelarBtn.addActionListener(e -> dialog.dispose());

        dialog.add(guardarBtn);
        dialog.add(cancelarBtn);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Métodos similares para mesas, comidas, categorías, ubicaciones...

    private void cancelarReserva(int row) {
        if (row == -1) {
            mostrarAdvertencia("Seleccione una reserva para cancelar");
            return;
        }

        int idReserva = (Integer) reservasModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de cancelar esta reserva?",
                "Confirmar Cancelación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE reservas SET estado = 'cancelada' WHERE id_reserva = ?")) {

                stmt.setInt(1, idReserva);
                stmt.executeUpdate();
                cargarReservas();
                JOptionPane.showMessageDialog(this, "Reserva cancelada exitosamente");

            } catch (SQLException e) {
                mostrarError("Error al cancelar reserva: " + e.getMessage());
            }
        }
    }

    private void verDetallesReserva(int row) {
        if (row == -1) {
            mostrarAdvertencia("Seleccione una reserva para ver detalles");
            return;
        }

        int idReserva = (Integer) reservasModel.getValueAt(row, 0);

        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT rc.cantidad, c.nombre, c.precio " +
                        "FROM reserva_comidas rc " +
                        "JOIN comidas c ON rc.id_comida = c.id_comida " +
                        "WHERE rc.id_reserva = ?")) {

            stmt.setInt(1, idReserva);
            ResultSet rs = stmt.executeQuery();

            StringBuilder detalles = new StringBuilder();
            detalles.append("Detalles de la reserva #").append(idReserva).append("\n\n");
            detalles.append("Comidas seleccionadas:\n");

            double total = 0;
            while (rs.next()) {
                int cantidad = rs.getInt("cantidad");
                String nombre = rs.getString("nombre");
                double precio = rs.getDouble("precio");
                double subtotal = cantidad * precio;

                detalles.append(String.format("- %d x %s: $%.2f (Subtotal: $%.2f)\n",
                        cantidad, nombre, precio, subtotal));
                total += subtotal;
            }

            detalles.append("\nTotal: $").append(String.format("%.2f", total));

            JOptionPane.showMessageDialog(this, detalles.toString(),
                    "Detalles de Reserva", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            mostrarError("Error al obtener detalles: " + e.getMessage());
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