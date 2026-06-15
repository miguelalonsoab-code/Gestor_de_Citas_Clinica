import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * CLASE: PanelCitas (JPanel)
 * ─────────────────────────────────────────────
 * Módulo visual para agendamiento y gestión de citas médicas.
 */
public class PanelCitas extends JPanel {

    private final GestorCitas     gestorCitas;
    private final GestorPacientes gestorPacientes;
    private final GestorMedicos   gestorMedicos;

    private JTable            tabla;
    private DefaultTableModel modeloTabla;

    private static final Color AZUL        = new Color(31, 78, 121);
    private static final Color VERDE       = new Color(40, 167, 69);
    private static final Color ROJO        = new Color(220, 53, 69);
    private static final Color NARANJA     = new Color(255, 140, 0);
    private static final Color AZUL_SUAVE  = new Color(235, 243, 251);

    public PanelCitas(GestorCitas gc, GestorPacientes gp, GestorMedicos gm) {
        this.gestorCitas     = gc;
        this.gestorPacientes = gp;
        this.gestorMedicos   = gm;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        add(crearTitulo(),  BorderLayout.NORTH);
        add(crearTabla(),   BorderLayout.CENTER);
        add(crearBotones(), BorderLayout.SOUTH);

        actualizarTabla();
    }

    private JLabel crearTitulo() {
        JLabel titulo = new JLabel("Gestión de Citas Médicas");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setForeground(AZUL);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        return titulo;
    }

    private JScrollPane crearTabla() {
        String[] cols = {"ID Cita", "Paciente", "Médico", "Especialidad",
                         "Fecha", "Hora", "Motivo", "Estado", "Costo (S/)"};
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

        int[] anchos = {90, 150, 150, 130, 100, 70, 150, 100, 90};
        for (int i = 0; i < anchos.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        return new JScrollPane(tabla);
    }

    private JPanel crearBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        JButton btnAgendar    = boton("📅 Agendar Cita",     VERDE);
        JButton btnCompletar  = boton("✔ Completar Cita",    AZUL);
        JButton btnCancelar   = boton("✕ Cancelar Cita",     ROJO);
        JButton btnHistorial  = boton("📋 Historial Paciente", NARANJA);

        btnAgendar.addActionListener(e   -> formularioAgendarCita());
        btnCompletar.addActionListener(e -> completarCita());
        btnCancelar.addActionListener(e  -> cancelarCita());
        btnHistorial.addActionListener(e -> verHistorialDesdeCita());

        panel.add(btnAgendar);
        panel.add(btnCompletar);
        panel.add(btnCancelar);
        panel.add(btnHistorial);
        return panel;
    }

    // ── Formulario agendar cita ───────────────────────────────────
    private void formularioAgendarCita() {
        // Verificar que existan pacientes y médicos
        if (gestorPacientes.getTodos().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Registra al menos un paciente antes de agendar.",
                    "Sin pacientes", JOptionPane.WARNING_MESSAGE); return;
        }
        if (gestorMedicos.getTodos().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Registra al menos un médico antes de agendar.",
                    "Sin médicos", JOptionPane.WARNING_MESSAGE); return;
        }

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Agendar Nueva Cita", true);
        dlg.setSize(460, 380);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Combo de pacientes
        String[] nomPacientes = gestorPacientes.getTodos().stream()
                .map(p -> p.getId() + " — " + p.getNombreCompleto())
                .toArray(String[]::new);
        JComboBox<String> comboPac = new JComboBox<>(nomPacientes);

        // Combo de médicos
        String[] nomMedicos = gestorMedicos.getTodos().stream()
                .map(m -> m.getId() + " — Dr. " + m.getNombreCompleto() + " (" + m.getEspecialidad() + ")")
                .toArray(String[]::new);
        JComboBox<String> comboMed = new JComboBox<>(nomMedicos);

        JTextField campoFecha  = new JTextField("DD/MM/YYYY", 15);
        JTextField campoHora   = new JTextField("HH:MM", 10);
        JTextField campoMotivo = new JTextField(20);

        Object[][] filas = {
            {"Paciente:",  comboPac},
            {"Médico:",    comboMed},
            {"Fecha:",     campoFecha},
            {"Hora:",      campoHora},
            {"Motivo:",    campoMotivo},
        };

        for (int i = 0; i < filas.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            JLabel lbl = new JLabel((String) filas[i][0]);
            lbl.setFont(new Font("Arial", Font.PLAIN, 13));
            form.add(lbl, gbc);
            gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            form.add((Component) filas[i][1], gbc);
        }

        dlg.add(new JScrollPane(form), BorderLayout.CENTER);

        JPanel bots = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bots.setBackground(Color.WHITE);
        JButton btnGuardar  = boton("📅 Agendar", VERDE);
        JButton btnCancelar2 = boton("Cancelar",  Color.GRAY);

        btnGuardar.addActionListener(e -> {
            if (campoFecha.getText().trim().isEmpty() || campoHora.getText().trim().isEmpty()
                    || campoMotivo.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Todos los campos son obligatorios.",
                        "Campos vacíos", JOptionPane.WARNING_MESSAGE); return;
            }
            // Extraer ID del paciente y médico del combo (formato "PAC-001 — Nombre")
            String idPac = ((String) comboPac.getSelectedItem()).split(" — ")[0].trim();
            String idMed = ((String) comboMed.getSelectedItem()).split(" — ")[0].trim();

            Paciente p = gestorPacientes.buscarPorId(idPac);
            Medico   m = gestorMedicos.buscarPorId(idMed);

            Cita c = gestorCitas.agendarCita(p, m,
                    campoFecha.getText().trim(),
                    campoHora.getText().trim(),
                    campoMotivo.getText().trim());

            if (c != null) {
                JOptionPane.showMessageDialog(dlg,
                        "✔ Cita agendada con ID: " + c.getIdCita(),
                        "Cita registrada", JOptionPane.INFORMATION_MESSAGE);
                actualizarTabla();
                dlg.dispose();
            } else {
                JOptionPane.showMessageDialog(dlg,
                        "⚠ El médico ya tiene cita en ese horario.\nElige otro horario.",
                        "Conflicto de horario", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancelar2.addActionListener(e -> dlg.dispose());
        bots.add(btnGuardar);
        bots.add(btnCancelar2);
        dlg.add(bots, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ── Completar cita con diagnóstico ────────────────────────────
    private void completarCita() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { sinSeleccion(); return; }

        String id     = (String) modeloTabla.getValueAt(fila, 0);
        String estado = (String) modeloTabla.getValueAt(fila, 7);
        Cita c = gestorCitas.buscarPorId(id);
        if (c == null) return;

        if (c.getEstado() == EstadoCita.CANCELADA || c.getEstado() == EstadoCita.COMPLETADA) {
            JOptionPane.showMessageDialog(this,
                    "La cita ya está " + estado + " y no puede modificarse.",
                    "Estado inválido", JOptionPane.WARNING_MESSAGE); return;
        }

        String diagnostico = JOptionPane.showInputDialog(this,
                "Ingresa el diagnóstico para la cita " + id + ":",
                "Registrar Diagnóstico", JOptionPane.QUESTION_MESSAGE);

        if (diagnostico != null && !diagnostico.trim().isEmpty()) {
            c.completarCita(diagnostico.trim());
            actualizarTabla();
            JOptionPane.showMessageDialog(this,
                    "✔ Cita completada.\nDiagnóstico registrado en historial del paciente.",
                    "Cita completada", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ── Cancelar cita ─────────────────────────────────────────────
    private void cancelarCita() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { sinSeleccion(); return; }

        String id = (String) modeloTabla.getValueAt(fila, 0);
        Cita c = gestorCitas.buscarPorId(id);
        if (c == null) return;

        if (c.getEstado() == EstadoCita.COMPLETADA || c.getEstado() == EstadoCita.CANCELADA) {
            JOptionPane.showMessageDialog(this, "La cita ya no puede cancelarse (estado: " + c.getEstado() + ").",
                    "No permitido", JOptionPane.WARNING_MESSAGE); return;
        }

        int conf = JOptionPane.showConfirmDialog(this, "¿Cancelar la cita " + id + "?",
                "Confirmar cancelación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf == JOptionPane.YES_OPTION) {
            c.cancelar();
            actualizarTabla();
            JOptionPane.showMessageDialog(this, "✔ Cita cancelada.", "Cancelada",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ── Ver historial del paciente de la cita seleccionada ────────
    private void verHistorialDesdeCita() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { sinSeleccion(); return; }
        String id = (String) modeloTabla.getValueAt(fila, 0);
        Cita c = gestorCitas.buscarPorId(id);
        if (c == null) return;

        Paciente p = c.getPaciente();
        String hist = p.getHistorial().isEmpty()
                ? "El paciente aún no tiene entradas en su historial médico."
                : p.getHistorial();

        JTextArea area = new JTextArea(hist);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JOptionPane.showMessageDialog(this, new JScrollPane(area),
                "Historial de " + p.getNombreCompleto(), JOptionPane.INFORMATION_MESSAGE);
    }

    public void actualizarTabla() {
        modeloTabla.setRowCount(0);
        for (Cita c : gestorCitas.getTodas())
            modeloTabla.addRow(new Object[]{
                c.getIdCita(),
                c.getPaciente().getNombreCompleto(),
                "Dr. " + c.getMedico().getNombreCompleto(),
                c.getMedico().getEspecialidad().getNombre(),
                c.getFecha(), c.getHora(), c.getMotivo(),
                c.getEstado().toString(),
                String.format("%.2f", c.getCosto())
            });
    }

    private void sinSeleccion() {
        JOptionPane.showMessageDialog(this, "Selecciona una cita de la tabla.",
                "Sin selección", JOptionPane.WARNING_MESSAGE);
    }

    private JButton boton(String texto, Color fondo) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(fondo);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return btn;
    }
}
