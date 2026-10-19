package com.transporte.model;

import java.util.ArrayList;
import java.util.List;

public class FlotaModel {
    private List<Autobus> listaAutobuses;
    private final List<Runnable> oyentes = new ArrayList<>();

    public FlotaModel() {
        this.listaAutobuses = new ArrayList<>();
    }

    public boolean existePlaca(String placa) {
        if (placa == null) return false;
        for (Autobus bus : listaAutobuses) {
            if (bus.getPlaca().equalsIgnoreCase(placa.trim())) {
                return true;
            }
        }
        return false;
    }

    public boolean existePlacaEnOtro(String placa, int indiceExcluido) {
        if (placa == null) return false;
        for (int i = 0; i < listaAutobuses.size(); i++) {
            if (i != indiceExcluido && listaAutobuses.get(i).getPlaca().equalsIgnoreCase(placa.trim())) {
                return true;
            }
        }
        return false;
    }

    public void agregarAutobus(Autobus bus) throws IllegalArgumentException {
        if (bus == null) {
            throw new IllegalArgumentException("El autobús no puede ser nulo.");
        }

        // Validación de Placa
        if (bus.getPlaca() == null || bus.getPlaca().trim().isEmpty()) {
            throw new IllegalArgumentException("La placa no puede estar vacía.");
        }
        if (bus.getPlaca().trim().length() > 10) {
            throw new IllegalArgumentException("La placa no puede exceder los 10 caracteres.");
        }

        // Validación de Modelo
        if (bus.getModelo() == null || bus.getModelo().trim().isEmpty()) {
            throw new IllegalArgumentException("El modelo no puede estar vacío.");
        }
        if (bus.getModelo().trim().length() > 40) {
            throw new IllegalArgumentException("El modelo no puede exceder los 40 caracteres.");
        }

        // Validación de Capacidad (1 a 100 pasajeros)
        if (bus.getCapacidad() <= 0 || bus.getCapacidad() > 100) {
            throw new IllegalArgumentException("La capacidad de pasajeros debe estar entre 1 y 100.");
        }

        String placaNormalizada = bus.getPlaca().trim().toUpperCase();
        bus.setPlaca(placaNormalizada);

        for (Autobus existente : listaAutobuses) {
            if (existente.getPlaca().equalsIgnoreCase(placaNormalizada)) {
                throw new IllegalArgumentException("Ya existe una unidad registrada con la placa: " + existente.getPlaca());
            }
        }

        listaAutobuses.add(bus);
        notificarCambio();
    }

    public Autobus obtenerAutobus(int indice) {
        if (indice >= 0 && indice < listaAutobuses.size()) {
            return listaAutobuses.get(indice);
        }
        return null;
    }

    public List<Autobus> getListaAutobuses() {
        return listaAutobuses;
    }

    public void actualizarAutobus(int indice, Autobus bus) throws IllegalArgumentException {
        if (indice < 0 || indice >= listaAutobuses.size()) {
            throw new IllegalArgumentException("Índice de unidad inválido.");
        }
        if (bus == null) {
            throw new IllegalArgumentException("Los datos de actualización no pueden ser nulos.");
        }

        if (bus.getPlaca() == null || bus.getPlaca().trim().isEmpty()) {
            throw new IllegalArgumentException("La placa no puede estar vacía.");
        }
        if (bus.getPlaca().trim().length() > 10) {
            throw new IllegalArgumentException("La placa no puede exceder los 10 caracteres.");
        }

        if (bus.getModelo() == null || bus.getModelo().trim().isEmpty()) {
            throw new IllegalArgumentException("El modelo no puede estar vacío.");
        }
        if (bus.getModelo().trim().length() > 40) {
            throw new IllegalArgumentException("El modelo no puede exceder los 40 caracteres.");
        }

        if (bus.getCapacidad() <= 0 || bus.getCapacidad() > 100) {
            throw new IllegalArgumentException("La capacidad de pasajeros debe estar entre 1 y 100.");
        }

        String placaNormalizada = bus.getPlaca().trim().toUpperCase();
        bus.setPlaca(placaNormalizada);

        for (int i = 0; i < listaAutobuses.size(); i++) {
            if (i != indice && listaAutobuses.get(i).getPlaca().equalsIgnoreCase(placaNormalizada)) {
                throw new IllegalArgumentException("Ya existe otra unidad registrada con la placa: " + listaAutobuses.get(i).getPlaca());
            }
        }

        Autobus existente = listaAutobuses.get(indice);
        existente.setPlaca(bus.getPlaca());
        existente.setModelo(bus.getModelo());
        existente.setCapacidad(bus.getCapacidad());
        existente.setEstado(bus.getEstado());
        notificarCambio();
    }

    public void eliminarAutobus(int indice) throws IllegalArgumentException {
        if (indice >= 0 && indice < listaAutobuses.size()) {
            listaAutobuses.remove(indice);
            notificarCambio();
        } else {
            throw new IllegalArgumentException("No se pudo eliminar: índice fuera de rango.");
        }
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

    public void agregarOyente(Runnable oyente) {
        oyentes.add(oyente);
    }

    public void notificarCambio() {
        for (Runnable oyente : oyentes) {
            oyente.run();
        }
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
}
