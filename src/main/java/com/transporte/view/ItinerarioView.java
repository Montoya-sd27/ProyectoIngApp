package com.transporte.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.transporte.controller.ItinerarioController;
import com.transporte.model.Itinerario;
import com.transporte.model.ReglaNegocioException;
import com.transporte.repository.ItinerarioObserver;

/**
 * lista de itinerarios, acciones Nuevo / Editar / Eliminar.
/*Solo se refresca cuando hay cambio */
public class ItinerarioView extends JFrame implements ItinerarioObserver {

    private static final String[] COLUMNAS =
            {"ID", "Ruta", "Tipo", "Unidad", "Conductor", "Día", "Salida", "Llegada", "Estado"};
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final ItinerarioController controller;
    private final NavegacionAdmin navegacion;
    private final DefaultTableModel modeloTabla;
    private final JTable tabla;

    public ItinerarioView(ItinerarioController controller, NavegacionAdmin navegacion) {
        super("Ruta Central - Gestión de itinerarios");
        this.controller = controller;
        this.navegacion = navegacion;

        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        configurarTabla();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(EstiloUI.encabezado(this::cerrarSesion), BorderLayout.NORTH);
        add(construirCentro(), BorderLayout.CENTER);

        controller.suscribir(this);   // registro como observador
        itinerariosActualizados();    // carga inicial
    }

    private JPanel construirCentro() {
        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(EstiloUI.GRIS);

        JPanel tarjeta = new JPanel(new BorderLayout(0, 12));
        tarjeta.setBackground(EstiloUI.BLANCO);
        tarjeta.setBorder(new EmptyBorder(20, 24, 20, 24));
        tarjeta.setPreferredSize(new Dimension(900, 480));

        JLabel titulo = new JLabel("Gestión de itinerarios", SwingConstants.CENTER);
        titulo.setFont(EstiloUI.FUENTE_TITULO);
        titulo.setForeground(EstiloUI.AZUL);
        JLabel sub = new JLabel("Administre las rutas, unidades y conductores asignados", SwingConstants.CENTER);
        sub.setForeground(EstiloUI.AZUL);
        JPanel cabecera = new JPanel(new GridLayout(2, 1));
        cabecera.setOpaque(false);
        cabecera.add(titulo);
        cabecera.add(sub);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(EstiloUI.GRIS);
        scroll.setBorder(BorderFactory.createLineBorder(EstiloUI.GRIS, 2));

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        acciones.setOpaque(false);
        EstiloUI.BotonRedondeado nuevo = EstiloUI.botonVerde("Nuevo itinerario");
        EstiloUI.BotonRedondeado editar = EstiloUI.botonVerde("Editar");
        EstiloUI.BotonRedondeado eliminar = EstiloUI.botonRojo("Eliminar");
        nuevo.addActionListener(e -> abrirFormulario(null));
        editar.addActionListener(e -> editarSeleccionado());
        eliminar.addActionListener(e -> eliminarSeleccionado());
        acciones.add(eliminar);
        acciones.add(editar);
        acciones.add(nuevo);

        EstiloUI.BotonRedondeado unidades = EstiloUI.botonAzul("Gestión de unidades");
        unidades.addActionListener(e -> navegacion.irAGestionUnidades());

        EstiloUI.BotonRedondeado usuarios = EstiloUI.botonAzul("Registrar usuario");
        usuarios.addActionListener(e -> navegacion.irABackofficeUsuarios());

        JPanel navegar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        navegar.setOpaque(false);
        navegar.add(unidades);
        navegar.add(usuarios);

        JPanel botones = new JPanel(new BorderLayout());
        botones.setOpaque(false);
        botones.add(navegar, BorderLayout.WEST);
        botones.add(acciones, BorderLayout.EAST);

        tarjeta.add(cabecera, BorderLayout.NORTH);
        tarjeta.add(scroll, BorderLayout.CENTER);
        tarjeta.add(botones, BorderLayout.SOUTH);
        fondo.add(tarjeta);
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

    /* recarga la tabla desde el controlador. */
    @Override
    public void itinerariosActualizados() {
        modeloTabla.setRowCount(0);
        for (Itinerario it : controller.listar()) {
            modeloTabla.addRow(new Object[]{
                    it.getIdItinerario(),
                    it.getRuta().getOrigen() + " → " + it.getRuta().getDestino(),
                    it.getRuta().getTipo(),
                    it.getUnidad().getPlaca(),
                    it.getConductor().getNombre(),
                    EstiloUI.diaEs(it.getHorario().getDia()),
                    it.getHorario().getSalida().format(HORA),
                    it.getHorario().getLlegada().format(HORA),
                    it.getEstadoRecorrido()
            });
        }
    }

    private void abrirFormulario(Itinerario aEditar) {
        new ItinerarioFormView(this, controller, aEditar).setVisible(true);
    }

    private String idSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un itinerario de la tabla.",
                    "Sin selección", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return (String) modeloTabla.getValueAt(fila, 0);
    }

    private void editarSeleccionado() {
        String id = idSeleccionado();
        if (id == null) return;
        try {
            Itinerario it = controller.buscar(id);
            if (!it.getEstadoRecorrido().esModificable()) {
                throw new ReglaNegocioException("No se puede modificar un itinerario "
                        + it.getEstadoRecorrido().toString().toLowerCase() + ".");
            }
            abrirFormulario(it);
        } catch (ReglaNegocioException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void eliminarSeleccionado() {
        String id = idSeleccionado();
        if (id == null) return;
        int r = JOptionPane.showConfirmDialog(this, "¿Eliminar el itinerario " + id + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) return;
        try {
            controller.eliminar(id);
        } catch (ReglaNegocioException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void cerrarSesion() {
        int r = JOptionPane.showConfirmDialog(this, "¿Desea cerrar sesión?", "Cerrar sesión",
                JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) navegacion.cerrarSesion();
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "No se pudo completar", JOptionPane.WARNING_MESSAGE);
    }
}
