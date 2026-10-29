package com.tfg.ong.security;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class IntentosLoginTest {

    /** Reloj que se puede adelantar a mano. */
    private static class RelojManual extends Clock {
        Instant ahora = Instant.parse("2026-10-29T10:00:00Z");

        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return ahora; }
    }

    @Test
    void bloqueaTrasElMaximoDeFallos() {
        IntentosLogin intentos = new IntentosLogin(new RelojManual());
        for (int i = 0; i < IntentosLogin.MAXIMO - 1; i++) {
            intentos.fallo("a");
            assertEquals(0, intentos.segundosBloqueado("a"));
        }
        intentos.fallo("a");
        assertTrue(intentos.segundosBloqueado("a") > 0);
        assertEquals(0, intentos.segundosBloqueado("otra"));
    }

    @Test
    void elBloqueoCaducaConElTiempo() {
        RelojManual reloj = new RelojManual();
        IntentosLogin intentos = new IntentosLogin(reloj);
        for (int i = 0; i < IntentosLogin.MAXIMO; i++) {
            intentos.fallo("a");
        }
        reloj.ahora = reloj.ahora.plus(Duration.ofMinutes(16));
        assertEquals(0, intentos.segundosBloqueado("a"));
    }

    @Test
    void unAccesoCorrectoReiniciaLaCuenta() {
        IntentosLogin intentos = new IntentosLogin(new RelojManual());
        for (int i = 0; i < IntentosLogin.MAXIMO - 1; i++) {
            intentos.fallo("a");
        }
        intentos.exito("a");
        intentos.fallo("a");
        assertEquals(0, intentos.segundosBloqueado("a"));
    }

    @Test
    void losFallosAntiguosNoSeAcumulan() {
        RelojManual reloj = new RelojManual();
        IntentosLogin intentos = new IntentosLogin(reloj);
        for (int i = 0; i < IntentosLogin.MAXIMO - 1; i++) {
            intentos.fallo("a");
        }
        reloj.ahora = reloj.ahora.plus(Duration.ofMinutes(20));
        intentos.fallo("a");
        assertEquals(0, intentos.segundosBloqueado("a"));
    }
}
