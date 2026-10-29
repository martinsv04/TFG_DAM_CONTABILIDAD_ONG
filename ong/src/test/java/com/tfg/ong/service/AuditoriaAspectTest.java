package com.tfg.ong.service;

import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.RegistroAuditoria;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.repository.ProyectoRepository;
import com.tfg.ong.repository.SubvencionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Comprueba que el aspecto anota las operaciones de los servicios (usa un proxy real de IngresoService). */
@ExtendWith(MockitoExtension.class)
class AuditoriaAspectTest {

    @Mock
    private IngresoRepository ingresoRepository;
    @Mock
    private ProyectoRepository proyectoRepository;
    @Mock
    private SubvencionRepository subvencionRepository;
    @Mock
    private AuditoriaService auditoriaService;

    private IngresoService servicio;
    private Ong ong;

    @BeforeEach
    void preparar() {
        ong = new Ong();
        ong.setId(1L);
        lenient().when(ingresoRepository.save(any(Ingreso.class))).thenAnswer(i -> {
            Ingreso guardado = i.getArgument(0);
            if (guardado.getId() == null) {
                guardado.setId(50L);
            }
            return guardado;
        });

        AspectJProxyFactory fabrica = new AspectJProxyFactory(new IngresoService(ingresoRepository, proyectoRepository, subvencionRepository));
        fabrica.setProxyTargetClass(true);
        fabrica.addAspect(new AuditoriaAspect(auditoriaService));
        servicio = fabrica.getProxy();
    }

    private Ingreso ingreso(Long id, String descripcion, String monto) {
        Ingreso i = new Ingreso();
        i.setId(id);
        i.setOng(ong);
        i.setDescripcion(descripcion);
        i.setTipo("DONACIÓN");
        i.setMonto(new BigDecimal(monto));
        i.setFecha(LocalDate.parse("2026-03-01"));
        return i;
    }

    @Test
    void registraLaCreacion() {
        servicio.createIngreso(ingreso(null, "Donación", "100"));

        verify(auditoriaService).registrar(eq(1L), eq("INGRESO"), eq(50L), eq(RegistroAuditoria.CREAR),
                argThat(t -> t.contains("Donación") && t.contains("100,00 €")));
    }

    @Test
    void registraLaEdicionConElAntesYElAhora() {
        when(ingresoRepository.findById(7L)).thenReturn(Optional.of(ingreso(7L, "Donación", "100")));

        servicio.updateIngresos(7L, ingreso(null, "Donación", "250"));

        verify(auditoriaService).registrar(eq(1L), eq("INGRESO"), eq(7L), eq(RegistroAuditoria.EDITAR),
                argThat(t -> t.startsWith("Antes: ") && t.contains("100,00 €") && t.contains("Ahora: ") && t.contains("250,00 €")));
    }

    @Test
    void registraElBorradoConLosDatosDelElementoEliminado() {
        when(ingresoRepository.findById(7L)).thenReturn(Optional.of(ingreso(7L, "Cuota", "40")));

        servicio.deleteIngreso(7L);

        verify(ingresoRepository).deleteById(7L);
        verify(auditoriaService).registrar(eq(1L), eq("INGRESO"), eq(7L), eq(RegistroAuditoria.ELIMINAR),
                argThat(t -> t.contains("Cuota") && t.contains("40,00 €")));
    }

    @Test
    void lasConsultasNoSeRegistran() {
        when(ingresoRepository.findAll()).thenReturn(List.of());

        servicio.getAllIngresos();

        verify(auditoriaService, never()).registrar(any(), any(), any(), any(), any());
    }

    @Test
    void siLaOperacionFallaNoSeRegistraNada() {
        Ingreso conProyectoInexistente = ingreso(null, "x", "1");
        com.tfg.ong.model.Proyecto fantasma = new com.tfg.ong.model.Proyecto();
        fantasma.setId(99L);
        conProyectoInexistente.setProyecto(fantasma);
        when(proyectoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> servicio.createIngreso(conProyectoInexistente));

        verify(auditoriaService, never()).registrar(any(), any(), any(), any(), any());
    }
}
