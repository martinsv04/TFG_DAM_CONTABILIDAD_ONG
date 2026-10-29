package com.tfg.ong.exception;

import org.springframework.security.access.AccessDeniedException;

/**
 * El plan de la ONG no permite la operación (límite alcanzado, función del plan Pro, ONG en solo lectura).
 * Extiende AccessDeniedException para que los controladores que ya relanzan esa excepción la dejen pasar;
 * GlobalExceptionHandler la convierte en un 402 (pago requerido).
 */
public class PlanException extends AccessDeniedException {

    public PlanException(String mensaje) {
        super(mensaje);
    }
}
