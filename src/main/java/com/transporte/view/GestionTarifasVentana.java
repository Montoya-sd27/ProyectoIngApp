package com.transporte.view;

import com.transporte.controller.TarifaConfiguracionController;
import com.transporte.model.ReglaNegocioException;
import com.transporte.model.Ruta;
import com.transporte.model.RutaFactory.TipoRuta;
import com.transporte.model.TarifaRuta;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

public class GestionTarifasVentana extends JFrame {

    private JComboBox<String> cmbTipo;
    private JComboBox<String> cmbRutas;
    private JTextField txtTarifaBase;

    // Campos de porcentaje editables por el usuario
    private JTextField txtPctEstudiante;
    private JTextField txtPctEmpleado;
    private JTextField txtPctPublico;

    // Etiquetas con el resultado calculado en Bolívares
    private JLabel lblCalcEstudiante;
    private JLabel lblCalcEmpleado;
    private JLabel lblCalcPublico;

    private final TarifaConfiguracionController controller;
    private final Runnable onVolver;
    private List<Ruta> listaRutas = new java.util.ArrayList<>();

    public GestionTarifasVentana(Runnable onVolver, TarifaConfiguracionController controller) {
        super("Ruta Central - THU-001: Configuración de Tarifas (Admin)");
        this.onVolver = onVolver;
        this.controller = controller;

        initComponents();
        cargarRutas();
    }

    private void initComponents() {
        setSize(740, 580);
        setMinimumSize(new Dimension(680, 540));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header / Volver
        JButton btnVolver = EstiloUI.botonAzul("Volver al Menú");
        btnVolver.addActionListener(e -> {
            if (onVolver != null) onVolver.run();
            dispose();
        });

        JPanel barraVolver = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        barraVolver.setOpaque(false);
        barraVolver.add(btnVolver);

        JLabel lblTitulo = new JLabel("Configuración de Tarifas de Rutas", SwingConstants.CENTER);
        lblTitulo.setFont(EstiloUI.FUENTE_TITULO);
        lblTitulo.setForeground(EstiloUI.AZUL);

        JPanel cabecera = new JPanel(new BorderLayout(0, 6));
        cabecera.setOpaque(false);
        cabecera.add(barraVolver, BorderLayout.NORTH);
        cabecera.add(lblTitulo, BorderLayout.CENTER);

        // Formulario
        cmbTipo = new JComboBox<>(new String[]{"Todas", "Urbanas", "Extraurbanas"});
        EstiloUI.estilizarCombo(cmbTipo);
        cmbTipo.addActionListener(e -> cargarRutas());

        cmbRutas = new JComboBox<>();
        EstiloUI.estilizarCombo(cmbRutas);
        cmbRutas.addActionListener(e -> alSeleccionarRuta());

        txtTarifaBase = new JTextField();
        txtTarifaBase.setFont(EstiloUI.FUENTE);

        // Instanciación de inputs de porcentaje
        txtPctEstudiante = new JTextField();
        txtPctEmpleado = new JTextField();
        txtPctPublico = new JTextField();

        // Labels con los montos en Bs.
        lblCalcEstudiante = new JLabel("Bs. 0.00");
        lblCalcEmpleado = new JLabel("Bs. 0.00");
        lblCalcPublico = new JLabel("Bs. 0.00");

        // Escuchador de eventos para actualizar montos al teclear
        DocumentListener docListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recalcularAlEscribir(); }
            @Override public void removeUpdate(DocumentEvent e) { recalcularAlEscribir(); }
            @Override public void changedUpdate(DocumentEvent e) { recalcularAlEscribir(); }
        };

        txtTarifaBase.getDocument().addDocumentListener(docListener);
        txtPctEstudiante.getDocument().addDocumentListener(docListener);
        txtPctEmpleado.getDocument().addDocumentListener(docListener);
        txtPctPublico.getDocument().addDocumentListener(docListener);

        JPanel pnlForm = new JPanel(new GridLayout(6, 2, 12, 10));
        pnlForm.setOpaque(false);

        pnlForm.add(EstiloUI.etiqueta("Tipo de ruta:"));
        pnlForm.add(cmbTipo);

        pnlForm.add(EstiloUI.etiqueta("Seleccionar Ruta:"));
        pnlForm.add(cmbRutas);

        pnlForm.add(EstiloUI.etiqueta("Nueva Tarifa Base (Bs.):"));
        pnlForm.add(txtTarifaBase);

        pnlForm.add(EstiloUI.etiqueta("Porcentaje Estudiante (%):"));
        pnlForm.add(crearSubpanelPorcentaje(txtPctEstudiante, lblCalcEstudiante));

        pnlForm.add(EstiloUI.etiqueta("Porcentaje Empleado (%):"));
        pnlForm.add(crearSubpanelPorcentaje(txtPctEmpleado, lblCalcEmpleado));

        pnlForm.add(EstiloUI.etiqueta("Porcentaje Público (%):"));
        pnlForm.add(crearSubpanelPorcentaje(txtPctPublico, lblCalcPublico));

        // Botón principal
        JButton btnGuardar = EstiloUI.botonAzul("Establecer tarifa");
        btnGuardar.addActionListener(e -> guardarTarifa());

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlBotones.setOpaque(false);
        pnlBotones.add(btnGuardar);

        // Tarjeta contenedor
        JPanel tarjeta = new JPanel(new BorderLayout(0, 14));
        tarjeta.setBackground(EstiloUI.BLANCO);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 230, 238), 1, true),
                new EmptyBorder(16, 24, 16, 24)
        ));
        tarjeta.add(cabecera, BorderLayout.NORTH);
        tarjeta.add(pnlForm, BorderLayout.CENTER);
        tarjeta.add(pnlBotones, BorderLayout.SOUTH);

        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(EstiloUI.GRIS);
        fondo.add(tarjeta);

        add(fondo, BorderLayout.CENTER);
    }

    private JPanel crearSubpanelPorcentaje(JTextField txtPct, JLabel lblCalc) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);

        txtPct.setPreferredSize(new Dimension(65, 28));
        txtPct.setFont(EstiloUI.FUENTE);

        JLabel lblFlecha = new JLabel("%  ➜ ");
        lblFlecha.setFont(EstiloUI.FUENTE);

        lblCalc.setFont(EstiloUI.FUENTE.deriveFont(Font.BOLD));
        lblCalc.setForeground(EstiloUI.AZUL);

        p.add(txtPct);
        p.add(lblFlecha);
        p.add(lblCalc);
        return p;
    }

    private TipoRuta tipoElegido() {
        switch (cmbTipo.getSelectedIndex()) {
            case 1: return TipoRuta.URBANA;
            case 2: return TipoRuta.EXTRAURBANA;
            default: return null;   // todas
        }
    }

    private void cargarRutas() {
        try {
            listaRutas = controller.obtenerRutas(tipoElegido());
        } catch (ReglaNegocioException e) {
            listaRutas = new java.util.ArrayList<>();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Atención", JOptionPane.WARNING_MESSAGE);
        }

        // se quita el oyente un momento para que no dispare la carga de campos por cada ítem agregado
        java.awt.event.ActionListener[] oyentes = cmbRutas.getActionListeners();
        for (java.awt.event.ActionListener l : oyentes) cmbRutas.removeActionListener(l);
        cmbRutas.removeAllItems();
        for (Ruta r : listaRutas) {
            cmbRutas.addItem(r.getIdRuta() + " - " + r.getOrigen() + " a " + r.getDestino()
                    + " (" + r.getTipo() + ")");
        }
        for (java.awt.event.ActionListener l : oyentes) cmbRutas.addActionListener(l);

        if (!listaRutas.isEmpty()) {
            cmbRutas.setSelectedIndex(0);
            alSeleccionarRuta();
        } else {
            limpiarCampos();
        }
    }

    // al elegir una ruta se muestra su tarifa guardada; si todavía no se configuró, los campos quedan vacíos
    private void alSeleccionarRuta() {
        int idx = cmbRutas.getSelectedIndex();
        if (idx < 0 || idx >= listaRutas.size()) return;

        limpiarCampos();
        try {
            controller.tarifaConfigurada(listaRutas.get(idx).getIdRuta()).ifPresent(t -> {
                txtTarifaBase.setText(numero(t.getBase()));
                txtPctEstudiante.setText(numero(t.getPctEstudiante()));
                txtPctEmpleado.setText(numero(t.getPctEmpleado()));
                txtPctPublico.setText(numero(t.getPctPublico()));
            });
        } catch (ReglaNegocioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Atención", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void limpiarCampos() {
        txtTarifaBase.setText("");
        txtPctEstudiante.setText("");
        txtPctEmpleado.setText("");
        txtPctPublico.setText("");
        lblCalcEstudiante.setText("Bs. 0.00");
        lblCalcEmpleado.setText("Bs. 0.00");
        lblCalcPublico.setText("Bs. 0.00");
    }

    // 50.0 -> "50"; 12.5 -> "12.5"
    private static String numero(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    // acepta coma o punto como separador decimal
    private static double leerNumero(String texto) {
        return Double.parseDouble(texto.trim().replace(',', '.'));
    }

    private void recalcularAlEscribir() {
        double base = 0.00;
        try {
            String strBase = txtTarifaBase.getText().trim();
            if (!strBase.isEmpty()) {
                base = leerNumero(strBase);
            }
        } catch (NumberFormatException ignored) {
        }

        actualizarCalculoIndividual(base, txtPctEstudiante, lblCalcEstudiante);
        actualizarCalculoIndividual(base, txtPctEmpleado, lblCalcEmpleado);
        actualizarCalculoIndividual(base, txtPctPublico, lblCalcPublico);
    }

    private void actualizarCalculoIndividual(double base, JTextField txtPct, JLabel lblCalc) {
        try {
            String strPct = txtPct.getText().trim();
            if (strPct.isEmpty()) {
                lblCalc.setText("Bs. 0.00");
                return;
            }
            double pct = leerNumero(strPct);
            if (pct < 0 || pct > 100) {
                lblCalc.setText("Inválido");
                return;
            }
            double monto = base * (pct / 100.0);
            lblCalc.setText(String.format("Bs. %.2f", monto));
        } catch (NumberFormatException ignored) {
            lblCalc.setText("Bs. 0.00");
        }
    }

    private void guardarTarifa() {
        try {
            int idx = cmbRutas.getSelectedIndex();
            if (idx < 0 || idx >= listaRutas.size()) throw new ReglaNegocioException("Debe seleccionar una ruta.");

            String idRuta = listaRutas.get(idx).getIdRuta();
            String textBase = txtTarifaBase.getText().trim();

            if (textBase.isEmpty()) {
                throw new ReglaNegocioException("Ingrese un monto para la tarifa base.");
            }

            double nuevaTarifa = leerNumero(textBase);

            // Validar porcentajes requeridos (0 a 100%)
            double pctEst = validarPorcentaje(txtPctEstudiante.getText().trim(), "Estudiante");
            double pctEmp = validarPorcentaje(txtPctEmpleado.getText().trim(), "Empleado");
            double pctPub = validarPorcentaje(txtPctPublico.getText().trim(), "Público General");

            TarifaRuta t = controller.establecerTarifa(idRuta, nuevaTarifa, pctEst, pctEmp, pctPub);

            JOptionPane.showMessageDialog(this,
                    String.format("Tarifa establecida con éxito para %s\n\n" +
                                  "Tarifa Base: Bs. %.2f\n" +
                                  "• Estudiante (%.1f%%): Bs. %.2f\n" +
                                  "• Empleado (%.1f%%): Bs. %.2f\n" +
                                  "• Público (%.1f%%): Bs. %.2f",
                            idRuta, t.getBase(),
                            t.getPctEstudiante(), t.precioPara("ESTUDIANTE"),
                            t.getPctEmpleado(), t.precioPara("EMPLEADO"),
                            t.getPctPublico(), t.precioPara("PUBLICO")),
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingrese un monto numérico válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (ReglaNegocioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Regla de Negocio", JOptionPane.WARNING_MESSAGE);
        }
    }

    private double validarPorcentaje(String texto, String perfil) throws ReglaNegocioException {
        if (texto.isEmpty()) {
            throw new ReglaNegocioException("Ingrese el porcentaje para " + perfil + ".");
        }
        try {
            double val = leerNumero(texto);
            if (val < 0 || val > 100) {
                throw new ReglaNegocioException("El porcentaje para " + perfil + " debe estar entre 0 y 100.");
            }
            return val;
        } catch (NumberFormatException e) {
            throw new ReglaNegocioException("El porcentaje para " + perfil + " debe ser un número válido.");
        }
    }
}