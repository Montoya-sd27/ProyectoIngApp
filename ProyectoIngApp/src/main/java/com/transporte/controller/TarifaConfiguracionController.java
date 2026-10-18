package com.transporte.controller;

import com.transporte.model.ReglaNegocioException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TarifaConfiguracionController {

    private String getRutaArchivo() {
        String folder = System.getProperty("ruta.datos", "datos");
        return folder + File.separator + "rutas.txt";
    }

    public List<String[]> obtenerRutasExtraurbanas() throws ReglaNegocioException {
        File archivo = new File(getRutaArchivo());

        if (!archivo.exists()) {
            try {
                if (archivo.getParentFile() != null) {
                    archivo.getParentFile().mkdirs();
                }
                archivo.createNewFile();
            } catch (IOException e) {
                throw new ReglaNegocioException("No se pudo inicializar la estructura de datos.");
            }
        }

        List<String[]> extraurbanas = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] partes = linea.split("\\|");
                if (partes.length >= 3 && "EXTRAURBANA".equalsIgnoreCase(partes[2].trim())) {
                    extraurbanas.add(partes);
                }
            }
        } catch (IOException e) {
            throw new ReglaNegocioException("Error al leer el archivo de rutas.");
        }

        if (extraurbanas.isEmpty()) {
            throw new ReglaNegocioException("No hay rutas extraurbanas registradas en el sistema para configurar.");
        }

        return extraurbanas;
    }

    public void actualizarTarifaBaseRuta(String idRuta, double nuevaTarifa) throws ReglaNegocioException {
        if (nuevaTarifa <= 0) {
            throw new ReglaNegocioException("La tarifa base debe ser mayor a 0.");
        }

        File archivo = new File(getRutaArchivo());
        if (!archivo.exists()) {
            throw new ReglaNegocioException("No se encontró el archivo de rutas.");
        }

        List<String> lineasActualizadas = new ArrayList<>();
        boolean encontrada = false;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] partes = linea.split("\\|");
                if (partes.length >= 4 && partes[3].trim().equals(idRuta)) {
                    // Busca el campo que contiene el monto numérico y lo actualiza
                    for (int i = partes.length - 1; i >= 0; i--) {
                        try {
                            Double.parseDouble(partes[i].trim());
                            partes[i] = String.format("%.2f", nuevaTarifa).replace(",", ".");
                            break;
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    lineasActualizadas.add(String.join("|", partes));
                    encontrada = true;
                } else {
                    lineasActualizadas.add(linea);
                }
            }
        } catch (IOException e) {
            throw new ReglaNegocioException("Error al procesar los datos de las rutas.");
        }

        if (!encontrada) {
            throw new ReglaNegocioException("No se encontró la ruta seleccionada: " + idRuta);
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(archivo))) {
            for (String l : lineasActualizadas) {
                pw.println(l);
            }
        } catch (IOException e) {
            throw new ReglaNegocioException("Error al guardar los cambios en el archivo.");
        }
    }
}