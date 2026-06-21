import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;


public class VentanaPrincipal extends JFrame {

    private final GestorPacientes gestorPacientes = new GestorPacientes();
    private final GestorMedicos   gestorMedicos   = new GestorMedicos();
    private final GestorCitas     gestorCitas      = new GestorCitas();

    private JLabel statusBar;

    public VentanaPrincipal() {
        ManejadorArchivos.inicializar();

        cargarDatosDesdeArchivos();

        if (gestorPacientes.getCantidad() == 0) {
            cargarDemostracion();
        }

        configurarVentana();

        construirUI();

        configurarGuardadoAlCerrar();
    }

    
    private void cargarDatosDesdeArchivos() {
        ArrayList<Paciente> pacientesCargados = ManejadorArchivos.cargarPacientes();
        gestorPacientes.cargarLista(pacientesCargados);

        ArrayList<Medico> medicosCargados = ManejadorArchivos.cargarMedicos();
        gestorMedicos.cargarLista(medicosCargados);

        ArrayList<Cita> citasCargadas = ManejadorArchivos.cargarCitas(
                gestorPacientes, gestorMedicos);
        gestorCitas.cargarLista(citasCargadas);
    }

   
    private void configurarVentana() {
        setTitle("Sistema de Gestión Clínica — UPN");
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    
    private void construirUI() {
        add(crearHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Arial", Font.PLAIN, 13));

        tabs.addTab("👤 Pacientes", new PanelPacientes(gestorPacientes));
        tabs.addTab("🩺 Médicos",   new PanelMedicos(gestorMedicos));
        tabs.addTab("📅 Citas",     new PanelCitas(gestorCitas, gestorPacientes, gestorMedicos));
        tabs.addTab("📊 Reportes",  new PanelReportes(gestorPacientes, gestorMedicos, gestorCitas));

        add(tabs, BorderLayout.CENTER);

        statusBar = new JLabel("  Sistema listo.  Pacientes: "
                + gestorPacientes.getCantidad()
                + "  |  Médicos: " + gestorMedicos.getTodos().size()
                + "  |  Citas: " + gestorCitas.getCantidad());
        statusBar.setFont(new Font("Arial", Font.PLAIN, 11));
        statusBar.setForeground(Color.DARK_GRAY);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        add(statusBar, BorderLayout.SOUTH);
    }

   
    private void configurarGuardadoAlCerrar() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int respuesta = JOptionPane.showConfirmDialog(
                        VentanaPrincipal.this,
                        "¿Deseas guardar los datos antes de salir?",
                        "Guardar y salir",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (respuesta == JOptionPane.CANCEL_OPTION) {
                    return;
                }

                if (respuesta == JOptionPane.YES_OPTION) {
                    ManejadorArchivos.guardarPacientes(gestorPacientes.getTodos());
                    ManejadorArchivos.guardarMedicos(gestorMedicos.getTodos());
                    ManejadorArchivos.guardarCitas(gestorCitas.getTodas());
                    System.out.println("✔ Datos guardados.");
                }

                System.exit(0);
            }
        });
    }

    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(31, 78, 121));
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel titulo = new JLabel("🏥  Sistema de Gestión Clínica");
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel("UPN — Técnicas de POO");
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitulo.setForeground(new Color(180, 210, 240));

        JPanel izquierda = new JPanel(new GridLayout(2, 1));
        izquierda.setOpaque(false);
        izquierda.add(titulo);
        izquierda.add(subtitulo);
        header.add(izquierda, BorderLayout.WEST);

        return header;
    }


    private void cargarDemostracion() {
        Medico m1 = gestorMedicos.registrarMedico("Carlos","Quispe","45123456",
                "999111222","c.quispe@clinica.pe","CMP-12345",
                Especialidad.MEDICINA_GENERAL, 80.0);
        Medico m2 = gestorMedicos.registrarMedico("Laura","Flores","46234567",
                "999333444","l.flores@clinica.pe","CMP-23456",
                Especialidad.PEDIATRIA, 100.0);
        Medico m3 = gestorMedicos.registrarMedico("Roberto","Mamani","47345678",
                "999555666","r.mamani@clinica.pe","CMP-34567",
                Especialidad.CARDIOLOGIA, 150.0);

        Paciente p1 = gestorPacientes.registrarPaciente("Ana","García","72111111",
                "987654321","ana@gmail.com","15/03/1995","O+","Penicilina");
        Paciente p2 = gestorPacientes.registrarPaciente("Juan","Ríos","72222222",
                "987123456","juan@gmail.com","22/07/1988","A+","Ninguna");
        Paciente p3 = gestorPacientes.registrarPaciente("María","López","72333333",
                "987789012","maria@gmail.com","05/11/2010","B-","Ibuprofeno");

        if (m1 != null && p1 != null)
            gestorCitas.agendarCita(p1, m1, "10/06/2025", "09:00", "Control general");
        if (m2 != null && p3 != null)
            gestorCitas.agendarCita(p3, m2, "10/06/2025", "10:00", "Control pediátrico");
        if (m3 != null && p2 != null)
            gestorCitas.agendarCita(p2, m3, "11/06/2025", "11:00", "Dolor en el pecho");
    }
}
