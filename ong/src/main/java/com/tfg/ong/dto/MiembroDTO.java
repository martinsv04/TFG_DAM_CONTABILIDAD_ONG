package com.tfg.ong.dto;

import com.tfg.ong.model.Rol;

/**
 * Datos de un miembro de una ONG que se envían al frontend.
 * Deliberadamente sin email, teléfono, NIF/CIF ni contraseña.
 */
public record MiembroDTO(Long id, String nombre, Rol rol) {
}
