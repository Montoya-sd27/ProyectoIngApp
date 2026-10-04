package com.transporte.repository;

/** PATRÓN OBSERVER: las vistas se suscriben para refrescarse cuando cambian los datos. */
public interface ItinerarioObserver {
    void itinerariosActualizados();
}
