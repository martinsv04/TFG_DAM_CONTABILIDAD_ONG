package com.tfg.ong.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.tfg.ong.model.Gasto;
import com.tfg.ong.model.Ingreso;

class CuentaContableTest {

    @Test
    void vacioOBlancoSeConvierteEnNull() {
        assertNull(CuentaContable.normalizar(null));
        assertNull(CuentaContable.normalizar(""));
        assertNull(CuentaContable.normalizar("   "));
    }

    @Test
    void codigoValidoSeDevuelveSinEspacios() {
        assertEquals("720", CuentaContable.normalizar(" 720 "));
        assertEquals("6230000", CuentaContable.normalizar("6230000"));
    }

    @Test
    void codigoNoNumericoOFueraDeRangoSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> CuentaContable.normalizar("72"));
        assertThrows(IllegalArgumentException.class, () -> CuentaContable.normalizar("72A"));
        assertThrows(IllegalArgumentException.class, () -> CuentaContable.normalizar("12345678"));
        assertThrows(IllegalArgumentException.class, () -> CuentaContable.normalizar("=1+1"));
    }

    @Test
    void laAuditoriaIncluyeLaCuentaSoloSiExiste() {
        Ingreso i = new Ingreso();
        i.setDescripcion("Cuota");
        i.setTipo("DONACIÓN");
        i.setMonto(new BigDecimal("10"));
        assertEquals("«Cuota» · DONACIÓN · 10,00 €", AuditoriaDescripciones.describir(i));
        i.setCuenta("720");
        assertEquals("«Cuota» · DONACIÓN · 10,00 € · cuenta 720", AuditoriaDescripciones.describir(i));

        Gasto g = new Gasto();
        g.setDescripcion("Alquiler");
        g.setMonto(new BigDecimal("5"));
        g.setCuenta("621");
        assertEquals("«Alquiler» · sin categoría · 5,00 € · cuenta 621", AuditoriaDescripciones.describir(g));
    }
}
