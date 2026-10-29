package com.tfg.ong.service;

import com.tfg.ong.dto.SubvencionDTO;
import com.tfg.ong.model.Categoria;
import com.tfg.ong.model.Gasto;
import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Proyecto;
import com.tfg.ong.model.Subvencion;
import com.tfg.ong.repository.GastoRepository;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.repository.OngRepository;
import com.tfg.ong.repository.ProyectoRepository;
import com.tfg.ong.repository.SubvencionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubvencionServiceTest {

    @Mock
    private SubvencionRepository subvencionRepository;
    @Mock
    private ProyectoRepository proyectoRepository;
    @Mock
    private GastoRepository gastoRepository;
    @Mock
    private IngresoRepository ingresoRepository;
    @Mock
    private OngRepository ongRepository;

    @InjectMocks
    private SubvencionService service;

    private Ong ong;
    private Proyecto proyecto;

    @BeforeEach
    void preparar() {
        ong = new Ong();
        ong.setId(1L);
        lenient().when(ongRepository.findById(1L)).thenReturn(Optional.of(ong));
        lenient().when(subvencionRepository.save(any(Subvencion.class))).thenAnswer(i -> i.getArgument(0));

        proyecto = new Proyecto();
        proyecto.setId(10L);
        proyecto.setOng(ong);
        proyecto.setNombre("Comedor social");
        lenient().when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));
    }

    private SubvencionService.Datos datos(String estado, String concedido, LocalDate justificacion, Long proyectoId) {
        return new SubvencionService.Datos("Ayuda alimentaria", " Ayuntamiento ", "EXP-1", estado, new BigDecimal("5000"),
                concedido == null ? null : new BigDecimal(concedido), LocalDate.parse("2026-01-10"), null, justificacion,
                proyectoId);
    }

    private Subvencion subvencion(long id, String estado, String concedido, LocalDate justificacion) {
        Subvencion s = new Subvencion();
        s.setId(id);
        s.setOng(ong);
        s.setNombre("Ayuda " + id);
        s.setEstado(estado);
        s.setImporteConcedido(concedido == null ? null : new BigDecimal(concedido));
        s.setFechaJustificacion(justificacion);
        s.setProyecto(proyecto);
        return s;
    }

    private Gasto gasto(Proyecto p, String monto) {
        Gasto g = new Gasto();
        g.setOng(ong);
        g.setProyecto(p);
        g.setCategoria(Categoria.values()[0]);
        g.setMonto(new BigDecimal(monto));
        g.setFecha(LocalDate.parse("2026-03-01"));
        return g;
    }

    private Ingreso ingreso(Subvencion s, String monto) {
        Ingreso i = new Ingreso();
        i.setOng(ong);
        i.setSubvencion(s);
        i.setMonto(new BigDecimal(monto));
        i.setFecha(LocalDate.parse("2026-03-01"));
        return i;
    }

    @Test
    void listaCalculaCobradoGastadoYPorcentaje() {
        Subvencion s = subvencion(1, Subvencion.CONCEDIDA, "4000", LocalDate.now().plusDays(30));
        when(subvencionRepository.findByOngId(1L)).thenReturn(List.of(s));
        when(gastoRepository.findByOngId(1L)).thenReturn(List.of(gasto(proyecto, "1000"), gasto(proyecto, "500"), gasto(null, "700")));
        when(ingresoRepository.findByOngId(1L)).thenReturn(List.of(ingreso(s, "2000"), ingreso(null, "99")));

        SubvencionDTO d = service.listar(1L).get(0);

        assertEquals(new BigDecimal("2000.00"), d.cobrado());
        assertEquals(new BigDecimal("1500.00"), d.gastado());
        assertEquals(new BigDecimal("37.5"), d.porcentajeEjecutado());
        assertEquals("Comedor social", d.proyectoNombre());
        assertEquals(30L, d.diasParaJustificar());
        assertFalse(d.justificacionVencida());
    }

    @Test
    void avisaSiElPlazoDeJustificacionYaPaso() {
        Subvencion s = subvencion(1, Subvencion.CONCEDIDA, "1000", LocalDate.now().minusDays(3));
        when(subvencionRepository.findByOngId(1L)).thenReturn(List.of(s));
        when(gastoRepository.findByOngId(1L)).thenReturn(List.of());
        when(ingresoRepository.findByOngId(1L)).thenReturn(List.of());

        SubvencionDTO d = service.listar(1L).get(0);

        assertTrue(d.justificacionVencida());
        assertEquals(-3L, d.diasParaJustificar());
    }

    @Test
    void unaSubvencionSolicitadaNoTienePorcentajeNiPlazo() {
        Subvencion s = subvencion(1, Subvencion.SOLICITADA, null, LocalDate.now().plusDays(10));
        s.setProyecto(null);
        when(subvencionRepository.findByOngId(1L)).thenReturn(List.of(s));
        when(gastoRepository.findByOngId(1L)).thenReturn(List.of());
        when(ingresoRepository.findByOngId(1L)).thenReturn(List.of());

        SubvencionDTO d = service.listar(1L).get(0);

        assertNull(d.porcentajeEjecutado());
        assertNull(d.diasParaJustificar());
        assertNull(d.proyectoId());
        assertEquals(new BigDecimal("0.00"), d.gastado());
    }

    @Test
    void creaConProyectoYLimpiaLosTextos() {
        Subvencion s = service.crear(1L, datos(Subvencion.CONCEDIDA, "4000", null, 10L));

        assertEquals("Ayuntamiento", s.getFinanciador());
        assertSame(proyecto, s.getProyecto());
        assertEquals(new BigDecimal("4000"), s.getImporteConcedido());
    }

    @Test
    void siNoEstaConcedidaSeIgnoraElImporteConcedido() {
        Subvencion s = service.crear(1L, datos(Subvencion.DENEGADA, "4000", null, null));

        assertNull(s.getImporteConcedido());
        assertNull(s.getProyecto());
    }

    @Test
    void validaLosDatos() {
        assertThrows(IllegalArgumentException.class, () -> service.crear(1L, datos("RARA", null, null, null)));
        // Concedida sin importe concedido
        assertThrows(IllegalArgumentException.class, () -> service.crear(1L, datos(Subvencion.CONCEDIDA, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> service.crear(1L, datos(Subvencion.CONCEDIDA, "0", null, null)));
        // Proyecto inexistente o de otra ONG
        assertThrows(IllegalArgumentException.class, () -> service.crear(1L, datos(Subvencion.SOLICITADA, null, null, 99L)));
        Ong otra = new Ong();
        otra.setId(2L);
        proyecto.setOng(otra);
        assertThrows(IllegalArgumentException.class, () -> service.crear(1L, datos(Subvencion.SOLICITADA, null, null, 10L)));
    }

    @Test
    void noSePuedeTocarUnaSubvencionDeOtraOng() {
        Ong otra = new Ong();
        otra.setId(2L);
        Subvencion ajena = subvencion(5, Subvencion.SOLICITADA, null, null);
        ajena.setOng(otra);
        when(subvencionRepository.findById(5L)).thenReturn(Optional.of(ajena));

        assertThrows(IllegalArgumentException.class, () -> service.eliminar(1L, 5L));
        assertThrows(IllegalArgumentException.class,
                () -> service.actualizar(1L, 5L, datos(Subvencion.SOLICITADA, null, null, null)));
    }
}
