package com.tfg.ong.service;

import com.tfg.ong.model.Ong;
import com.tfg.ong.repository.OngRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

public class OngServiceTest {

    private OngRepository ongRepository;
    private OngService ongService;

    private Ong sampleOng;

    @BeforeEach
    public void setUp() {
        ongRepository = mock(OngRepository.class);
        ongService = new OngService(ongRepository);

        sampleOng = new Ong();
        sampleOng.setId(1L);
        sampleOng.setNombre("ONG Prueba");
        sampleOng.setFechaCreacion(LocalDate.now());
    }

    @Test
    public void testGetAllOngs() {
        when(ongRepository.findAll()).thenReturn(Arrays.asList(sampleOng));

        List<Ong> result = ongService.getAllOngs();

        assertEquals(1, result.size());
        assertEquals("ONG Prueba", result.get(0).getNombre());
        verify(ongRepository, times(1)).findAll();
    }

    @Test
    public void testGetOngById_Found() {
        when(ongRepository.findById(1L)).thenReturn(Optional.of(sampleOng));

        Ong result = ongService.getOngById(1L);

        assertNotNull(result);
        assertEquals("ONG Prueba", result.getNombre());
        verify(ongRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetOngById_NotFound() {
        when(ongRepository.findById(2L)).thenReturn(Optional.empty());

        Ong result = ongService.getOngById(2L);

        assertNull(result);
        verify(ongRepository, times(1)).findById(2L);
    }

    @Test
    public void testCreateOng() {
        when(ongRepository.save(ArgumentMatchers.any(Ong.class))).thenReturn(sampleOng);

        Ong result = ongService.createOng(sampleOng);

        assertNotNull(result);
        assertEquals("ONG Prueba", result.getNombre());
        verify(ongRepository, times(1)).save(sampleOng);
    }

    @Test
    public void testUpdateOngs() {
        sampleOng.setNombre("ONG Actualizada");
        when(ongRepository.save(ArgumentMatchers.any(Ong.class))).thenReturn(sampleOng);

        Ong result = ongService.updateOngs(1L, sampleOng);

        assertNotNull(result);
        assertEquals("ONG Actualizada", result.getNombre());
        assertEquals(1L, result.getId());
        verify(ongRepository, times(1)).save(sampleOng);
    }

    @Test
    public void testDeleteOng() {
        doNothing().when(ongRepository).deleteById(1L);

        ongService.deleteOng(1L);

        verify(ongRepository, times(1)).deleteById(1L);
    }
}
