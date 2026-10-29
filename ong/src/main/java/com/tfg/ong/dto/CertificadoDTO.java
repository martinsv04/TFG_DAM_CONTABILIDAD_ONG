package com.tfg.ong.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Datos de un certificado de donación (individual o anual), listos para dibujarlo en el PDF.
 * tipo: "DONACION" (una sola donación) o "ANUAL" (todas las donaciones de un año de un donante).
 */
public record CertificadoDTO(
        String numero,
        String tipo,
        int anio,
        LocalDate fechaEmision,
        EntidadDTO entidad,
        DonanteDTO donante,
        BigDecimal importeTotal,
        List<LineaDTO> donaciones) {

    public record EntidadDTO(String nombre, String nif, String direccion, String representante, String logo, String firma) {
    }

    public record DonanteDTO(String nombre, String nif, String direccion) {
    }

    public record LineaDTO(LocalDate fecha, BigDecimal importe, String descripcion) {
    }
}
