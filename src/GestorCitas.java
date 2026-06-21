import java.util.ArrayList;

public class GestorCitas {

    private ArrayList<Cita> citas;
    private int contadorId;

    public GestorCitas() {
        this.citas      = new ArrayList<>();
        this.contadorId = 1;
    }

    private String generarId() { return String.format("CIT-%04d", contadorId++); }

    
    public Cita agendarCita(Paciente paciente, Medico medico,
                             String fecha, String hora, String motivo) {
        
        if (existeConflicto(medico, fecha, hora)) {
            System.out.println("⚠ El médico ya tiene una cita en " + fecha + " " + hora);
            return null;
        }
        Cita c = new Cita(generarId(), paciente, medico, fecha, hora, motivo);
        citas.add(c);
        System.out.println("✔ Cita agendada: " + c.getIdCita()
                + " | " + paciente.getNombreCompleto()
                + " con Dr(a). " + medico.getNombreCompleto()
                + " el " + fecha + " a las " + hora);
        return c;
    }

    
    private boolean existeConflicto(Medico medico, String fecha, String hora) {
        for (Cita c : citas)
            if (c.getMedico().getId().equals(medico.getId())
                    && c.getFecha().equals(fecha)
                    && c.getHora().equals(hora)
                    && c.getEstado() != EstadoCita.CANCELADA)
                return true;
        return false;
    }

    public Cita buscarPorId(String id) {
        for (Cita c : citas)
            if (c.getIdCita().equalsIgnoreCase(id)) return c;
        return null;
    }

    
    public ArrayList<Cita> citasDePaciente(String idPaciente) {
        ArrayList<Cita> resultado = new ArrayList<>();
        for (Cita c : citas)
            if (c.getPaciente().getId().equals(idPaciente)) resultado.add(c);
        return resultado;
    }

   
    public ArrayList<Cita> citasDeMediaco(String idMedico, String fecha) {
        ArrayList<Cita> resultado = new ArrayList<>();
        for (Cita c : citas)
            if (c.getMedico().getId().equals(idMedico) && c.getFecha().equals(fecha))
                resultado.add(c);
        return resultado;
    }

    public void listarCitas() {
        System.out.println("\n══ CITAS REGISTRADAS (" + citas.size() + ") ══");
        if (citas.isEmpty()) { System.out.println("  (Sin citas)"); return; }
        for (Cita c : citas)
            System.out.printf("  %s | %s | %s %s | Dr. %s | %s%n",
                    c.getIdCita(), c.getPaciente().getNombreCompleto(),
                    c.getFecha(), c.getHora(),
                    c.getMedico().getNombreCompleto(), c.getEstado());
    }

    public ArrayList<Cita> getTodas() { return citas; }
    public int getCantidad() { return citas.size(); }
    
    public void cargarLista(ArrayList<Cita> lista) {
        for (Cita c : lista) {
            citas.add(c);
            try {
                int num = Integer.parseInt(c.getIdCita().replace("CIT-", ""));
                if (num >= contadorId) contadorId = num + 1;
            } catch (NumberFormatException ignored) { }
        }
    }
}
