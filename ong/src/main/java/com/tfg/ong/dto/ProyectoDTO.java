package com.tfg.ong.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Proyecto con su ejecución: lo gastado frente al presupuesto y lo ingresado. */
public record ProyectoDTO(Long id, String nombre, String descripcion, String estado, LocalDate fechaInicio,
                          LocalDate fechaFin, BigDecimal presupuesto, BigDecimal gastado, BigDecimal ingresado,
                          BigDecimal resultado, BigDecimal porcentajeEjecutado, boolean superaPresupuesto,
                          int numGastos, int numIngresos) {
}
