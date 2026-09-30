package model;

public class Itinerario {
    private String idItinerario;
    private Rutas ruta;
    private Unidad unidad;
    private Conductor conductor;
    private String horarios;
    private String estadoRecorrido;

    public Itinerario(String idItinerario, Rutas ruta, Unidad unidad, Conductor conductor, String horarios, String estadoRecorrido) {
        this.idItinerario = idItinerario;
        this.ruta = ruta;
        this.unidad = unidad;
        this.conductor = conductor;
        this.horarios = horarios;
        this.estadoRecorrido = estadoRecorrido;
    }

    public String getIdItinerario() { return idItinerario; }
    public Rutas getRuta() { return ruta; }
    public Unidad getUnidad() { return unidad; }
    public Conductor getConductor() { return conductor; }
    public String getHorarios() { return horarios; }
    public String getEstadoRecorrido() { return estadoRecorrido; }
}