public class Cita {

    private String      idCita;
    private Paciente    paciente;
    private Medico      medico;
    private String      fecha;       // formato: DD/MM/YYYY
    private String      hora;        // formato: HH:MM
    private EstadoCita  estado;
    private String      motivo;
    private String      diagnostico;
    private double      costo;

    public Cita(String idCita, Paciente paciente, Medico medico,
                String fecha, String hora, String motivo) {
        this.idCita      = idCita;
        this.paciente    = paciente;
        this.medico      = medico;
        this.fecha       = fecha;
        this.hora        = hora;
        this.motivo      = motivo;
        this.estado      = EstadoCita.PENDIENTE;
        this.diagnostico = "";
        this.costo       = medico.getTarifaConsulta();
    }

    public void mostrarResumen() {
        System.out.println("┌─── CITA MÉDICA ─────────────────────────────┐");
        System.out.println("│ ID Cita    : " + idCita);
        System.out.println("│ Paciente   : " + paciente.getNombreCompleto());
        System.out.println("│ Médico     : Dr(a). " + medico.getNombreCompleto());
        System.out.println("│ Especialid.: " + medico.getEspecialidad());
        System.out.println("│ Fecha/Hora : " + fecha + " a las " + hora);
        System.out.println("│ Motivo     : " + motivo);
        System.out.println("│ Estado     : " + estado);
        System.out.println("│ Costo      : S/ " + String.format("%.2f", costo));
        if (!diagnostico.isEmpty())
            System.out.println("│ Diagnóstico: " + diagnostico);
        System.out.println("└─────────────────────────────────────────────┘");
    }

    public void completarCita(String diagnostico) {
        this.diagnostico = diagnostico;
        this.estado      = EstadoCita.COMPLETADA;
        paciente.agregarHistorial(fecha + " | Dr. " + medico.getNombreCompleto()
                + " | " + medico.getEspecialidad() + " | " + diagnostico);
    }

    public void cancelar() { this.estado = EstadoCita.CANCELADA; }
    public void confirmar() { this.estado = EstadoCita.CONFIRMADA; }

    // ── Getters ──────────────────────────────────────────────────
    public String     getIdCita()     { return idCita; }
    public Paciente   getPaciente()   { return paciente; }
    public Medico     getMedico()     { return medico; }
    public String     getFecha()      { return fecha; }
    public String     getHora()       { return hora; }
    public EstadoCita getEstado()     { return estado; }
    public String     getMotivo()     { return motivo; }
    public String     getDiagnostico(){ return diagnostico; }
    public double     getCosto()      { return costo; }
    public void       setEstado(EstadoCita estado) { this.estado = estado; }
}
