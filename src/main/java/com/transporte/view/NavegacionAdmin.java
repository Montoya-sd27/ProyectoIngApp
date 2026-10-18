package com.transporte.view;

/**
 * Acciones de navegación que la pantalla del administrador necesita pero que no le
 * corresponde implementar (abrir otras ventanas, volver al login). La vista solo
 * conoce esta interfaz, no a las demás pantallas -> bajo acoplamiento entre módulos.
 */
public interface NavegacionAdmin {
    /** Abre la pantalla de gestión de unidades (módulo de flota). */
    void irAGestionUnidades();

    /** Vuelve a la pantalla de gestión de itinerarios (inicio del administrador). */
    void irAItinerarios();

    /** Abre el backoffice de registro de usuarios. */
    void irABackofficeUsuarios();

    /** Cierra la sesión actual y vuelve al inicio de sesión. */
    void cerrarSesion();
}
