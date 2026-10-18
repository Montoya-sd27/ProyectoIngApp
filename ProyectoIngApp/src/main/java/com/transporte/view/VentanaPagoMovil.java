package com.transporte.view;

import com.transporte.controller.RecargaController;
import com.transporte.model.DatosPagoMovil;
import com.transporte.model.UsuarioConSaldo;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Muestra los datos a los que se debe hacer el pago móvil y el botón para reportar el pago. */
public class VentanaPagoMovil extends JDialog {

    private final UsuarioConSaldo usuario;
    private final RecargaController recarga;
    private final Runnable alAcreditar;

    /**
     * @param alAcreditar se ejecuta cuando el pago se valida y el saldo ya fue actualizado
     */
    public VentanaPagoMovil(Window owner, UsuarioConSaldo usuario, RecargaController recarga, Runnable alAcreditar) {
        super(owner, "Recargar saldo - Pago móvil", Dialog.ModalityType.APPLICATION_MODAL);
        this.usuario = usuario;
        this.recarga = recarga;
        this.alAcreditar = alAcreditar;
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(EstiloUI.GRIS);
        fondo.add(construirTarjeta());
        setContentPane(fondo);
        pack();
        setMinimumSize(new Dimension(400, 0));
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private JPanel construirTarjeta() {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(EstiloUI.BLANCO);
        tarjeta.setBorder(new EmptyBorder(24, 32, 24, 32));

        JLabel titulo = new JLabel("Pago móvil");
        titulo.setFont(EstiloUI.FUENTE_TITULO);
        titulo.setForeground(EstiloUI.AZUL);
        titulo.setAlignmentX(CENTER_ALIGNMENT);
        JLabel sub = new JLabel("realice su pago a los siguientes datos");
        sub.setForeground(EstiloUI.AZUL);
        sub.setAlignmentX(CENTER_ALIGNMENT);
        tarjeta.add(titulo);
        tarjeta.add(sub);
        tarjeta.add(Box.createVerticalStrut(16));

        JPanel datos = new JPanel(new GridBagLayout());
        datos.setBackground(EstiloUI.GRIS);
        datos.setBorder(new EmptyBorder(14, 18, 14, 18));
        agregarDato(datos, 0, "RIF:", DatosPagoMovil.RIF);
        agregarDato(datos, 1, "Teléfono:", DatosPagoMovil.TELEFONO);
        agregarDato(datos, 2, "Banco:", DatosPagoMovil.BANCO);
        datos.setAlignmentX(CENTER_ALIGNMENT);
        tarjeta.add(datos);
        tarjeta.add(Box.createVerticalStrut(20));

        EstiloUI.BotonRedondeado reportar = EstiloUI.botonAmarillo("Reportar pago");
        reportar.setAlignmentX(CENTER_ALIGNMENT);
        reportar.addActionListener(e -> abrirFormulario());
        tarjeta.add(reportar);
        return tarjeta;
    }

    private void agregarDato(JPanel panel, int fila, String nombre, String valor) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = fila;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(3, 0, 3, 12);

        JLabel etiqueta = new JLabel(nombre);
        etiqueta.setFont(EstiloUI.FUENTE.deriveFont(Font.BOLD));
        etiqueta.setForeground(EstiloUI.AZUL);
        g.gridx = 0;
        panel.add(etiqueta, g);

        JLabel dato = new JLabel(valor);
        dato.setFont(EstiloUI.FUENTE);
        g.gridx = 1;
        g.insets = new Insets(3, 0, 3, 0);
        panel.add(dato, g);
    }

    // al validarse el pago se cierran ambas ventanas; el saldo se refresca en la pantalla de inicio
    private void abrirFormulario() {
        new VentanaReportarPago(this, usuario, recarga, () -> {
            alAcreditar.run();
            dispose();
        }).setVisible(true);
    }
}
