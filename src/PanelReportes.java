import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * CLASE: PanelReportes (JPanel)
 * ─────────────────────────────────────────────
 * Módulo visual de reportes y estadísticas del sistema.
 *
 * Muestra:
 *  - Tarjetas de resumen (KPI cards) con totales
 *  - Tabla de distribución de citas por estado
 *  - Lista de médicos con su cantidad de citas atendidas
 *
 * CONCEPTO CLAVE: este panel SOLO LEE datos de los gestores.
 * No modifica nada → responsabilidad única bien aplicada.
 */
public class PanelReportes extends JPanel {

    private final GestorPacientes gestorPacientes;
    private final GestorMedicos   gestorMedicos;
    private final GestorCitas     gestorCitas;

    // Colores del tema
    private static final Color AZUL       = new Color(31, 78, 121);
    private static final Color VERDE      = new Color(40, 167, 69);
    private static final Color NARANJA    = new Color(255, 140, 0);
    private static final Color ROJO       = new Color(220, 53, 69);
    private static final Color GRIS_FONDO = new Color(245, 247, 250);

    // Etiquetas de las tarjetas KPI (se actualizan al refrescar)
    private JLabel lblPacientes, lblMedicos, lblCitas, lblCompletadas;

    // Tabla de distribución de citas
    private DefaultTableModel modeloEstados;
    // Tabla de rendimiento por médico
    private DefaultTableModel modeloMedicos;

    public PanelReportes(GestorPacientes gp, GestorMedicos gm, GestorCitas gc) {
        this.gestorPacientes = gp;
        this.gestorMedicos   = gm;
        this.gestorCitas     = gc;

        // Usamos BorderLayout en el panel principal
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        add(crearEncabezado(),          BorderLayout.NORTH);
        add(crearCuerpo(),              BorderLayout.CENTER);
        add(crearBarraAcciones(),       BorderLayout.SOUTH);

        // Cargar datos al abrir la pestaña
        actualizarReporte();
    }

    // ═══════════════════════════════════════════
    // ENCABEZADO: título + botón de actualizar
    // ═══════════════════════════════════════════
    private JPanel crearEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        // Título izquierda
        JLabel titulo = new JLabel("Reportes y Estadísticas");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setForeground(AZUL);
        panel.add(titulo, BorderLayout.WEST);

        // Botón actualizar derecha
        JButton btnRefrescar = new JButton("🔄  Actualizar");
        btnRefrescar.setFont(new Font("Arial", Font.BOLD, 13));
        btnRefrescar.setBackground(AZUL);
        btnRefrescar.setForeground(Color.WHITE);
        btnRefrescar.setFocusPainted(false);
        btnRefrescar.setBorderPainted(false);
        btnRefrescar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefrescar.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btnRefrescar.addActionListener(e -> actualizarReporte());
        panel.add(btnRefrescar, BorderLayout.EAST);

        return panel;
    }

    // ═══════════════════════════════════════════
    // CUERPO: tarjetas KPI + tablas
    // ═══════════════════════════════════════════
    private JPanel crearCuerpo() {
        JPanel cuerpo = new JPanel(new BorderLayout(0, 15));
        cuerpo.setBackground(Color.WHITE);

        // Fila superior: 4 tarjetas KPI
        cuerpo.add(crearFilaKPI(),    BorderLayout.NORTH);

        // Área central: dos tablas lado a lado
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                crearTablaEstados(), crearTablaMedicos());
        split.setResizeWeight(0.5);       // división 50/50
        split.setDividerSize(6);
        split.setBorder(null);
        cuerpo.add(split, BorderLayout.CENTER);

        return cuerpo;
    }

    // ── Fila de tarjetas KPI ──────────────────────────────────────
    /**
     * Crea 4 "cards" visuales con los indicadores clave del sistema.
     * Cada tarjeta tiene un color de acento, un ícono, una etiqueta
     * y un JLabel numérico que se actualiza al refrescar.
     *
     * JPanel con GridLayout(1,4) para que las 4 tarjetas se repartan
     * el espacio horizontal disponible de forma equitativa.
     */
    private JPanel crearFilaKPI() {
        JPanel fila = new JPanel(new GridLayout(1, 4, 12, 0));
        fila.setBackground(Color.WHITE);
        fila.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        lblPacientes   = new JLabel("0", SwingConstants.CENTER);
        lblMedicos     = new JLabel("0", SwingConstants.CENTER);
        lblCitas       = new JLabel("0", SwingConstants.CENTER);
        lblCompletadas = new JLabel("0", SwingConstants.CENTER);

        fila.add(crearTarjeta("👤 Pacientes",    lblPacientes,   new Color(31, 97, 141)));
        fila.add(crearTarjeta("🩺 Médicos",      lblMedicos,     new Color(39, 174, 96)));
        fila.add(crearTarjeta("📅 Total Citas",  lblCitas,       new Color(192, 57, 43)));
        fila.add(crearTarjeta("✔ Completadas",   lblCompletadas, new Color(142, 68, 173)));

        return fila;
    }

    /**
     * Construye una tarjeta individual de KPI.
     *
     * Estructura (de arriba a abajo):
     *   JPanel (BoxLayout Y)
     *     └─ JLabel etiqueta  (texto pequeño en blanco)
     *     └─ JLabel valor     (número grande en blanco)
     *
     * @param etiqueta  texto descriptivo del indicador
     * @param lblValor  JLabel que muestra el número (se guarda referencia para actualizarlo)
     * @param color     color de fondo de la tarjeta
     */
    private JPanel crearTarjeta(String etiqueta, JLabel lblValor, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(18, 12, 18, 12));

        JLabel lblEtiqueta = new JLabel(etiqueta);
        lblEtiqueta.setFont(new Font("Arial", Font.PLAIN, 13));
        lblEtiqueta.setForeground(new Color(220, 235, 250));
        lblEtiqueta.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblValor.setFont(new Font("Arial", Font.BOLD, 38));
        lblValor.setForeground(Color.WHITE);
        lblValor.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblEtiqueta);
        card.add(Box.createVerticalStrut(6));
        card.add(lblValor);

        return card;
    }

    // ── Tabla: distribución de citas por estado ───────────────────
    /**
     * Tabla izquierda: cuántas citas hay en cada estado.
     * Usa DefaultTableModel de solo lectura (isCellEditable → false).
     */
    private JPanel crearTablaEstados() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "  Citas por Estado  ",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), AZUL));

        modeloEstados = new DefaultTableModel(
                new String[]{"Estado", "Cantidad"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tablaEstados = new JTable(modeloEstados);
        estilizarTabla(tablaEstados);
        tablaEstados.getColumnModel().getColumn(0).setPreferredWidth(180);
        tablaEstados.getColumnModel().getColumn(1).setPreferredWidth(80);

        panel.add(new JScrollPane(tablaEstados), BorderLayout.CENTER);
        return panel;
    }

    // ── Tabla: citas atendidas por médico ─────────────────────────
    /**
     * Tabla derecha: cuántas citas completadas tiene cada médico.
     * Sirve para medir la productividad por profesional.
     */
    private JPanel crearTablaMedicos() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "  Citas Completadas por Médico  ",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), AZUL));

        modeloMedicos = new DefaultTableModel(
                new String[]{"Médico", "Especialidad", "Completadas"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tablaMedicos = new JTable(modeloMedicos);
        estilizarTabla(tablaMedicos);
        tablaMedicos.getColumnModel().getColumn(0).setPreferredWidth(160);
        tablaMedicos.getColumnModel().getColumn(1).setPreferredWidth(130);
        tablaMedicos.getColumnModel().getColumn(2).setPreferredWidth(90);

        panel.add(new JScrollPane(tablaMedicos), BorderLayout.CENTER);
        return panel;
    }

    // ── Barra inferior de acciones ────────────────────────────────
    private JPanel crearBarraAcciones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        JButton btnExportar = new JButton("📄 Exportar Reporte (TXT)");
        btnExportar.setFont(new Font("Arial", Font.BOLD, 12));
        btnExportar.setBackground(new Color(100, 100, 100));
        btnExportar.setForeground(Color.WHITE);
        btnExportar.setFocusPainted(false);
        btnExportar.setBorderPainted(false);
        btnExportar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExportar.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnExportar.addActionListener(e -> exportarReporteTxt());

        panel.add(btnExportar);
        return panel;
    }

    // ═══════════════════════════════════════════
    // LÓGICA DE ACTUALIZACIÓN
    // ═══════════════════════════════════════════
    /**
     * Recalcula todos los indicadores leyendo directamente
     * los gestores. Se llama al abrir la pestaña y al presionar
     * el botón "Actualizar".
     *
     * Usa streams de Java 8 para contar citas por estado y por médico.
     */
    public void actualizarReporte() {
        ArrayList<Cita> todasCitas = gestorCitas.getTodas();

        // ── KPI: totales ──────────────────────────────────────────
        long totalCitas      = todasCitas.size();
        long completadas     = todasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.COMPLETADA).count();
        long canceladas      = todasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.CANCELADA).count();
        long pendientes      = todasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.PENDIENTE).count();
        long confirmadas     = todasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.CONFIRMADA).count();

        lblPacientes.setText(String.valueOf(gestorPacientes.getCantidad()));
        lblMedicos.setText(String.valueOf(gestorMedicos.getTodos().size()));
        lblCitas.setText(String.valueOf(totalCitas));
        lblCompletadas.setText(String.valueOf(completadas));

        // ── Tabla de estados ──────────────────────────────────────
        modeloEstados.setRowCount(0);
        modeloEstados.addRow(new Object[]{"✔ Completadas",  completadas});
        modeloEstados.addRow(new Object[]{"⏳ Pendientes",  pendientes});
        modeloEstados.addRow(new Object[]{"✅ Confirmadas", confirmadas});
        modeloEstados.addRow(new Object[]{"✕ Canceladas",   canceladas});
        modeloEstados.addRow(new Object[]{"─────────────", "───"});
        modeloEstados.addRow(new Object[]{"TOTAL",          totalCitas});

        // ── Tabla de citas por médico ─────────────────────────────
        modeloMedicos.setRowCount(0);
        for (Medico m : gestorMedicos.getTodos()) {
            long citasMedico = todasCitas.stream()
                    .filter(c -> c.getMedico().getId().equals(m.getId())
                              && c.getEstado() == EstadoCita.COMPLETADA)
                    .count();
            modeloMedicos.addRow(new Object[]{
                "Dr. " + m.getNombreCompleto(),
                m.getEspecialidad().getNombre(),
                citasMedico
            });
        }

        // Forzar repintado
        revalidate();
        repaint();
    }

    // ── Exportar reporte a texto plano ────────────────────────────
    /**
     * Genera un archivo .txt con el resumen del sistema.
     * Usa JFileChooser para que el usuario elija dónde guardarlo.
     *
     * PrintWriter escribe el contenido línea por línea.
     * try-with-resources cierra el writer automáticamente.
     */
    private void exportarReporteTxt() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("Reporte_Clinica.txt"));
        int resultado = fc.showSaveDialog(this);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            java.io.File archivo = fc.getSelectedFile();
            try (java.io.PrintWriter pw = new java.io.PrintWriter(archivo, "UTF-8")) {
                pw.println("═══════════════════════════════════════════");
                pw.println("   REPORTE DEL SISTEMA — CLÍNICA UPN");
                pw.println("═══════════════════════════════════════════");
                pw.println("Total de pacientes  : " + gestorPacientes.getCantidad());
                pw.println("Total de médicos    : " + gestorMedicos.getTodos().size());
                pw.println("Total de citas      : " + gestorCitas.getCantidad());
                pw.println();
                pw.println("CITAS POR ESTADO:");
                for (EstadoCita est : EstadoCita.values()) {
                    long n = gestorCitas.getTodas().stream()
                            .filter(c -> c.getEstado() == est).count();
                    pw.printf("  %-14s : %d%n", est, n);
                }
                pw.println();
                pw.println("MÉDICOS REGISTRADOS:");
                for (Medico m : gestorMedicos.getTodos())
                    pw.println("  Dr. " + m.getNombreCompleto()
                            + " | " + m.getEspecialidad());

                JOptionPane.showMessageDialog(this,
                        "✔ Reporte exportado en:\n" + archivo.getAbsolutePath(),
                        "Exportación exitosa", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al guardar el archivo: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Helper: aplicar estilo uniforme a una JTable ─────────────
    private void estilizarTabla(JTable t) {
        t.setFont(new Font("Arial", Font.PLAIN, 13));
        t.setRowHeight(26);
        t.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        t.getTableHeader().setBackground(AZUL);
        t.getTableHeader().setForeground(Color.WHITE);
        t.setSelectionBackground(new Color(235, 243, 251));
        t.setGridColor(new Color(220, 220, 220));
    }
}
