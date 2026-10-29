package com.tfg.ong.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Ingreso o gasto de un proyecto (concepto = tipo del ingreso o categoría del gasto). */
public record MovimientoDTO(Long id, LocalDate fecha, String descripcion, String concepto, BigDecimal monto) {
}
