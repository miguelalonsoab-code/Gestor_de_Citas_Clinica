import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class PanelRecetas extends JPanel {

    private final GestorCitas       gestorCitas;
    private final ArrayList<Receta> recetas;
    private int                     contadorReceta = 1;

    private JTable          tabla;
    private DefaultTableModel modeloTabla;

    private static final Color AZUL      = new Color(31, 78, 121);
    private static final Color VERDE     = new Color(40, 167, 69);
    private static final Color AZUL_SUB  = new Color(235, 243, 251);

    public PanelRecetas(GestorCitas gestorCitas, ArrayList<Receta> recetas) {
        this.gestorCitas = gestorCitas;
        this.recetas     = recetas;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        add(crearTitulo(),  BorderLayout.NORTH);
        add(crearTabla(),   BorderLayout.CENTER);
        add(crearBotones(), BorderLayout.SOUTH);

        actualizarTabla();
    }

    private JLabel crearTitulo() {
        JLabel t = new JLabel("Gestión de Recetas Médicas");
        t.setFont(new Font("Arial", Font.BOLD, 18));
        t.setForeground(AZUL);
        t.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        return t;
    }

    private JScrollPane crearTabla() {
        String[] cols = {"ID Receta", "Paciente", "Médico", "Cita Origen",
                         "Fecha Emisión", "Medicamentos", "Indicaciones"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setFont(new Font("Arial", Font.PLAIN, 13));
        tabla.setRowHeight(28);
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(AZUL);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.setSelectionBackground(AZUL_SUB);
        tabla.setGridColor(new Color(220, 220, 220));

        int[] anchos = {90, 150, 150, 90, 100, 80, 200};
        for (int i = 0; i < anchos.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        return new JScrollPane(tabla);
    }

    private JPanel crearBotones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        JButton btnGenerar = boton("📋 Generar Receta", VERDE);
        JButton btnVer     = boton("🔍 Ver Detalle",     AZUL);

        btnGenerar.addActionListener(e -> generarReceta());
        btnVer.addActionListener(e     -> verDetalle());

        p.add(btnGenerar);
        p.add(btnVer);
        return p;
    }

    // ── HU-07: Generar receta desde cita completada ───────────────
    private void generarReceta() {
        // Filtrar solo citas completadas
        ArrayList<Cita> completadas = new ArrayList<>();
        for (Cita c : gestorCitas.getTodas())
            if (c.getEstado() == EstadoCita.COMPLETADA) completadas.add(c);

        if (completadas.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No hay citas completadas disponibles para generar receta.\n"
              + "Completa una cita registrando su diagnóstico primero.",
                "Sin citas completadas", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Combo con citas completadas
        String[] opciones = completadas.stream()
            .map(c -> c.getIdCita() + " — " + c.getPaciente().getNombreCompleto()
                    + " | Dr. " + c.getMedico().getNombreCompleto()
                    + " (" + c.getFecha() + ")")
            .toArray(String[]::new);

        JComboBox<String> comboCitas = new JComboBox<>(opciones);
        comboCitas.setFont(new Font("Arial", Font.PLAIN, 13));

        JTextField campoFecha       = new JTextField(java.time.LocalDate.now().toString(), 15);
        JTextField campoIndicaciones = new JTextField(30);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.anchor = GridBagConstraints.WEST;

        Object[][] filas = {{"Cita completada:", comboCitas},
                            {"Fecha emisión:",   campoFecha},
                            {"Indicaciones:",    campoIndicaciones}};
        for (int i = 0; i < filas.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
            form.add(new JLabel((String) filas[i][0]), gbc);
            gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            form.add((Component) filas[i][1], gbc);
        }

        int res = JOptionPane.showConfirmDialog(this, form,
            "Nueva Receta Médica", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);

        if (res != JOptionPane.OK_OPTION) return;

        String idCita = opciones[comboCitas.getSelectedIndex()].split(" — ")[0].trim();
        Cita cita = gestorCitas.buscarPorId(idCita);
        if (cita == null) return;

        // Verificar que no tenga ya una receta
        for (Receta r : recetas) {
            if (r.getCita().getIdCita().equals(idCita)) {
                JOptionPane.showMessageDialog(this,
                    "Esta cita ya tiene una receta generada.",
                    "Receta duplicada", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String idReceta  = String.format("REC-%03d", contadorReceta++);
        Receta receta    = new Receta(idReceta, cita,
                campoFecha.getText().trim(),
                campoIndicaciones.getText().trim());

        // Agregar medicamentos en un bucle
        boolean seguir = true;
        while (seguir) {
            String med = JOptionPane.showInputDialog(this,
                "Ingresa medicamento (nombre, dosis, frecuencia):\n"
              + "Ejemplo: Paracetamol 500mg, 1 tableta cada 8 horas",
                "Agregar Medicamento", JOptionPane.QUESTION_MESSAGE);

            if (med != null && !med.trim().isEmpty())
                receta.agregarMedicamento(med.trim());

            int otro = JOptionPane.showConfirmDialog(this,
                "¿Agregar otro medicamento?",
                "Medicamentos", JOptionPane.YES_NO_OPTION);
            seguir = (otro == JOptionPane.YES_OPTION);
        }

        recetas.add(receta);
        actualizarTabla();
        JOptionPane.showMessageDialog(this,
            "✔ Receta " + idReceta + " generada correctamente para "
            + cita.getPaciente().getNombreCompleto(),
            "Receta generada", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── HU-18: Ver detalle de receta seleccionada ─────────────────
    private void verDetalle() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una receta de la tabla.",
                "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) modeloTabla.getValueAt(fila, 0);
        Receta r  = recetas.stream()
                .filter(rec -> rec.getIdReceta().equals(id))
                .findFirst().orElse(null);
        if (r == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════╗\n");
        sb.append("║           RECETA MÉDICA              ║\n");
        sb.append("╠══════════════════════════════════════╣\n");
        sb.append("║ Receta N°  : ").append(r.getIdReceta()).append("\n");
        sb.append("║ Paciente   : ").append(r.getCita().getPaciente().getNombreCompleto()).append("\n");
        sb.append("║ Médico     : Dr. ").append(r.getCita().getMedico().getNombreCompleto()).append("\n");
        sb.append("║ Fecha      : ").append(r.getFechaEmision()).append("\n");
        sb.append("╠══════════════════════════════════════╣\n");
        sb.append("║ MEDICAMENTOS:\n");
        ArrayList<String> meds = r.getMedicamentos();
        if (meds.isEmpty()) sb.append("║   (Sin medicamentos)\n");
        else for (int i = 0; i < meds.size(); i++)
            sb.append("║  ").append(i + 1).append(". ").append(meds.get(i)).append("\n");
        sb.append("╠══════════════════════════════════════╣\n");
        sb.append("║ Indicaciones: ").append(r.getIndicaciones()).append("\n");
        sb.append("╚══════════════════════════════════════╝");

        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JOptionPane.showMessageDialog(this, new JScrollPane(area),
            "Detalle de " + r.getIdReceta(), JOptionPane.INFORMATION_MESSAGE);
    }

    public void actualizarTabla() {
        modeloTabla.setRowCount(0);
        for (Receta r : recetas)
            modeloTabla.addRow(new Object[]{
                r.getIdReceta(),
                r.getCita().getPaciente().getNombreCompleto(),
                "Dr. " + r.getCita().getMedico().getNombreCompleto(),
                r.getCita().getIdCita(),
                r.getFechaEmision(),
                r.getMedicamentos().size() + " medicamento(s)",
                r.getIndicaciones()
            });
    }

    public ArrayList<Receta> getRecetas() { return recetas; }

    private JButton boton(String t, Color c) {
        JButton b = new JButton(t);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setBackground(c); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return b;
    }
}
