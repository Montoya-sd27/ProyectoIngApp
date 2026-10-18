package com.transporte.controller;

import com.transporte.model.ReglaNegocioException;
import com.transporte.model.Ruta;
import com.transporte.model.RutaExtraurbana;
import com.transporte.model.RutaFactory.TipoRuta;
import com.transporte.model.TarifaRuta;
import com.transporte.persistence.TarifaArchivo;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Configuración de tarifas (administrador) y consulta de tarifas (cobro). Las tarifas de las rutas
 * urbanas y extraurbanas se guardan en el mismo archivo (tarifas.txt), que se lee en cada consulta:
 * en cuanto el administrador guarda un cambio, el siguiente precio que ve un usuario ya es el nuevo.
 */
public class TarifaConfiguracionController {

    private final Supplier<List<Ruta>> rutas;
    private final TarifaArchivo archivo;

    /**
     * @param rutas   de dónde salen las rutas que se pueden configurar (se consulta cada vez)
     * @param archivo archivo donde se guardan las tarifas
     */
    public TarifaConfiguracionController(Supplier<List<Ruta>> rutas, TarifaArchivo archivo) {
        this.rutas = rutas;
        this.archivo = archivo;
    }

    /** Rutas del tipo indicado (todas si es null), sin repetir ids. */
    public List<Ruta> obtenerRutas(TipoRuta tipo) throws ReglaNegocioException {
        List<Ruta> resultado = new ArrayList<>();
        for (Ruta r : rutas.get()) {
            if (tipo != null && tipoDe(r) != tipo) continue;
            boolean repetida = resultado.stream().anyMatch(x -> x.getIdRuta().equalsIgnoreCase(r.getIdRuta()));
            if (!repetida) resultado.add(r);
        }
        if (resultado.isEmpty()) {
            throw new ReglaNegocioException("No hay rutas registradas en el sistema para configurar.");
        }
        return resultado;
    }

    /** Tarifa que el administrador guardó para la ruta, si la hay. */
    public Optional<TarifaRuta> tarifaConfigurada(String idRuta) throws ReglaNegocioException {
        try {
            return archivo.buscar(idRuta);
        } catch (UncheckedIOException e) {
            throw new ReglaNegocioException("No se pudo leer el archivo de tarifas: " + e.getMessage());
        }
    }

    /** Tarifa vigente de la ruta: la configurada o, si todavía no se configuró, la tarifa por defecto. */
    public TarifaRuta tarifaDe(String idRuta) throws ReglaNegocioException {
        return tarifaConfigurada(idRuta).orElseGet(() -> TarifaRuta.porDefecto(idRuta));
    }

    /** Guarda (o reemplaza) la tarifa base y el porcentaje que paga cada rol en la ruta. */
    public TarifaRuta establecerTarifa(String idRuta, double base, double pctEstudiante, double pctEmpleado,
                                       double pctPublico) throws ReglaNegocioException {
        if (idRuta == null || idRuta.isBlank()) {
            throw new ReglaNegocioException("Debe seleccionar una ruta.");
        }
        if (!(base > 0)) {
            throw new ReglaNegocioException("La tarifa base debe ser mayor a 0.");
        }
        try {
            TarifaRuta tarifa = new TarifaRuta(idRuta, base, pctEstudiante, pctEmpleado, pctPublico);
            archivo.guardar(tarifa);
            return tarifa;
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException(e.getMessage());
        } catch (UncheckedIOException e) {
            throw new ReglaNegocioException("No se pudo guardar la tarifa: " + e.getMessage());
        }
    }

    private static TipoRuta tipoDe(Ruta ruta) {
        return ruta instanceof RutaExtraurbana ? TipoRuta.EXTRAURBANA : TipoRuta.URBANA;
    }
}
