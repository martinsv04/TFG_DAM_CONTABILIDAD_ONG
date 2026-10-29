package com.tfg.ong.service;

import com.tfg.ong.model.Gasto;
import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Proyecto;
import com.tfg.ong.model.Subvencion;
import com.tfg.ong.repository.GastoRepository;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.repository.ProyectoRepository;
import com.tfg.ong.repository.SubvencionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** El proyecto y la subvención que se asignan a un ingreso o gasto tienen que ser de la misma ONG. */
@ExtendWith(MockitoExtension.class)
class VinculoProyectoTest {

    @Mock
    private IngresoRepository ingresoRepository;
    @Mock
    private GastoRepository gastoRepository;
    @Mock
    private ProyectoRepository proyectoRepository;
    @Mock
    private SubvencionRepository subvencionRepository;

    private IngresoService ingresoService;
    private GastoService gastoService;
    private Ong ong;
    private Ong otra;

    @BeforeEach
    void preparar() {
        ingresoService = new IngresoService(ingresoRepository, proyectoRepository, subvencionRepository);
        gastoService = new GastoService(gastoRepository, proyectoRepository);
        ong = new Ong();
        ong.setId(1L);
        otra = new Ong();
        otra.setId(2L);
        lenient().when(ingresoRepository.save(any(Ingreso.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(gastoRepository.save(any(Gasto.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Proyecto proyecto(Long id, Ong de) {
        Proyecto p = new Proyecto();
        p.setId(id);
        p.setOng(de);
        return p;
    }

    private Subvencion subvencion(Long id, Ong de) {
        Subvencion s = new Subvencion();
        s.setId(id);
        s.setOng(de);
        return s;
    }

    @Test
    void ingresoConProyectoYSubvencionDeLaMismaOngSeGuarda() {
        Proyecto p = proyecto(10L, ong);
        Subvencion s = subvencion(20L, ong);
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(p));
        when(subvencionRepository.findById(20L)).thenReturn(Optional.of(s));

        Ingreso i = new Ingreso();
        i.setOng(ong);
        i.setProyecto(proyecto(10L, null));
        i.setSubvencion(subvencion(20L, null));

        Ingreso guardado = ingresoService.createIngreso(i);

        assertSame(p, guardado.getProyecto());
        assertSame(s, guardado.getSubvencion());
    }

    @Test
    void ingresoConProyectoDeOtraOngSeRechaza() {
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto(10L, otra)));
        Ingreso i = new Ingreso();
        i.setOng(ong);
        i.setProyecto(proyecto(10L, null));

        assertThrows(IllegalArgumentException.class, () -> ingresoService.createIngreso(i));
        verify(ingresoRepository, never()).save(any());
    }

    @Test
    void ingresoConSubvencionInexistenteSeRechaza() {
        when(subvencionRepository.findById(20L)).thenReturn(Optional.empty());
        Ingreso i = new Ingreso();
        i.setOng(ong);
        i.setSubvencion(subvencion(20L, null));

        assertThrows(IllegalArgumentException.class, () -> ingresoService.createIngreso(i));
    }

    @Test
    void ingresoSinProyectoNoConsultaNada() {
        Ingreso i = new Ingreso();
        i.setOng(ong);

        ingresoService.createIngreso(i);

        verify(proyectoRepository, never()).findById(any());
    }

    @Test
    void gastoConProyectoDeOtraOngSeRechaza() {
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto(10L, otra)));
        Gasto g = new Gasto();
        g.setOng(ong);
        g.setProyecto(proyecto(10L, null));

        assertThrows(IllegalArgumentException.class, () -> gastoService.createGasto(g));
        verify(gastoRepository, never()).save(any());
    }

    @Test
    void gastoConProyectoDeLaMismaOngSeGuarda() {
        Proyecto p = proyecto(10L, ong);
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(p));
        Gasto g = new Gasto();
        g.setOng(ong);
        g.setProyecto(proyecto(10L, null));

        assertSame(p, gastoService.createGasto(g).getProyecto());
    }
}
