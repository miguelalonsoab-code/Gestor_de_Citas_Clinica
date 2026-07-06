import java.sql.*;
import java.util.ArrayList;

/**
 * CLASE: PacienteDAO (Data Access Object)
 * ─────────────────────────────────────────────
 * Reemplaza la persistencia en ArrayList de GestorPacientes
 * con operaciones SQL reales sobre MySQL.
 *
 * DAO es un patrón de diseño que separa la lógica de acceso
 * a datos del resto de la aplicación. Si en el futuro cambias
 * de MySQL a PostgreSQL, solo cambias esta clase — los gestores
 * y paneles Swing no se tocan.
 *
 * OPERACIONES CRUD:
 *   C → insertar()
 *   R → buscarPorId(), buscarPorDni(), buscarTodos()
 *   U → actualizarContacto(), actualizarEstado()
 *   D → darDeBaja() (baja lógica, no DELETE físico)
 *
 * CONCEPTO CLAVE — PreparedStatement vs Statement:
 *   Statement:         "SELECT * WHERE dni = '" + dni + "'"  ← INSEGURO (SQL Injection)
 *   PreparedStatement: "SELECT * WHERE dni = ?"              ← SEGURO (parámetros)
 *   Siempre usar PreparedStatement con parámetros externos.
 */
public class PacienteDAO {

    // ══════════════════════════════════════════
    // C — INSERTAR
    // ══════════════════════════════════════════
    /**
     * Inserta un nuevo paciente en la tabla 'pacientes'.
     *
     * Flujo JDBC para escritura:
     *   1. Obtener conexión desde ConexionDB
     *   2. Preparar la sentencia SQL con PreparedStatement
     *   3. Asignar los parámetros con setString(), setDouble(), etc.
     *   4. Ejecutar con executeUpdate() → retorna filas afectadas
     *   5. Cerrar el PreparedStatement (try-with-resources)
     *
     * @param p paciente a insertar
     * @return true si se insertó correctamente
     */
    public boolean insertar(Paciente p) {
        String sql = "INSERT INTO pacientes "
                   + "(id, nombre, apellido, dni, telefono, correo, "
                   + " fecha_nacimiento, grupo_sanguineo, alergias, historial, activo) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {

            ps.setString(1,  p.getId());
            ps.setString(2,  p.getNombre());
            ps.setString(3,  p.getApellido());
            ps.setString(4,  p.getDni());
            ps.setString(5,  p.getTelefono());
            ps.setString(6,  p.getCorreo());
            ps.setString(7,  p.getFechaNacimiento());
            ps.setString(8,  p.getGrupoSanguineo());
            ps.setString(9,  p.getAlergias());
            ps.setString(10, p.getHistorial());
            ps.setInt(11,    p.isActivo() ? 1 : 0);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.out.println("✘ Error al insertar paciente: " + e.getMessage());
            return false;
        }
    }

    // ══════════════════════════════════════════
    // R — LEER
    // ══════════════════════════════════════════
    /**
     * Retorna todos los pacientes registrados.
     *
     * Flujo JDBC para lectura:
     *   1. Preparar sentencia SELECT
     *   2. Ejecutar con executeQuery() → retorna ResultSet
     *   3. Recorrer ResultSet con rs.next()
     *   4. Extraer valores con rs.getString(), rs.getInt(), etc.
     *   5. Construir objetos Paciente y agregarlos a la lista
     *
     * @return ArrayList con todos los pacientes
     */
    public ArrayList<Paciente> buscarTodos() {
        ArrayList<Paciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM pacientes ORDER BY apellido, nombre";

        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(construirPaciente(rs));
            }

        } catch (SQLException e) {
            System.out.println("✘ Error al obtener pacientes: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Busca un paciente por su ID único.
     *
     * @param id ID del paciente (ej: "PAC-001")
     * @return Paciente encontrado o null si no existe
     */
    public Paciente buscarPorId(String id) {
        String sql = "SELECT * FROM pacientes WHERE id = ?";

        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return construirPaciente(rs);
            }

        } catch (SQLException e) {
            System.out.println("✘ Error al buscar paciente por ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Busca un paciente por su DNI.
     * Se usa para validar duplicados antes de registrar.
     *
     * @param dni DNI a buscar
     * @return Paciente encontrado o null
     */
    public Paciente buscarPorDni(String dni) {
        String sql = "SELECT * FROM pacientes WHERE dni = ?";

        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, dni);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return construirPaciente(rs);
            }

        } catch (SQLException e) {
            System.out.println("✘ Error al buscar paciente por DNI: " + e.getMessage());
        }
        return null;
    }

    /**
     * Busca pacientes cuyo nombre o apellido contenga el texto.
     * LIKE '%texto%' hace la búsqueda parcial en SQL.
     *
     * @param texto texto a buscar (parcial, sin distinguir mayúsculas)
     * @return lista de pacientes que coincidan
     */
    public ArrayList<Paciente> buscarPorNombre(String texto) {
        ArrayList<Paciente> lista = new ArrayList<>();
        // CONCAT para buscar en nombre y apellido en una sola query
        String sql = "SELECT * FROM pacientes "
                   + "WHERE LOWER(CONCAT(nombre, ' ', apellido)) LIKE LOWER(?)";

        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, "%" + texto + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(construirPaciente(rs));
            }

        } catch (SQLException e) {
            System.out.println("✘ Error al buscar por nombre: " + e.getMessage());
        }
        return lista;
    }

    // ══════════════════════════════════════════
    // U — ACTUALIZAR
    // ══════════════════════════════════════════
    /**
     * Actualiza el teléfono y correo de un paciente (HU-11).
     *
     * @param id       ID del paciente
     * @param telefono nuevo teléfono
     * @param correo   nuevo correo
     * @return true si se actualizó correctamente
     */
    public boolean actualizarContacto(String id, String telefono, String correo) {
        String sql = "UPDATE pacientes SET telefono = ?, correo = ? WHERE id = ?";

        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, telefono);
            ps.setString(2, correo);
            ps.setString(3, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("✘ Error al actualizar contacto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza el historial médico del paciente.
     * Se llama automáticamente al completar una cita.
     *
     * @param id       ID del paciente
     * @param historial historial completo actualizado
     * @return true si se guardó correctamente
     */
    public boolean actualizarHistorial(String id, String historial) {
        String sql = "UPDATE pacientes SET historial = ? WHERE id = ?";

        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, historial);
            ps.setString(2, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("✘ Error al actualizar historial: " + e.getMessage());
            return false;
        }
    }

    // ══════════════════════════════════════════
    // D — BAJA LÓGICA (no DELETE físico)
    // ══════════════════════════════════════════
    /**
     * Da de baja lógica al paciente (activo = 0).
     * Nunca se elimina físicamente para preservar el historial.
     *
     * @param id ID del paciente a dar de baja
     * @return true si se realizó la baja correctamente
     */
    public boolean darDeBaja(String id) {
        String sql = "UPDATE pacientes SET activo = 0 WHERE id = ?";

        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("✘ Error al dar de baja: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cuenta el total de pacientes registrados.
     * Se usa para generar el próximo ID correlativo.
     *
     * @return número total de pacientes
     */
    public int contarPacientes() {
        String sql = "SELECT COUNT(*) FROM pacientes";

        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.out.println("✘ Error al contar pacientes: " + e.getMessage());
        }
        return 0;
    }

    // ══════════════════════════════════════════
    // HELPER: construir objeto Paciente desde ResultSet
    // ══════════════════════════════════════════
    /**
     * Convierte una fila del ResultSet en un objeto Paciente.
     * Centraliza la construcción para evitar código repetido en
     * buscarPorId(), buscarTodos(), buscarPorNombre(), etc.
     *
     * @param rs ResultSet posicionado en la fila a leer
     * @return objeto Paciente construido con los datos de la fila
     */
    private Paciente construirPaciente(ResultSet rs) throws SQLException {
        Paciente p = new Paciente(
            rs.getString("id"),
            rs.getString("nombre"),
            rs.getString("apellido"),
            rs.getString("dni"),
            rs.getString("telefono"),
            rs.getString("correo"),
            rs.getString("fecha_nacimiento"),
            rs.getString("grupo_sanguineo"),
            rs.getString("alergias")
        );
        // Restaurar el historial acumulado
        String hist = rs.getString("historial");
        if (hist != null && !hist.isEmpty()) {
            // El historial se guarda completo — lo asignamos directamente
            // evitando agregar el prefijo "\n  - " de agregarHistorial()
            p.setHistorialDirecto(hist);
        }
        p.setActivo(rs.getInt("activo") == 1);
        return p;
    }
}
