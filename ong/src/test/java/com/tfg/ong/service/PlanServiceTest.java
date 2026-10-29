package com.tfg.ong.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.tfg.ong.exception.PlanException;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Proyecto;
import com.tfg.ong.model.Subvencion;
import com.tfg.ong.model.Suscripcion;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.FacturaRepository;
import com.tfg.ong.repository.GastoRepository;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.repository.OngRepository;
import com.tfg.ong.repository.ProyectoRepository;
import com.tfg.ong.repository.SubvencionRepository;
import com.tfg.ong.repository.SuscripcionRepository;
import com.tfg.ong.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlanServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 30);

    @Mock SuscripcionRepository suscripcionRepository;
    @Mock OngRepository ongRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock ProyectoRepository proyectoRepository;
    @Mock SubvencionRepository subvencionRepository;
    @Mock IngresoRepository ingresoRepository;
    @Mock GastoRepository gastoRepository;
    @Mock FacturaRepository facturaRepository;

    @InjectMocks PlanService plan;

    private Usuario admin;
    private Suscripcion suscripcion;

    @BeforeEach
    void preparar() {
        plan.usarReloj(Clock.fixed(Instant.parse("2026-10-30T10:00:00Z"), ZoneOffset.UTC));
        admin = new Usuario();
        admin.setId(1L);
        suscripcion = new Suscripcion();
        suscripcion.setUsuarioId(1L);
        suscripcion.setPruebaHasta(HOY.minusDays(1)); // prueba terminada = plan Básico
        when(suscripcionRepository.findByUsuarioId(1L)).thenReturn(Optional.of(suscripcion));
        when(suscripcionRepository.save(any(Suscripcion.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Ong ong(Long id) {
        Ong o = new Ong();
        o.setId(id);
        o.setAdmin(admin);
        when(ongRepository.findById(id)).thenReturn(Optional.of(o));
        return o;
    }

    // ------------------------------------------------------------ plan vigente

    @Test
    void laPruebaDaPlanProYDespuesPasaAlBasico() {
        Suscripcion s = new Suscripcion();
        s.setPruebaHasta(HOY.plusDays(5));
        PlanService.Estado e = PlanService.calcular(s, HOY);
        assertTrue(e.pro());
        assertEquals(PlanService.Origen.PRUEBA, e.origen());
        assertEquals(5, e.diasPrueba());

        s.setPruebaHasta(HOY);
        assertTrue(PlanService.calcular(s, HOY).pro());

        s.setPruebaHasta(HOY.minusDays(1));
        PlanService.Estado b = PlanService.calcular(s, HOY);
        assertFalse(b.pro());
        assertEquals(PlanService.Origen.GRATIS, b.origen());
    }

    @Test
    void laSuscripcionActivaOConPagoPendienteMantieneElPro() {
        Suscripcion s = new Suscripcion();
        s.setPruebaHasta(HOY.minusDays(30));
        s.setEstado("active");
        assertTrue(PlanService.calcular(s, HOY).pro());

        s.setEstado("past_due");
        PlanService.Estado e = PlanService.calcular(s, HOY);
        assertTrue(e.pro());
        assertTrue(e.pagoPendiente());

        s.setEstado("canceled");
        assertFalse(PlanService.calcular(s, HOY).pro());
        s.setEstado("unpaid");
        assertFalse(PlanService.calcular(s, HOY).pro());
    }

    @Test
    void creaLaSuscripcionConCatorceDiasDePruebaLaPrimeraVez() {
        when(suscripcionRepository.findByUsuarioId(7L)).thenReturn(Optional.empty());

        Suscripcion s = plan.suscripcionDe(7L);

        assertEquals(HOY.plusDays(14), s.getPruebaHasta());
        verify(suscripcionRepository).save(any(Suscripcion.class));
    }

    // ------------------------------------------------------------ ONG activa

    @Test
    void laOngActivaPorDefectoEsLaMasAntigua() {
        List<Ong> propias = List.of(ong(5L), ong(3L), ong(9L));
        when(ongRepository.findByAdminId(1L)).thenReturn(propias);
        assertEquals(3L, plan.ongActiva(1L));
    }

    @Test
    void sePuedeElegirOtraOngActivaPeroSoloDeLasPropias() {
        List<Ong> propias = List.of(ong(3L), ong(5L));
        when(ongRepository.findByAdminId(1L)).thenReturn(propias);

        plan.cambiarOngActiva(1L, 5L);

        assertEquals(5L, suscripcion.getOngActivaId());
        assertEquals(5L, plan.ongActiva(1L));
        assertThrows(IllegalArgumentException.class, () -> plan.cambiarOngActiva(1L, 99L));
    }

    // ------------------------------------------------------------ comprobaciones

    @Test
    void enElBasicoSoloSeEscribeEnLaOngActiva() {
        List<Ong> propias = List.of(ong(3L), ong(5L));
        when(ongRepository.findByAdminId(1L)).thenReturn(propias);

        assertDoesNotThrow(() -> plan.exigirEscritura(3L));
        PlanException e = assertThrows(PlanException.class, () -> plan.exigirEscritura(5L));
        assertTrue(e.getMessage().contains("solo lectura"));
    }

    @Test
    void conElProSePuedeEscribirEnTodasLasOng() {
        suscripcion.setEstado("active");
        List<Ong> propias = List.of(ong(3L), ong(5L));
        when(ongRepository.findByAdminId(1L)).thenReturn(propias);

        assertDoesNotThrow(() -> plan.exigirEscritura(5L));
        assertDoesNotThrow(() -> plan.exigirNuevaOng(1L));
        assertDoesNotThrow(() -> plan.exigirPro(5L));
    }

    @Test
    void elBasicoLimitaProyectosSubvencionesYMiembros() {
        Ong o = ong(3L);
        when(ongRepository.findByAdminId(1L)).thenReturn(List.of(o));
        when(proyectoRepository.findByOngId(3L)).thenReturn(List.of(new Proyecto(), new Proyecto()));
        when(subvencionRepository.findByOngId(3L)).thenReturn(List.of(new Subvencion()));
        when(usuarioRepository.findByOngId(3L)).thenReturn(List.of(new Usuario(), new Usuario(), new Usuario()));

        assertThrows(PlanException.class, () -> plan.exigirCrearProyecto(3L));
        assertThrows(PlanException.class, () -> plan.exigirCrearSubvencion(3L));
        assertThrows(PlanException.class, () -> plan.exigirCrearMiembro(3L));
    }

    @Test
    void elBasicoPermiteCrearSiNoSeHaAlcanzadoElLimite() {
        Ong o = ong(3L);
        when(ongRepository.findByAdminId(1L)).thenReturn(List.of(o));
        when(proyectoRepository.findByOngId(3L)).thenReturn(List.of(new Proyecto()));
        when(ingresoRepository.findByOngIdAndFechaBetween(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        when(gastoRepository.findByOngIdAndFechaBetween(anyLong(), any(), any())).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> plan.exigirCrearProyecto(3L));
        assertDoesNotThrow(() -> plan.exigirCrearMovimiento(3L));
    }

    @Test
    void elBasicoNoPermiteUnaSegundaOngNiLasFuncionesPro() {
        List<Ong> propias = List.of(ong(3L));
        when(ongRepository.findByAdminId(1L)).thenReturn(propias);

        assertThrows(PlanException.class, () -> plan.exigirNuevaOng(1L));
        assertThrows(PlanException.class, () -> plan.exigirPro(3L));
    }

    @Test
    void unaOngSinAdministradorNoTieneLimites() {
        Ong huerfana = new Ong();
        huerfana.setId(8L);
        when(ongRepository.findById(8L)).thenReturn(Optional.of(huerfana));

        assertDoesNotThrow(() -> plan.exigirEscritura(8L));
        assertDoesNotThrow(() -> plan.exigirPro(8L));
    }
}
