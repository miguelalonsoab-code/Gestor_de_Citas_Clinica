public abstract class Persona {

    protected String id;
    protected String nombre;
    protected String apellido;
    protected String dni;
    protected String telefono;
    protected String correo;

    public Persona(String id, String nombre, String apellido, String dni,
                   String telefono, String correo) {
        this.id        = id;
        this.nombre    = nombre;
        this.apellido  = apellido;
        this.dni       = dni;
        this.telefono  = telefono;
        this.correo    = correo;
    }

    public abstract void mostrarInfo();

    public String getId()        { return id; }
    public String getNombre()    { return nombre; }
    public String getApellido()  { return apellido; }
    public String getDni()       { return dni; }
    public String getTelefono()  { return telefono; }
    public String getCorreo()    { return correo; }

    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setCorreo(String correo)     { this.correo   = correo; }

    public String getNombreCompleto() { return nombre + " " + apellido; }

    @Override
    public String toString() {
        return "[" + id + "] " + getNombreCompleto() + " | DNI: " + dni;
    }
}
