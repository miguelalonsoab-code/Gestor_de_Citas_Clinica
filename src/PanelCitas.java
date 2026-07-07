import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;


public class PanelCitas extends JPanel {

    private final GestorCitas     gestorCitas;
    private final GestorPacientes gestorPacientes;
    private final GestorMedicos   gestorMedicos;

    private JTable            tabla;
    private DefaultTableModel modeloTabla;

    private static final Color AZUL      = new Color(31,78,121);
    private static final Color VERDE     = new Color(40,167,69);
    private static final Color ROJO      = new Color(220,53,69);
    private static final Color NARANJA   = new Color(255,140,0);
    private static final Color MORADO    = new Color(128,0,128);
    private static final Color AZUL_SUB  = new Color(235,243,251);

    public PanelCitas(GestorCitas gc, GestorPacientes gp, GestorMedicos gm) {
        this.gestorCitas=gc; this.gestorPacientes=gp; this.gestorMedicos=gm;
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        setBackground(Color.WHITE);
        add(crearTitulo(),  BorderLayout.NORTH);
        add(crearTabla(),   BorderLayout.CENTER);
        add(crearBotones(), BorderLayout.SOUTH);
        actualizarTabla();
    }

    private JLabel crearTitulo() {
        JLabel t = new JLabel("Gestión de Citas Médicas");
        t.setFont(new Font("Arial",Font.BOLD,18));
        t.setForeground(AZUL);
        t.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        return t;
    }

    private JScrollPane crearTabla() {
        String[] cols = {"ID Cita","Paciente","Médico","Especialidad","Fecha","Hora","Motivo","Estado","Costo (S/)"};
        modeloTabla = new DefaultTableModel(cols,0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        tabla = new JTable(modeloTabla);
        tabla.setFont(new Font("Arial",Font.PLAIN,13));
        tabla.setRowHeight(28);
        tabla.getTableHeader().setFont(new Font("Arial",Font.BOLD,13));
        tabla.getTableHeader().setBackground(AZUL);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.setSelectionBackground(AZUL_SUB);
        tabla.setGridColor(new Color(220,220,220));
        int[] w={90,140,140,120,90,65,130,100,80};
        for(int i=0;i<w.length;i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        return new JScrollPane(tabla);
    }

    private JPanel crearBotones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT,6,8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(1,0,0,0,Color.LIGHT_GRAY));

        JButton btnAgendar   = boton("📅 Agendar",            VERDE);
        JButton btnConfirmar = boton("✅ Confirmar",           MORADO);  // HU-15
        JButton btnCompletar = boton("✔ Completar",           AZUL);
        JButton btnCancelar  = boton("✕ Cancelar",            ROJO);
        JButton btnPaciente  = boton("👤 Citas del Paciente", NARANJA); // HU-14
        JButton btnMedico    = boton("🩺 Citas por Fecha",    new Color(0,128,128)); // HU-13

        btnAgendar.addActionListener(e   -> agendarCita());
        btnConfirmar.addActionListener(e -> confirmarCita());   // HU-15
        btnCompletar.addActionListener(e -> completarCita());
        btnCancelar.addActionListener(e  -> cancelarCita());
        btnPaciente.addActionListener(e  -> citasPaciente());   // HU-14
        btnMedico.addActionListener(e    -> citasMedicoPorFecha()); // HU-13

        p.add(btnAgendar); p.add(btnConfirmar); p.add(btnCompletar);
        p.add(btnCancelar); p.add(btnPaciente); p.add(btnMedico);
        return p;
    }

    // ── HU-05: Agendar cita ───────────────────────────────────────
    private void agendarCita() {
        if (gestorPacientes.getTodos().isEmpty() || gestorMedicos.getTodos().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Necesitas registrar al menos un paciente y un médico.",
                "Datos insuficientes", JOptionPane.WARNING_MESSAGE); return;
        }
        JDialog dlg = new JDialog((Frame)SwingUtilities.getWindowAncestor(this),
                "Agendar Cita",true);
        dlg.setSize(460,350); dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20,30,10,30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets=new Insets(6,5,6,5); gbc.anchor=GridBagConstraints.WEST;

        String[] nPac = gestorPacientes.getTodos().stream()
                .map(p->p.getId()+" — "+p.getNombreCompleto()).toArray(String[]::new);
        String[] nMed = gestorMedicos.getTodos().stream()
                .map(m->m.getId()+" — Dr. "+m.getNombreCompleto()+" ("+m.getEspecialidad()+")").toArray(String[]::new);

        JComboBox<String> cbPac   = new JComboBox<>(nPac);
        JComboBox<String> cbMed   = new JComboBox<>(nMed);
        JTextField campoFecha     = new JTextField("DD/MM/YYYY",15);
        JTextField campoHora      = new JTextField("HH:MM",10);
        JTextField campoMotivo    = new JTextField(20);

        Object[][] fs = {{"Paciente:",cbPac},{"Médico:",cbMed},
                         {"Fecha:",campoFecha},{"Hora:",campoHora},{"Motivo:",campoMotivo}};
        for(int i=0;i<fs.length;i++){
            gbc.gridx=0;gbc.gridy=i;gbc.weightx=0;gbc.fill=GridBagConstraints.NONE;
            JLabel l=new JLabel((String)fs[i][0]);l.setFont(new Font("Arial",Font.PLAIN,13));
            form.add(l,gbc);
            gbc.gridx=1;gbc.weightx=1;gbc.fill=GridBagConstraints.HORIZONTAL;
            form.add((Component)fs[i][1],gbc);
        }
        dlg.add(new JScrollPane(form),BorderLayout.CENTER);

        JPanel bts = new JPanel(new FlowLayout(FlowLayout.CENTER,10,10));
        bts.setBackground(Color.WHITE);
        JButton bg=boton("📅 Agendar",VERDE); JButton bc=boton("Cancelar",Color.GRAY);
        bg.addActionListener(e->{
            if(campoFecha.getText().trim().isEmpty()||campoHora.getText().trim().isEmpty()||campoMotivo.getText().trim().isEmpty()){
                JOptionPane.showMessageDialog(dlg,"Todos los campos son obligatorios.",
                        "Campos vacíos",JOptionPane.WARNING_MESSAGE); return;
            }
            String idP=((String)cbPac.getSelectedItem()).split(" — ")[0].trim();
            String idM=((String)cbMed.getSelectedItem()).split(" — ")[0].trim();
            Cita c=gestorCitas.agendarCita(gestorPacientes.buscarPorId(idP),
                    gestorMedicos.buscarPorId(idM),
                    campoFecha.getText().trim(),campoHora.getText().trim(),
                    campoMotivo.getText().trim());
            if(c!=null){
                JOptionPane.showMessageDialog(dlg,"✔ Cita agendada: "+c.getIdCita(),
                        "Cita registrada",JOptionPane.INFORMATION_MESSAGE);
                actualizarTabla(); dlg.dispose();
            } else {
                JOptionPane.showMessageDialog(dlg,"⚠ El médico ya tiene cita en ese horario.",
                        "Conflicto de horario",JOptionPane.ERROR_MESSAGE);
            }
        });
        bc.addActionListener(e->dlg.dispose());
        bts.add(bg); bts.add(bc);
        dlg.add(bts,BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ── HU-15: Confirmar cita pendiente ──────────────────────────
    private void confirmarCita() {
        int fila = tabla.getSelectedRow();
        if (fila<0){sinSel();return;}
        Cita c = gestorCitas.buscarPorId((String)modeloTabla.getValueAt(fila,0));
        if(c==null) return;
        if(c.getEstado()!=EstadoCita.PENDIENTE){
            JOptionPane.showMessageDialog(this,
                "Solo se pueden confirmar citas en estado PENDIENTE.\nEstado actual: "+c.getEstado(),
                "Estado inválido",JOptionPane.WARNING_MESSAGE); return;
        }
        boolean ok = gestorCitas.confirmarCita(c.getIdCita());
        if (ok) {
            actualizarTabla();
            JOptionPane.showMessageDialog(this,
                "✔ Cita "+c.getIdCita()+" confirmada para "+c.getPaciente().getNombreCompleto(),
                "Cita confirmada",JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "⚠ No se pudo confirmar la cita en la base de datos.",
                "Error de actualización",JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── HU-06: Completar cita con diagnóstico ────────────────────
    private void completarCita(){
        int fila=tabla.getSelectedRow();
        if(fila<0){sinSel();return;}
        Cita c=gestorCitas.buscarPorId((String)modeloTabla.getValueAt(fila,0));
        if(c==null) return;
        if(c.getEstado()==EstadoCita.CANCELADA||c.getEstado()==EstadoCita.COMPLETADA){
            JOptionPane.showMessageDialog(this,"La cita ya está "+c.getEstado()+".",
                    "No permitido",JOptionPane.WARNING_MESSAGE); return;
        }
        String diag=JOptionPane.showInputDialog(this,
                "Diagnóstico para cita "+c.getIdCita()+":",
                "Registrar Diagnóstico",JOptionPane.QUESTION_MESSAGE);
        if(diag!=null&&!diag.trim().isEmpty()){
            boolean ok = gestorCitas.completarCita(c.getIdCita(), diag.trim());
            if (ok) {
                actualizarTabla();
                JOptionPane.showMessageDialog(this,
                        "✔ Cita completada. Diagnóstico registrado en historial del paciente.",
                        "Cita completada",JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "⚠ No se pudo completar la cita en la base de datos.",
                        "Error de actualización",JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── HU-09: Cancelar cita ─────────────────────────────────────
    private void cancelarCita(){
        int fila=tabla.getSelectedRow();
        if(fila<0){sinSel();return;}
        Cita c=gestorCitas.buscarPorId((String)modeloTabla.getValueAt(fila,0));
        if(c==null) return;
        if(c.getEstado()==EstadoCita.COMPLETADA||c.getEstado()==EstadoCita.CANCELADA){
            JOptionPane.showMessageDialog(this,"La cita no puede cancelarse (estado: "+c.getEstado()+").",
                    "No permitido",JOptionPane.WARNING_MESSAGE); return;
        }
        int conf=JOptionPane.showConfirmDialog(this,"¿Cancelar cita "+c.getIdCita()+"?",
                "Confirmar",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(conf==JOptionPane.YES_OPTION){
            boolean ok = gestorCitas.cancelarCita(c.getIdCita());
            if (ok) {
                actualizarTabla();
                JOptionPane.showMessageDialog(this,"✔ Cita cancelada.","Cancelada",JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "⚠ No se pudo cancelar la cita en la base de datos.",
                        "Error de actualización",JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── HU-14: Citas de un paciente ──────────────────────────────
    private void citasPaciente(){
        String id=JOptionPane.showInputDialog(this,"ID del paciente (ej: PAC-001):","Citas por Paciente",JOptionPane.QUESTION_MESSAGE);
        if(id==null||id.trim().isEmpty()) return;
        ArrayList<Cita> lista=gestorCitas.citasDePaciente(id.trim());
        if(lista.isEmpty()){
            JOptionPane.showMessageDialog(this,"No hay citas para el paciente: "+id,"Sin resultados",JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        modeloTabla.setRowCount(0);
        for(Cita c:lista) modeloTabla.addRow(filaCita(c));
        JOptionPane.showMessageDialog(this,"Mostrando "+lista.size()+" cita(s) del paciente "+id,
                "Filtro aplicado",JOptionPane.INFORMATION_MESSAGE);
    }

    // ── HU-13: Citas de un médico por fecha ──────────────────────
    private void citasMedicoPorFecha(){
        String idMed=JOptionPane.showInputDialog(this,"ID del médico (ej: MED-001):","Citas por Médico/Fecha",JOptionPane.QUESTION_MESSAGE);
        if(idMed==null||idMed.trim().isEmpty()) return;
        String fecha=JOptionPane.showInputDialog(this,"Fecha (DD/MM/YYYY):","Citas por Médico/Fecha",JOptionPane.QUESTION_MESSAGE);
        if(fecha==null||fecha.trim().isEmpty()) return;

        ArrayList<Cita> lista=gestorCitas.citasDeMediaco(idMed.trim(),fecha.trim());
        if(lista.isEmpty()){
            JOptionPane.showMessageDialog(this,
                    "No hay citas para el médico "+idMed+" en la fecha "+fecha,
                    "Sin resultados",JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        modeloTabla.setRowCount(0);
        for(Cita c:lista) modeloTabla.addRow(filaCita(c));
        JOptionPane.showMessageDialog(this,"Mostrando "+lista.size()+" cita(s) del médico "+idMed+" el "+fecha,
                "Filtro aplicado",JOptionPane.INFORMATION_MESSAGE);
    }

    public void actualizarTabla(){
        modeloTabla.setRowCount(0);
        for(Cita c:gestorCitas.getTodas())
            modeloTabla.addRow(filaCita(c));
    }

    private Object[] filaCita(Cita c){
        return new Object[]{c.getIdCita(),c.getPaciente().getNombreCompleto(),
                "Dr. "+c.getMedico().getNombreCompleto(),
                c.getMedico().getEspecialidad().getNombre(),
                c.getFecha(),c.getHora(),c.getMotivo(),
                c.getEstado().toString(),String.format("%.2f",c.getCosto())};
    }

    private void sinSel(){JOptionPane.showMessageDialog(this,"Selecciona una cita de la tabla.","Sin selección",JOptionPane.WARNING_MESSAGE);}
    private JButton boton(String t,Color c){
        JButton b=new JButton(t);
        b.setFont(new Font("Arial",Font.BOLD,12));
        b.setBackground(c);b.setForeground(Color.WHITE);
        b.setFocusPainted(false);b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(7,12,7,12));
        return b;
    }
}
