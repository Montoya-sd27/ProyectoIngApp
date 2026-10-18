package com.transporte.view;

import com.transporte.controller.RegistroPublicoController;
import com.transporte.controller.ValidadorUsuario;
import com.transporte.model.ReglaNegocioException;
import com.transporte.model.Usuario;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class VentanaRegistro extends JDialog {

    private static final String LETRAS = "[\\p{L}\\p{M} ]*";

    private final RegistroPublicoController controller;

    private final JTextField txtNombre = new JTextField();
    private final JTextField txtApellido = new JTextField();
    private final JTextField txtCedula = new JTextField();
    private final JTextField txtCorreo = new JTextField();
    private final JPasswordField txtClave = new JPasswordField();
    private final JPasswordField txtConfirmar = new JPasswordField();
    private final JComboBox<String> cmbRol = new JComboBox<>();

    private String correoRegistrado;

    public VentanaRegistro(Frame owner, RegistroPublicoController controller) {
        super(owner, "Ruta Central - Crear cuenta", true);
        this.controller = controller;

        controller.rolesDisponibles().forEach(cmbRol::addItem);
        for (JTextField c : new JTextField[]{txtNombre, txtApellido, txtCedula, txtCorreo, txtClave, txtConfirmar}) {
            EstiloUI.estilizarCampo(c);
            c.addActionListener(e -> registrar());   // ENTER registra
        }
        EstiloUI.estilizarCombo(cmbRol);
        EstiloUI.limitarEntrada(txtNombre, ValidadorUsuario.NOMBRE_MAX, LETRAS);
        EstiloUI.limitarEntrada(txtApellido, ValidadorUsuario.NOMBRE_MAX, LETRAS);
        EstiloUI.limitarEntrada(txtCedula, ValidadorUsuario.CEDULA_MAX_DIGITOS, "\\d*");
        EstiloUI.limitarEntrada(txtCorreo, ValidadorUsuario.CORREO_MAX, "\\S*");

        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(EstiloUI.AZUL);
        fondo.add(construirTarjeta());
        setContentPane(fondo);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    public String getCorreoRegistrado() { return correoRegistrado; }

    private JPanel construirTarjeta() {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(EstiloUI.GRIS);
        tarjeta.setBorder(new EmptyBorder(24, 32, 24, 32));

        JLabel titulo = new JLabel("Crear cuenta", SwingConstants.CENTER);
        titulo.setFont(EstiloUI.FUENTE_TITULO);
        titulo.setForeground(EstiloUI.AZUL);
        titulo.setAlignmentX(CENTER_ALIGNMENT);
        JLabel sub = new JLabel("Llene todos los campos para registrarse", SwingConstants.CENTER);
        sub.setForeground(EstiloUI.AZUL);
        sub.setAlignmentX(CENTER_ALIGNMENT);
        tarjeta.add(titulo);
        tarjeta.add(sub);
        tarjeta.add(Box.createVerticalStrut(14));

        agregarCampo(tarjeta, "Nombre", txtNombre);
        agregarCampo(tarjeta, "Apellido", txtApellido);
        agregarCampo(tarjeta, "Cédula", txtCedula);
        agregarCampo(tarjeta, "Correo electrónico", txtCorreo);
        agregarCampo(tarjeta, "Contraseña", txtClave);
        agregarCampo(tarjeta, "Confirmar contraseña", txtConfirmar);
        agregarCampo(tarjeta, "Rol", cmbRol);

        tarjeta.add(Box.createVerticalStrut(10));
        EstiloUI.BotonRedondeado registrar = EstiloUI.botonAmarillo("Registrarse");
        EstiloUI.BotonRedondeado volver = EstiloUI.botonAzul("Volver al inicio de sesión");
        registrar.addActionListener(e -> registrar());
        volver.addActionListener(e -> dispose());
        registrar.setAlignmentX(CENTER_ALIGNMENT);
        volver.setAlignmentX(CENTER_ALIGNMENT);
        tarjeta.add(registrar);
        tarjeta.add(Box.createVerticalStrut(8));
        tarjeta.add(volver);
        return tarjeta;
    }

private void agregarCampo(JPanel panel, String texto, JComponent campo) {
    // Panel contenedor individual para cada campo
    JPanel bloque = new JPanel();
    bloque.setLayout(new BoxLayout(bloque, BoxLayout.Y_AXIS));
    bloque.setOpaque(false);
    bloque.setAlignmentX(CENTER_ALIGNMENT); // Centra la caja dentro de la tarjeta

    JLabel l = EstiloUI.etiqueta(texto);
    l.setAlignmentX(LEFT_ALIGNMENT); // Pega la etiqueta a la izquierda del campo
    campo.setAlignmentX(LEFT_ALIGNMENT);

    // Definimos el tamaño fijo uniforme (320px de ancho)
    Dimension tamanoFijo = new Dimension(320, 34);
    campo.setPreferredSize(tamanoFijo);
    campo.setMaximumSize(tamanoFijo);

    bloque.add(l);
    bloque.add(Box.createVerticalStrut(3));
    bloque.add(campo);
    bloque.add(Box.createVerticalStrut(8));

    panel.add(bloque);
}

    private void registrar() {
        try {
            Usuario nuevo = controller.registrar(txtNombre.getText(), txtApellido.getText(), txtCedula.getText(),
                    txtCorreo.getText(), new String(txtClave.getPassword()), new String(txtConfirmar.getPassword()),
                    (String) cmbRol.getSelectedItem());
            correoRegistrado = nuevo.getCorreo();
            JOptionPane.showMessageDialog(this, "Cuenta creada exitosamente. Ya puede iniciar sesión.",
                    "Mensaje", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (ReglaNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Verifique los datos", JOptionPane.WARNING_MESSAGE);
        }
    }
}