package com.transporte.controller;

import com.transporte.model.Autobus;
import com.transporte.model.FlotaModel;
import com.transporte.view.GestionFlotaVentana;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class AutobusController {

    private final FlotaModel modelo;
    private final GestionFlotaVentana vista;

    public AutobusController(FlotaModel modelo, GestionFlotaVentana vista) {
        this.modelo = modelo;
        this.vista = vista;
        inicializarEventos();
        actualizarTabla();
    }

    private void inicializarEventos() {
        vista.getBtnAgregar().addActionListener(e -> agregarUnidad());
        vista.getBtnEditar().addActionListener(e -> editarUnidad());
        vista.getBtnEliminar().addActionListener(e -> eliminarUnidad());
        vista.getBtnCambiarEstado().addActionListener(e -> alternarEstado());
        vista.getBtnLimpiarSeleccion().addActionListener(e -> vista.limpiarSeleccionYCampos());

        vista.getTxtPlaca().addActionListener(e -> agregarUnidad());
        vista.getTxtModelo().addActionListener(e -> agregarUnidad());
        vista.getTxtCapacidad().addActionListener(e -> agregarUnidad());

        vista.getTablaAutobuses().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cargarFilaSeleccionadaEnCampos();
            }
        });
    }

    private void cargarFilaSeleccionadaEnCampos() {
        int fila = vista.getTablaAutobuses().getSelectedRow();
        if (fila != -1) {
            vista.getTxtPlaca().setText((String) vista.getModeloTabla().getValueAt(fila, 0));
            vista.getTxtModelo().setText((String) vista.getModeloTabla().getValueAt(fila, 1));
            vista.getTxtCapacidad().setText(String.valueOf(vista.getModeloTabla().getValueAt(fila, 2)));
            vista.getCmbEstado().setSelectedItem(vista.getModeloTabla().getValueAt(fila, 3));
        }
    }

    private void agregarUnidad() {
        String placa = vista.getTxtPlaca().getText().trim();
        String modeloTexto = vista.getTxtModelo().getText().trim();
        String capacidadTexto = vista.getTxtCapacidad().getText().trim();
        String estado = (String) vista.getCmbEstado().getSelectedItem();

        int capacidad;
        try {
            capacidad = Integer.parseInt(capacidadTexto);
        } catch (NumberFormatException e) {
            mostrarAviso("Ingresa una cantidad de pasajeros válida (solo números).");
            return;
        }

        try {
            Autobus nuevoBus = new Autobus(placa, modeloTexto, capacidad, estado);
            modelo.agregarAutobus(nuevoBus);
            actualizarTabla();
            vista.limpiarSeleccionYCampos();
            mostrarExito("Unidad agregada exitosamente.");
        } catch (IllegalArgumentException ex) {
            mostrarAviso(ex.getMessage());
        }
    }

    private void editarUnidad() {
        int fila = vista.getTablaAutobuses().getSelectedRow();
        if (fila == -1) {
            mostrarAviso("Debe seleccionar una unidad de la tabla para editar.");
            return;
        }

        String placa = vista.getTxtPlaca().getText().trim();
        String modeloTexto = vista.getTxtModelo().getText().trim();
        String capacidadTexto = vista.getTxtCapacidad().getText().trim();
        String estado = (String) vista.getCmbEstado().getSelectedItem();

        int capacidad;
        try {
            capacidad = Integer.parseInt(capacidadTexto);
        } catch (NumberFormatException e) {
            mostrarAviso("Ingresa una cantidad de pasajeros válida (solo números).");
            return;
        }

        try {
            Autobus busActualizado = new Autobus(placa, modeloTexto, capacidad, estado);
            modelo.actualizarAutobus(fila, busActualizado);
            actualizarTabla();
            vista.limpiarSeleccionYCampos();
            mostrarExito("Unidad modificada exitosamente.");
        } catch (IllegalArgumentException | IndexOutOfBoundsException ex) {
            mostrarAviso(ex.getMessage());
        }
    }

    private void eliminarUnidad() {
        int fila = vista.getTablaAutobuses().getSelectedRow();
        if (fila == -1) {
            mostrarAviso("Debe seleccionar una unidad de la tabla.");
            return;
        }

        String placa = (String) vista.getModeloTabla().getValueAt(fila, 0);
        int confirmacion = JOptionPane.showConfirmDialog(
                vista,
                "¿Está seguro de que desea eliminar la unidad con placa " + placa + "?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                modelo.eliminarAutobus(fila);
                actualizarTabla();
                vista.limpiarSeleccionYCampos();
                mostrarExito("Unidad eliminada exitosamente.");
            } catch (IndexOutOfBoundsException ex) {
                mostrarAviso(ex.getMessage());
            }
        }
    }

    private void alternarEstado() {
        int fila = vista.getTablaAutobuses().getSelectedRow();
        if (fila == -1) {
            mostrarAviso("Debe seleccionar una unidad de la tabla.");
            return;
        }

        try {
            modelo.alternarEstado(fila);
            actualizarTabla();
            vista.getTablaAutobuses().setRowSelectionInterval(fila, fila);
            cargarFilaSeleccionadaEnCampos();
        } catch (IndexOutOfBoundsException ex) {
            mostrarAviso(ex.getMessage());
        }
    }

    public void actualizarTabla() {
        DefaultTableModel model = vista.getModeloTabla();
        model.setRowCount(0);
        List<Autobus> lista = modelo.getListaAutobuses();
        for (Autobus bus : lista) {
            model.addRow(new Object[]{
                    bus.getPlaca(),
                    bus.getModelo(),
                    bus.getCapacidad(),
                    bus.getEstado()
            });
        }
    }

    private void mostrarExito(String mensaje) {
        JOptionPane.showMessageDialog(vista, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarAviso(String mensaje) {
        JOptionPane.showMessageDialog(vista, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
}
