package com.transporte.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.transporte.controller.UsuarioAdminController;
import com.transporte.model.ReglaNegocioException;
import com.transporte.model.Usuario;

/*  el administrador registra estudiantes, empleados, conductores y otros administradores. Los usuarios creados aquí pueden iniciar sesión después*/
public class RegistroUsuarioAdminVentana extends JFrame {

    private static final String[] COLUMNAS = {"Nombre", "Apellido", "Cédula", "Correo", "Rol"};

    private final UsuarioAdminController controller;
    private final NavegacionAdmin navegacion;

    private final JTextField txtNombre = new JTextField();
    private final JTextField txtApellido = new JTextField();
    private final JTextField txtCedula = new JTextField();
    private final JTextField txtCorreo = new JTextField();
    private final JPasswordField txtClave = new JPasswordField();
    private final JComboBox<String> cmbRol = new JComboBox<>();
    private final DefaultTableModel modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modeloTabla);

    public RegistroUsuarioAdminVentana(UsuarioAdminController controller, NavegacionAdmin navegacion) {
        super("Ruta Central - Registro de usuarios");
        this.controller = controller;
        this.navegacion = navegacion;

        controller.rolesDisponibles().forEach(cmbRol::addItem);
        for (JTextField c : new JTextField[]{txtNombre, txtApellido, txtCedula, txtCorreo, txtClave}) {
            EstiloUI.estilizarCampo(c);
            c.addActionListener(e -> registrar());   // ENTER registra
        }
        EstiloUI.estilizarCombo(cmbRol);
        configurarTabla();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);   // cerrar esta ventana no cierra la aplicación
        setSize(1000, 700);
        setMinimumSize(new Dimension(900, 640));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(EstiloUI.encabezado(this::cerrarSesion), BorderLayout.NORTH);
        add(construirCentro(), BorderLayout.CENTER);

        recargarTabla();
    }

    private JPanel construirCentro() {
        JButton volver = EstiloUI.botonAzul("Volver a itinerarios");
        volver.addActionListener(e -> navegacion.irAItinerarios());
        JPanel barraVolver = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        barraVolver.setOpaque(false);
        barraVolver.add(volver);

        JLabel titulo = new JLabel( "Registro de usuarios", SwingConstants.CENTER);
        titulo.setFont(EstiloUI.FUENTE_TITULO);
        titulo.setForeground(EstiloUI.AZUL);
        JLabel sub = new JLabel("Cree cuentas de estudiante, empleado, conductor o administrador", SwingConstants.CENTER);
        sub.setForeground(EstiloUI.AZUL);
        JPanel textoCabecera = new JPanel(new GridLayout(2, 1));
        textoCabecera.setOpaque(false);
        textoCabecera.add(titulo);
        textoCabecera.add(sub);

        JPanel formulario = new JPanel(new GridLayout(4, 3, 12, 4));
        formulario.setOpaque(false);
        formulario.add(EstiloUI.etiqueta("Nombre:"));
        formulario.add(EstiloUI.etiqueta("Apellido:"));
        formulario.add(EstiloUI.etiqueta("Cédula:"));
        formulario.add(txtNombre);
        formulario.add(txtApellido);
        formulario.add(txtCedula);
        formulario.add(EstiloUI.etiqueta("Correo:"));
        formulario.add(EstiloUI.etiqueta("Contraseña:"));
        formulario.add(EstiloUI.etiqueta("Rol:"));
        formulario.add(txtCorreo);
        formulario.add(txtClave);
        formulario.add(cmbRol);

        JPanel norte = new JPanel(new BorderLayout(0, 10));
        norte.setOpaque(false);
        JPanel arriba = new JPanel(new BorderLayout(0, 8));
        arriba.setOpaque(false);
        arriba.add(barraVolver, BorderLayout.NORTH);
        arriba.add(textoCabecera, BorderLayout.CENTER);
        norte.add(arriba, BorderLayout.NORTH);
        norte.add(formulario, BorderLayout.CENTER);

        EstiloUI.BotonRedondeado limpiar = EstiloUI.botonAmarillo("Limpiar");
        EstiloUI.BotonRedondeado registrar = EstiloUI.botonVerde("Registrar usuario");
        limpiar.addActionListener(e -> limpiarCampos());
        registrar.addActionListener(e -> registrar());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 4));
        botones.setOpaque(false);
        botones.add(limpiar);
        botones.add(registrar);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(EstiloUI.GRIS);
        scroll.setBorder(BorderFactory.createLineBorder(EstiloUI.GRIS, 2));

        JPanel centro = new JPanel(new BorderLayout(0, 6));
        centro.setOpaque(false);
        centro.add(botones, BorderLayout.NORTH);
        centro.add(scroll, BorderLayout.CENTER);

        JPanel tarjeta = new JPanel(new BorderLayout(0, 12));
        tarjeta.setBackground(EstiloUI.BLANCO);
        tarjeta.setBorder(new EmptyBorder(20, 24, 20, 24));
        tarjeta.add(norte, BorderLayout.NORTH);
        tarjeta.add(centro, BorderLayout.CENTER);

        JPanel fondo = new JPanel(new BorderLayout());
        fondo.setBackground(EstiloUI.GRIS);
        fondo.setBorder(new EmptyBorder(20, 30, 20, 30));
        fondo.add(tarjeta, BorderLayout.CENTER);
        return fondo;
    }

    private void configurarTabla() {
        tabla.setRowHeight(28);
        tabla.setFont(EstiloUI.FUENTE);
        tabla.setBackground(EstiloUI.GRIS);
        tabla.setGridColor(EstiloUI.BLANCO);
        tabla.setSelectionBackground(EstiloUI.AZUL);
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer cab = new DefaultTableCellRenderer();
        cab.setBackground(EstiloUI.AZUL);
        cab.setForeground(Color.WHITE);
        cab.setHorizontalAlignment(SwingConstants.CENTER);
        cab.setFont(EstiloUI.FUENTE.deriveFont(Font.BOLD));
        tabla.getTableHeader().setDefaultRenderer(cab);
        tabla.getTableHeader().setReorderingAllowed(false);
    }

    private void registrar() {
        try {
            controller.registrar(txtNombre.getText(), txtApellido.getText(), txtCedula.getText(),
                    txtCorreo.getText(), new String(txtClave.getPassword()), (String) cmbRol.getSelectedItem());
            recargarTabla();
            limpiarCampos();
            JOptionPane.showMessageDialog(this, "Usuario registrado exitosamente.", "Mensaje",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (ReglaNegocioException ex) {
            // los datos escritos se conservan para poder corregirlos
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Verifique los datos", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void recargarTabla() {
        modeloTabla.setRowCount(0);
        for (Usuario u : controller.listar()) {
            modeloTabla.addRow(new Object[]{u.getNombre(), u.getApellido(), u.getCedula(), u.getCorreo(), u.getRol()});
        }
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtApellido.setText("");
        txtCedula.setText("");
        txtCorreo.setText("");
        txtClave.setText("");
        cmbRol.setSelectedIndex(0);
        txtNombre.requestFocusInWindow();
    }

    private void cerrarSesion() {
        int r = JOptionPane.showConfirmDialog(this, "¿Desea cerrar sesión?", "Cerrar sesión",
                JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) navegacion.cerrarSesion();
    }
}
