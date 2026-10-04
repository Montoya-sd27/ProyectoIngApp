package com.transporte.view;

import com.transporte.controller.ItinerarioController;
import com.transporte.model.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Formulario (crear / editar) con el mismo estilo del registro de unidad. */
public class ItinerarioFormView extends JDialog {

    private final ItinerarioController controller;
    private final Itinerario aEditar;

    // campos del formulario
    private final JTextField txtInicio = new JTextField();
    private final JTextField txtFinal = new JTextField();
    private final JComboBox<RutaFactory.TipoRuta> cmbTipoRuta = new JComboBox<>(RutaFactory.TipoRuta.values());
    private final JComboBox<Autobus> cmbUnidad = new JComboBox<>();
    private final JComboBox<Conductor> cmbConductor = new JComboBox<>();
    private final JComboBox<DayOfWeek> cmbDia = new JComboBox<>(DayOfWeek.values());
    private final JTextField txtSalida = new JTextField();
    private final JTextField txtLlegada = new JTextField();
    private final JComboBox<EstadoRecorrido> cmbEstado = new JComboBox<>(EstadoRecorrido.values());

    public ItinerarioFormView(Frame owner, ItinerarioController controller, Itinerario aEditar) {
        super(owner, aEditar == null ? "Registro de itinerario" : "Editar itinerario", true);
        this.controller = controller;
        this.aEditar = aEditar;

        cargarCombos();
        if (aEditar != null) precargar(aEditar);

        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(EstiloUI.GRIS);
        fondo.add(construirTarjeta());
        setContentPane(fondo);
        pack();
        setMinimumSize(new Dimension(460, 0));
        setLocationRelativeTo(owner);
    }

    // llena las listas con los datos que da el controlador
    private void cargarCombos() {
        controller.unidadesOperativas().forEach(cmbUnidad::addItem);
        controller.conductoresDisponibles().forEach(cmbConductor::addItem);

        cmbUnidad.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                String texto = v instanceof Autobus a
                        ? a.getPlaca() + " · " + a.getModelo() + " (" + a.getCapacidad() + " pax)"
                        : "";
                return super.getListCellRendererComponent(l, texto, i, s, f);
            }
        });
        cmbTipoRuta.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                String texto = v == RutaFactory.TipoRuta.URBANA ? "Ruta Urbana"
                        : v == RutaFactory.TipoRuta.EXTRAURBANA ? "Ruta Extraurbana" : "";
                return super.getListCellRendererComponent(l, texto, i, s, f);
            }
        });
        cmbDia.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                return super.getListCellRendererComponent(l, EstiloUI.diaEs((DayOfWeek) v), i, s, f);
            }
        });
        for (JComboBox<?> c : new JComboBox<?>[]{cmbTipoRuta, cmbUnidad, cmbConductor, cmbDia, cmbEstado}) {
            EstiloUI.estilizarCombo(c);
        }
        EstiloUI.estilizarCampo(txtInicio);
        EstiloUI.estilizarCampo(txtFinal);
        EstiloUI.estilizarCampo(txtSalida);
        EstiloUI.estilizarCampo(txtLlegada);
        txtSalida.setText("07:00");
        txtLlegada.setText("08:00");
    }

    // al editar, rellena el formulario con los datos actuales
    private void precargar(Itinerario it) {
        txtInicio.setText(it.getRuta().getOrigen());
        txtFinal.setText(it.getRuta().getDestino());
        cmbTipoRuta.setSelectedItem(it.getRuta() instanceof RutaUrbana
                ? RutaFactory.TipoRuta.URBANA : RutaFactory.TipoRuta.EXTRAURBANA);
        // Seleccionar por identidad lógica (placa/cédula/id) por si el objeto no es la misma instancia
        // Si la unidad asignada ya no está operativa, igual se muestra (la validación avisará al guardar).
        boolean enLista = false;
        for (int i = 0; i < cmbUnidad.getItemCount(); i++) {
            if (cmbUnidad.getItemAt(i) == it.getUnidad()) enLista = true;
        }
        if (!enLista) cmbUnidad.insertItemAt(it.getUnidad(), 0);
        cmbUnidad.setSelectedItem(it.getUnidad());
        seleccionar(cmbConductor, c -> c.getCedula().equals(it.getConductor().getCedula()));
        cmbDia.setSelectedItem(it.getHorario().getDia());
        txtSalida.setText(it.getHorario().getSalida().toString());
        txtLlegada.setText(it.getHorario().getLlegada().toString());
        cmbEstado.setSelectedItem(it.getEstadoRecorrido());
    }

    private <T> void seleccionar(JComboBox<T> combo, java.util.function.Predicate<T> criterio) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (criterio.test(combo.getItemAt(i))) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    // arma la tarjeta blanca con los campos y los botones
    private JPanel construirTarjeta() {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(EstiloUI.BLANCO);
        tarjeta.setBorder(new EmptyBorder(24, 32, 24, 32));

        JLabel titulo = new JLabel(aEditar == null ? "Registro de itinerario" : "Editar itinerario");
        titulo.setFont(EstiloUI.FUENTE_TITULO);
        titulo.setForeground(EstiloUI.AZUL);
        titulo.setAlignmentX(CENTER_ALIGNMENT);
        JLabel sub = new JLabel("llene el formulario");
        sub.setForeground(EstiloUI.AZUL);
        sub.setAlignmentX(CENTER_ALIGNMENT);
        tarjeta.add(titulo);
        tarjeta.add(sub);
        tarjeta.add(Box.createVerticalStrut(14));

        agregarCampo(tarjeta, "Inicio de la ruta", txtInicio);
        agregarCampo(tarjeta, "Final de la ruta", txtFinal);
        agregarCampo(tarjeta, "Tipo de ruta", cmbTipoRuta);
        agregarCampo(tarjeta, "Unidad (solo operativas)", cmbUnidad);
        agregarCampo(tarjeta, "Conductor", cmbConductor);
        agregarCampo(tarjeta, "Día", cmbDia);
        agregarCampo(tarjeta, "Hora de salida (HH:mm)", txtSalida);
        agregarCampo(tarjeta, "Hora de llegada (HH:mm)", txtLlegada);
        agregarCampo(tarjeta, "Estado del recorrido", cmbEstado);

        tarjeta.add(Box.createVerticalStrut(10));
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        botones.setOpaque(false);
        EstiloUI.BotonRedondeado regresar = EstiloUI.botonRojo("Regresar");
        EstiloUI.BotonRedondeado guardar = EstiloUI.botonVerde(aEditar == null ? "Registrar itinerario" : "Guardar cambios");
        regresar.addActionListener(e -> dispose());
        guardar.addActionListener(e -> guardar());
        botones.add(regresar);
        botones.add(guardar);
        tarjeta.add(botones);
        return tarjeta;
    }

    // agrega una etiqueta y su campo al formulario
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

    // lee el formulario y crea o edita según corresponda
    private void guardar() {
        try {
            Horario horario = new Horario(
                    (DayOfWeek) cmbDia.getSelectedItem(),
                    LocalTime.parse(txtSalida.getText().trim()),
                    LocalTime.parse(txtLlegada.getText().trim()));
            String inicio = txtInicio.getText().trim();
            String fin = txtFinal.getText().trim();
            if (inicio.isEmpty() || fin.isEmpty()) {
                error("Debe indicar el inicio y el final de la ruta.");
                return;
            }
            RutaFactory.TipoRuta tipo = (RutaFactory.TipoRuta) cmbTipoRuta.getSelectedItem();
            // al editar se conserva el id de la ruta; al crear se genera uno nuevo
            String idRuta = aEditar != null ? aEditar.getRuta().getIdRuta() : nuevoIdRuta(tipo);
            // Urbana: sin paradas intermedias; Extraurbana: la ciudad dormitorio es el destino
            Ruta ruta = RutaFactory.crear(tipo, idRuta, inicio, fin,
                    tipo == RutaFactory.TipoRuta.EXTRAURBANA ? fin : null);
            Autobus unidad = (Autobus) cmbUnidad.getSelectedItem();
            Conductor conductor = (Conductor) cmbConductor.getSelectedItem();
            EstadoRecorrido estado = (EstadoRecorrido) cmbEstado.getSelectedItem();

            // sin itinerario previo se crea uno nuevo; si no, se edita
            if (aEditar == null) {
                controller.crear(ruta, unidad, conductor, horario, estado);
            } else {
                controller.editar(aEditar.getIdItinerario(), ruta, unidad, conductor, horario, estado);
            }
            dispose();
        } catch (DateTimeParseException ex) {
            // la hora no tiene el formato HH:mm
            error("Formato de hora inválido. Use HH:mm (ejemplo: 07:30).");
        } catch (ReglaNegocioException ex) {
            // se rompió una regla de negocio (por ejemplo un choque de horario)
            error(ex.getMessage());
        }
    }

    // genera el siguiente id de ruta (RU-xx urbana / RE-xx extraurbana) sin repetir los existentes
    private String nuevoIdRuta(RutaFactory.TipoRuta tipo) {
        String prefijo = tipo == RutaFactory.TipoRuta.URBANA ? "RU-" : "RE-";
        int max = 0;
        java.util.List<Ruta> existentes = new java.util.ArrayList<>(controller.rutasDisponibles());
        controller.listar().forEach(it -> existentes.add(it.getRuta()));
        for (Ruta r : existentes) {
            if (!r.getIdRuta().startsWith(prefijo)) continue;
            try {
                max = Math.max(max, Integer.parseInt(r.getIdRuta().substring(prefijo.length())));
            } catch (NumberFormatException ignorado) { }
        }
        return String.format("%s%02d", prefijo, max + 1);
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Verifique los datos", JOptionPane.WARNING_MESSAGE);
    }
}
