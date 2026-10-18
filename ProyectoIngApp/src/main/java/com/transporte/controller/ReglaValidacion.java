package com.transporte.controller;

import com.transporte.model.ReglaNegocioException;

/** PATRÓN STRATEGY (interfaz): cada regla de validación de un campo es una estrategia intercambiable. */
public interface ReglaValidacion {
    String validar(String valor) throws ReglaNegocioException;
}