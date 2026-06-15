import java.util.ArrayList;
import java.util.Scanner;

/**
 * CLASE PRINCIPAL: SistemaClinica
 * ─────────────────────────────────────────────
 * Punto de entrada del sistema.
 * Orquesta todos los gestores y muestra el menú principal.
 *
 * PARA DEPURAR EN ECLIPSE:
 *   Click derecho → Run As → Java Application
 */
public class SistemaClinica {

    private static GestorPacientes gestorPacientes = new GestorPacientes();
    private static GestorMedicos   gestorMedicos   = new GestorMedicos();
    private static GestorCitas     gestorCitas      = new GestorCitas();
    private static ArrayList<Receta> recetas        = new ArrayList<>();
    private static int contadorReceta = 1;

    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        cargarDatosDemostracion();

        int opcion;
        do {
            mostrarMenuPrincipal();
            System.out.print("Elige una opción: ");
            opcion = leerEntero();
            procesarOpcion(opcion);
        } while (opcion != 0);

        sc.close();
        System.out.println("\n¡Hasta luego! Sistema cerrado.");
    }


    private static void mostrarMenuPrincipal() {
        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║     SISTEMA DE GESTIÓN CLÍNICA - UPN         ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║  1. Gestión de Pacientes                     ║");
        System.out.println("║  2. Gestión de Médicos                       ║");
        System.out.println("║  3. Gestión de Citas                         ║");
        System.out.println("║  4. Recetas Médicas                          ║");
        System.out.println("║  5. Reportes                                 ║");
        System.out.println("║  0. Salir                                    ║");
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    private static void procesarOpcion(int opcion) {
        switch (opcion) {
            case 1: menuPacientes(); break;
            case 2: menuMedicos();   break;
            case 3: menuCitas();     break;
            case 4: menuRecetas();   break;
            case 5: menuReportes();  break;
            case 0: break;
            default: System.out.println("⚠ Opción no válida.");
        }
    }


    private static void menuPacientes() {
        System.out.println("\n── GESTIÓN DE PACIENTES ────────────────────");
        System.out.println("  1. Registrar nuevo paciente");
        System.out.println("  2. Buscar paciente");
        System.out.println("  3. Ver todos los pacientes");
        System.out.println("  4. Ver historial de paciente");
        System.out.println("  5. Dar de baja paciente");
        System.out.println("  0. Volver");
        System.out.print("Opción: ");

        switch (leerEntero()) {
            case 1: registrarPaciente(); break;
            case 2: buscarPaciente();    break;
            case 3: gestorPacientes.listarPacientes(); break;
            case 4: verHistorialPaciente(); break;
            case 5: darDeBajaPaciente(); break;
        }
    }

    private static void registrarPaciente() {
        System.out.println("\n── NUEVO PACIENTE ──");
        System.out.print("Nombre      : "); String nombre = sc.nextLine();
        System.out.print("Apellido    : "); String apellido = sc.nextLine();
        System.out.print("DNI         : "); String dni = sc.nextLine();
        System.out.print("Teléfono    : "); String tel = sc.nextLine();
        System.out.print("Correo      : "); String correo = sc.nextLine();
        System.out.print("Fecha Nac.  : "); String fnac = sc.nextLine();
        System.out.print("Grupo Sang. : "); String sangre = sc.nextLine();
        System.out.print("Alergias    : "); String alergias = sc.nextLine();
        gestorPacientes.registrarPaciente(nombre, apellido, dni, tel, correo, fnac, sangre, alergias);
    }

    private static void buscarPaciente() {
        System.out.print("Ingresa nombre o apellido: ");
        String texto = sc.nextLine();
        ArrayList<Paciente> encontrados = gestorPacientes.buscarPorNombre(texto);
        if (encontrados.isEmpty()) { System.out.println("No se encontraron resultados."); return; }
        for (Paciente p : encontrados) p.mostrarInfo();
    }

    private static void verHistorialPaciente() {
        System.out.print("ID del paciente (ej: PAC-001): ");
        Paciente p = gestorPacientes.buscarPorId(sc.nextLine());
        if (p == null) { System.out.println("Paciente no encontrado."); return; }
        p.mostrarHistorial();
    }

    private static void darDeBajaPaciente() {
        System.out.print("ID del paciente a dar de baja: ");
        gestorPacientes.darDeBaja(sc.nextLine());
    }


    private static void menuMedicos() {
        System.out.println("\n── GESTIÓN DE MÉDICOS ──────────────────────");
        System.out.println("  1. Registrar nuevo médico");
        System.out.println("  2. Ver todos los médicos");
        System.out.println("  3. Buscar por especialidad");
        System.out.println("  0. Volver");
        System.out.print("Opción: ");

        switch (leerEntero()) {
            case 1: registrarMedico(); break;
            case 2: gestorMedicos.listarMedicos(); break;
            case 3: buscarPorEspecialidad(); break;
        }
    }

    private static void registrarMedico() {
        System.out.println("\n── NUEVO MÉDICO ──");
        System.out.print("Nombre       : "); String nombre = sc.nextLine();
        System.out.print("Apellido     : "); String apellido = sc.nextLine();
        System.out.print("DNI          : "); String dni = sc.nextLine();
        System.out.print("Teléfono     : "); String tel = sc.nextLine();
        System.out.print("Correo       : "); String correo = sc.nextLine();
        System.out.print("Colegiatura  : "); String col = sc.nextLine();
        System.out.println("Especialidades: 0-MEDICINA_GENERAL 1-PEDIATRIA 2-CARDIOLOGIA "
                + "3-DERMATOLOGIA 4-NEUROLOGIA 5-GINECOLOGIA "
                + "6-TRAUMATOLOGIA 7-OFTALMOLOGIA 8-ODONTOLOGIA 9-PSICOLOGIA");
        System.out.print("Especialidad (0-9): ");
        int espIdx = leerEntero();
        Especialidad esp = Especialidad.values()[Math.min(espIdx, Especialidad.values().length - 1)];
        System.out.print("Tarifa consulta (S/): ");
        double tarifa = leerDouble();
        gestorMedicos.registrarMedico(nombre, apellido, dni, tel, correo, col, esp, tarifa);
    }

    private static void buscarPorEspecialidad() {
        System.out.println("0-MED.GENERAL 1-PEDIATRIA 2-CARDIOLOGIA 3-DERMATOLOGIA 4-NEUROLOGIA");
        System.out.print("Especialidad (0-4): ");
        int idx = leerEntero();
        Especialidad esp = Especialidad.values()[Math.min(idx, Especialidad.values().length-1)];
        ArrayList<Medico> lista = gestorMedicos.buscarPorEspecialidad(esp);
        if (lista.isEmpty()) { System.out.println("No hay médicos disponibles en " + esp); return; }
        System.out.println("\nMédicos disponibles en " + esp + ":");
        for (Medico m : lista) m.mostrarInfo();
    }


    private static void menuCitas() {
        System.out.println("\n── GESTIÓN DE CITAS ────────────────────────");
        System.out.println("  1. Agendar nueva cita");
        System.out.println("  2. Ver todas las citas");
        System.out.println("  3. Buscar cita por ID");
        System.out.println("  4. Completar cita (registrar diagnóstico)");
        System.out.println("  5. Cancelar cita");
        System.out.println("  6. Citas de un paciente");
        System.out.println("  0. Volver");
        System.out.print("Opción: ");

        switch (leerEntero()) {
            case 1: agendarCita();   break;
            case 2: gestorCitas.listarCitas(); break;
            case 3: buscarCita();    break;
            case 4: completarCita(); break;
            case 5: cancelarCita();  break;
            case 6: citasPaciente(); break;
        }
    }

    private static void agendarCita() {
        System.out.println("\n── AGENDAR CITA ──");
        System.out.print("ID Paciente (ej: PAC-001): ");
        Paciente p = gestorPacientes.buscarPorId(sc.nextLine());
        if (p == null) { System.out.println("Paciente no encontrado."); return; }

        System.out.print("ID Médico   (ej: MED-001): ");
        Medico m = gestorMedicos.buscarPorId(sc.nextLine());
        if (m == null) { System.out.println("Médico no encontrado."); return; }

        System.out.print("Fecha (DD/MM/YYYY): "); String fecha = sc.nextLine();
        System.out.print("Hora  (HH:MM)     : "); String hora  = sc.nextLine();
        System.out.print("Motivo            : "); String motivo = sc.nextLine();

        gestorCitas.agendarCita(p, m, fecha, hora, motivo);
    }

    private static void buscarCita() {
        System.out.print("ID de cita (ej: CIT-0001): ");
        Cita c = gestorCitas.buscarPorId(sc.nextLine());
        if (c == null) { System.out.println("Cita no encontrada."); return; }
        c.mostrarResumen();
    }

    private static void completarCita() {
        System.out.print("ID de cita: ");
        Cita c = gestorCitas.buscarPorId(sc.nextLine());
        if (c == null) { System.out.println("Cita no encontrada."); return; }
        System.out.print("Diagnóstico: ");
        c.completarCita(sc.nextLine());
        System.out.println("✔ Cita completada y diagnóstico registrado en historial del paciente.");
    }

    private static void cancelarCita() {
        System.out.print("ID de cita a cancelar: ");
        Cita c = gestorCitas.buscarPorId(sc.nextLine());
        if (c == null) { System.out.println("Cita no encontrada."); return; }
        c.cancelar();
        System.out.println("✔ Cita cancelada: " + c.getIdCita());
    }

    private static void citasPaciente() {
        System.out.print("ID del paciente: ");
        String id = sc.nextLine();
        ArrayList<Cita> lista = gestorCitas.citasDePaciente(id);
        System.out.println("\nCitas del paciente " + id + ":");
        if (lista.isEmpty()) { System.out.println("  (Sin citas)"); return; }
        for (Cita c : lista) c.mostrarResumen();
    }


    private static void menuRecetas() {
        System.out.println("\n── RECETAS MÉDICAS ─────────────────────────");
        System.out.println("  1. Generar receta para una cita");
        System.out.println("  2. Ver todas las recetas");
        System.out.println("  0. Volver");
        System.out.print("Opción: ");

        switch (leerEntero()) {
            case 1: generarReceta(); break;
            case 2: verRecetas();    break;
        }
    }

    private static void generarReceta() {
        System.out.print("ID de cita completada: ");
        Cita c = gestorCitas.buscarPorId(sc.nextLine());
        if (c == null) { System.out.println("Cita no encontrada."); return; }
        if (c.getEstado() != EstadoCita.COMPLETADA) {
            System.out.println("⚠ Solo se pueden generar recetas para citas completadas."); return;
        }
        System.out.print("Fecha de emisión: "); String fecha = sc.nextLine();
        System.out.print("Indicaciones generales: "); String indicaciones = sc.nextLine();

        String idReceta = String.format("REC-%03d", contadorReceta++);
        Receta r = new Receta(idReceta, c, fecha, indicaciones);

        System.out.print("¿Cuántos medicamentos agregar? ");
        int n = leerEntero();
        for (int i = 1; i <= n; i++) {
            System.out.print("Medicamento " + i + " (nombre, dosis, frecuencia): ");
            r.agregarMedicamento(sc.nextLine());
        }

        recetas.add(r);
        r.mostrarReceta();
        System.out.println("✔ Receta generada: " + idReceta);
    }

    private static void verRecetas() {
        System.out.println("\n══ RECETAS EMITIDAS (" + recetas.size() + ") ══");
        if (recetas.isEmpty()) { System.out.println("  (Sin recetas)"); return; }
        for (Receta r : recetas) r.mostrarReceta();
    }


    private static void menuReportes() {
        System.out.println("\n── REPORTES ────────────────────────────────");
        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║  RESUMEN DEL SISTEMA             ║");
        System.out.println("╠══════════════════════════════════╣");
        System.out.printf( "║  Total pacientes  : %-12d║%n", gestorPacientes.getCantidad());
        System.out.printf( "║  Total médicos    : %-12d║%n", gestorMedicos.getTodos().size());
        System.out.printf( "║  Total citas      : %-12d║%n", gestorCitas.getCantidad());
        System.out.printf( "║  Total recetas    : %-12d║%n", recetas.size());

        long completadas = gestorCitas.getTodas().stream()
                .filter(c -> c.getEstado() == EstadoCita.COMPLETADA).count();
        long canceladas  = gestorCitas.getTodas().stream()
                .filter(c -> c.getEstado() == EstadoCita.CANCELADA).count();
        System.out.printf("║  Citas completadas: %-12d║%n", completadas);
        System.out.printf("║  Citas canceladas : %-12d║%n", canceladas);
        System.out.println("╚══════════════════════════════════╝");
    }


    private static void cargarDatosDemostracion() {
        System.out.println("Cargando datos de demostración...");

        // Médicos
        Medico m1 = gestorMedicos.registrarMedico("Carlos", "Quispe", "45123456",
                "999111222", "c.quispe@clinica.pe", "CMP-12345",
                Especialidad.MEDICINA_GENERAL, 80.0);
        Medico m2 = gestorMedicos.registrarMedico("Laura", "Flores", "46234567",
                "999333444", "l.flores@clinica.pe", "CMP-23456",
                Especialidad.PEDIATRIA, 100.0);
        Medico m3 = gestorMedicos.registrarMedico("Roberto", "Mamani", "47345678",
                "999555666", "r.mamani@clinica.pe", "CMP-34567",
                Especialidad.CARDIOLOGIA, 150.0);

        // Pacientes
        Paciente p1 = gestorPacientes.registrarPaciente("Ana", "García", "72111111",
                "987654321", "ana@gmail.com", "15/03/1995", "O+", "Penicilina");
        Paciente p2 = gestorPacientes.registrarPaciente("Juan", "Ríos", "72222222",
                "987123456", "juan@gmail.com", "22/07/1988", "A+", "Ninguna");
        Paciente p3 = gestorPacientes.registrarPaciente("María", "López", "72333333",
                "987789012", "maria@gmail.com", "05/11/2010", "B-", "Ibuprofeno");

        // Citas
        if (m1 != null && p1 != null)
            gestorCitas.agendarCita(p1, m1, "10/06/2025", "09:00", "Control general");
        if (m2 != null && p3 != null)
            gestorCitas.agendarCita(p3, m2, "10/06/2025", "10:00", "Control pediátrico");
        if (m3 != null && p2 != null)
            gestorCitas.agendarCita(p2, m3, "11/06/2025", "11:00", "Dolor en el pecho");

        System.out.println("✔ Datos cargados. Sistema listo.\n");
    }


    private static int leerEntero() {
        try {
            int val = Integer.parseInt(sc.nextLine().trim());
            return val;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static double leerDouble() {
        try { return Double.parseDouble(sc.nextLine().trim()); }
        catch (NumberFormatException e) { return 0.0; }
    }
}
