package com.transporte.controller;

import com.transporte.model.CargaDatosLogin;
import com.transporte.model.RegistroPago;
import com.transporte.model.ReglaNegocioException;
import com.transporte.model.UsuarioConSaldo;
import com.transporte.persistence.PagoMovilArchivo;
import java.io.UncheckedIOException;
import java.util.List;

/** Recarga de saldo por pago móvil: valida el pago reportado contra el archivo de pagos y suma el monto al saldo. */
public class RecargaController {

    private final PagoMovilArchivo pagos;

    public RecargaController(PagoMovilArchivo pagos) {
        this.pagos = pagos;
    }

    /**
     * Valida los datos reportados. Si existe un pago pendiente que coincide, lo marca como acreditado
     * y suma su monto al saldo del usuario.
     *
     * @return el monto acreditado en Bs
     * @throws ReglaNegocioException si algún dato es inválido, el pago no existe o ya fue acreditado
     */
    public float reportarPago(UsuarioConSaldo usuario, String telefono, String cedula, String banco, String referencia)
            throws ReglaNegocioException {
        telefono = telefono == null ? "" : telefono.trim();
        cedula = cedula == null ? "" : cedula.trim();
        referencia = referencia == null ? "" : referencia.trim();

        if (!telefono.matches("\\d{11}")) {
            throw new ReglaNegocioException("El número de teléfono debe tener 11 dígitos.");
        }
        if (!cedula.matches("\\d{6,9}")) {
            throw new ReglaNegocioException("La cédula debe tener entre 6 y 9 dígitos.");
        }
        if (banco == null || banco.isBlank()) {
            throw new ReglaNegocioException("Seleccione el banco desde el que realizó el pago.");
        }
        if (!referencia.matches("\\d{4}")) {
            throw new ReglaNegocioException("Debe indicar los últimos 4 dígitos de la referencia.");
        }

        try {
            List<RegistroPago> registros = pagos.cargar();
            RegistroPago encontrado = null;
            boolean yaAcreditado = false;
            for (RegistroPago r : registros) {
                if (!r.coincideCon(telefono, cedula, banco, referencia)) continue;
                if (r.isAcreditado()) {
                    yaAcreditado = true;
                } else {
                    encontrado = r;
                    break;
                }
            }
            if (encontrado == null) {
                throw new ReglaNegocioException(yaAcreditado
                        ? "Este pago ya fue acreditado anteriormente."
                        : "No se encontró ningún pago con esos datos. Verifique la información e intente de nuevo.");
            }

            // primero se marca el pago como usado para que no se pueda acreditar dos veces
            pagos.marcarAcreditado(encontrado);
            usuario.recargar(encontrado.getMonto());
            CargaDatosLogin.notificarCambio();
            return encontrado.getMonto();
        } catch (UncheckedIOException | IllegalStateException e) {
            throw new ReglaNegocioException("No se pudo validar el pago en este momento: " + e.getMessage());
        }
    }
}
