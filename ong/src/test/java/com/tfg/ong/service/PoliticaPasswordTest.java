package com.tfg.ong.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PoliticaPasswordTest {

    @Test
    void aceptaUnaContrasenaValida() {
        assertDoesNotThrow(() -> PoliticaPassword.validar("clave1234"));
    }

    @Test
    void rechazaLasQueNoCumplen() {
        assertThrows(IllegalArgumentException.class, () -> PoliticaPassword.validar(null));
        assertThrows(IllegalArgumentException.class, () -> PoliticaPassword.validar("corta1"));
        assertThrows(IllegalArgumentException.class, () -> PoliticaPassword.validar("sololetras"));
        assertThrows(IllegalArgumentException.class, () -> PoliticaPassword.validar("12345678"));
        assertThrows(IllegalArgumentException.class, () -> PoliticaPassword.validar("a1".repeat(40)));
    }
}
