package com.transporte.controller;

import com.transporte.model.Ticket;
import com.transporte.model.Usuario;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CobroService {
    private static final String ARCHIVO_TICKETS = "datos/tickets.txt";

    // Calcular tarifa según el rol
    public double calcularTarifa(String rol, double tarifaBase) {
        if (rol == null) return tarifaBase;
        
        switch (rol.toUpperCase()) {
            case "ESTUDIANTE":
                return tarifaBase * 0.50; // 50% de descuento
            case "EMPLEADO":
                return tarifaBase; // 100%
            case "CONDUCTOR":
            case "ADMINISTRADOR":
                return 0.0; // Exento
            default:
                return tarifaBase;
        }
    }

    // Procesar el cobro y emitir el ticket
    public Ticket procesarCobro(Usuario usuario, String ruta, double tarifaBase, double saldoActual) throws Exception {
        double tarifaFinal = calcularTarifa(usuario.getRol(), tarifaBase);

        if (saldoActual < tarifaFinal) {
            throw new Exception("Saldo insuficiente. Su tarifa es " + String.format("%.2f", tarifaFinal) + " y su saldo actual es " + String.format("%.2f", saldoActual));
        }

        Ticket nuevoTicket = new Ticket(usuario.getCorreo(), usuario.getRol(), ruta, tarifaFinal);
        guardarTicket(nuevoTicket);
        return nuevoTicket;
    }

    // Persistencia: Guardar ticket en datos/tickets.txt
    private void guardarTicket(Ticket ticket) {
        File carpeta = new File("datos");
        if (!carpeta.exists()) carpeta.mkdirs();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO_TICKETS, true))) {
            bw.write(ticket.aFormatoTexto());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Consultar lista de tickets de un usuario específico
    public List<Ticket> obtenerTicketsPorUsuario(String correoUsuario) {
        List<Ticket> tickets = new ArrayList<>();
        File archivo = new File(ARCHIVO_TICKETS);
        if (!archivo.exists()) return tickets;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("\\|");
                if (partes.length == 6 && partes[1].equalsIgnoreCase(correoUsuario)) {
                    tickets.add(new Ticket(partes[0], partes[1], partes[2], partes[3], Double.parseDouble(partes[4]), partes[5]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tickets;
    }
}