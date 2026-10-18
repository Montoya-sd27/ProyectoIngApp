package com.transporte.model;

/**
 * Tarifa de una ruta: el precio base en Bs y el porcentaje de ese precio que paga cada rol.
 * Ejemplo: base 100 y estudiante 50% -> el estudiante paga Bs. 50,00.
 */
public final class TarifaRuta {

    /** Valores que se usan mientras el administrador no haya configurado la ruta. */
    public static final double BASE_POR_DEFECTO = 10.0;
    public static final double PCT_ESTUDIANTE_POR_DEFECTO = 50.0;
    public static final double PCT_EMPLEADO_POR_DEFECTO = 100.0;
    public static final double PCT_PUBLICO_POR_DEFECTO = 100.0;

    private final String idRuta;
    private final double base;
    private final double pctEstudiante;
    private final double pctEmpleado;
    private final double pctPublico;

    public TarifaRuta(String idRuta, double base, double pctEstudiante, double pctEmpleado, double pctPublico) {
        if (!(base >= 0)) throw new IllegalArgumentException("La tarifa base no puede ser negativa.");
        validarPorcentaje(pctEstudiante, "Estudiante");
        validarPorcentaje(pctEmpleado, "Empleado");
        validarPorcentaje(pctPublico, "Público");
        this.idRuta = idRuta;
        this.base = base;
        this.pctEstudiante = pctEstudiante;
        this.pctEmpleado = pctEmpleado;
        this.pctPublico = pctPublico;
    }

    /** Tarifa que se aplica a una ruta que todavía no fue configurada. */
    public static TarifaRuta porDefecto(String idRuta) {
        return new TarifaRuta(idRuta, BASE_POR_DEFECTO, PCT_ESTUDIANTE_POR_DEFECTO,
                PCT_EMPLEADO_POR_DEFECTO, PCT_PUBLICO_POR_DEFECTO);
    }

    private static void validarPorcentaje(double pct, String perfil) {
        if (!(pct >= 0 && pct <= 100)) {
            throw new IllegalArgumentException("El porcentaje para " + perfil + " debe estar entre 0 y 100.");
        }
    }

    public String getIdRuta() { return idRuta; }
    public double getBase() { return base; }
    public double getPctEstudiante() { return pctEstudiante; }
    public double getPctEmpleado() { return pctEmpleado; }
    public double getPctPublico() { return pctPublico; }

    /** Porcentaje del precio base que paga el rol indicado (conductor y administrador no pagan). */
    public double porcentajePara(String rol) {
        if (rol == null) return pctPublico;
        switch (rol.trim().toUpperCase()) {
            case "ESTUDIANTE":
                return pctEstudiante;
            case "EMPLEADO":
                return pctEmpleado;
            case "CONDUCTOR":
            case "ADMINISTRADOR":
                return 0.0;
            default:
                return pctPublico;
        }
    }

    /** Precio final en Bs que paga el rol indicado, redondeado a centésimas. */
    public double precioPara(String rol) {
        return Math.round(base * porcentajePara(rol)) / 100.0;
    }
}
