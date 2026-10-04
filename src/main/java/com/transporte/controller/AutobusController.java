package com.transporte.controller;

import javax.swing.JOptionPane;

import com.transporte.model.Autobus;
import com.transporte.model.FlotaModel;
import com.transporte.view.GestionFlotaVentana;

public class AutobusController {
    private FlotaModel modelo;
    private GestionFlotaVentana vista;

    public AutobusController(FlotaModel modelo, GestionFlotaVentana vista) {
        this.modelo = modelo;
        this.vista = vista;
        inicializarEventos();
    }

    private void inicializarEventos() {
        // Clic en los botones de acción
        vista.getBtnAgregar().addActionListener(e -> agregarAutobus());
        vista.getBtnEditar().addActionListener(e -> editarAutobus());
        vista.getBtnEliminar().addActionListener(e -> eliminarAutobus());
        vista.getBtnCambiarEstado().addActionListener(e -> cambiarEstado());

        // La tecla ENTER actúa como "Aceptar":
        // Si hay una fila seleccionada, Guarda la edición.
        // Si no hay fila seleccionada, Registra una nueva unidad.
        vista.getTxtPlaca().addActionListener(e -> confirmarAccionPorEnter());
        vista.getTxtModelo().addActionListener(e -> confirmarAccionPorEnter());
        vista.getTxtCapacidad().addActionListener(e -> confirmarAccionPorEnter());

        // Al hacer clic en una unidad de la tabla, carga sus datos en los campos
        vista.getTablaAutobuses().getSelectionModel().addListSelectionListener(e -> {
            int fila = vista.getTablaAutobuses().getSelectedRow();
            if (fila != -1) {
                Autobus bus = modelo.obtenerAutobus(fila);
                if (bus != null) {
                    vista.getTxtPlaca().setText(bus.getPlaca());
                    vista.getTxtModelo().setText(bus.getModelo());
                    vista.getTxtCapacidad().setText(String.valueOf(bus.getCapacidad()));
                    vista.getCmbEstado().setSelectedItem(bus.getEstado());
                }
            }
        });

        // Botón Limpiar Selección: quita selección de la tabla y vacía el formulario
        vista.getBtnLimpiarSeleccion().addActionListener(e -> vista.limpiarSeleccionYCampos());
    }

    /**
     * Resuelve la acción de la tecla ENTER según el contexto del usuario.
     */
    private void confirmarAccionPorEnter() {
        int fila = vista.getTablaAutobuses().getSelectedRow();
        if (fila != -1) {
            editarAutobus();
        } else {
            agregarAutobus();
        }
    }

    
     /* Las reglas (placa, modelo, capacidad, duplicados) viven SOLO en {@link FlotaModel}: aquí únicamente  se convierte el texto de capacidad a número y se muestra el mensaje que lance el modelo.
     */
    private void agregarAutobus() {
        try {
            Autobus bus = leerFormulario();
            modelo.agregarAutobus(bus);   // valida y normaliza la placa (mayúsculas)

            vista.getModeloTabla().addRow(new Object[]{bus.getPlaca(), bus.getModelo(), bus.getCapacidad(), bus.getEstado()});
            vista.limpiarSeleccionYCampos();
            JOptionPane.showMessageDialog(vista, "Unidad registrada exitosamente.", "Mensaje", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            // incluye NumberFormatException (capacidad no numérica) y las reglas del modelo
            avisar(ex);
        }
    }

    private void editarAutobus() {
        int fila = vista.getTablaAutobuses().getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(vista, "Selecciona una unidad de la tabla para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Autobus busActualizado = leerFormulario();
            modelo.actualizarAutobus(fila, busActualizado);   // valida, incluida la placa repetida en OTRA unidad

            vista.getModeloTabla().setValueAt(busActualizado.getPlaca(), fila, 0);
            vista.getModeloTabla().setValueAt(busActualizado.getModelo(), fila, 1);
            vista.getModeloTabla().setValueAt(busActualizado.getCapacidad(), fila, 2);
            vista.getModeloTabla().setValueAt(busActualizado.getEstado(), fila, 3);

            JOptionPane.showMessageDialog(vista, "Unidad actualizada con éxito.", "Mensaje", JOptionPane.INFORMATION_MESSAGE);
            vista.limpiarSeleccionYCampos();
        } catch (IllegalArgumentException ex) {
            avisar(ex);
        }
    }

    /** Lee los campos del formulario. Lanza IllegalArgumentException si la capacidad no es un número. */
    private Autobus leerFormulario() {
        String placa = vista.getTxtPlaca().getText().trim();
        String modeloTexto = vista.getTxtModelo().getText().trim();
        String capacidadTexto = vista.getTxtCapacidad().getText().trim();
        String estado = (String) vista.getCmbEstado().getSelectedItem();

        int capacidad;
        try {
            capacidad = Integer.parseInt(capacidadTexto);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Ingresa una cantidad de pasajeros válida (solo números).");
        }
        return new Autobus(placa, modeloTexto, capacidad, estado);
    }

    private void avisar(IllegalArgumentException ex) {
        JOptionPane.showMessageDialog(vista, ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    private void eliminarAutobus() {
        int fila = vista.getTablaAutobuses().getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(vista, "Selecciona una unidad de la tabla para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
            vista,
            "¿Estás seguro de que deseas eliminar esta unidad?",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                modelo.eliminarAutobus(fila);
                vista.getModeloTabla().removeRow(fila);
                vista.limpiarSeleccionYCampos();
                JOptionPane.showMessageDialog(vista, "Unidad eliminada correctamente.", "Mensaje", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(vista, ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void cambiarEstado() {
        int fila = vista.getTablaAutobuses().getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(vista, "Selecciona una unidad para cambiar su estado.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Autobus bus = modelo.obtenerAutobus(fila);
        String estadoActual = bus.getEstado();
        String nuevoEstado;

        // Ciclo circular: Operativo  -En Mantenimiento - Fuera de Servicio - Operativo
        switch (estadoActual != null ? estadoActual.trim() : "") {
            case "Operativo":
                nuevoEstado = "En Mantenimiento";
                break;
            case "En Mantenimiento":
                nuevoEstado = "Fuera de Servicio";
                break;
            case "Fuera de Servicio":
                nuevoEstado = "Operativo";
                break;
            default:
                nuevoEstado = "Operativo";
                break;
        }

        bus.setEstado(nuevoEstado);
        vista.getModeloTabla().setValueAt(nuevoEstado, fila, 3);
        vista.getCmbEstado().setSelectedItem(nuevoEstado);
    }
}
