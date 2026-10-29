package com.tfg.ong.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Subvención con su ejecución: cobrado = ingresos asignados a la subvención; gastado = gastos del proyecto que financia.
 * porcentajeEjecutado = gastado / concedido (null si todavía no hay importe concedido).
 */
public record SubvencionDTO(Long id, String nombre, String financiador, String expediente, String estado,
                            BigDecimal importeSolicitado, BigDecimal importeConcedido, BigDecimal cobrado,
                            Long proyectoId, String proyectoNombre, BigDecimal gastado, BigDecimal porcentajeEjecutado,
                            LocalDate fechaSolicitud, LocalDate fechaConcesion, LocalDate fechaJustificacion,
                            Long diasParaJustificar, boolean justificacionVencida) {
}
