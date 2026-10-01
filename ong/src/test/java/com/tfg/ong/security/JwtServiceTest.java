package com.tfg.ong.security;

import com.tfg.ong.model.Usuario;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRETO = "una-clave-de-prueba-de-al-menos-32-caracteres";

    private Usuario usuario(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        return usuario;
    }

    @Test
    void tokenGeneradoSeValidaYDevuelveElIdDelUsuario() {
        JwtService jwtService = new JwtService(SECRETO, 60_000);

        String token = jwtService.generateToken(usuario(7L));

        assertEquals(Optional.of(7L), jwtService.extractUserId(token));
    }

    @Test
    void unTokenInventadoNoEsValido() {
        JwtService jwtService = new JwtService(SECRETO, 60_000);

        assertTrue(jwtService.extractUserId("esto.no.es.un.token").isEmpty());
        assertTrue(jwtService.extractUserId("").isEmpty());
    }

    @Test
    void unTokenCaducadoNoEsValido() {
        JwtService jwtService = new JwtService(SECRETO, -1_000);

        String token = jwtService.generateToken(usuario(7L));

        assertTrue(jwtService.extractUserId(token).isEmpty());
    }

    @Test
    void unTokenFirmadoConOtraClaveNoEsValido() {
        JwtService emisor = new JwtService(SECRETO, 60_000);
        JwtService otro = new JwtService("otra-clave-distinta-de-al-menos-32-caracteres", 60_000);

        String token = emisor.generateToken(usuario(7L));

        assertTrue(otro.extractUserId(token).isEmpty());
    }

    @Test
    void conElMismoSecretoDosInstanciasSeEntienden() {
        // Es lo que permite que las sesiones sobrevivan a un reinicio del backend
        String token = new JwtService(SECRETO, 60_000).generateToken(usuario(3L));

        assertEquals(Optional.of(3L), new JwtService(SECRETO, 60_000).extractUserId(token));
    }

    @Test
    void sinSecretoSeUsaUnaClaveAleatoriaQueFunciona() {
        JwtService jwtService = new JwtService("", 60_000);

        String token = jwtService.generateToken(usuario(9L));

        assertEquals(Optional.of(9L), jwtService.extractUserId(token));
    }

    @Test
    void unSecretoDemasiadoCortoSeRechaza() {
        assertThrows(IllegalStateException.class, () -> new JwtService("corto", 60_000));
    }
}
