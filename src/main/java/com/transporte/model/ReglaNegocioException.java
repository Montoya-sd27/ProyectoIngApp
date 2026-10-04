package com.transporte.model;

/** Se lanza cuando una operación viola una regla de negocio de itinerarios. */
public class ReglaNegocioException extends Exception {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
