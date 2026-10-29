package com.tfg.ong.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Datos del informe de transparencia de una ONG en un año: de dónde viene el dinero (público/privado, por
 * financiador y por tipo) y a qué se destina (misión, administración, captación de fondos).
 */
public record TransparenciaDTO(
        int anio,
        List<Integer> anios,
        BigDecimal ingresos,
        BigDecimal gastos,
        BigDecimal resultado,
        List<Parte> financiacion,
        List<Parte> financiadores,
        List<Parte> ingresosPorTipo,
        List<Parte> gastosPorArea,
        List<Parte> gastosPorCategoria,
        BigDecimal mayorFinanciadorPorcentaje,
        String mayorFinanciador,
        int gastosSinClasificar,
        int ingresosSinClasificar,
        boolean publicada) {

    /** Una porción de un total: clave interna, nombre para mostrar, importe y porcentaje (1 decimal). */
    public record Parte(String clave, String nombre, BigDecimal importe, BigDecimal porcentaje) {
    }
}
