package com.tfg.ong;

import com.tfg.ong.model.Rol;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Simula un usuario autenticado en los tests. El principal es un AuthUser, igual que en la aplicación real,
 * para que @AuthenticationPrincipal AuthUser funcione en los controladores.
 * Por defecto: ADMIN con id 1 perteneciente a la ONG 1. Se puede poner en la clase o en un método.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
@WithSecurityContext(factory = WithMockAuthUserFactory.class)
public @interface WithMockAuthUser {

    long id() default 1L;

    String email() default "admin@example.com";

    Rol rol() default Rol.ADMIN;

    /** Id de la ONG del usuario; 0 o menos significa "sin ONG". */
    long ongId() default 1L;
}
