package com.transporte.persistence;

import com.transporte.model.RegistroPago;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Archivo con los pagos móviles que "recibió el banco". Una línea por pago:
 * PAGO|telefono|cedula|banco|ultimos4Referencia|monto|estado   (estado: PENDIENTE o ACREDITADO)
 */
public final class PagoMovilArchivo {

    private static final String TIPO = "PAGO";
    private static final int CAMPOS = 7;
    private static final String PENDIENTE = "PENDIENTE";
    private static final String ACREDITADO = "ACREDITADO";
    // pagos de ejemplo que vienen dentro de la aplicación y se copian la primera vez
    private static final String RECURSO_EJEMPLO = "/pagos_movil.txt";

    private final Path archivo;

    public PagoMovilArchivo(Path archivo) {
        this.archivo = archivo;
    }

    public Path getArchivo() { return archivo; }

    /** Si el archivo todavía no existe lo crea con los pagos de ejemplo. */
    public void crearSiNoExiste() {
        if (Files.exists(archivo)) return;
        try (InputStream ejemplo = PagoMovilArchivo.class.getResourceAsStream(RECURSO_EJEMPLO)) {
            Path carpeta = archivo.toAbsolutePath().getParent();
            if (carpeta != null) Files.createDirectories(carpeta);
            if (ejemplo != null) {
                Files.copy(ejemplo, archivo);
            } else {
                ArchivoLineas.escribir(archivo, new ArrayList<>());
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear " + archivo, e);
        }
    }

    public List<RegistroPago> cargar() {
        List<RegistroPago> pagos = new ArrayList<>();
        List<String> lineas = ArchivoLineas.leer(archivo);
        for (int i = 0; i < lineas.size(); i++) {
            List<String> c = ArchivoLineas.dividir(lineas.get(i));
            if (c.size() != CAMPOS || !TIPO.equals(c.get(0))) {
                ArchivoLineas.advertirLineaInvalida(archivo, i + 1, "formato incorrecto");
                continue;
            }
            String estado = c.get(6).trim().toUpperCase();
            if (!estado.equals(PENDIENTE) && !estado.equals(ACREDITADO)) {
                ArchivoLineas.advertirLineaInvalida(archivo, i + 1, "estado desconocido: " + c.get(6));
                continue;
            }
            try {
                float monto = Float.parseFloat(c.get(5).trim().replace(',', '.'));
                if (!(monto > 0)) throw new NumberFormatException("monto no positivo");
                pagos.add(new RegistroPago(c.get(1).trim(), c.get(2).trim(), c.get(3).trim(), c.get(4).trim(),
                        monto, estado.equals(ACREDITADO), i));
            } catch (NumberFormatException e) {
                ArchivoLineas.advertirLineaInvalida(archivo, i + 1, "monto inválido: " + c.get(5));
            }
        }
        return pagos;
    }

    /** Cambia solo la línea de ese pago a ACREDITADO; las demás líneas del archivo quedan como estaban. */
    public void marcarAcreditado(RegistroPago pago) {
        List<String> lineas = ArchivoLineas.leer(archivo);
        int i = pago.getIndiceLinea();
        if (i < 0 || i >= lineas.size()) {
            throw new IllegalStateException("El archivo de pagos cambió; intente de nuevo.");
        }
        lineas.set(i, ArchivoLineas.unir(TIPO, pago.getTelefono(), pago.getCedula(), pago.getBanco(),
                pago.getReferencia(), Float.toString(pago.getMonto()), ACREDITADO));
        ArchivoLineas.escribir(archivo, lineas);
    }
}
