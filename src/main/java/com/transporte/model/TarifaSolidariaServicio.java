package com.transporte.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de Lógica de Negocio para la Consulta de Tarifas Solidarias (THU-002).
 * Implementa el patrón Strategy para la aplicación de descuentos por rol.
 */
public class TarifaSolidariaServicio {

    public interface EstrategiaTarifa {
        double calcular(double tarifaBase);
        String getDescripcion();
    }

    public static class EstrategiaEstudiante implements EstrategiaTarifa {
        @Override
        public double calcular(double tarifaBase) {
            return tarifaBase * 0.20;
        }

        @Override
        public String getDescripcion() {
            return "Descuento Estudiante (20% del costo base)";
        }
    }

    public static class EstrategiaEmpleado implements EstrategiaTarifa {
        @Override
        public double calcular(double tarifaBase) {
            return tarifaBase * 0.50;
        }

        @Override
        public String getDescripcion() {
            return "Descuento Empleado (50% del costo base)";
        }
    }

    public static class EstrategiaPublico implements EstrategiaTarifa {
        @Override
        public double calcular(double tarifaBase) {
            return tarifaBase * 1.00;
        }

        @Override
        public String getDescripcion() {
            return "Tarifa Público General (100% del costo base)";
        }
    }

    private EstrategiaTarifa estrategiaActual = new EstrategiaPublico();

    public EstrategiaTarifa getEstrategiaActual() {
        return estrategiaActual;
    }

    public void setEstrategia(EstrategiaTarifa estrategia) {
        if (estrategia != null) {
            this.estrategiaActual = estrategia;
        }
    }

    public void setEstrategiaPorRol(String rol) {
        if (rol == null) {
            this.estrategiaActual = new EstrategiaPublico();
            return;
        }
        String r = rol.trim().toUpperCase();
        if (r.contains("ESTUDIANTE")) {
            this.estrategiaActual = new EstrategiaEstudiante();
        } else if (r.contains("EMPLEADO")) {
            this.estrategiaActual = new EstrategiaEmpleado();
        } else {
            this.estrategiaActual = new EstrategiaPublico();
        }
    }

    public double calcular(double tarifaBase) {
        if (tarifaBase < 0) {
            throw new IllegalArgumentException("La tarifa base no puede ser negativa.");
        }
        return estrategiaActual.calcular(tarifaBase);
    }

    public String getDescripcion() {
        return estrategiaActual.getDescripcion();
    }

    public double calcularTarifa(double tarifaBase, EstrategiaTarifa estrategia) {
        if (estrategia == null) return tarifaBase;
        return estrategia.calcular(tarifaBase);
    }

    private String getRutaArchivo() {
        String folder = System.getProperty("ruta.datos", "datos");
        return folder + File.separator + "rutas.txt";
    }

    public List<String[]> obtenerRutas() throws ReglaNegocioException {
        File archivo = new File(getRutaArchivo());

        if (!archivo.exists()) {
            try {
                if (archivo.getParentFile() != null) {
                    archivo.getParentFile().mkdirs();
                }
                archivo.createNewFile();
            } catch (IOException e) {
                throw new ReglaNegocioException("No se pudo acceder a la estructura de datos de rutas.");
            }
        }

        List<String[]> rutas = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] partes = linea.split("\\|");
                if (partes.length >= 4) {
                    rutas.add(partes);
                }
            }
        } catch (IOException e) {
            throw new ReglaNegocioException("Error al leer el archivo de rutas.");
        }

        if (rutas.isEmpty()) {
            throw new ReglaNegocioException("No hay rutas registradas en el sistema.");
        }

        return rutas;
    }

    public double extraerTarifaBase(String[] partes) {
        if (partes == null || partes.length == 0) return 0.0;

        for (int i = partes.length - 1; i >= 0; i--) {
            try {
                return Double.parseDouble(partes[i].trim());
            } catch (NumberFormatException ignored) {
                // Omite textos no numéricos como 'PROGRAMADO'
            }
        }
        return 0.0;
    }
}
