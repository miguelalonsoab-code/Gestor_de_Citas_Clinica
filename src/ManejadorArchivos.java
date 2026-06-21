import java.io.*;
import java.util.ArrayList;

public class ManejadorArchivos {

    // Rutas de los archivos de datos
    private static final String RUTA_PACIENTES = "datos/pacientes.csv";
    private static final String RUTA_MEDICOS   = "datos/medicos.csv";
    private static final String RUTA_CITAS     = "datos/citas.csv";
    private static final String SEPARADOR      = ",";

    
    public static void inicializar() {
        File carpeta = new File("datos");
        if (!carpeta.exists()) {
            boolean creada = carpeta.mkdirs();
            System.out.println(creada
                    ? "✔ Carpeta 'datos/' creada."
                    : "⚠ No se pudo crear la carpeta 'datos/'.");
        }
    }

    
    public static void guardarPacientes(ArrayList<Paciente> pacientes) {
        try (PrintWriter pw = new PrintWriter(
                new BufferedWriter(new FileWriter(RUTA_PACIENTES)))) {

            
            pw.println("id,nombre,apellido,dni,telefono,correo,"
                     + "fechaNacimiento,grupoSanguineo,alergias,activo");

            for (Paciente p : pacientes) {
                // Escapar comas dentro de campos (ej: alergias con coma)
                pw.println(
                    escapar(p.getId())              + SEPARADOR +
                    escapar(p.getNombre())          + SEPARADOR +
                    escapar(p.getApellido())        + SEPARADOR +
                    escapar(p.getDni())             + SEPARADOR +
                    escapar(p.getTelefono())        + SEPARADOR +
                    escapar(p.getCorreo())          + SEPARADOR +
                    escapar(p.getFechaNacimiento()) + SEPARADOR +
                    escapar(p.getGrupoSanguineo())  + SEPARADOR +
                    escapar(p.getAlergias())        + SEPARADOR +
                    p.isActivo()
                );
            }
            System.out.println("✔ " + pacientes.size() + " paciente(s) guardados en " + RUTA_PACIENTES);

        } catch (IOException e) {
            System.out.println("✘ Error al guardar pacientes: " + e.getMessage());
        }
    }

    
    public static ArrayList<Paciente> cargarPacientes() {
        ArrayList<Paciente> lista = new ArrayList<>();
        File archivo = new File(RUTA_PACIENTES);

        if (!archivo.exists()) {
            System.out.println("ℹ No se encontró " + RUTA_PACIENTES + " — se iniciará con lista vacía.");
            return lista;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            boolean primeraLinea = true; 

            while ((linea = br.readLine()) != null) {
                if (primeraLinea) { primeraLinea = false; continue; }
                if (linea.trim().isEmpty()) continue;

               
                String[] campos = linea.split(SEPARADOR, -1);
                if (campos.length < 10) continue; 

                try {
                    Paciente p = new Paciente(
                        campos[0],  // id
                        campos[1],  // nombre
                        campos[2],  // apellido
                        campos[3],  // dni
                        campos[4],  // telefono
                        campos[5],  // correo
                        campos[6],  // fechaNacimiento
                        campos[7],  // grupoSanguineo
                        campos[8]   // alergias
                    );
                    p.setActivo(Boolean.parseBoolean(campos[9]));
                    lista.add(p);
                } catch (Exception e) {
                    System.out.println("⚠ Línea ignorada (formato inválido): " + linea);
                }
            }
            System.out.println("✔ " + lista.size() + " paciente(s) cargados desde " + RUTA_PACIENTES);

        } catch (IOException e) {
            System.out.println("✘ Error al cargar pacientes: " + e.getMessage());
        }
        return lista;
    }

    
    public static void guardarMedicos(ArrayList<Medico> medicos) {
        try (PrintWriter pw = new PrintWriter(
                new BufferedWriter(new FileWriter(RUTA_MEDICOS)))) {

            pw.println("id,nombre,apellido,dni,telefono,correo,"
                     + "colegiatura,especialidad,tarifa,disponible");

            for (Medico m : medicos) {
                pw.println(
                    escapar(m.getId())                            + SEPARADOR +
                    escapar(m.getNombre())                        + SEPARADOR +
                    escapar(m.getApellido())                      + SEPARADOR +
                    escapar(m.getDni())                           + SEPARADOR +
                    escapar(m.getTelefono())                      + SEPARADOR +
                    escapar(m.getCorreo())                        + SEPARADOR +
                    escapar(m.getColegiatura())                   + SEPARADOR +
                    m.getEspecialidad().name()                    + SEPARADOR +
                    m.getTarifaConsulta()                         + SEPARADOR +
                    m.isDisponible()
                );
            }
            System.out.println("✔ " + medicos.size() + " médico(s) guardados en " + RUTA_MEDICOS);

        } catch (IOException e) {
            System.out.println("✘ Error al guardar médicos: " + e.getMessage());
        }
    }

    
    public static ArrayList<Medico> cargarMedicos() {
        ArrayList<Medico> lista = new ArrayList<>();
        File archivo = new File(RUTA_MEDICOS);
        if (!archivo.exists()) return lista;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            boolean primera = true;

            while ((linea = br.readLine()) != null) {
                if (primera) { primera = false; continue; }
                if (linea.trim().isEmpty()) continue;

                String[] c = linea.split(SEPARADOR, -1);
                if (c.length < 10) continue;

                try {
                    Especialidad esp = Especialidad.valueOf(c[7].trim());
                    Medico m = new Medico(
                        c[0], c[1], c[2], c[3], c[4], c[5], c[6],
                        esp, Double.parseDouble(c[8].trim())
                    );
                    m.setDisponible(Boolean.parseBoolean(c[9].trim()));
                    lista.add(m);
                } catch (Exception e) {
                    System.out.println("⚠ Médico ignorado: " + e.getMessage());
                }
            }
            System.out.println("✔ " + lista.size() + " médico(s) cargados desde " + RUTA_MEDICOS);

        } catch (IOException e) {
            System.out.println("✘ Error al cargar médicos: " + e.getMessage());
        }
        return lista;
    }

    
    public static void guardarCitas(ArrayList<Cita> citas) {
        try (PrintWriter pw = new PrintWriter(
                new BufferedWriter(new FileWriter(RUTA_CITAS)))) {

            pw.println("id,idPaciente,idMedico,fecha,hora,motivo,estado,diagnostico,costo");

            for (Cita c : citas) {
                pw.println(
                    escapar(c.getIdCita())                  + SEPARADOR +
                    escapar(c.getPaciente().getId())         + SEPARADOR +
                    escapar(c.getMedico().getId())           + SEPARADOR +
                    escapar(c.getFecha())                    + SEPARADOR +
                    escapar(c.getHora())                     + SEPARADOR +
                    escapar(c.getMotivo())                   + SEPARADOR +
                    c.getEstado().name()                     + SEPARADOR +
                    escapar(c.getDiagnostico())              + SEPARADOR +
                    c.getCosto()
                );
            }
            System.out.println("✔ " + citas.size() + " cita(s) guardadas en " + RUTA_CITAS);

        } catch (IOException e) {
            System.out.println("✘ Error al guardar citas: " + e.getMessage());
        }
    }

   
    public static ArrayList<Cita> cargarCitas(
            GestorPacientes gestorP, GestorMedicos gestorM) {

        ArrayList<Cita> lista = new ArrayList<>();
        File archivo = new File(RUTA_CITAS);
        if (!archivo.exists()) return lista;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            boolean primera = true;

            while ((linea = br.readLine()) != null) {
                if (primera) { primera = false; continue; }
                if (linea.trim().isEmpty()) continue;

                String[] c = linea.split(SEPARADOR, -1);
                if (c.length < 9) continue;

                try {
                    Paciente p  = gestorP.buscarPorId(c[1].trim());
                    Medico   m  = gestorM.buscarPorId(c[2].trim());
                    if (p == null || m == null) continue; // referencia rota

                    Cita cita = new Cita(c[0].trim(), p, m,
                            c[3].trim(), c[4].trim(), c[5].trim());
                    cita.setEstado(EstadoCita.valueOf(c[6].trim()));
                    if (!c[7].trim().isEmpty())
                        cita.completarCita(c[7].trim()); // restaurar diagnóstico

                    lista.add(cita);
                } catch (Exception e) {
                    System.out.println("⚠ Cita ignorada: " + e.getMessage());
                }
            }
            System.out.println("✔ " + lista.size() + " cita(s) cargadas desde " + RUTA_CITAS);

        } catch (IOException e) {
            System.out.println("✘ Error al cargar citas: " + e.getMessage());
        }
        return lista;
    }

    
    public static void exportarReporte(
            GestorPacientes gestorP,
            GestorMedicos gestorM,
            GestorCitas gestorC) {

        String nombreArchivo = "datos/reporte_" +
                java.time.LocalDate.now().toString().replace("-","") + ".txt";

        try (PrintWriter pw = new PrintWriter(
                new BufferedWriter(new FileWriter(nombreArchivo)))) {

            pw.println("═══════════════════════════════════════════");
            pw.println("   REPORTE DEL SISTEMA — CLÍNICA UPN");
            pw.println("   Fecha: " + java.time.LocalDate.now());
            pw.println("═══════════════════════════════════════════");
            pw.printf("  Pacientes registrados : %d%n", gestorP.getCantidad());
            pw.printf("  Médicos registrados   : %d%n", gestorM.getTodos().size());
            pw.printf("  Citas totales         : %d%n", gestorC.getCantidad());
            pw.println();
            pw.println("DISTRIBUCIÓN DE CITAS POR ESTADO:");
            for (EstadoCita est : EstadoCita.values()) {
                long n = gestorC.getTodas().stream()
                        .filter(c -> c.getEstado() == est).count();
                pw.printf("  %-16s : %d%n", est, n);
            }
            pw.println();
            pw.println("MÉDICOS REGISTRADOS:");
            for (Medico m : gestorM.getTodos())
                pw.printf("  %-30s | %s%n",
                        "Dr. " + m.getNombreCompleto(), m.getEspecialidad());

            System.out.println("✔ Reporte exportado: " + nombreArchivo);

        } catch (IOException e) {
            System.out.println("✘ Error al exportar reporte: " + e.getMessage());
        }
    }

    
    private static String escapar(String valor) {
        if (valor == null) return "";
        return valor.contains(SEPARADOR) ? "\"" + valor + "\"" : valor;
    }
}
