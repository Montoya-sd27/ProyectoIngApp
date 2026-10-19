package com.transporte.repository;

import com.transporte.model.Autobus;
import com.transporte.model.Conductor;
import com.transporte.model.EstadoRecorrido;
import com.transporte.model.FlotaModel;
import com.transporte.model.Horario;
import com.transporte.model.Itinerario;
import com.transporte.model.ReglaNegocioException;
import com.transporte.model.Ruta;
import com.transporte.model.RutaExtraurbana;
import com.transporte.model.RutaFactory;
import com.transporte.model.RutaFactory.TipoRuta;
import com.transporte.persistence.ArchivoLineas;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de itinerarios respaldado por un archivo de texto: al iniciar lee el archivo y
 * después de cada cambio lo vuelve a escribir. Una línea por itinerario, con la ruta, el
 * chofer, la unidad y el horario en la misma línea:
 *
 * RUTA|id|tipoRuta|idRuta|origen|destino|detalle|cedulaChofer|nombreChofer|licenciaChofer|
 *      placa|modeloUnidad|capacidadUnidad|estadoUnidad|dia|horaSalida|horaLlegada|estado
 *
 * (en el archivo van todos los campos seguidos, separados por "|").
 */
public final class ItinerarioRepositorioArchivo implements ItinerarioRepositorio {

    private static final String TIPO = "RUTA";
    private static final int CAMPOS = 18;

    private final Path archivo;
    private final FlotaModel flota;
    private final ItinerarioRepositorioMemoria memoria = ItinerarioRepositorioMemoria.crearAislado();
    private int contador = 0;

    /**
     * @param archivo archivo donde se guardan los itinerarios
     * @param flota   flota ya cargada: las unidades de cada itinerario se enlazan con ella por placa
     */
    public ItinerarioRepositorioArchivo(Path archivo, FlotaModel flota) {
        this.archivo = archivo;
        this.flota = flota;
        cargar();
    }

    public Path getArchivo() { return archivo; }

    @Override
    public synchronized String siguienteId() {
        contador++;
        return String.format("IT-%03d", contador);
    }

    @Override
    public void guardar(Itinerario itinerario) {
        memoria.guardar(itinerario);
        persistir();
    }

    @Override
    public void eliminar(String id) {
        memoria.eliminar(id);
        persistir();
    }

    @Override
    public Optional<Itinerario> buscarPorId(String id) {
        return memoria.buscarPorId(id);
    }

    @Override
    public List<Itinerario> listar() {
        return memoria.listar();
    }

    @Override
    public void agregarObservador(ItinerarioObserver observer) {
        memoria.agregarObservador(observer);
    }

    /** Reescribe el archivo con el estado actual (también se usa cuando cambia una unidad de la flota). */
    public void persistir() {
        List<String> lineas = new ArrayList<>();
        for (Itinerario it : memoria.listar()) {
            lineas.add(formatear(it));
        }
        try {
            ArchivoLineas.escribir(archivo, lineas);
        } catch (UncheckedIOException e) {
            System.err.println("[Persistencia] No se pudieron guardar los itinerarios: " + e.getMessage());
        }
    }

    // ---------- Lectura ----------

    private void cargar() {
        List<String> lineas = ArchivoLineas.leer(archivo);
        for (int i = 0; i < lineas.size(); i++) {
            List<String> c = ArchivoLineas.dividir(lineas.get(i));
            if ((c.size() != CAMPOS && c.size() != CAMPOS + 1) || !TIPO.equals(c.get(0))) {
                ArchivoLineas.advertirLineaInvalida(archivo, i + 1, "formato incorrecto");
                continue;
            }
            try {
                Itinerario it = interpretar(c);
                memoria.guardar(it);
                actualizarContador(it.getIdItinerario());
            } catch (RuntimeException | ReglaNegocioException e) {
                ArchivoLineas.advertirLineaInvalida(archivo, i + 1, e.getMessage());
            }
        }
    }

    private Itinerario interpretar(List<String> c) throws ReglaNegocioException {
        TipoRuta tipo = TipoRuta.valueOf(c.get(2));
        Ruta ruta = RutaFactory.crear(tipo, c.get(3), c.get(4), c.get(5), c.get(6));
        Conductor conductor = new Conductor(c.get(7), c.get(8), c.get(9));

        Autobus unidad = buscarUnidad(c.get(10));
        if (unidad == null) {
            // la unidad ya no está en la flota: se conserva tal como quedó registrada en el itinerario
            unidad = new Autobus(c.get(10), c.get(11), Integer.parseInt(c.get(12).trim()), c.get(13));
        }

        Horario horario = new Horario(DayOfWeek.valueOf(c.get(14)),
                LocalTime.parse(c.get(15)), LocalTime.parse(c.get(16)));

        return new Itinerario.Builder()
                .id(c.get(1))
                .ruta(ruta)
                .unidad(unidad)
                .conductor(conductor)
                .horario(horario)
                .estado(EstadoRecorrido.valueOf(c.get(17)))
                .cuposReservados(c.size() > CAMPOS ? Integer.parseInt(c.get(CAMPOS).trim()) : 0)
                .build();
    }

    private Autobus buscarUnidad(String placa) {
        for (Autobus b : flota.getListaAutobuses()) {
            if (b.getPlaca().equalsIgnoreCase(placa)) return b;
        }
        return null;
    }

    /** Evita repetir identificadores: el siguiente id continúa después del mayor que ya existe. */
    private void actualizarContador(String id) {
        if (id != null && id.startsWith("IT-")) {
            try {
                contador = Math.max(contador, Integer.parseInt(id.substring(3)));
            } catch (NumberFormatException ignorado) {
                // identificador con otro formato: no afecta al contador
            }
        }
    }

    // ---------- Escritura ----------

    private static String formatear(Itinerario it) {
        Ruta r = it.getRuta();
        Conductor c = it.getConductor();
        Autobus u = it.getUnidad();
        Horario h = it.getHorario();
        TipoRuta tipo = r instanceof RutaExtraurbana ? TipoRuta.EXTRAURBANA : TipoRuta.URBANA;
        String[] base = {TIPO, it.getIdItinerario(), tipo.name(),
                r.getIdRuta(), r.getOrigen(), r.getDestino(), r.getDetalle(),
                c.getCedula(), c.getNombre(), c.getLicencia(),
                u.getPlaca(), u.getModelo(), String.valueOf(u.getCapacidad()), u.getEstado(),
                h.getDia().name(), h.getSalida().toString(), h.getLlegada().toString(),
                it.getEstadoRecorrido().name()};
        if (it.getCuposReservados() == 0) return ArchivoLineas.unir(base);
        String[] conReservas = java.util.Arrays.copyOf(base, base.length + 1);
        conReservas[base.length] = String.valueOf(it.getCuposReservados());
        return ArchivoLineas.unir(conReservas);
    }
}
