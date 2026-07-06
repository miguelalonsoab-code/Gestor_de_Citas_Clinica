import java.util.ArrayList;

/**
 * CLASE: GestorPacientes — versión MySQL
 * ─────────────────────────────────────────────
 * Gestiona todas las operaciones de pacientes delegando
 * al PacienteDAO que ejecuta las queries SQL reales.
 *
 * PATRÓN FACHADA: los paneles Swing siguen llamando a los
 * mismos métodos de siempre. No saben si internamente se
 * usa ArrayList o MySQL — eso es transparente para ellos.
 *
 * FLUJO DE DATOS:
 *   PanelPacientes
 *       └── GestorPacientes  (esta clase — lógica de negocio)
 *               └── PacienteDAO  (acceso a datos — SQL)
 *                       └── MySQL (tabla pacientes)
 */
public class GestorPacientes {

    // ── DAO: ejecuta las queries SQL ──────────────────────────────
    private final PacienteDAO dao = new PacienteDAO();

    // ── Contador de IDs ───────────────────────────────────────────
    // Se inicializa con el total de pacientes ya en MySQL para que
    // el próximo ID generado no colisione con los existentes.
    // Ejemplo: si hay 3 pacientes (PAC-001, PAC-002, PAC-003),
    // contadorId arranca en 4 → próximo será PAC-004.
    private int contadorId;

    // ══════════════════════════════════════════
    // CONSTRUCTOR
    // ══════════════════════════════════════════
    public GestorPacientes() {
        // dao.contarPacientes() ejecuta: SELECT COUNT(*) FROM pacientes
        this.contadorId = dao.contarPacientes() + 1;
    }

    // ── Generador de ID único ─────────────────────────────────────
    /**
     * Genera IDs con formato PAC-001, PAC-002, etc.
     * String.format("%03d") asegura que el número siempre
     * tenga 3 dígitos con ceros a la izquierda.
     * Ejemplo: 1 → "001", 12 → "012", 100 → "100"
     */
    private String generarId() {
        return String.format("PAC-%03d", contadorId++);
    }

    // ══════════════════════════════════════════
    // REGISTRAR PACIENTE
    // ══════════════════════════════════════════
    /**
     * Registra un nuevo paciente en MySQL.
     *
     * VALIDACIÓN PREVIA:
     *   Antes de insertar, verifica en MySQL que no exista
     *   otro paciente con el mismo DNI. Si ya existe, retorna
     *   null y muestra advertencia — nunca genera duplicados.
     *
     * @return Paciente creado o null si el DNI está duplicado
     */
    public Paciente registrarPaciente(String nombre, String apellido,
            String dni, String telefono, String correo,
            String fechaNac, String sangre, String alergias) {

        // Busca en MySQL: SELECT * FROM pacientes WHERE dni = ?
        if (dao.buscarPorDni(dni) != null) {
            System.out.println("⚠ Ya existe un paciente con DNI: " + dni);
            return null;
        }

        Paciente p = new Paciente(generarId(), nombre, apellido,
                dni, telefono, correo, fechaNac, sangre, alergias);

        // INSERT INTO pacientes (...) VALUES (...)
        if (dao.insertar(p)) {
            System.out.println("✔ Paciente registrado: ["
                    + p.getId() + "] " + p.getNombreCompleto());
            return p;
        }
        return null;
    }

    // ══════════════════════════════════════════
    // BUSCAR
    // ══════════════════════════════════════════
    /**
     * Busca por ID exacto.
     * SQL: SELECT * FROM pacientes WHERE id = ?
     */
    public Paciente buscarPorId(String id) {
        return dao.buscarPorId(id);
    }

    /**
     * Busca por DNI exacto.
     * SQL: SELECT * FROM pacientes WHERE dni = ?
     */
    public Paciente buscarPorDni(String dni) {
        return dao.buscarPorDni(dni);
    }

    /**
     * Busca por nombre o apellido (coincidencia parcial).
     * SQL: WHERE LOWER(CONCAT(nombre,' ',apellido)) LIKE LOWER(?)
     * El % antes y después permite encontrar el texto en cualquier posición.
     * Ejemplo: buscar "gar" encuentra "Ana García", "Garza López", etc.
     */
    public ArrayList<Paciente> buscarPorNombre(String texto) {
        return dao.buscarPorNombre(texto);
    }

    // ══════════════════════════════════════════
    // LISTAR
    // ══════════════════════════════════════════
    /**
     * Retorna todos los pacientes ordenados por apellido.
     * SQL: SELECT * FROM pacientes ORDER BY apellido, nombre
     */
    public ArrayList<Paciente> getTodos() {
        return dao.buscarTodos();
    }

    /**
     * Muestra todos los pacientes en consola (modo debug).
     */
    public void listarPacientes() {
        ArrayList<Paciente> lista = dao.buscarTodos();
        System.out.println("\n══ PACIENTES (" + lista.size() + ") ══");
        for (Paciente p : lista)
            System.out.println("  " + p
                    + " | " + (p.isActivo() ? "Activo" : "Inactivo"));
    }

    // ══════════════════════════════════════════
    // ACTUALIZAR
    // ══════════════════════════════════════════
    /**
     * Actualiza teléfono y correo en MySQL (HU-11).
     * SQL: UPDATE pacientes SET telefono=?, correo=? WHERE id=?
     *
     * @return true si la actualización fue exitosa
     */
    public boolean actualizarContacto(String id,
            String telefono, String correo) {
        boolean ok = dao.actualizarContacto(id, telefono, correo);
        if (ok) System.out.println("✔ Contacto actualizado: " + id);
        return ok;
    }

    /**
     * Actualiza el historial médico en MySQL.
     * Se llama automáticamente al completar una cita (RF-07).
     * SQL: UPDATE pacientes SET historial=? WHERE id=?
     *
     * @param id       ID del paciente
     * @param historial texto acumulado del historial completo
     */
    public void actualizarHistorial(String id, String historial) {
        dao.actualizarHistorial(id, historial);
    }

    // ══════════════════════════════════════════
    // BAJA LÓGICA
    // ══════════════════════════════════════════
    /**
     * Da de baja lógica: cambia activo=0 en MySQL.
     * NO elimina el registro físicamente para preservar
     * el historial médico del paciente (principio ético).
     *
     * SQL: UPDATE pacientes SET activo=0 WHERE id=?
     */
    public boolean darDeBaja(String id) {
        Paciente p = dao.buscarPorId(id);
        if (p == null) {
            System.out.println("⚠ Paciente no encontrado: " + id);
            return false;
        }
        boolean ok = dao.darDeBaja(id);
        if (ok) System.out.println("✔ Paciente dado de baja: "
                + p.getNombreCompleto());
        return ok;
    }

    // ══════════════════════════════════════════
    // SOPORTE
    // ══════════════════════════════════════════
    /**
     * Retorna el total de pacientes en MySQL.
     * SQL: SELECT COUNT(*) FROM pacientes
     */
    public int getCantidad() {
        return dao.contarPacientes();
    }

    /**
     * Compatibilidad con versión CSV anterior.
     * Con MySQL los datos ya están en BD — este método
     * no necesita hacer nada.
     */
    public void cargarLista(ArrayList<Paciente> lista) {
        // MySQL: datos ya persistidos, no se necesita carga manual
    }
}
