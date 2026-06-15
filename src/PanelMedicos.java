import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * CLASE: PanelMedicos (JPanel)
 * ─────────────────────────────────────────────
 * Módulo visual para gestión de médicos.
 */
public class PanelMedicos extends JPanel {

    private final GestorMedicos gestor;
    private JTable              tabla;
    private DefaultTableModel   modeloTabla;

    private static final Color AZUL  = new Color(31, 78, 121);
    private static final Color VERDE = new Color(40, 167, 69);
    private static final Color AZUL_SUAVE = new Color(235, 243, 251);

    public PanelMedicos(GestorMedicos gestor) {
        this.gestor = gestor;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        add(crearTitulo(),    BorderLayout.NORTH);
        add(crearTabla(),     BorderLayout.CENTER);
        add(crearBotones(),   BorderLayout.SOUTH);

        actualizarTabla();
    }

    private JLabel crearTitulo() {
        JLabel titulo = new JLabel("Gestión de Médicos");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setForeground(AZUL);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        return titulo;
    }

    private JScrollPane crearTabla() {
        String[] cols = {"ID", "Nombre Completo", "DNI", "Colegiatura",
                         "Especialidad", "Tarifa (S/)", "Teléfono", "Disponible"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabla = new JTable(modeloTabla);
        tabla.setFont(new Font("Arial", Font.PLAIN, 13));
        tabla.setRowHeight(28);
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(AZUL);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.setSelectionBackground(AZUL_SUAVE);
        tabla.setGridColor(new Color(220, 220, 220));

        int[] anchos = {80, 160, 100, 110, 150, 90, 110, 80};
        for (int i = 0; i < anchos.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        return scroll;
    }

    private JPanel crearBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        JButton btnRegistrar    = boton("➕ Registrar Médico",    VERDE);
        JButton btnVerDetalle   = boton("🔍 Ver Detalle",          AZUL);
        JButton btnDisponible   = boton("🔄 Cambiar Disponibilidad", new Color(255, 140, 0));

        btnRegistrar.addActionListener(e  -> formularioRegistro());
        btnVerDetalle.addActionListener(e -> verDetalle());
        btnDisponible.addActionListener(e -> cambiarDisponibilidad());

        panel.add(btnRegistrar);
        panel.add(btnVerDetalle);
        panel.add(btnDisponible);
        return panel;
    }

    private void formularioRegistro() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Registrar Nuevo Médico", true);
        dlg.setSize(480, 480);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        String[] etiquetas = {"Nombre:", "Apellido:", "DNI:", "Teléfono:",
                              "Correo:", "Colegiatura:", "Tarifa (S/):"};
        JTextField[] campos = new JTextField[etiquetas.length];

        for (int i = 0; i < etiquetas.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
            form.add(new JLabel(etiquetas[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
            campos[i] = new JTextField(20);
            campos[i].setFont(new Font("Arial", Font.PLAIN, 13));
            form.add(campos[i], gbc);
        }

        // Combo de especialidades
        gbc.gridx = 0; gbc.gridy = etiquetas.length; gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Especialidad:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JComboBox<Especialidad> comboEsp = new JComboBox<>(Especialidad.values());
        comboEsp.setFont(new Font("Arial", Font.PLAIN, 13));
        form.add(comboEsp, gbc);

        dlg.add(new JScrollPane(form), BorderLayout.CENTER);

        JPanel bots = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bots.setBackground(Color.WHITE);
        JButton btnGuardar  = boton("💾 Guardar", VERDE);
        JButton btnCancelar = boton("Cancelar",   Color.GRAY);

        btnGuardar.addActionListener(e -> {
            try {
                for (int i = 0; i < campos.length; i++)
                    if (campos[i].getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(dlg,
                                "El campo '" + etiquetas[i].replace(":", "") + "' es obligatorio.",
                                "Campo vacío", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                double tarifa = Double.parseDouble(campos[6].getText().trim());
                Medico m = gestor.registrarMedico(
                        campos[0].getText().trim(), campos[1].getText().trim(),
                        campos[2].getText().trim(), campos[3].getText().trim(),
                        campos[4].getText().trim(), campos[5].getText().trim(),
                        (Especialidad) comboEsp.getSelectedItem(), tarifa);

                if (m != null) {
                    JOptionPane.showMessageDialog(dlg,
                            "✔ Médico registrado con ID: " + m.getId(),
                            "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
                    actualizarTabla();
                    dlg.dispose();
                } else {
                    JOptionPane.showMessageDialog(dlg,
                            "⚠ Ya existe un médico con esa colegiatura.",
                            "Colegiatura duplicada", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg,
                        "La tarifa debe ser un número (ej: 80.50)",
                        "Formato incorrecto", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancelar.addActionListener(e -> dlg.dispose());
        bots.add(btnGuardar);
        bots.add(btnCancelar);
        dlg.add(bots, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void verDetalle() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un médico de la tabla.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE); return;
        }
        String id = (String) modeloTabla.getValueAt(fila, 0);
        Medico m  = gestor.buscarPorId(id);
        if (m == null) return;

        String info = "ID           : " + m.getId()                    + "\n"
                    + "Nombre       : Dr(a). " + m.getNombreCompleto() + "\n"
                    + "DNI          : " + m.getDni()                   + "\n"
                    + "Colegiatura  : " + m.getColegiatura()           + "\n"
                    + "Especialidad : " + m.getEspecialidad()          + "\n"
                    + "Teléfono     : " + m.getTelefono()              + "\n"
                    + "Correo       : " + m.getCorreo()                + "\n"
                    + String.format("Tarifa       : S/ %.2f%n", m.getTarifaConsulta())
                    + "Disponible   : " + (m.isDisponible() ? "Sí" : "No");

        JTextArea area = new JTextArea(info);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JOptionPane.showMessageDialog(this, area,
                "Detalle de " + m.getNombreCompleto(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void cambiarDisponibilidad() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un médico.", "Sin selección",
                    JOptionPane.WARNING_MESSAGE); return;
        }
        String id = (String) modeloTabla.getValueAt(fila, 0);
        Medico m  = gestor.buscarPorId(id);
        if (m == null) return;
        m.setDisponible(!m.isDisponible());
        actualizarTabla();
        JOptionPane.showMessageDialog(this,
                "✔ Disponibilidad de Dr(a). " + m.getNombreCompleto() +
                " cambiada a: " + (m.isDisponible() ? "Disponible" : "No disponible"),
                "Cambio realizado", JOptionPane.INFORMATION_MESSAGE);
    }

    public void actualizarTabla() {
        modeloTabla.setRowCount(0);
        for (Medico m : gestor.getTodos())
            modeloTabla.addRow(new Object[]{
                m.getId(), "Dr(a). " + m.getNombreCompleto(), m.getDni(),
                m.getColegiatura(), m.getEspecialidad().getNombre(),
                String.format("%.2f", m.getTarifaConsulta()),
                m.getTelefono(), m.isDisponible() ? "Sí" : "No"
            });
    }

    private JButton boton(String texto, Color fondo) {
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
