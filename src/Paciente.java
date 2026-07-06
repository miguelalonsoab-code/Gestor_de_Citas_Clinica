public class Paciente extends Persona {

    private String fechaNacimiento;
    private String grupoSanguineo;
    private String alergias;
    private String historialMedico;
    private boolean activo;

    public Paciente(String id, String nombre, String apellido, String dni,
                    String telefono, String correo,
                    String fechaNacimiento, String grupoSanguineo, String alergias) {
        super(id, nombre, apellido, dni, telefono, correo);
        this.fechaNacimiento = fechaNacimiento;
        this.grupoSanguineo  = grupoSanguineo;
        this.alergias        = alergias;
        this.historialMedico = "";
        this.activo          = true;
    }

    @Override
    public void mostrarInfo() {
        System.out.println("┌─── DATOS DEL PACIENTE ──────────────────────┐");
        System.out.println("│ ID       : " + id);
        System.out.println("│ Nombre   : " + getNombreCompleto());
        System.out.println("│ DNI      : " + dni);
        System.out.println("│ Teléfono : " + telefono);
        System.out.println("│ Correo   : " + correo);
        System.out.println("│ F. Nac.  : " + fechaNacimiento);
        System.out.println("│ Sangre   : " + grupoSanguineo);
        System.out.println("│ Alergias : " + (alergias.isEmpty() ? "Ninguna" : alergias));
        System.out.println("│ Estado   : " + (activo ? "Activo" : "Inactivo"));
        System.out.println("└─────────────────────────────────────────────┘");
    }

    public void agregarHistorial(String entrada) {
        this.historialMedico += "\n  - " + entrada;
    }

    public void mostrarHistorial() {
        System.out.println("Historial médico de " + getNombreCompleto() + ":");
        if (historialMedico.isEmpty()) System.out.println("  (Sin registros)");
        else System.out.println(historialMedico);
    }

    public String getFechaNacimiento() { return fechaNacimiento; }
    public String getGrupoSanguineo()  { return grupoSanguineo; }
    public String getAlergias()        { return alergias; }
    public String getHistorial()       { return historialMedico; }
    public boolean isActivo()          { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public void setHistorialDirecto(String historial) {
        this.historialMedico = historial;
    }
    public void setAlergias(String alergias) { this.alergias = alergias; }
}
