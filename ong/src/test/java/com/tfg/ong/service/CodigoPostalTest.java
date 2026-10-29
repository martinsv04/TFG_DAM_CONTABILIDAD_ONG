package com.tfg.ong.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodigoPostalTest {

    @Test
    void validaCincoCifrasConProvinciaReal() {
        assertTrue(CodigoPostal.esValido("41001"));
        assertFalse(CodigoPostal.esValido("00100"));
        assertFalse(CodigoPostal.esValido("53000"));
        assertFalse(CodigoPostal.esValido("4100"));
        assertFalse(CodigoPostal.esValido("41A01"));
        assertFalse(CodigoPostal.esValido(null));
    }

    @Test
    void provinciaYNormalizar() {
        assertEquals("28", CodigoPostal.provincia("28013"));
        assertNull(CodigoPostal.provincia("99999"));
        assertEquals("41001", CodigoPostal.normalizar(" 410 01 "));
        assertNull(CodigoPostal.normalizar("  "));
    }
}
