import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelMedicos extends JPanel {

    private final GestorMedicos gestor;
    private JTable              tabla;
    private DefaultTableModel   modelo;

    private static final Color AZUL      = new Color(31,78,121);
    private static final Color VERDE     = new Color(40,167,69);
    private static final Color NARANJA   = new Color(255,140,0);
    private static final Color MORADO    = new Color(128,0,128);
    private static final Color AZUL_SUB  = new Color(235,243,251);

    public PanelMedicos(GestorMedicos gestor){
        this.gestor=gestor;
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        setBackground(Color.WHITE);
        add(crearTitulo(),  BorderLayout.NORTH);
        add(crearTabla(),   BorderLayout.CENTER);
        add(crearBotones(), BorderLayout.SOUTH);
        actualizarTabla();
    }

    private JLabel crearTitulo(){
        JLabel t=new JLabel("Gestión de Médicos");
        t.setFont(new Font("Arial",Font.BOLD,18));
        t.setForeground(AZUL);
        t.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        return t;
    }

    private JScrollPane crearTabla(){
        String[] cols={"ID","Nombre","DNI","Colegiatura","Especialidad","Tarifa (S/)","Teléfono","Disponible"};
        modelo=new DefaultTableModel(cols,0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        tabla=new JTable(modelo);
        tabla.setFont(new Font("Arial",Font.PLAIN,13));
        tabla.setRowHeight(28);
        tabla.getTableHeader().setFont(new Font("Arial",Font.BOLD,13));
        tabla.getTableHeader().setBackground(AZUL);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.setSelectionBackground(AZUL_SUB);
        tabla.setGridColor(new Color(220,220,220));
        int[] w={80,160,100,110,150,90,110,80};
        for(int i=0;i<w.length;i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        return new JScrollPane(tabla);
    }

    private JPanel crearBotones(){
        JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,8,8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(1,0,0,0,Color.LIGHT_GRAY));

        JButton btnReg   = boton("➕ Registrar Médico",      VERDE);
        JButton btnTarifa= boton("💲 Actualizar Tarifa",     NARANJA); // HU-16
        JButton btnDisp  = boton("🔄 Disponibilidad",        MORADO);
        JButton btnDet   = boton("🔍 Ver Detalle",           AZUL);

        btnReg.addActionListener(e    -> formularioRegistro());
        btnTarifa.addActionListener(e -> actualizarTarifa());   // HU-16
        btnDisp.addActionListener(e   -> cambiarDisponibilidad());
        btnDet.addActionListener(e    -> verDetalle());

        p.add(btnReg);p.add(btnTarifa);p.add(btnDisp);p.add(btnDet);
        return p;
    }

    // ── HU-04: Registrar médico ───────────────────────────────────
    private void formularioRegistro(){
        JDialog dlg=new JDialog((Frame)SwingUtilities.getWindowAncestor(this),"Registrar Médico",true);
        dlg.setSize(480,460); dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form=new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20,30,10,30));
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.insets=new Insets(5,5,5,5); gbc.anchor=GridBagConstraints.WEST;

        String[] et={"Nombre:","Apellido:","DNI:","Teléfono:","Correo:","Colegiatura:","Tarifa (S/):"};
        JTextField[] campos=new JTextField[et.length];
        for(int i=0;i<et.length;i++){
            gbc.gridx=0;gbc.gridy=i;gbc.weightx=0;gbc.fill=GridBagConstraints.NONE;
            JLabel l=new JLabel(et[i]);l.setFont(new Font("Arial",Font.PLAIN,13));
            form.add(l,gbc);
            gbc.gridx=1;gbc.weightx=1;gbc.fill=GridBagConstraints.HORIZONTAL;
            campos[i]=new JTextField(20);campos[i].setFont(new Font("Arial",Font.PLAIN,13));
            form.add(campos[i],gbc);
        }
        gbc.gridx=0;gbc.gridy=et.length;gbc.weightx=0;gbc.fill=GridBagConstraints.NONE;
        form.add(new JLabel("Especialidad:"),gbc);
        gbc.gridx=1;gbc.weightx=1;gbc.fill=GridBagConstraints.HORIZONTAL;
        JComboBox<Especialidad> cbEsp=new JComboBox<>(Especialidad.values());
        cbEsp.setFont(new Font("Arial",Font.PLAIN,13));
        form.add(cbEsp,gbc);

        dlg.add(new JScrollPane(form),BorderLayout.CENTER);

        JPanel bts=new JPanel(new FlowLayout(FlowLayout.CENTER,10,10));
        bts.setBackground(Color.WHITE);
        JButton bg=boton("💾 Guardar",VERDE); JButton bc=boton("Cancelar",Color.GRAY);
        bg.addActionListener(e->{
            try{
                for(int i=0;i<campos.length;i++)
                    if(campos[i].getText().trim().isEmpty()){
                        JOptionPane.showMessageDialog(dlg,"Campo obligatorio: "+et[i].replace(":",""),
                                "Campo vacío",JOptionPane.WARNING_MESSAGE); return;
                    }
                double tarifa=Double.parseDouble(campos[6].getText().trim());
                Medico m=gestor.registrarMedico(
                        campos[0].getText().trim(),campos[1].getText().trim(),
                        campos[2].getText().trim(),campos[3].getText().trim(),
                        campos[4].getText().trim(),campos[5].getText().trim(),
                        (Especialidad)cbEsp.getSelectedItem(),tarifa);
                if(m!=null){
                    JOptionPane.showMessageDialog(dlg,"✔ Médico registrado: "+m.getId(),
                            "Registro exitoso",JOptionPane.INFORMATION_MESSAGE);
                    actualizarTabla(); dlg.dispose();
                } else {
                    JOptionPane.showMessageDialog(dlg,"⚠ Colegiatura ya registrada.",
                            "Colegiatura duplicada",JOptionPane.ERROR_MESSAGE);
                }
            } catch(NumberFormatException ex){
                JOptionPane.showMessageDialog(dlg,"La tarifa debe ser un número (ej: 80.50)",
                        "Formato incorrecto",JOptionPane.ERROR_MESSAGE);
            }
        });
        bc.addActionListener(e->dlg.dispose());
        bts.add(bg);bts.add(bc);
        dlg.add(bts,BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ── HU-16: Actualizar tarifa de consulta ─────────────────────
    private void actualizarTarifa(){
        int fila=tabla.getSelectedRow();
        if(fila<0){sinSel();return;}
        String id=(String)modelo.getValueAt(fila,0);
        Medico m=gestor.buscarPorId(id);
        if(m==null) return;

        String input=JOptionPane.showInputDialog(this,
            "Tarifa actual de Dr. "+m.getNombreCompleto()+": S/ "+
            String.format("%.2f",m.getTarifaConsulta())+
            "\n\nIngresa la nueva tarifa (S/):",
            "Actualizar Tarifa",JOptionPane.QUESTION_MESSAGE);

        if(input!=null&&!input.trim().isEmpty()){
            try{
                double nueva=Double.parseDouble(input.trim());
                if(nueva<=0){
                    JOptionPane.showMessageDialog(this,"La tarifa debe ser mayor a 0.",
                            "Valor inválido",JOptionPane.WARNING_MESSAGE); return;
                }
                boolean ok = gestor.actualizarTarifa(id, nueva);
                if (ok) {
                    actualizarTabla();
                    JOptionPane.showMessageDialog(this,
                        "✔ Tarifa actualizada a S/ "+String.format("%.2f",nueva)+
                        " para Dr. "+m.getNombreCompleto(),
                        "Tarifa actualizada",JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "⚠ No se pudo actualizar la tarifa en la base de datos.",
                        "Error de actualización",JOptionPane.ERROR_MESSAGE);
                }
            } catch(NumberFormatException ex){
                JOptionPane.showMessageDialog(this,"Ingresa un número válido (ej: 120.00)",
                        "Formato incorrecto",JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── HU-17: Cambiar disponibilidad ────────────────────────────
    private void cambiarDisponibilidad(){
        int fila=tabla.getSelectedRow();
        if(fila<0){sinSel();return;}
        Medico m=gestor.buscarPorId((String)modelo.getValueAt(fila,0));
        if(m==null) return;
        boolean nuevaDisponibilidad = !m.isDisponible();
        boolean ok = gestor.actualizarDisponibilidad(m.getId(), nuevaDisponibilidad);
        if (ok) {
            actualizarTabla();
            JOptionPane.showMessageDialog(this,
                "✔ Dr. "+m.getNombreCompleto()+" → "+(nuevaDisponibilidad?"Disponible":"No disponible"),
                "Disponibilidad actualizada",JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "⚠ No se pudo actualizar la disponibilidad en la base de datos.",
                "Error de actualización",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void verDetalle(){
        int fila=tabla.getSelectedRow();
        if(fila<0){sinSel();return;}
        Medico m=gestor.buscarPorId((String)modelo.getValueAt(fila,0));
        if(m==null) return;
        String info="ID           : "+m.getId()+"\n"
                   +"Nombre       : Dr(a). "+m.getNombreCompleto()+"\n"
                   +"DNI          : "+m.getDni()+"\n"
                   +"Colegiatura  : "+m.getColegiatura()+"\n"
                   +"Especialidad : "+m.getEspecialidad()+"\n"
                   +"Teléfono     : "+m.getTelefono()+"\n"
                   +"Correo       : "+m.getCorreo()+"\n"
                   +String.format("Tarifa       : S/ %.2f%n",m.getTarifaConsulta())
                   +"Disponible   : "+(m.isDisponible()?"Sí":"No");
        JTextArea area=new JTextArea(info);
        area.setEditable(false); area.setFont(new Font("Monospaced",Font.PLAIN,13));
        area.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JOptionPane.showMessageDialog(this,area,"Detalle — Dr. "+m.getNombreCompleto(),JOptionPane.INFORMATION_MESSAGE);
    }

    public void actualizarTabla(){
        modelo.setRowCount(0);
        for(Medico m:gestor.getTodos())
            modelo.addRow(new Object[]{m.getId(),"Dr(a). "+m.getNombreCompleto(),
                m.getDni(),m.getColegiatura(),m.getEspecialidad().getNombre(),
                String.format("%.2f",m.getTarifaConsulta()),
                m.getTelefono(),m.isDisponible()?"Sí":"No"});
    }

    private void sinSel(){JOptionPane.showMessageDialog(this,"Selecciona un médico de la tabla.","Sin selección",JOptionPane.WARNING_MESSAGE);}
    private JButton boton(String t,Color c){
        JButton b=new JButton(t);
        b.setFont(new Font("Arial",Font.BOLD,13));
        b.setBackground(c);b.setForeground(Color.WHITE);
        b.setFocusPainted(false);b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
        return b;
    }
}
