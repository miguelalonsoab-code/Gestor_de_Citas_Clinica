public enum Especialidad {
    MEDICINA_GENERAL    ("Medicina General"),
    PEDIATRIA           ("Pediatría"),
    CARDIOLOGIA         ("Cardiología"),
    DERMATOLOGIA        ("Dermatología"),
    NEUROLOGIA          ("Neurología"),
    GINECOLOGIA         ("Ginecología"),
    TRAUMATOLOGIA       ("Traumatología"),
    OFTALMOLOGIA        ("Oftalmología"),
    ODONTOLOGIA         ("Odontología"),
    PSICOLOGIA          ("Psicología");

    private final String nombre;

    Especialidad(String nombre) { this.nombre = nombre; }

    public String getNombre() { return nombre; }

    @Override
    public String toString() { return nombre; }
}
