import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * CLASE: MainApp
 * ─────────────────────────────────────────────
 * Nuevo punto de entrada del sistema con interfaz gráfica Swing.
 *
 * Los gestores y clases de negocio (Persona, Paciente, Medico, etc.)
 * NO cambian en absoluto. Solo cambia la capa de presentación.
 *
 * PARA EJECUTAR EN ECLIPSE:
 *   Click derecho sobre MainApp.java → Run As → Java Application
 */
public class MainApp {

    public static void main(String[] args) {

        // SwingUtilities.invokeLater garantiza que la GUI se cree
        // en el Event Dispatch Thread (EDT), que es el hilo correcto
        // para todas las operaciones de Swing. Regla de oro en Swing.
        SwingUtilities.invokeLater(() -> {

            // Intentar usar el Look & Feel del sistema operativo
            // (se verá nativo en Windows, macOS o Linux)
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Si falla, Swing usa su propio L&F por defecto
            }

            // Crear y mostrar la ventana principal
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}
