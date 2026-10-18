package com.transporte.view;

import com.transporte.controller.CobroService;
import com.transporte.model.ReglaNegocioException;
import com.transporte.model.TarifaRuta;
import com.transporte.model.Ticket;
import com.transporte.model.UsuarioConSaldo;
import java.awt.*;
import java.util.Locale;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Ventana que se abre al pulsar "Reservar cupo": muestra el precio total del viaje para el rol del
 * usuario y, solo cuando pulsa "Pagar cupo", se cobra el saldo y se emite el ticket digital.
 */
public class DialogoPagoCupo extends JDialog {

    private static final Locale ES_VE = new Locale("es", "VE");
    private static final Color ROJO_TEXTO = new Color(0xE02020);

    private final UsuarioConSaldo usuario;
    private final String descripcionRuta;
    private final TarifaRuta tarifa;
    private final CobroService cobro;
    private final Consumer<Ticket> alPagar;

    /**
     * @param titulo      nombre de la ruta que se muestra arriba (por ejemplo "RU-01 Plaza Venezuela")
     * @param origen      punto de salida
     * @param horario     horario ya formateado (por ejemplo "Lun, 6:00 am - 7:00 am")
     * @param tarifa      tarifa vigente de la ruta (base y porcentajes por rol)
     * @param alPagar     se ejecuta con el ticket emitido, una vez cobrado el cupo
     */
    public DialogoPagoCupo(Window padre, UsuarioConSaldo usuario, String titulo, String origen, String horario,
                           String descripcionRuta, TarifaRuta tarifa, CobroService cobro, Consumer<Ticket> alPagar) {
        super(padre, "Pagar cupo", ModalityType.APPLICATION_MODAL);
        this.usuario = usuario;
        this.descripcionRuta = descripcionRuta;
        this.tarifa = tarifa;
        this.cobro = cobro;
        this.alPagar = alPagar;

        double precio = cobro.calcularTarifa(usuario.getRol(), tarifa);
        double pct = tarifa.porcentajePara(usuario.getRol());
        boolean alcanza = usuario.getSaldo() >= precio;

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBackground(EstiloUI.BLANCO);
        contenido.setBorder(new EmptyBorder(18, 22, 16, 22));

        JLabel lblTitulo = new JLabel("Resumen del viaje");
        lblTitulo.setFont(EstiloUI.FUENTE_TITULO);
        lblTitulo.setForeground(EstiloUI.AZUL);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(lblTitulo);
        contenido.add(Box.createVerticalStrut(10));

        contenido.add(fila("Ruta", titulo, false));
        contenido.add(fila("Salida", origen, false));
        contenido.add(fila("Horario", horario, false));
        contenido.add(fila("Pasajero", usuario.getNombre() + " (" + usuario.getRol() + ")", false));
        contenido.add(Box.createVerticalStrut(8));
        contenido.add(fila("Tarifa base", dinero(tarifa.getBase()), false));
        contenido.add(fila("Porcentaje a pagar (" + usuario.getRol() + ")", String.format(ES_VE, "%,.1f%%", pct), false));

        // precio total, bien visible
        JLabel lblTotal = new JLabel("Total a pagar: " + dinero(precio), SwingConstants.CENTER);
        lblTotal.setFont(EstiloUI.FUENTE.deriveFont(Font.BOLD, 22f));
        lblTotal.setForeground(EstiloUI.AZUL);
        JPanel cajaTotal = new JPanel(new BorderLayout());
        cajaTotal.setBackground(EstiloUI.GRIS);
        cajaTotal.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(EstiloUI.AMARILLO, 2, true), new EmptyBorder(10, 12, 10, 12)));
        cajaTotal.add(lblTotal, BorderLayout.CENTER);
        cajaTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        cajaTotal.setMaximumSize(new Dimension(Integer.MAX_VALUE, cajaTotal.getPreferredSize().height));
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(cajaTotal);
        contenido.add(Box.createVerticalStrut(8));

        contenido.add(fila("Su saldo", dinero(usuario.getSaldo()), false));
        if (!alcanza) {
            JLabel aviso = new JLabel("<html>Saldo insuficiente. Recargue su saldo para poder pagar este cupo.</html>");
            aviso.setFont(EstiloUI.FUENTE.deriveFont(Font.BOLD, 12f));
            aviso.setForeground(ROJO_TEXTO);
            aviso.setAlignmentX(Component.LEFT_ALIGNMENT);
            contenido.add(Box.createVerticalStrut(4));
            contenido.add(aviso);
        }

        EstiloUI.BotonRedondeado btnCancelar = EstiloUI.botonAzul("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        EstiloUI.BotonRedondeado btnPagar = EstiloUI.botonAmarillo("Pagar cupo");
        btnPagar.setEnabled(alcanza);
        btnPagar.addActionListener(e -> pagar());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        botones.add(btnCancelar);
        botones.add(btnPagar);
        botones.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(Box.createVerticalStrut(16));
        contenido.add(botones);

        setContentPane(contenido);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setSize(Math.max(getWidth(), 400), getHeight());
        setLocationRelativeTo(padre);
    }

    // cobra el cupo y emite el ticket; el ticket solo existe a partir de este momento
    private void pagar() {
        try {
            Ticket ticket = cobro.pagarCupo(usuario, descripcionRuta, tarifa);
            dispose();
            alPagar.accept(ticket);
        } catch (ReglaNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Atención", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static JPanel fila(String etiqueta, String valor, boolean negrita) {
        JLabel l = new JLabel(etiqueta);
        l.setFont(EstiloUI.FUENTE.deriveFont(13f));
        l.setForeground(Color.GRAY);
        JLabel v = new JLabel(valor, SwingConstants.RIGHT);
        v.setFont(EstiloUI.FUENTE.deriveFont(negrita ? Font.BOLD : Font.PLAIN, 14f));
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(3, 0, 3, 0));
        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.CENTER);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    private static String dinero(double monto) {
        return String.format(ES_VE, "Bs. %,.2f", monto);
    }
}
