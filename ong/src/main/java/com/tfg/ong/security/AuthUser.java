package com.tfg.ong.security;

import com.tfg.ong.model.Rol;

/**
 * Datos mínimos del usuario autenticado que se guardan en el contexto de seguridad.
 * Se obtiene en los controladores con @AuthenticationPrincipal.
 * ongId es la ONG a la que pertenece el usuario (puede ser null, por ejemplo en un ADMIN sin ONG propia asignada).
 */
public record AuthUser(Long id, String email, Rol rol, Long ongId) {
}
