package com.tfg.ong.service;

import com.tfg.ong.model.Categoria;
import com.tfg.ong.model.Gasto;
import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Proyecto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AuditoriaDescripcionesTest {

    @Test
    void describeUnIngreso() {
        Ingreso i = new Ingreso();
        i.setDescripcion(" Donación otoño ");
        i.setTipo("DONACIÓN");
        i.setMonto(new BigDecimal("1200.5"));

        assertEquals("«Donación otoño» · DONACIÓN · 1200,50 €", AuditoriaDescripciones.describir(i));
    }

    @Test
    void describeUnGastoYUnProyecto() {
        Gasto g = new Gasto();
        g.setCategoria(Categoria.values()[0]);
        g.setMonto(new BigDecimal("10"));
        assertEquals("sin descripción · " + Categoria.values()[0].name() + " · 10,00 €", AuditoriaDescripciones.describir(g));

        Proyecto p = new Proyecto();
        p.setNombre("Comedor");
        p.setEstado(Proyecto.EN_CURSO);
        p.setPresupuesto(new BigDecimal("8000"));
        assertEquals("«Comedor» · EN_CURSO · presupuesto 8000,00 €", AuditoriaDescripciones.describir(p));
    }

    @Test
    void obtieneIdYOng() {
        Ong ong = new Ong();
        ong.setId(3L);
        Ingreso i = new Ingreso();
        i.setId(9L);
        i.setOng(ong);

        assertEquals(9L, AuditoriaDescripciones.id(i));
        assertEquals(3L, AuditoriaDescripciones.ongId(i));
        assertNull(AuditoriaDescripciones.ongId(new Ingreso()));
    }

    @Test
    void unTipoDesconocidoNoSeDescribe() {
        assertNull(AuditoriaDescripciones.describir("texto"));
        assertNull(AuditoriaDescripciones.id("texto"));
        assertNull(AuditoriaDescripciones.ongId(null));
    }
}
