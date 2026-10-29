package com.tfg.ong.dto;

import java.math.BigDecimal;

/** Resumen de lo donado por una persona en un año, para la pantalla de certificados. */
public record DonanteResumenDTO(
        Long usuarioId,
        String nombre,
        String email,
        String nifCif,
        String direccion,
        String codigoPostal,
        BigDecimal totalAnio,
        int numDonaciones,
        boolean datosCompletos) {
}
