package com.tfg.ong.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NifTest {

    @Test
    void normalizaEspaciosGuionesYMayusculas() {
        assertEquals("12345678Z", Nif.normalizar(" 12345678-z "));
        assertNull(Nif.normalizar("   "));
        assertNull(Nif.normalizar(null));
    }

    @Test
    void normalizaCaracteresInvisiblesPuntosYGuionesTipograficos() {
        assertEquals("G12345678", Nif.normalizar("G\u00a012345678"));      // espacio de no separación (al copiar y pegar)
        assertEquals("G12345678", Nif.normalizar("G\u200b12345678"));      // espacio de ancho cero
        assertEquals("G12345678", Nif.normalizar("G-1234567\u20138"));     // guion largo
        assertEquals("G12345678", Nif.normalizar("G.1234567.8"));
    }

    @Test
    void aceptaDniNieYCif() {
        assertTrue(Nif.esValido("12345678Z"));
        assertTrue(Nif.esValido("X1234567L"));
        assertTrue(Nif.esValido("G12345678"));
        assertTrue(Nif.esValido("Q2826000H"));
    }

    @Test
    void rechazaFormatosIncorrectos() {
        assertFalse(Nif.esValido("1234"));
        assertFalse(Nif.esValido("ABCDEFGHI"));
        assertFalse(Nif.esValido("123456789"));
        assertFalse(Nif.esValido(null));
    }
}
