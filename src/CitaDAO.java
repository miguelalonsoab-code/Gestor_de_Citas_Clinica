import java.sql.*;
import java.util.ArrayList;

/**
 * CLASE: CitaDAO
 * Acceso a datos para la tabla 'citas' en MySQL.
 *
 * Las citas tienen FOREIGN KEY hacia pacientes y medicos,
 * por eso necesita PacienteDAO y MedicoDAO para resolver
 * las referencias al leer.
 */
public class CitaDAO {

    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final MedicoDAO   medicoDAO   = new MedicoDAO();

    // ══════════════════════════════════════════
    // C — INSERTAR
    // ══════════════════════════════════════════
    public boolean insertar(Cita c) {
        String sql = "INSERT INTO citas "
                   + "(id_cita, id_paciente, id_medico, fecha, hora, "
                   + " motivo, estado, diagnostico, costo) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, c.getIdCita());
            ps.setString(2, c.getPaciente().getId());
            ps.setString(3, c.getMedico().getId());
            ps.setString(4, c.getFecha());
            ps.setString(5, c.getHora());
            ps.setString(6, c.getMotivo());
            ps.setString(7, c.getEstado().name());
            ps.setString(8, c.getDiagnostico());
            ps.setDouble(9, c.getCosto());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("✘ Error al insertar cita: " + e.getMessage());
            return false;
        }
    }

    // ══════════════════════════════════════════
    // R — LEER
    // ══════════════════════════════════════════
    public ArrayList<Cita> buscarTodas() {
        ArrayList<Cita> lista = new ArrayList<>();
        String sql = "SELECT * FROM citas ORDER BY fecha, hora";

        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Cita c = construirCita(rs);
                if (c != null) lista.add(c);
            }
        } catch (SQLException e) {
            System.out.println("✘ Error al obtener citas: " + e.getMessage());
        }
        return lista;
    }

    public Cita buscarPorId(String idCita) {
        String sql = "SELECT * FROM citas WHERE id_cita = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, idCita);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return construirCita(rs);
            }
        } catch (SQLException e) {
            System.out.println("✘ Error al buscar cita: " + e.getMessage());
        }
        return null;
    }

    public ArrayList<Cita> buscarPorPaciente(String idPaciente) {
        ArrayList<Cita> lista = new ArrayList<>();
        String sql = "SELECT * FROM citas WHERE id_paciente = ? ORDER BY fecha, hora";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, idPaciente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cita c = construirCita(rs);
                    if (c != null) lista.add(c);
                }
            }
        } catch (SQLException e) {
            System.out.println("✘ Error al buscar citas por paciente: " + e.getMessage());
        }
        return lista;
    }

    public ArrayList<Cita> buscarPorMedicoYFecha(String idMedico, String fecha) {
        ArrayList<Cita> lista = new ArrayList<>();
        String sql = "SELECT * FROM citas WHERE id_medico = ? AND fecha = ? ORDER BY hora";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, idMedico);
            ps.setString(2, fecha);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cita c = construirCita(rs);
                    if (c != null) lista.add(c);
                }
            }
        } catch (SQLException e) {
            System.out.println("✘ Error al buscar citas por médico/fecha: " + e.getMessage());
        }
        return lista;
    }

    // ══════════════════════════════════════════
    // U — ACTUALIZAR ESTADO Y DIAGNÓSTICO
    // ══════════════════════════════════════════
    public boolean actualizarEstado(String idCita, EstadoCita nuevoEstado, String diagnostico) {
        String sql = "UPDATE citas SET estado = ?, diagnostico = ? WHERE id_cita = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.name());
            ps.setString(2, diagnostico != null ? diagnostico : "");
            ps.setString(3, idCita);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("✘ Error al actualizar estado de cita: " + e.getMessage());
            return false;
        }
    }

    public int contarCitas() {
        String sql = "SELECT COUNT(*) FROM citas";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("✘ Error al contar citas: " + e.getMessage());
        }
        return 0;
    }

    // ══════════════════════════════════════════
    // HELPER
    // ══════════════════════════════════════════
    /**
     * Construye un objeto Cita desde una fila del ResultSet.
     * Resuelve las referencias a Paciente y Médico consultando sus DAOs.
     */
    private Cita construirCita(ResultSet rs) throws SQLException {
        String idPaciente = rs.getString("id_paciente");
        String idMedico   = rs.getString("id_medico");

        Paciente p = pacienteDAO.buscarPorId(idPaciente);
        Medico   m = medicoDAO.buscarPorId(idMedico);

        // Si alguna referencia está rota, ignorar la cita
        if (p == null || m == null) {
            System.out.println("⚠ Cita ignorada — referencia rota: " + rs.getString("id_cita"));
            return null;
        }

        Cita c = new Cita(
            rs.getString("id_cita"), p, m,
            rs.getString("fecha"),
            rs.getString("hora"),
            rs.getString("motivo")
        );
        c.setEstado(EstadoCita.valueOf(rs.getString("estado")));

        String diag = rs.getString("diagnostico");
        if (diag != null && !diag.isEmpty()) {
            c.completarCita(diag); // restaura diagnóstico y actualiza historial
        }
        return c;
    }
}
