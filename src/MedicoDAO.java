import java.sql.*;
import java.util.ArrayList;

/**
 * CLASE: MedicoDAO
 * Acceso a datos para la tabla 'medicos' en MySQL.
 */
public class MedicoDAO {

    // ══════════════════════════════════════════
    // C — INSERTAR
    // ══════════════════════════════════════════
    public boolean insertar(Medico m) {
        String sql = "INSERT INTO medicos "
                   + "(id, nombre, apellido, dni, telefono, correo, "
                   + " colegiatura, especialidad, tarifa_consulta, disponible) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, m.getId());
            ps.setString(2, m.getNombre());
            ps.setString(3, m.getApellido());
            ps.setString(4, m.getDni());
            ps.setString(5, m.getTelefono());
            ps.setString(6, m.getCorreo());
            ps.setString(7, m.getColegiatura());
            // Guardamos el nombre del enum como String
            ps.setString(8, m.getEspecialidad().name());
            ps.setDouble(9, m.getTarifaConsulta());
            ps.setInt(10,   m.isDisponible() ? 1 : 0);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("✘ Error al insertar médico: " + e.getMessage());
            return false;
        }
    }

    // ══════════════════════════════════════════
    // R — LEER
    // ══════════════════════════════════════════
    public ArrayList<Medico> buscarTodos() {
        ArrayList<Medico> lista = new ArrayList<>();
        String sql = "SELECT * FROM medicos ORDER BY apellido, nombre";

        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(construirMedico(rs));

        } catch (SQLException e) {
            System.out.println("✘ Error al obtener médicos: " + e.getMessage());
        }
        return lista;
    }

    public Medico buscarPorId(String id) {
        String sql = "SELECT * FROM medicos WHERE id = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return construirMedico(rs);
            }
        } catch (SQLException e) {
            System.out.println("✘ Error al buscar médico: " + e.getMessage());
        }
        return null;
    }

    public Medico buscarPorColegiatura(String colegiatura) {
        String sql = "SELECT * FROM medicos WHERE colegiatura = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, colegiatura);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return construirMedico(rs);
            }
        } catch (SQLException e) {
            System.out.println("✘ Error al buscar por colegiatura: " + e.getMessage());
        }
        return null;
    }

    public ArrayList<Medico> buscarPorEspecialidad(Especialidad esp) {
        ArrayList<Medico> lista = new ArrayList<>();
        String sql = "SELECT * FROM medicos WHERE especialidad = ? AND disponible = 1";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, esp.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(construirMedico(rs));
            }
        } catch (SQLException e) {
            System.out.println("✘ Error al buscar por especialidad: " + e.getMessage());
        }
        return lista;
    }

    // ══════════════════════════════════════════
    // U — ACTUALIZAR
    // ══════════════════════════════════════════
    public boolean actualizarTarifa(String id, double nuevaTarifa) {
        String sql = "UPDATE medicos SET tarifa_consulta = ? WHERE id = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setDouble(1, nuevaTarifa);
            ps.setString(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("✘ Error al actualizar tarifa: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarDisponibilidad(String id, boolean disponible) {
        String sql = "UPDATE medicos SET disponible = ? WHERE id = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, disponible ? 1 : 0);
            ps.setString(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("✘ Error al actualizar disponibilidad: " + e.getMessage());
            return false;
        }
    }

    public int contarMedicos() {
        String sql = "SELECT COUNT(*) FROM medicos";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("✘ Error al contar médicos: " + e.getMessage());
        }
        return 0;
    }

    // ══════════════════════════════════════════
    // HELPER
    // ══════════════════════════════════════════
    private Medico construirMedico(ResultSet rs) throws SQLException {
        // Especialidad.valueOf() convierte el String "CARDIOLOGIA" al enum
        Especialidad esp = Especialidad.valueOf(rs.getString("especialidad"));
        Medico m = new Medico(
            rs.getString("id"),
            rs.getString("nombre"),
            rs.getString("apellido"),
            rs.getString("dni"),
            rs.getString("telefono"),
            rs.getString("correo"),
            rs.getString("colegiatura"),
            esp,
            rs.getDouble("tarifa_consulta")
        );
        m.setDisponible(rs.getInt("disponible") == 1);
        return m;
    }
}
