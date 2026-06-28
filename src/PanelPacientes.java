import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class PanelPacientes extends JPanel {

    private final GestorPacientes gestor;
    private JTable          tabla;
    private DefaultTableModel modeloTabla;
    private JTextField      campoBusqueda;

    private static final Color AZUL      = new Color(31, 78, 121);
    private static final Color AZUL_SUAVE = new Color(235, 243, 251);
    private static final Color VERDE     = new Color(40, 167, 69);
    private static final Color ROJO      = new Color(220, 53, 69);
    private static final Color NARANJA   = new Color(255, 140, 0);

    public PanelPacientes(GestorPacientes gestor) {
        this.gestor = gestor;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);
        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(crearTabla(),         BorderLayout.CENTER);
        add(crearBotones(),       BorderLayout.SOUTH);
        actualizarTabla();
    }

    private JPanel crearPanelSuperior() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        JLabel t = new JLabel("Gestión de Pacientes");
        t.setFont(new Font("Arial", Font.BOLD, 18));
        t.setForeground(AZUL);
        p.add(t, BorderLayout.WEST);

        JPanel bus = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        bus.setBackground(Color.WHITE);
        campoBusqueda = new JTextField(20);
        campoBusqueda.setFont(new Font("Arial", Font.PLAIN, 13));
        JButton btnB = boton("🔍 Buscar",  AZUL);
        JButton btnL = boton("✕ Limpiar", Color.GRAY);
        btnB.addActionListener(e -> buscar());
        btnL.addActionListener(e -> { campoBusqueda.setText(""); actualizarTabla(); });
        bus.add(new JLabel("Buscar: ")); bus.add(campoBusqueda);
        bus.add(btnB); bus.add(btnL);
        p.add(bus, BorderLayout.EAST);
        return p;
    }

    private JScrollPane crearTabla() {
        String[] cols = {"ID","Nombre","DNI","Teléfono","Correo","F.Nac.","Sangre","Estado"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setFont(new Font("Arial", Font.PLAIN, 13));
        tabla.setRowHeight(28);
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(AZUL);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.setSelectionBackground(AZUL_SUAVE);
        tabla.setGridColor(new Color(220,220,220));
        int[] w = {80,160,100,110,160,100,70,80};
        for (int i=0;i<w.length;i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        JScrollPane sc = new JScrollPane(tabla);
        sc.setBorder(BorderFactory.createLineBorder(new Color(200,200,200)));
        return sc;
    }

    private JPanel crearBotones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(1,0,0,0,Color.LIGHT_GRAY));

        JButton btnReg   = boton("➕ Registrar",       VERDE);
        JButton btnAct   = boton("✏ Actualizar Contacto", NARANJA); // HU-11
        JButton btnHist  = boton("📋 Ver Historial",   AZUL);
        JButton btnBaja  = boton("🚫 Dar de Baja",     ROJO);

        btnReg.addActionListener(e  -> formularioRegistro());
        btnAct.addActionListener(e  -> actualizarContacto());   // HU-11
        btnHist.addActionListener(e -> verHistorial());
        btnBaja.addActionListener(e -> darDeBaja());

        p.add(btnReg); p.add(btnAct); p.add(btnHist); p.add(btnBaja);
        return p;
    }

    // ── HU-01: Registrar paciente ─────────────────────────────────
    private void formularioRegistro() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Registrar Nuevo Paciente", true);
        dlg.setSize(480, 450); dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20,30,10,30));
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5); gbc.anchor = GridBagConstraints.WEST;

        String[] et = {"Nombre:","Apellido:","DNI:","Teléfono:","Correo:","Fecha Nac.:","Grupo Sang.:","Alergias:"};
        JTextField[] campos = new JTextField[et.length];
        for (int i=0;i<et.length;i++) {
            gbc.gridx=0; gbc.gridy=i; gbc.weightx=0; gbc.fill=GridBagConstraints.NONE;
            JLabel l = new JLabel(et[i]); l.setFont(new Font("Arial",Font.PLAIN,13));
            form.add(l, gbc);
            gbc.gridx=1; gbc.weightx=1; gbc.fill=GridBagConstraints.HORIZONTAL;
            campos[i] = new JTextField(20); campos[i].setFont(new Font("Arial",Font.PLAIN,13));
            form.add(campos[i], gbc);
        }
        dlg.add(new JScrollPane(form), BorderLayout.CENTER);

        JPanel bots = new JPanel(new FlowLayout(FlowLayout.CENTER,10,10));
        bots.setBackground(Color.WHITE);
        JButton btnG = boton("💾 Guardar", VERDE);
        JButton btnC = boton("Cancelar",   Color.GRAY);
        btnG.addActionListener(e -> {
            for (int i=0;i<campos.length;i++)
                if (campos[i].getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dlg,"Campo obligatorio: "+et[i].replace(":",""),
                            "Campo vacío", JOptionPane.WARNING_MESSAGE);
                    campos[i].requestFocus(); return;
                }
            Paciente p = gestor.registrarPaciente(
                campos[0].getText().trim(), campos[1].getText().trim(),
                campos[2].getText().trim(), campos[3].getText().trim(),
                campos[4].getText().trim(), campos[5].getText().trim(),
                campos[6].getText().trim(), campos[7].getText().trim());
            if (p != null) {
                JOptionPane.showMessageDialog(dlg,"✔ Paciente registrado: "+p.getId(),
                        "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
                actualizarTabla(); dlg.dispose();
            } else {
                JOptionPane.showMessageDialog(dlg,"⚠ DNI ya registrado en el sistema.",
                        "DNI duplicado", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnC.addActionListener(e -> dlg.dispose());
        bots.add(btnG); bots.add(btnC);
        dlg.add(bots, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ── HU-11: Actualizar datos de contacto ───────────────────────
    private void actualizarContacto() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { sinSeleccion(); return; }
        String id = (String) modeloTabla.getValueAt(fila, 0);
        Paciente p = gestor.buscarPorId(id);
        if (p == null) return;

        JTextField campoTel    = new JTextField(p.getTelefono(), 20);
        JTextField campoCorreo = new JTextField(p.getCorreo(),   20);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,5,6,5); gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx=0; gbc.gridy=0; form.add(new JLabel("Nuevo teléfono:"), gbc);
        gbc.gridx=1; gbc.fill=GridBagConstraints.HORIZONTAL; gbc.weightx=1;
        form.add(campoTel, gbc);
        gbc.gridx=0; gbc.gridy=1; gbc.fill=GridBagConstraints.NONE; gbc.weightx=0;
        form.add(new JLabel("Nuevo correo:"), gbc);
        gbc.gridx=1; gbc.fill=GridBagConstraints.HORIZONTAL; gbc.weightx=1;
        form.add(campoCorreo, gbc);

        int res = JOptionPane.showConfirmDialog(this, form,
                "Actualizar Contacto — " + p.getNombreCompleto(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            String tel    = campoTel.getText().trim();
            String correo = campoCorreo.getText().trim();
            if (tel.isEmpty() || correo.isEmpty()) {
                JOptionPane.showMessageDialog(this,"Teléfono y correo no pueden estar vacíos.",
                        "Campo vacío", JOptionPane.WARNING_MESSAGE);
                return;
            }
            p.setTelefono(tel);
            p.setCorreo(correo);
            actualizarTabla();
            JOptionPane.showMessageDialog(this,
                    "✔ Datos de contacto actualizados para " + p.getNombreCompleto(),
                    "Actualización exitosa", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ── HU-03: Ver historial ──────────────────────────────────────
    private void verHistorial() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { sinSeleccion(); return; }
        Paciente p = gestor.buscarPorId((String) modeloTabla.getValueAt(fila,0));
        if (p == null) return;
        String hist = p.getHistorial().isEmpty()
                ? "Este paciente no tiene registros en su historial médico."
                : p.getHistorial();
        JTextArea area = new JTextArea(hist);
        area.setEditable(false); area.setFont(new Font("Monospaced",Font.PLAIN,13));
        area.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JOptionPane.showMessageDialog(this, new JScrollPane(area),
                "Historial de " + p.getNombreCompleto(), JOptionPane.INFORMATION_MESSAGE);
    }

    // ── HU-12: Dar de baja ────────────────────────────────────────
    private void darDeBaja() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { sinSeleccion(); return; }
        String id = (String) modeloTabla.getValueAt(fila,0);
        String nom = (String) modeloTabla.getValueAt(fila,1);
        int c = JOptionPane.showConfirmDialog(this,"¿Dar de baja a: "+nom+"?",
                "Confirmar baja", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) {
            gestor.darDeBaja(id); actualizarTabla();
            JOptionPane.showMessageDialog(this,"✔ Paciente dado de baja.",
                    "Baja registrada", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ── HU-02: Buscar ─────────────────────────────────────────────
    private void buscar() {
        String texto = campoBusqueda.getText().trim();
        if (texto.isEmpty()) { actualizarTabla(); return; }
        modeloTabla.setRowCount(0);
        ArrayList<Paciente> res = gestor.buscarPorNombre(texto);
        if (res.isEmpty()) {
            Paciente p = gestor.buscarPorDni(texto);
            if (p != null) res.add(p);
        }
        for (Paciente p : res)
            modeloTabla.addRow(fila(p));
        if (res.isEmpty())
            JOptionPane.showMessageDialog(this,"Sin resultados para: "+texto,
                    "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
    }

    public void actualizarTabla() {
        modeloTabla.setRowCount(0);
        for (Paciente p : gestor.getTodos())
            modeloTabla.addRow(fila(p));
    }

    private Object[] fila(Paciente p) {
        return new Object[]{p.getId(),p.getNombreCompleto(),p.getDni(),
            p.getTelefono(),p.getCorreo(),p.getFechaNacimiento(),
            p.getGrupoSanguineo(),p.isActivo()?"Activo":"Inactivo"};
    }

    private void sinSeleccion() {
        JOptionPane.showMessageDialog(this,"Selecciona un paciente de la tabla.",
                "Sin selección", JOptionPane.WARNING_MESSAGE);
    }

    private JButton boton(String t, Color c) {
        JButton b = new JButton(t);
        b.setFont(new Font("Arial",Font.BOLD,13));
        b.setBackground(c); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
        return b;
    }
}
