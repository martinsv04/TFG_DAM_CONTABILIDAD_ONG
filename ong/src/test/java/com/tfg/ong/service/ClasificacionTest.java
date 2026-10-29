package com.tfg.ong.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ClasificacionTest {

    @Test
    public void origenAceptaPublicoYPrivadoSinImportarMayusculas() {
        assertEquals("PUBLICO", Clasificacion.origen(" publico "));
        assertEquals("PRIVADO", Clasificacion.origen("PRIVADO"));
    }

    @Test
    public void origenVacioSignificaSinIndicar() {
        assertNull(Clasificacion.origen(null));
        assertNull(Clasificacion.origen("  "));
    }

    @Test
    public void origenInvalidoSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> Clasificacion.origen("MIXTO"));
    }

    @Test
    public void financiadorSeRecortaYLimita() {
        assertEquals("Ayuntamiento de Madrid", Clasificacion.financiador("  Ayuntamiento de Madrid "));
        assertNull(Clasificacion.financiador(" "));
        assertThrows(IllegalArgumentException.class, () -> Clasificacion.financiador("x".repeat(121)));
    }
}
