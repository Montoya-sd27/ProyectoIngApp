package com.transporte.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Ticket {
    private String idTicket;
    private String correoUsuario;
    private String rolUsuario;
    private String ruta;
    private double montoCobrado;
    private String fechaHora;

    // Constructor para un nuevo ticket (genera ID y fecha actual)
    public Ticket(String correoUsuario, String rolUsuario, String ruta, double montoCobrado) {
        this.idTicket = "TICK-" + System.currentTimeMillis() % 100000;
        this.correoUsuario = correoUsuario;
        this.rolUsuario = rolUsuario;
        this.ruta = ruta;
        this.montoCobrado = montoCobrado;
        this.fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    // Constructor para cargar tickets guardados
    public Ticket(String idTicket, String correoUsuario, String rolUsuario, String ruta, double montoCobrado, String fechaHora) {
        this.idTicket = idTicket;
        this.correoUsuario = correoUsuario;
        this.rolUsuario = rolUsuario;
        this.ruta = ruta;
        this.montoCobrado = montoCobrado;
        this.fechaHora = fechaHora;
    }

    public String getIdTicket() { return idTicket; }
    public String getCorreoUsuario() { return correoUsuario; }
    public String getRolUsuario() { return rolUsuario; }
    public String getRuta() { return ruta; }
    public double getMontoCobrado() { return montoCobrado; }
    public String getFechaHora() { return fechaHora; }

    // Formato para guardar en archivo (datos/tickets.txt)
    public String aFormatoTexto() {
        return idTicket + "|" + correoUsuario + "|" + rolUsuario + "|" + ruta + "|" + montoCobrado + "|" + fechaHora;
    }
}