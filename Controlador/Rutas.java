package model;

public class Rutas {
    private String idRuta;
    private String origen;
    private String destino;

    public Rutas(String idRuta, String origen, String destino) {
        this.idRuta = idRuta;
        this.origen = origen;
        this.destino = destino;
    }

    public String getIdRuta() { return idRuta; }
    public String getOrigen() { return origen; }
    public String getDestino() { return destino; }

    @Override
    public String toString() {
        return idRuta + ": " + origen + " - " + destino;
    }}