package com.transporte.controller;

import com.transporte.model.CargaDatosLogin;
import com.transporte.model.ReglaNegocioException;
import com.transporte.model.TarifaRuta;
import com.transporte.model.Ticket;
import com.transporte.model.Usuario;
import com.transporte.model.UsuarioConSaldo;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CobroService {
    private static final String ARCHIVO_TICKETS = "tickets.txt";

    /** Carpeta de datos (la misma que usa el resto de la persistencia). */
    private static File archivoTickets() {
        return new File(System.getProperty("ruta.datos", "datos"), ARCHIVO_TICKETS);
    }

    /** Precio que paga el rol según la tarifa de la ruta (porcentaje configurado por el administrador). */
    public double calcularTarifa(String rol, TarifaRuta tarifa) {
        return tarifa.precioPara(rol);
    }

    /** Igual que {@link #calcularTarifa(String, TarifaRuta)} con los porcentajes por defecto (estudiante 50%, empleado 100%). */
    public double calcularTarifa(String rol, double tarifaBase) {
        return calcularTarifa(rol, new TarifaRuta("", tarifaBase,
                TarifaRuta.PCT_ESTUDIANTE_POR_DEFECTO, TarifaRuta.PCT_EMPLEADO_POR_DEFECTO,
                TarifaRuta.PCT_PUBLICO_POR_DEFECTO));
    }

    /**
     * Paga un cupo: valida el saldo, lo descuenta, guarda el saldo nuevo y emite el ticket digital.
     * Solo se llama cuando el usuario confirma con "Pagar cupo".
     */
    public Ticket pagarCupo(UsuarioConSaldo usuario, String ruta, TarifaRuta tarifa) throws ReglaNegocioException {
        double precio = calcularTarifa(usuario.getRol(), tarifa);
        if (usuario.getSaldo() < precio) {
            throw new ReglaNegocioException(String.format(Locale.US,
                    "Saldo insuficiente. El cupo cuesta Bs. %.2f y su saldo es Bs. %.2f.", precio, usuario.getSaldo()));
        }
        Ticket ticket = new Ticket(usuario.getCorreo(), usuario.getRol(), ruta, precio);
        usuario.descontar((float) precio);
        CargaDatosLogin.notificarCambio();   // guarda el saldo nuevo en usuarios.txt
        guardarTicket(ticket);
        return ticket;
    }

    // Procesar el cobro y emitir el ticket (sin tocar el saldo del usuario)
    public Ticket procesarCobro(Usuario usuario, String ruta, double tarifaBase, double saldoActual) throws Exception {
        double tarifaFinal = calcularTarifa(usuario.getRol(), tarifaBase);

        if (saldoActual < tarifaFinal) {
            throw new Exception("Saldo insuficiente. Su tarifa es " + String.format("%.2f", tarifaFinal) + " y su saldo actual es " + String.format("%.2f", saldoActual));
        }

        Ticket nuevoTicket = new Ticket(usuario.getCorreo(), usuario.getRol(), ruta, tarifaFinal);
        guardarTicket(nuevoTicket);
        return nuevoTicket;
    }

    // Persistencia: Guardar ticket en datos/tickets.txt (UTF-8)
    private void guardarTicket(Ticket ticket) {
        File archivo = archivoTickets();
        if (archivo.getParentFile() != null) archivo.getParentFile().mkdirs();

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(archivo, true), StandardCharsets.UTF_8))) {
            bw.write(ticket.aFormatoTexto());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Consultar lista de tickets de un usuario específico (más recientes al final)
    public List<Ticket> obtenerTicketsPorUsuario(String correoUsuario) {
        List<Ticket> tickets = new ArrayList<>();
        File archivo = archivoTickets();
        if (!archivo.exists()) return tickets;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(archivo), StandardCharsets.UTF_8))) {
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
