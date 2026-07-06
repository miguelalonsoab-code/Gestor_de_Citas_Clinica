import java.util.ArrayList;

/**
 * CLASE: GestorMedicos — versión MySQL
 * ─────────────────────────────────────────────
 * Gestiona todas las operaciones de médicos delegando
 * al MedicoDAO que ejecuta las queries SQL reales.
 *
 * FLUJO DE DATOS:
 *   PanelMedicos
 *       └── GestorMedicos  (esta clase — lógica de negocio)
 *               └── MedicoDAO  (acceso a datos — SQL)
 *                       └── MySQL (tabla medicos)
 */
public class GestorMedicos {

    // ── DAO: ejecuta las queries SQL ──────────────────────────────
    private final MedicoDAO dao = new MedicoDAO();

    // ── Contador de IDs ───────────────────────────────────────────
    private int contadorId;

    // ══════════════════════════════════════════
    // CONSTRUCTOR
    // ══════════════════════════════════════════
    public GestorMedicos() {
        // SELECT COUNT(*) FROM medicos
        this.contadorId = dao.contarMedicos() + 1;
    }

    // ── Generador de ID único ─────────────────────────────────────
    /**
     * Genera IDs con formato MED-001, MED-002, etc.
     */
    private String generarId() {
        return String.format("MED-%03d", contadorId++);
    }

    // ══════════════════════════════════════════
    // REGISTRAR MÉDICO
    // ══════════════════════════════════════════
    /**
     * Registra un nuevo médico en MySQL.
     *
     * VALIDACIÓN PREVIA:
     *   Verifica que la colegiatura (número CMP) sea única.
     *   Un médico no puede registrarse dos veces con el mismo CMP.
     *   SQL: SELECT * FROM medicos WHERE colegiatura = ?
     *
     * @return Medico creado o null si la colegiatura está duplicada
     */
    public Medico registrarMedico(String nombre, String apellido,
            String dni, String telefono, String correo,
            String colegiatura, Especialidad especialidad,
            double tarifa) {

        // Verificar colegiatura única en MySQL
        if (dao.buscarPorColegiatura(colegiatura) != null) {
            System.out.println("⚠ Ya existe un médico con colegiatura: "
                    + colegiatura);
            return null;
        }

        Medico m = new Medico(generarId(), nombre, apellido,
                dni, telefono, correo, colegiatura, especialidad, tarifa);

        // INSERT INTO medicos (...) VALUES (...)
        if (dao.insertar(m)) {
            System.out.println("✔ Médico registrado: ["
                    + m.getId() + "] Dr. " + m.getNombreCompleto()
                    + " — " + especialidad);
            return m;
        }
        return null;
    }

    // ══════════════════════════════════════════
    // BUSCAR
    // ══════════════════════════════════════════
    /**
     * Busca médico por ID exacto.
     * SQL: SELECT * FROM medicos WHERE id = ?
     */
    public Medico buscarPorId(String id) {
        return dao.buscarPorId(id);
    }

    /**
     * Busca médico por número de colegiatura exacto.
     * SQL: SELECT * FROM medicos WHERE colegiatura = ?
     */
    public Medico buscarPorColegiatura(String colegiatura) {
        return dao.buscarPorColegiatura(colegiatura);
    }

    /**
     * Busca médicos disponibles de una especialidad específica.
     * SQL: SELECT * FROM medicos
     *      WHERE especialidad = ? AND disponible = 1
     *
     * Solo retorna médicos con disponible=1 (activos).
     * Se usa al agendar citas para mostrar opciones al paciente (HU-17).
     */
    public ArrayList<Medico> buscarPorEspecialidad(Especialidad esp) {
        return dao.buscarPorEspecialidad(esp);
    }

    // ══════════════════════════════════════════
    // LISTAR
    // ══════════════════════════════════════════
    /**
     * Retorna todos los médicos ordenados por apellido.
     * SQL: SELECT * FROM medicos ORDER BY apellido, nombre
     */
    public ArrayList<Medico> getTodos() {
        return dao.buscarTodos();
    }

    /**
     * Muestra todos los médicos en consola (modo debug).
     */
    public void listarMedicos() {
        ArrayList<Medico> lista = dao.buscarTodos();
        System.out.println("\n══ MÉDICOS (" + lista.size() + ") ══");
        for (Medico m : lista)
            System.out.println("  " + m
                    + " | " + m.getEspecialidad()
                    + " | Disponible: "
                    + (m.isDisponible() ? "Sí" : "No"));
    }

    // ══════════════════════════════════════════
    // ACTUALIZAR
    // ══════════════════════════════════════════
    /**
     * Actualiza la tarifa de consulta del médico (HU-16).
     * SQL: UPDATE medicos SET tarifa_consulta=? WHERE id=?
     *
     * @param id     ID del médico
     * @param tarifa nueva tarifa en soles
     * @return true si la actualización fue exitosa
     */
    public boolean actualizarTarifa(String id, double tarifa) {
        boolean ok = dao.actualizarTarifa(id, tarifa);
        if (ok) System.out.printf("✔ Tarifa actualizada: %s → S/ %.2f%n",
                id, tarifa);
        return ok;
    }

    /**
     * Cambia la disponibilidad del médico (HU-13 / HU-17).
     * SQL: UPDATE medicos SET disponible=? WHERE id=?
     *
     * disponible=1 → puede recibir nuevas citas
     * disponible=0 → no aparece al buscar por especialidad
     *
     * @param id         ID del médico
     * @param disponible true=disponible, false=no disponible
     * @return true si la actualización fue exitosa
     */
    public boolean actualizarDisponibilidad(String id,
            boolean disponible) {
        boolean ok = dao.actualizarDisponibilidad(id, disponible);
        if (ok) System.out.println("✔ Disponibilidad cambiada: "
                + id + " → " + (disponible ? "Disponible" : "No disponible"));
        return ok;
    }

    // ══════════════════════════════════════════
    // SOPORTE
    // ══════════════════════════════════════════
    /**
     * Retorna el total de médicos en MySQL.
     * SQL: SELECT COUNT(*) FROM medicos
     */
    public int getCantidad() {
        return dao.contarMedicos();
    }

    /**
     * Compatibilidad con versión CSV anterior.
     * Con MySQL los datos ya están en BD.
     */
    public void cargarLista(ArrayList<Medico> lista) {
        // MySQL: datos ya persistidos, no se necesita carga manual
    }
}
