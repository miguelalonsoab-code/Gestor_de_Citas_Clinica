import java.util.ArrayList;

public class GestorPacientes {

    private ArrayList<Paciente> pacientes;
    private int contadorId;

    public GestorPacientes() {
        this.pacientes   = new ArrayList<>();
        this.contadorId  = 1;
    }

    
    private String generarId() {
        return String.format("PAC-%03d", contadorId++);
    }

    
    public Paciente registrarPaciente(String nombre, String apellido, String dni,
                                      String telefono, String correo,
                                      String fechaNac, String grupoSanguineo, String alergias) {
        
        if (buscarPorDni(dni) != null) {
            System.out.println("⚠ Ya existe un paciente con DNI: " + dni);
            return null;
        }
        Paciente p = new Paciente(generarId(), nombre, apellido, dni,
                                   telefono, correo, fechaNac, grupoSanguineo, alergias);
        pacientes.add(p);
        System.out.println("✔ Paciente registrado: " + p.getNombreCompleto() + " [" + p.getId() + "]");
        return p;
    }

    
    public Paciente buscarPorId(String id) {
        for (Paciente p : pacientes)
            if (p.getId().equalsIgnoreCase(id)) return p;
        return null;
    }

    
    public Paciente buscarPorDni(String dni) {
        for (Paciente p : pacientes)
            if (p.getDni().equals(dni)) return p;
        return null;
    }

    
    public ArrayList<Paciente> buscarPorNombre(String texto) {
        ArrayList<Paciente> resultado = new ArrayList<>();
        for (Paciente p : pacientes)
            if (p.getNombreCompleto().toLowerCase().contains(texto.toLowerCase()))
                resultado.add(p);
        return resultado;
    }

    
    public void listarPacientes() {
        System.out.println("\n══ LISTA DE PACIENTES (" + pacientes.size() + " registrados) ══");
        if (pacientes.isEmpty()) { System.out.println("  (Sin pacientes)"); return; }
        for (Paciente p : pacientes)
            System.out.println("  " + p + " | Estado: " + (p.isActivo() ? "Activo" : "Inactivo"));
    }

    public boolean darDeBaja(String id) {
        Paciente p = buscarPorId(id);
        if (p == null) { System.out.println("⚠ Paciente no encontrado: " + id); return false; }
        p.setActivo(false);
        System.out.println("✔ Paciente dado de baja: " + p.getNombreCompleto());
        return true;
    }

    public ArrayList<Paciente> getTodos() { return pacientes; }
    public int getCantidad() { return pacientes.size(); }
}
