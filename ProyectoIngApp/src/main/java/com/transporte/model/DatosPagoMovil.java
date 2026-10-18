package com.transporte.model;

/** Datos a los que se debe hacer el pago móvil y bancos desde los que el usuario puede pagar. */
public final class DatosPagoMovil {

    public static final String RIF = "J-01234567891";
    public static final String TELEFONO = "04241934744";
    public static final String BANCO = "Banco Central de Venezuela (0102)";

    // bancos que se muestran al reportar el pago (simulan los bancos del usuario)
    private static final String[] BANCOS_EMISORES = {"Banco de Venezuela", "Provincial", "Banesco"};

    private DatosPagoMovil() { }

    public static String[] bancosEmisores() {
        return BANCOS_EMISORES.clone();
    }
}
