public enum EstadoCita {
    PENDIENTE   ("Pendiente"),
    CONFIRMADA  ("Confirmada"),
    EN_CURSO    ("En curso"),
    COMPLETADA  ("Completada"),
    CANCELADA   ("Cancelada"),
    NO_ASISTIO  ("No asistió");

    private final String etiqueta;
    EstadoCita(String etiqueta) { this.etiqueta = etiqueta; }

    @Override
    public String toString() { return etiqueta; }
}
