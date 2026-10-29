package com.tfg.ong.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.tfg.ong.model.MiembroGobierno;
import com.tfg.ong.model.Ong;
import com.tfg.ong.repository.MiembroGobiernoRepository;
import com.tfg.ong.repository.OngRepository;

public class GobiernoServiceTest {

    @Mock private MiembroGobiernoRepository repository;
    @Mock private OngRepository ongRepository;

    @InjectMocks
    private GobiernoService servicio;

    private Ong ong;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ong = new Ong();
        ong.setId(1L);
        when(repository.save(any(MiembroGobierno.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private MiembroGobierno datos(String nombre, String cargo, String organo) {
        MiembroGobierno m = new MiembroGobierno();
        m.setNombre(nombre);
        m.setCargo(cargo);
        m.setOrgano(organo);
        return m;
    }

    @Test
    public void creaYLimpiaLosDatos() {
        when(ongRepository.findById(1L)).thenReturn(Optional.of(ong));

        MiembroGobierno creado = servicio.crear(1L, datos("  Ana Pérez ", " Presidenta ", "gobierno"));

        assertEquals("Ana Pérez", creado.getNombre());
        assertEquals("Presidenta", creado.getCargo());
        assertEquals("GOBIERNO", creado.getOrgano());
        assertSame(ong, creado.getOng());
    }

    @Test
    public void sinOrganoSeConsideraGobierno() {
        when(ongRepository.findById(1L)).thenReturn(Optional.of(ong));
        assertEquals("GOBIERNO", servicio.crear(1L, datos("Luis", "Vocal", null)).getOrgano());
    }

    @Test
    public void rechazaDatosIncompletosOInvalidos() {
        when(ongRepository.findById(1L)).thenReturn(Optional.of(ong));
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(1L, datos(" ", "Vocal", "GOBIERNO")));
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(1L, datos("Luis", "", "GOBIERNO")));
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(1L, datos("Luis", "Vocal", "OTRO")));
    }

    @Test
    public void ongInexistenteFalla() {
        when(ongRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(9L, datos("Luis", "Vocal", "GOBIERNO")));
    }

    @Test
    public void ongDeDevuelveLaOngOnull() {
        MiembroGobierno m = datos("Luis", "Vocal", "GOBIERNO");
        m.setOng(ong);
        when(repository.findById(5L)).thenReturn(Optional.of(m));
        when(repository.findById(6L)).thenReturn(Optional.empty());

        assertEquals(1L, servicio.ongDe(5L));
        assertNull(servicio.ongDe(6L));
    }
}
