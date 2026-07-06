import java.util.ArrayList;


public class GestorCitas {

    // ── DAO: ejecuta las queries SQL ──────────────────────────────
    private final CitaDAO dao;

    // ── Referencia a GestorPacientes ─────────────────────────────
    // Necesaria para actualizar el historial del paciente
    // automáticamente al completar una cita (RF-07).
    // Esta dependencia existe porque una cita afecta
    // los datos de un paciente.
    private final GestorPacientes gestorPacientes;

    // ── Contador de IDs ───────────────────────────────────────────
    private int contadorId;

    // ══════════════════════════════════════════
    // CONSTRUCTOR
    // ══════════════════════════════════════════
    /**
     * Recibe GestorPacientes como parámetro (inyección de dependencia).
     * Esto permite que al completar una cita, el gestor pueda
     * actualizar el historial del paciente directamente en MySQL.
     *
     * @param gestorPacientes gestor ya inicializado con conexión MySQL
     */
    public GestorCitas(GestorPacientes gestorPacientes) {
        this.dao             = new CitaDAO();
        this.gestorPacientes = gestorPacientes;
        // SELECT COUNT(*) FROM citas
        this.contadorId      = dao.contarCitas() + 1;
    }

    // ── Generador de ID único ─────────────────────────────────────
    /**
     * Genera IDs con formato CIT-0001, CIT-0002, etc.
     * Usa 4 dígitos para soportar hasta 9,999 citas.
     */
    private String generarId() {
        return String.format("CIT-%04d", contadorId++);
    }

    // ══════════════════════════════════════════
    // AGENDAR CITA
    // ══════════════════════════════════════════
    /**
     * Agenda una nueva cita verificando conflicto de horario en MySQL.
     *
     * FLUJO DETALLADO:
     *   1. dao.existeConflicto() ejecuta en MySQL:
     *      SELECT COUNT(*) FROM citas
     *      WHERE id_medico = ?
     *        AND fecha = ?
     *        AND hora = ?
     *        AND estado != 'CANCELADA'
     *
     *   2. Si COUNT > 0 → hay conflicto → retorna null
     *
     *   3. Si COUNT = 0 → horario libre → inserta la cita:
     *      INSERT INTO citas (...) VALUES (...)
     *
     * Las citas canceladas NO bloquean el horario — si una cita
     * fue cancelada, ese horario queda libre para otra.
     *
     * @param paciente paciente que solicita la cita
     * @param medico   médico que atenderá
     * @param fecha    formato DD/MM/YYYY
     * @param hora     formato HH:MM
     * @param motivo   motivo de la consulta
     * @return la Cita creada o null si hay conflicto
     */
    public Cita agendarCita(Paciente paciente, Medico medico,
            String fecha, String hora, String motivo) {

        if (dao.existeConflicto(medico.getId(), fecha, hora)) {
            System.out.println("⚠ Conflicto de horario: Dr. "
                    + medico.getNombreCompleto()
                    + " ya tiene cita el " + fecha
                    + " a las " + hora);
            return null;
        }

        Cita c = new Cita(generarId(), paciente, medico,
                fecha, hora, motivo);

        if (dao.insertar(c)) {
            System.out.println("✔ Cita agendada: ["
                    + c.getIdCita() + "] "
                    + paciente.getNombreCompleto()
                    + " con Dr. " + medico.getNombreCompleto()
                    + " — " + fecha + " " + hora);
            return c;
        }
        return null;
    }

    // ══════════════════════════════════════════
    // COMPLETAR CITA CON DIAGNÓSTICO
    // ══════════════════════════════════════════
    /**
     * Completa una cita registrando el diagnóstico del médico.
     *
     * OPERACIONES EN MySQL (en orden):
     *   1. UPDATE citas
     *      SET estado='COMPLETADA', diagnostico=?
     *      WHERE id_cita=?
     *
     *   2. UPDATE pacientes
     *      SET historial=?
     *      WHERE id=?
     *      (agrega la nueva entrada al historial acumulado)
     *
     * RF-07: el historial se actualiza AUTOMÁTICAMENTE, sin que
     * el usuario tenga que hacer nada extra.
     *
     * @param idCita      ID de la cita a completar
     * @param diagnostico texto del diagnóstico emitido
     * @return true si ambas operaciones fueron exitosas
     */
    public boolean completarCita(String idCita, String diagnostico) {
        Cita c = dao.buscarPorId(idCita);
        if (c == null) {
            System.out.println("⚠ Cita no encontrada: " + idCita);
            return false;
        }

        // 1. Actualizar estado + diagnóstico en tabla citas
        boolean ok = dao.actualizarEstado(
                idCita, EstadoCita.COMPLETADA, diagnostico);

        if (ok) {
            // 2. Agregar nueva entrada al historial del paciente
            c.getPaciente().agregarHistorial(
                    c.getFecha()
                    + " | Dr. " + c.getMedico().getNombreCompleto()
                    + " | "     + c.getMedico().getEspecialidad()
                    + " | "     + diagnostico);

            // 3. Guardar historial completo actualizado en MySQL
            gestorPacientes.actualizarHistorial(
                    c.getPaciente().getId(),
                    c.getPaciente().getHistorial());

            System.out.println("✔ Cita [" + idCita + "] completada."
                    + " Historial de "
                    + c.getPaciente().getNombreCompleto()
                    + " actualizado en MySQL.");
        }
        return ok;
    }

    // ══════════════════════════════════════════
    // CANCELAR CITA
    // ══════════════════════════════════════════
    /**
     * Cancela una cita cambiando su estado a CANCELADA.
     * SQL: UPDATE citas SET estado='CANCELADA', diagnostico=''
     *      WHERE id_cita=?
     *
     * Una cita cancelada libera el horario del médico para
     * que pueda ser asignado a otro paciente.
     */
    public boolean cancelarCita(String idCita) {
        boolean ok = dao.actualizarEstado(
                idCita, EstadoCita.CANCELADA, "");
        if (ok) System.out.println("✔ Cita cancelada: [" + idCita + "]");
        return ok;
    }

    // ══════════════════════════════════════════
    // CONFIRMAR CITA
    // ══════════════════════════════════════════
    /**
     * Confirma una cita cambiando su estado de PENDIENTE a CONFIRMADA.
     * SQL: UPDATE citas SET estado='CONFIRMADA', diagnostico=''
     *      WHERE id_cita=?
     *
     * Sirve para que el paciente sepa que su cita fue aprobada.
     */
    public boolean confirmarCita(String idCita) {
        boolean ok = dao.actualizarEstado(
                idCita, EstadoCita.CONFIRMADA, "");
        if (ok) System.out.println("✔ Cita confirmada: [" + idCita + "]");
        return ok;
    }

    // ══════════════════════════════════════════
    // BUSCAR
    // ══════════════════════════════════════════
    /**
     * Busca una cita por su ID exacto.
     * SQL: SELECT * FROM citas WHERE id_cita = ?
     */
    public Cita buscarPorId(String id) {
        return dao.buscarPorId(id);
    }

    /**
     * Retorna todas las citas de un paciente específico.
     * SQL: SELECT * FROM citas WHERE id_paciente = ?
     *      ORDER BY fecha, hora
     *
     * Usado en HU-14: el recepcionista consulta
     * el historial de citas de un paciente.
     */
    public ArrayList<Cita> citasDePaciente(String idPaciente) {
        return dao.buscarPorPaciente(idPaciente);
    }

    /**
     * Retorna citas de un médico en una fecha específica.
     * SQL: SELECT * FROM citas
     *      WHERE id_medico = ? AND fecha = ?
     *      ORDER BY hora
     *
     * Usado en HU-13: el médico consulta su agenda del día.
     */
    public ArrayList<Cita> citasDeMediaco(String idMedico,
            String fecha) {
        return dao.buscarPorMedicoYFecha(idMedico, fecha);
    }

    // ══════════════════════════════════════════
    // LISTAR
    // ══════════════════════════════════════════
    /**
     * Retorna todas las citas ordenadas por fecha y hora.
     * SQL: SELECT * FROM citas ORDER BY fecha, hora
     */
    public ArrayList<Cita> getTodas() {
        return dao.buscarTodas();
    }

    /**
     * Muestra todas las citas en consola (modo debug).
     */
    public void listarCitas() {
        ArrayList<Cita> lista = dao.buscarTodas();
        System.out.println("\n══ CITAS (" + lista.size() + ") ══");
        for (Cita c : lista)
            System.out.printf("  %s | %-20s | Dr. %-20s | %s %s | %s%n",
                    c.getIdCita(),
                    c.getPaciente().getNombreCompleto(),
                    c.getMedico().getNombreCompleto(),
                    c.getFecha(), c.getHora(),
                    c.getEstado());
    }

    // ══════════════════════════════════════════
    // SOPORTE
    // ══════════════════════════════════════════
    /**
     * Retorna el total de citas en MySQL.
     * SQL: SELECT COUNT(*) FROM citas
     */
    public int getCantidad() {
        return dao.contarCitas();
    }

    /**
     * Compatibilidad con versión CSV anterior.
     * Con MySQL los datos ya están en BD.
     */
    public void cargarLista(ArrayList<Cita> lista) {
        // MySQL: datos ya persistidos, no se necesita carga manual
    }
}
