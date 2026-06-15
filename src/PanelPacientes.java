import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * CLASE: PanelPacientes (JPanel)
 * ─────────────────────────────────────────────
 * Módulo visual completo para gestión de pacientes.
 *
 * Contiene:
 *  - JTable con la lista de pacientes
 *  - Formulario de registro en un JDialog
 *  - Botones: Registrar, Ver Historial, Dar de Baja, Buscar
 *
 * Usa el GestorPacientes existente — no modifica la lógica.
 */
public class PanelPacientes extends JPanel {

    private final GestorPacientes gestor;

    // ── Componentes de la tabla ───────────────────────────────────
    private JTable          tabla;
    private DefaultTableModel modeloTabla;
    private JTextField      campoBusqueda;

    // ── Colores del tema ──────────────────────────────────────────
    private static final Color AZUL_PRIMARIO  = new Color(31, 78, 121);
    private static final Color AZUL_SUAVE     = new Color(235, 243, 251);
    private static final Color VERDE          = new Color(40, 167, 69);
    private static final Color ROJO           = new Color(220, 53, 69);

    public PanelPacientes(GestorPacientes gestor) {
        this.gestor = gestor;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(crearPanelTabla(),    BorderLayout.CENTER);
        add(crearPanelBotones(),  BorderLayout.SOUTH);

        actualizarTabla(); // cargar datos iniciales
    }

    // ── Panel superior: título + búsqueda ─────────────────────────
    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel titulo = new JLabel("Gestión de Pacientes");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setForeground(AZUL_PRIMARIO);
        panel.add(titulo, BorderLayout.WEST);

        // Barra de búsqueda
        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        busqueda.setBackground(Color.WHITE);
        campoBusqueda = new JTextField(20);
        campoBusqueda.setFont(new Font("Arial", Font.PLAIN, 13));
        campoBusqueda.putClientProperty("JTextField.placeholderText", "Buscar por nombre o DNI…");
        JButton btnBuscar = crearBoton("🔍 Buscar", AZUL_PRIMARIO);
        JButton btnLimpiar = crearBoton("✕ Limpiar", Color.GRAY);

        btnBuscar.addActionListener(e -> buscarPaciente());
        btnLimpiar.addActionListener(e -> { campoBusqueda.setText(""); actualizarTabla(); });

        busqueda.add(new JLabel("Buscar: "));
        busqueda.add(campoBusqueda);
        busqueda.add(btnBuscar);
        busqueda.add(btnLimpiar);
        panel.add(busqueda, BorderLayout.EAST);

        return panel;
    }

    // ── Tabla de pacientes ────────────────────────────────────────
    private JScrollPane crearPanelTabla() {
        String[] columnas = {"ID", "Nombre Completo", "DNI", "Teléfono",
                             "Correo", "F. Nacimiento", "Sangre", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabla = new JTable(modeloTabla);
        tabla.setFont(new Font("Arial", Font.PLAIN, 13));
        tabla.setRowHeight(28);
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(AZUL_PRIMARIO);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.setSelectionBackground(AZUL_SUAVE);
        tabla.setGridColor(new Color(220, 220, 220));

        // Anchos de columna
        int[] anchos = {80, 160, 100, 110, 160, 100, 70, 80};
        for (int i = 0; i < anchos.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        return scroll;
    }

    // ── Panel inferior: botones de acción ─────────────────────────
    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        JButton btnRegistrar  = crearBoton("➕ Registrar Paciente", VERDE);
        JButton btnHistorial  = crearBoton("📋 Ver Historial",      AZUL_PRIMARIO);
        JButton btnDarBaja    = crearBoton("🚫 Dar de Baja",        ROJO);

        btnRegistrar.addActionListener(e -> mostrarFormularioRegistro());
        btnHistorial.addActionListener(e -> verHistorial());
        btnDarBaja.addActionListener(e   -> darDeBajaPaciente());

        panel.add(btnRegistrar);
        panel.add(btnHistorial);
        panel.add(btnDarBaja);

        // Contador de registros
        JLabel lblTotal = new JLabel();
        lblTotal.setFont(new Font("Arial", Font.ITALIC, 12));
        lblTotal.setForeground(Color.GRAY);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(lblTotal);

        return panel;
    }

    // ── Formulario de registro ────────────────────────────────────
    private void mostrarFormularioRegistro() {
        JDialog dialogo = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Registrar Nuevo Paciente", true);
        dialogo.setSize(480, 480);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout());

        // Panel del formulario con GridBagLayout para alineación precisa
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Campos del formulario
        String[] etiquetas = {"Nombre:", "Apellido:", "DNI:", "Teléfono:",
                              "Correo:", "Fecha Nac.:", "Grupo Sang.:", "Alergias:"};
        JTextField[] campos = new JTextField[etiquetas.length];

        for (int i = 0; i < etiquetas.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            JLabel lbl = new JLabel(etiquetas[i]);
            lbl.setFont(new Font("Arial", Font.PLAIN, 13));
            form.add(lbl, gbc);

            gbc.gridx = 1; gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            campos[i] = new JTextField(20);
            campos[i].setFont(new Font("Arial", Font.PLAIN, 13));
            form.add(campos[i], gbc);
        }

        dialogo.add(new JScrollPane(form), BorderLayout.CENTER);

        // Botones del diálogo
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        botones.setBackground(Color.WHITE);
        JButton btnGuardar  = crearBoton("💾 Guardar", VERDE);
        JButton btnCancelar = crearBoton("Cancelar",   Color.GRAY);

        btnGuardar.addActionListener(e -> {
            // Validar campos no vacíos
            for (int i = 0; i < campos.length; i++) {
                if (campos[i].getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialogo,
                            "El campo '" + etiquetas[i].replace(":", "") + "' es obligatorio.",
                            "Campo vacío", JOptionPane.WARNING_MESSAGE);
                    campos[i].requestFocus();
                    return;
                }
            }
            Paciente p = gestor.registrarPaciente(
                    campos[0].getText().trim(), campos[1].getText().trim(),
                    campos[2].getText().trim(), campos[3].getText().trim(),
                    campos[4].getText().trim(), campos[5].getText().trim(),
                    campos[6].getText().trim(), campos[7].getText().trim());

            if (p != null) {
                JOptionPane.showMessageDialog(dialogo,
                        "✔ Paciente registrado con ID: " + p.getId(),
                        "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
                actualizarTabla();
                dialogo.dispose();
            } else {
                JOptionPane.showMessageDialog(dialogo,
                        "⚠ Ya existe un paciente con ese DNI.",
                        "DNI duplicado", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancelar.addActionListener(e -> dialogo.dispose());
        botones.add(btnGuardar);
        botones.add(btnCancelar);
        dialogo.add(botones, BorderLayout.SOUTH);
        dialogo.setVisible(true);
    }

    // ── Ver historial de paciente seleccionado ────────────────────
    private void verHistorial() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un paciente de la tabla.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) modeloTabla.getValueAt(fila, 0);
        Paciente p = gestor.buscarPorId(id);
        if (p == null) return;

        String historial = p.getHistorial().isEmpty()
                ? "Este paciente no tiene registros en su historial médico."
                : p.getHistorial();

        JTextArea area = new JTextArea(historial);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JOptionPane.showMessageDialog(this,
                new JScrollPane(area),
                "Historial de " + p.getNombreCompleto(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Dar de baja al paciente seleccionado ──────────────────────
    private void darDeBajaPaciente() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un paciente de la tabla.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id     = (String) modeloTabla.getValueAt(fila, 0);
        String nombre = (String) modeloTabla.getValueAt(fila, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Dar de baja a: " + nombre + "?",
                "Confirmar baja", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            gestor.darDeBaja(id);
            actualizarTabla();
            JOptionPane.showMessageDialog(this, "✔ Paciente dado de baja correctamente.",
                    "Baja registrada", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ── Buscar pacientes ──────────────────────────────────────────
    private void buscarPaciente() {
        String texto = campoBusqueda.getText().trim();
        if (texto.isEmpty()) { actualizarTabla(); return; }

        modeloTabla.setRowCount(0);
        ArrayList<Paciente> resultados = gestor.buscarPorNombre(texto);

        // Si no encontró por nombre, intenta por DNI
        if (resultados.isEmpty()) {
            Paciente p = gestor.buscarPorDni(texto);
            if (p != null) resultados.add(p);
        }

        for (Paciente p : resultados)
            modeloTabla.addRow(new Object[]{
                p.getId(), p.getNombreCompleto(), p.getDni(), p.getTelefono(),
                p.getCorreo(), p.getFechaNacimiento(), p.getGrupoSanguineo(),
                p.isActivo() ? "Activo" : "Inactivo"
            });

        if (resultados.isEmpty())
            JOptionPane.showMessageDialog(this, "No se encontraron resultados para: " + texto,
                    "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Actualizar tabla desde el gestor ──────────────────────────
    public void actualizarTabla() {
        modeloTabla.setRowCount(0); // limpiar
        for (Paciente p : gestor.getTodos())
            modeloTabla.addRow(new Object[]{
                p.getId(), p.getNombreCompleto(), p.getDni(), p.getTelefono(),
                p.getCorreo(), p.getFechaNacimiento(), p.getGrupoSanguineo(),
                p.isActivo() ? "Activo" : "Inactivo"
            });
    }

    // ── Helper: crear botón con estilo uniforme ───────────────────
    private JButton crearBoton(String texto, Color fondo) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setBackground(fondo);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }
}
