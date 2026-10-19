package com.transporte.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Modelo para la gestión en memoria de la flota de autobuses con validaciones estrictas y persistencia reactiva.
 */
public class FlotaModel {

    private final List<Autobus> listaAutobuses;
    private final List<Runnable> oyentes = new ArrayList<>();

    public FlotaModel() {
        this.listaAutobuses = new ArrayList<>();
    }

    public List<Autobus> getListaAutobuses() {
        return Collections.unmodifiableList(listaAutobuses);
    }

    public int getCantidadUnidades() {
        return listaAutobuses.size();
    }

    public Autobus obtenerAutobus(int indice) {
        if (indice < 0 || indice >= listaAutobuses.size()) {
            throw new IndexOutOfBoundsException("La unidad seleccionada no existe.");
        }
        return listaAutobuses.get(indice);
    }

    public void agregarAutobus(Autobus bus) {
        validarAutobus(bus, -1);
        bus.setPlaca(bus.getPlaca().trim().toUpperCase());
        bus.setModelo(bus.getModelo().trim());
        listaAutobuses.add(bus);
        notificarCambio();
    }

    public void actualizarAutobus(int indice, Autobus busActualizado) {
        if (indice < 0 || indice >= listaAutobuses.size()) {
            throw new IndexOutOfBoundsException("La unidad seleccionada no existe.");
        }
        validarAutobus(busActualizado, indice);

        Autobus existente = listaAutobuses.get(indice);
        existente.setPlaca(busActualizado.getPlaca().trim().toUpperCase());
        existente.setModelo(busActualizado.getModelo().trim());
        existente.setCapacidad(busActualizado.getCapacidad());
        existente.setEstado(busActualizado.getEstado());
        notificarCambio();
    }

    public void eliminarAutobus(int indice) {
        if (indice < 0 || indice >= listaAutobuses.size()) {
            throw new IndexOutOfBoundsException("La unidad seleccionada no existe.");
        }
        listaAutobuses.remove(indice);
        notificarCambio();
    }

    public void alternarEstado(int indice) {
        if (indice < 0 || indice >= listaAutobuses.size()) {
            throw new IndexOutOfBoundsException("La unidad seleccionada no existe.");
        }
        Autobus bus = listaAutobuses.get(indice);
        String estadoActual = bus.getEstado();
        String nuevoEstado = alternarSiguienteEstado(estadoActual);
        bus.setEstado(nuevoEstado);
        notificarCambio();
    }

    public String alternarSiguienteEstado(String estadoActual) {
        if (estadoActual == null) return "Operativo";

        switch (estadoActual.trim()) {
            case "Operativo":
                return "En Mantenimiento";
            case "En Mantenimiento":
                return "Fuera de Servicio";
            case "Fuera de Servicio":
                return "Operativo";
            default:
                return "Operativo";
        }
    }

    public boolean existePlaca(String placa) {
        return existePlaca(placa, -1);
    }

    public boolean existePlaca(String placa, int indiceExcluir) {
        if (placa == null) {
            return false;
        }
        String placaNormalizada = placa.trim().toUpperCase();
        for (int i = 0; i < listaAutobuses.size(); i++) {
            if (i != indiceExcluir) {
                if (listaAutobuses.get(i).getPlaca().equalsIgnoreCase(placaNormalizada)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void agregarOyente(Runnable oyente) {
        if (oyente != null) {
            oyentes.add(oyente);
        }
    }

    public void notificarCambio() {
        for (Runnable oyente : oyentes) {
            oyente.run();
        }
    }

    private void validarAutobus(Autobus bus, int indiceExcluir) {
        if (bus == null) {
            throw new IllegalArgumentException("Los datos de la unidad no pueden ser nulos.");
        }

        // 1. Validación estricta de Placa (formato AA111AA)
        String placa = bus.getPlaca() != null ? bus.getPlaca().trim().toUpperCase() : "";
        if (placa.isEmpty()) {
            throw new IllegalArgumentException("La placa no puede estar vacía.");
        }
        if (placa.length() != 7) {
            throw new IllegalArgumentException("La placa debe tener exactamente 7 caracteres.");
        }
        if (!placa.matches("^[A-Z]{2}[0-9]{3}[A-Z]{2}$")) {
            throw new IllegalArgumentException("La placa no cumple el formato obligatorio: 2 letras, 3 números y 2 letras (ej: AA111AA).");
        }
        if (existePlaca(placa, indiceExcluir)) {
            throw new IllegalArgumentException("Ya existe una unidad registrada con la placa: " + placa);
        }

        // 2. Validación estricta de Modelo
        String modelo = bus.getModelo() != null ? bus.getModelo().trim() : "";
        if (modelo.isEmpty()) {
            throw new IllegalArgumentException("El modelo no puede estar vacío.");
        }
        if (modelo.length() < 2) {
            throw new IllegalArgumentException("Modelo inválido.");
        }
        if (modelo.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("El modelo no puede ser únicamente numérico.");
        }

        // 3. Validación de Capacidad
        int capacidad = bus.getCapacidad();
        if (capacidad < 1 || capacidad > 100) {
            throw new IllegalArgumentException("La capacidad de pasajeros debe estar entre 1 y 100.");
        }

        // 4. Validación de Estado
        String estado = bus.getEstado();
        if (estado == null || estado.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar un estado operacional válido.");
        }
    }
}
