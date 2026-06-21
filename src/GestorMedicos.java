import java.util.ArrayList;


public class GestorMedicos {

    private ArrayList<Medico> medicos;
    private int contadorId;

    public GestorMedicos() {
        this.medicos     = new ArrayList<>();
        this.contadorId  = 1;
    }

    private String generarId() { return String.format("MED-%03d", contadorId++); }

    public Medico registrarMedico(String nombre, String apellido, String dni,
                                   String telefono, String correo, String colegiatura,
                                   Especialidad especialidad, double tarifa) {
        if (buscarPorColegiatura(colegiatura) != null) {
            System.out.println("⚠ Ya existe un médico con colegiatura: " + colegiatura);
            return null;
        }
        Medico m = new Medico(generarId(), nombre, apellido, dni,
                               telefono, correo, colegiatura, especialidad, tarifa);
        medicos.add(m);
        System.out.println("✔ Médico registrado: Dr(a). " + m.getNombreCompleto() + " [" + m.getId() + "]");
        return m;
    }

    public Medico buscarPorId(String id) {
        for (Medico m : medicos)
            if (m.getId().equalsIgnoreCase(id)) return m;
        return null;
    }

    public Medico buscarPorColegiatura(String colegiatura) {
        for (Medico m : medicos)
            if (m.getColegiatura().equals(colegiatura)) return m;
        return null;
    }

    
    public ArrayList<Medico> buscarPorEspecialidad(Especialidad esp) {
        ArrayList<Medico> resultado = new ArrayList<>();
        for (Medico m : medicos)
            if (m.getEspecialidad() == esp && m.isDisponible()) resultado.add(m);
        return resultado;
    }

    public void listarMedicos() {
        System.out.println("\n══ LISTA DE MÉDICOS (" + medicos.size() + " registrados) ══");
        if (medicos.isEmpty()) { System.out.println("  (Sin médicos)"); return; }
        for (Medico m : medicos)
            System.out.println("  " + m + " | " + m.getEspecialidad()
                    + " | Disponible: " + (m.isDisponible() ? "Sí" : "No"));
    }

    public ArrayList<Medico> getTodos() { return medicos; }
    
    public void cargarLista(ArrayList<Medico> lista) {
        for (Medico m : lista) {
            medicos.add(m);
            try {
                int num = Integer.parseInt(m.getId().replace("MED-", ""));
                if (num >= contadorId) contadorId = num + 1;
            } catch (NumberFormatException ignored) { }
        }
    }
}
