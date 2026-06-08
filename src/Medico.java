public class Medico extends Persona {

    private String      colegiatura;
    private Especialidad especialidad;
    private boolean     disponible;
    private double      tarifaConsulta;

    public Medico(String id, String nombre, String apellido, String dni,
                  String telefono, String correo,
                  String colegiatura, Especialidad especialidad, double tarifaConsulta) {
        super(id, nombre, apellido, dni, telefono, correo);
        this.colegiatura    = colegiatura;
        this.especialidad   = especialidad;
        this.tarifaConsulta = tarifaConsulta;
        this.disponible     = true;
    }

    @Override
    public void mostrarInfo() {
        System.out.println("┌─── DATOS DEL MÉDICO ────────────────────────┐");
        System.out.println("│ ID           : " + id);
        System.out.println("│ Nombre       : " + "Dr(a). " + getNombreCompleto());
        System.out.println("│ DNI          : " + dni);
        System.out.println("│ Colegiatura  : " + colegiatura);
        System.out.println("│ Especialidad : " + especialidad);
        System.out.println("│ Tarifa       : S/ " + String.format("%.2f", tarifaConsulta));
        System.out.println("│ Disponible   : " + (disponible ? "Sí" : "No"));
        System.out.println("└─────────────────────────────────────────────┘");
    }

    public String       getColegiatura()   { return colegiatura; }
    public Especialidad getEspecialidad()  { return especialidad; }
    public boolean      isDisponible()     { return disponible; }
    public double       getTarifaConsulta(){ return tarifaConsulta; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public void setTarifaConsulta(double tarifa)  { this.tarifaConsulta = tarifa; }
}
