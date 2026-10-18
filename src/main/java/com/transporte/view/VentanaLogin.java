package com.transporte.view;

import com.transporte.controller.LoginController;
import com.transporte.model.Usuario;
import java.util.function.Consumer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VentanaLogin extends JFrame {

    Color colorAzulFondo = Color.decode("#003366");
    Color colorGrisFormulario = Color.decode("#E9E1E1");
    Color colorAmarilloBoton = Color.decode("#EAB915");
    Color colorTextoAzul = Color.decode("#003366");

    // componetntes login
    JTextField txtCorreoLogin;
    JPasswordField txtPassLogin;

    // Qué hacer cuando las credenciales son válidas (lo decide AppNavegacion según el rol)
    private final Consumer<Usuario> onLoginExitoso;

    public VentanaLogin() {
        this(usuario -> { });
    }

    public VentanaLogin(Consumer<Usuario> onLoginExitoso) {
        this.onLoginExitoso = onLoginExitoso;
        setTitle("Inicio Sistema");
        setSize(450, 750); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(null); 
        getContentPane().setBackground(colorAzulFondo); 

        
        JPanel panelFormulario = new JPanel();
        panelFormulario.setBackground(colorGrisFormulario);
        panelFormulario.setLayout(null);
        panelFormulario.setBounds(40, 100, 370, 450); 
        add(panelFormulario);

        
        JLabel lblTitulo = new JLabel("Ruta Central");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(colorTextoAzul);
        lblTitulo.setBounds(100, 30, 200, 40); 
        panelFormulario.add(lblTitulo);

        // login
        
        JLabel lblCorreo = new JLabel("Correo electronico");
        lblCorreo.setFont(new Font("Arial", Font.PLAIN, 14));
        lblCorreo.setForeground(Color.BLACK);
        lblCorreo.setBounds(40, 100, 200, 20);
        panelFormulario.add(lblCorreo);

        txtCorreoLogin = new JTextField();
        txtCorreoLogin.setFont(new Font("Arial", Font.PLAIN, 14));
        txtCorreoLogin.setBackground(Color.decode("#D9D9D9")); 
        txtCorreoLogin.setBorder(null); 
        txtCorreoLogin.setBounds(40, 125, 290, 35);
        panelFormulario.add(txtCorreoLogin);

        
        JLabel lblPass = new JLabel("Contraseña");
        lblPass.setFont(new Font("Arial", Font.PLAIN, 14));
        lblPass.setForeground(Color.BLACK);
        lblPass.setBounds(40, 180, 200, 20);
        panelFormulario.add(lblPass);

        txtPassLogin = new JPasswordField();
        txtPassLogin.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPassLogin.setBackground(Color.decode("#D9D9D9"));
        txtPassLogin.setBorder(null);
        txtPassLogin.setBounds(40, 205, 290, 35);
        panelFormulario.add(txtPassLogin);

        
        JButton btnIngresar = new JButton("Iniciar Sesion");
        btnIngresar.setFont(new Font("Arial", Font.BOLD, 16));
        btnIngresar.setBackground(colorAmarilloBoton);
        btnIngresar.setForeground(colorTextoAzul);
        btnIngresar.setFocusPainted(false); 
        btnIngresar.setBorder(null);
        btnIngresar.setBounds(40, 280, 290, 45);
        panelFormulario.add(btnIngresar);

        
        btnIngresar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accionIngresar();
            }
        });

    }

    // ingresar
    private void accionIngresar() {
        String correo = txtCorreoLogin.getText();
        String pass = new String(txtPassLogin.getPassword());

        LoginController controlador = new LoginController();
        Usuario usuario = controlador.iniciarSesion(correo, pass);

        if (usuario != null) {
            onLoginExitoso.accept(usuario);
        } else {
            JOptionPane.showMessageDialog(null, "Correo o contraseña incorrectos", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
