import java.util.ArrayList;

public class Receta {

    private String            idReceta;
    private Cita              cita;
    private ArrayList<String> medicamentos;
    private String            indicaciones;
    private String            fechaEmision;

    public Receta(String idReceta, Cita cita, String fechaEmision, String indicaciones) {
        this.idReceta     = idReceta;
        this.cita         = cita;
        this.fechaEmision = fechaEmision;
        this.indicaciones = indicaciones;
        this.medicamentos = new ArrayList<>();
    }

    /**
     * @param medicamento Nombre + dosis + frecuencia
     */
    public void agregarMedicamento(String medicamento) {
        medicamentos.add(medicamento);
    }

    public void mostrarReceta() {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║            RECETA MÉDICA                     ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║ Receta N°  : " + idReceta);
        System.out.println("║ Paciente   : " + cita.getPaciente().getNombreCompleto());
        System.out.println("║ Médico     : Dr(a). " + cita.getMedico().getNombreCompleto());
        System.out.println("║ Fecha      : " + fechaEmision);
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║ MEDICAMENTOS:");
        if (medicamentos.isEmpty()) {
            System.out.println("║   (Sin medicamentos prescritos)");
        } else {
            for (int i = 0; i < medicamentos.size(); i++)
                System.out.println("║   " + (i+1) + ". " + medicamentos.get(i));
        }
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║ Indicaciones: " + indicaciones);
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    public String     getIdReceta()     { return idReceta; }
    public Cita       getCita()         { return cita; }
    public ArrayList<String> getMedicamentos() { return medicamentos; }
    public String     getIndicaciones() { return indicaciones; }
    public String getFechaEmision() { return fechaEmision; }
}
