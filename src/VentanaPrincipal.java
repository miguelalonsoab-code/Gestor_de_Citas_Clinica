import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class VentanaPrincipal extends JFrame {

    private final GestorPacientes   gestorPacientes;
    private final GestorMedicos     gestorMedicos;
    private final GestorCitas       gestorCitas;
    private final ArrayList<Receta> recetas = new ArrayList<>();
    private JLabel statusBar;

    public VentanaPrincipal() {
        // PASO 1: Conectar MySQL PRIMERO
        inicializarConexionMySQL();

        // PASO 2: Crear gestores (leen de MySQL, no de ArrayList)
        gestorPacientes = new GestorPacientes();
        gestorMedicos   = new GestorMedicos();
        gestorCitas     = new GestorCitas(gestorPacientes);

        // PASO 3: Cargar demo solo si MySQL está vacío
        if (gestorPacientes.getCantidad() == 0) {
            cargarDemostracion();
        }

        configurarVentana();
        construirUI();
        configurarCierre();
    }

    // ── Conexión MySQL ────────────────────────────────────────────
    private void inicializarConexionMySQL() {
        boolean conectado = false;
        while (!conectado) {
            try {
                ConexionDB.getConexion();
                conectado = true;
                System.out.println("✔ Sistema iniciado con MySQL.");
            } catch (Exception e) {
                int resp = JOptionPane.showConfirmDialog(null,
                    "No se pudo conectar a MySQL.\n\n"
                    + "Verifica que:\n"
                    + "  1. MySQL Server esté corriendo\n"
                    + "     (Servicios Windows → MySQL80 → Iniciado)\n"
                    + "  2. La contraseña en ConexionDB.java sea correcta\n"
                    + "  3. La BD 'clinica_upn' exista en Workbench\n\n"
                    + "Error: " + e.getMessage(),
                    "Error de Conexión MySQL",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE);

                if (resp != JOptionPane.YES_OPTION) System.exit(0);
            }
        }
    }

    // ── Cierre: solo cerrar conexión (MySQL guarda automático) ────
    private void configurarCierre() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int r = JOptionPane.showConfirmDialog(
                    VentanaPrincipal.this,
                    "¿Deseas salir?",
                    "Cerrar Sistema",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                if (r == JOptionPane.YES_OPTION) {
                    ConexionDB.cerrarConexion();
                    System.exit(0);
                }
            }
        });
    }

    // ── Configurar ventana ────────────────────────────────────────
    private void configurarVentana() {
        setTitle("Sistema de Gestión Clínica — UPN | MySQL");
        setSize(1150, 720);
        setMinimumSize(new Dimension(950, 620));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    // ── Construir UI ──────────────────────────────────────────────
    private void construirUI() {
        add(crearHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Arial", Font.PLAIN, 13));
        tabs.addTab("👤 Pacientes", new PanelPacientes(gestorPacientes));
        tabs.addTab("🩺 Médicos",   new PanelMedicos(gestorMedicos));
        tabs.addTab("📅 Citas",     new PanelCitas(gestorCitas,
                                        gestorPacientes, gestorMedicos));
        tabs.addTab("📋 Recetas",   new PanelRecetas(gestorCitas, recetas));
        tabs.addTab("📊 Reportes",  new PanelReportes(gestorPacientes,
                                        gestorMedicos, gestorCitas));
        add(tabs, BorderLayout.CENTER);

        // Barra inferior — muestra estado MySQL
        statusBar = new JLabel(
            "  ✔ Conectado a MySQL  |  Pacientes: "
            + gestorPacientes.getCantidad()
            + "  |  Médicos: "   + gestorMedicos.getCantidad()
            + "  |  Citas: "     + gestorCitas.getCantidad());
        statusBar.setFont(new Font("Arial", Font.PLAIN, 11));
        statusBar.setForeground(new Color(40, 167, 69)); // verde
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        add(statusBar, BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel crearHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(new Color(31, 78, 121));
        h.setPreferredSize(new Dimension(0, 60));
        h.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel titulo = new JLabel("🏥  Sistema de Gestión Clínica");
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        titulo.setForeground(Color.WHITE);

        JLabel sub = new JLabel("UPN — Técnicas de POO  |  MySQL 8.0");
        sub.setFont(new Font("Arial", Font.PLAIN, 13));
        sub.setForeground(new Color(180, 210, 240));

        JPanel iz = new JPanel(new GridLayout(2, 1));
        iz.setOpaque(false);
        iz.add(titulo);
        iz.add(sub);
        h.add(iz, BorderLayout.WEST);
        return h;
    }

    // ── Datos de demo (solo si MySQL está vacío) ──────────────────
    private void cargarDemostracion() {
        System.out.println("BD vacía — insertando datos de demo en MySQL...");
        Medico m1 = gestorMedicos.registrarMedico("Carlos","Quispe",
            "45123456","999111222","c.quispe@clinica.pe",
            "CMP-12345", Especialidad.MEDICINA_GENERAL, 80.0);
        Medico m2 = gestorMedicos.registrarMedico("Laura","Flores",
            "46234567","999333444","l.flores@clinica.pe",
            "CMP-23456", Especialidad.PEDIATRIA, 100.0);
        Medico m3 = gestorMedicos.registrarMedico("Roberto","Mamani",
            "47345678","999555666","r.mamani@clinica.pe",
            "CMP-34567", Especialidad.CARDIOLOGIA, 150.0);

        Paciente p1 = gestorPacientes.registrarPaciente("Ana","García",
            "72111111","987654321","ana@gmail.com",
            "15/03/1995","O+","Penicilina");
        Paciente p2 = gestorPacientes.registrarPaciente("Juan","Ríos",
            "72222222","987123456","juan@gmail.com",
            "22/07/1988","A+","Ninguna");
        Paciente p3 = gestorPacientes.registrarPaciente("María","López",
            "72333333","987789012","maria@gmail.com",
            "05/11/2010","B-","Ibuprofeno");

        if (m1!=null && p1!=null)
            gestorCitas.agendarCita(p1,m1,"10/06/2025","09:00","Control general");
        if (m2!=null && p3!=null)
            gestorCitas.agendarCita(p3,m2,"10/06/2025","10:00","Control pediátrico");
        if (m3!=null && p2!=null)
            gestorCitas.agendarCita(p2,m3,"11/06/2025","11:00","Dolor en el pecho");

        System.out.println("✔ Demo insertada en MySQL.");
    }
}