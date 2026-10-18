package com.transporte.model;

/** Un pago móvil registrado en el archivo de pagos (lo que "recibió" el banco). */
public final class RegistroPago {

    private final String telefono;
    private final String cedula;
    private final String banco;
    private final String referencia;
    private final float monto;
    private final boolean acreditado;
    private final int indiceLinea;   // posición de la línea en el archivo, para poder marcarla

    public RegistroPago(String telefono, String cedula, String banco, String referencia,
                        float monto, boolean acreditado, int indiceLinea) {
        this.telefono = telefono;
        this.cedula = cedula;
        this.banco = banco;
        this.referencia = referencia;
        this.monto = monto;
        this.acreditado = acreditado;
        this.indiceLinea = indiceLinea;
    }

    /** true si los datos reportados por el usuario son los de este pago (el banco no distingue mayúsculas). */
    public boolean coincideCon(String telefono, String cedula, String banco, String referencia) {
        return this.telefono.equals(telefono)
                && this.cedula.equals(cedula)
                && this.banco.equalsIgnoreCase(banco)
                && this.referencia.equals(referencia);
    }

    public String getTelefono() { return telefono; }
    public String getCedula() { return cedula; }
    public String getBanco() { return banco; }
    public String getReferencia() { return referencia; }
    public float getMonto() { return monto; }
    public boolean isAcreditado() { return acreditado; }
    public int getIndiceLinea() { return indiceLinea; }
}
