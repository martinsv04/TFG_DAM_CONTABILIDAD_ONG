package com.tfg.ong.service;

import com.tfg.ong.model.Reporte;
import com.tfg.ong.model.TipoReporte;
import com.tfg.ong.model.Ong;
import com.tfg.ong.repository.ReporteRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReporteServiceTest {

    private ReporteRepository reporteRepository;
    private ReporteService reporteService;

    @BeforeEach
    public void setUp() {
        reporteRepository = Mockito.mock(ReporteRepository.class);
        reporteService = new ReporteService(reporteRepository);
    }

    private Reporte ejemploReporte() {
        Reporte r = new Reporte();
        r.setId(1L);
        r.setContenido("Contenido del reporte");
        r.setFechaGeneracion(LocalDateTime.now());
        r.setTipo(TipoReporte.ESTADO_RESULTADOS);
        r.setOng(new Ong());
        return r;
    }

    @Test
    public void testGetAllReportes() {
        when(reporteRepository.findAll()).thenReturn(Arrays.asList(ejemploReporte(), ejemploReporte()));

        List<Reporte> result = reporteService.getAllReportes();

        assertEquals(2, result.size());
        verify(reporteRepository, times(1)).findAll();
    }

    @Test
    public void testGetReporteById_Exists() {
        Reporte reporte = ejemploReporte();
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));

        Reporte result = reporteService.getReporteById(1L);

        assertNotNull(result);
        assertEquals("Contenido del reporte", result.getContenido());
    }

    @Test
    public void testGetReporteById_NotExists() {
        when(reporteRepository.findById(99L)).thenReturn(Optional.empty());

        Reporte result = reporteService.getReporteById(99L);

        assertNull(result);
    }

    @Test
    public void testCreateReporte() {
        Reporte nuevo = ejemploReporte();

        when(reporteRepository.save(nuevo)).thenReturn(nuevo);

        Reporte result = reporteService.createReporte(nuevo);

        assertEquals(nuevo.getContenido(), result.getContenido());
        verify(reporteRepository, times(1)).save(nuevo);
    }

    @Test
    public void testUpdateReporte() {
        Reporte actualizado = ejemploReporte();
        actualizado.setContenido("Actualizado");

        when(reporteRepository.save(actualizado)).thenReturn(actualizado);

        Reporte result = reporteService.updateReportes(1L, actualizado);

        assertEquals("Actualizado", result.getContenido());
        assertEquals(1L, result.getId());
    }

    @Test
    public void testDeleteReporte() {
        doNothing().when(reporteRepository).deleteById(1L);

        reporteService.deleteReporte(1L);

        verify(reporteRepository, times(1)).deleteById(1L);
    }
}
