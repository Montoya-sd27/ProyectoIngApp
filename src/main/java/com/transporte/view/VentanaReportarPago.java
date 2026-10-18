package com.transporte.view;

import com.transporte.controller.RecargaController;
import com.transporte.model.DatosPagoMovil;
import com.transporte.model.ReglaNegocioException;
import com.transporte.model.UsuarioConSaldo;
import java.awt.*;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/** Formulario donde el usuario reporta el pago móvil que hizo para recargar su saldo. */
public class VentanaReportarPago extends JDialog {

    private static final Locale ES_VE = new Locale("es", "VE");

    private final UsuarioConSaldo usuario;
    private final RecargaController recarga;
    private final Runnable alAcreditar;

    private final JTextField txtTelefono = new JTextField();
    private final JTextField txtCedula = new JTextField();
    private final JComboBox<String> cmbBanco = new JComboBox<>(DatosPagoMovil.bancosEmisores());
    private final JTextField txtReferencia = new JTextField();

    /**
     * @param alAcreditar se ejecuta cuando el pago se valida y el saldo ya fue actualizado
     */
    public VentanaReportarPago(Window owner, UsuarioConSaldo usuario, RecargaController recarga, Runnable alAcreditar) {
        super(owner, "Reportar pago", Dialog.ModalityType.APPLICATION_MODAL);
        this.usuario = usuario;
        this.recarga = recarga;
        this.alAcreditar = alAcreditar;
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // en los campos numéricos solo se pueden escribir dígitos
        soloDigitos(txtTelefono, 11);
        soloDigitos(txtCedula, 9);
        soloDigitos(txtReferencia, 4);
        EstiloUI.estilizarCampo(txtTelefono);
        EstiloUI.estilizarCampo(txtCedula);
        EstiloUI.estilizarCampo(txtReferencia);
        EstiloUI.estilizarCombo(cmbBanco);

        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(EstiloUI.GRIS);
        fondo.add(construirTarjeta());
        setContentPane(fondo);
        pack();
        setMinimumSize(new Dimension(420, 0));
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private JPanel construirTarjeta() {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(EstiloUI.BLANCO);
        tarjeta.setBorder(new EmptyBorder(24, 32, 24, 32));

        JLabel titulo = new JLabel("Reportar pago");
        titulo.setFont(EstiloUI.FUENTE_TITULO);
        titulo.setForeground(EstiloUI.AZUL);
        JLabel sub = new JLabel("llene el formulario con los datos de su pago");
        sub.setForeground(EstiloUI.AZUL);
        // se estiran al ancho de la tarjeta con el texto centrado, para que no desplacen a los campos
        for (JLabel l : new JLabel[]{titulo, sub}) {
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setAlignmentX(LEFT_ALIGNMENT);
            l.setMaximumSize(new Dimension(Integer.MAX_VALUE, l.getPreferredSize().height));
        }
        tarjeta.add(titulo);
        tarjeta.add(sub);
        tarjeta.add(Box.createVerticalStrut(14));

        agregarCampo(tarjeta, "Número de teléfono", txtTelefono);
        agregarCampo(tarjeta, "Cédula", txtCedula);
        agregarCampo(tarjeta, "Banco", cmbBanco);
        agregarCampo(tarjeta, "Últimos 4 dígitos de la referencia", txtReferencia);

        tarjeta.add(Box.createVerticalStrut(10));
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        botones.setOpaque(false);
        botones.setAlignmentX(LEFT_ALIGNMENT);
        EstiloUI.BotonRedondeado regresar = EstiloUI.botonRojo("Regresar");
        EstiloUI.BotonRedondeado validar = EstiloUI.botonVerde("Validar pago");
        regresar.addActionListener(e -> dispose());
        validar.addActionListener(e -> validarPago());
        botones.add(regresar);
        botones.add(validar);
        tarjeta.add(botones);
        return tarjeta;
    }

    private void agregarCampo(JPanel panel, String texto, JComponent campo) {
        JLabel l = EstiloUI.etiqueta(texto);
        l.setAlignmentX(LEFT_ALIGNMENT);
        campo.setAlignmentX(LEFT_ALIGNMENT);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        campo.setPreferredSize(new Dimension(340, 34));
        panel.add(l);
        panel.add(Box.createVerticalStrut(3));
        panel.add(campo);
        panel.add(Box.createVerticalStrut(8));
    }

    // envía los datos al controlador; si el pago es válido se acredita el saldo
    private void validarPago() {
        try {
            float monto = recarga.reportarPago(usuario, txtTelefono.getText(), txtCedula.getText(),
                    (String) cmbBanco.getSelectedItem(), txtReferencia.getText());
            JOptionPane.showMessageDialog(this,
                    String.format(ES_VE, "Pago validado. Se acreditaron Bs. %,.2f a su saldo.", monto),
                    "Recarga exitosa", JOptionPane.INFORMATION_MESSAGE);
            alAcreditar.run();
            dispose();
        } catch (ReglaNegocioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "No se pudo validar el pago", JOptionPane.WARNING_MESSAGE);
        }
    }

    // el campo solo acepta dígitos y no pasa del máximo (también al pegar texto)
    private static void soloDigitos(JTextField campo, int maximo) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String texto, AttributeSet attr)
                    throws BadLocationException {
                if (texto != null) filtrar(fb, offset, 0, texto, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String texto, AttributeSet attrs)
                    throws BadLocationException {
                if (texto != null) filtrar(fb, offset, length, texto, attrs);
            }

            private void filtrar(FilterBypass fb, int offset, int length, String texto, AttributeSet attrs)
                    throws BadLocationException {
                String digitos = texto.replaceAll("\\D", "");
                if (digitos.isEmpty() && !texto.isEmpty()) return;   // solo había letras u otros símbolos
                int espacio = maximo - (fb.getDocument().getLength() - length);
                if (digitos.length() > espacio) digitos = digitos.substring(0, Math.max(espacio, 0));
                super.replace(fb, offset, length, digitos, attrs);
            }
        });
    }
}
