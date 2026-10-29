
package com.tfg.ong.service;

import com.tfg.ong.model.*;
import com.tfg.ong.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class IngresoServiceTest {

    @Mock
    private IngresoRepository ingresoRepository;

    @InjectMocks
    private IngresoService ingresoService;

    private Ingreso mockIngreso;
    private Ong mockOng;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        mockOng = new Ong();
        mockOng.setId(1L);

        mockIngreso = new Ingreso();
        mockIngreso.setId(1L);
        mockIngreso.setMonto(new BigDecimal("200.00"));
        mockIngreso.setFecha(LocalDate.of(2024, 5, 1));
        mockIngreso.setTipo("Donación");
        mockIngreso.setDescripcion("Ingreso de prueba");
        mockIngreso.setOng(mockOng);
    }

    @Test
    public void testGetAllIngresos() {
        ingresoService.getAllIngresos();
        verify(ingresoRepository, times(1)).findAll();
    }

    @Test
    public void testGetIngresoById() {
        when(ingresoRepository.findById(1L)).thenReturn(Optional.of(mockIngreso));
        Ingreso result = ingresoService.getIngresoById(1L);
        assertNotNull(result);
        assertEquals("Ingreso de prueba", result.getDescripcion());
    }

    @Test
    public void testUpdateIngresoCambiaSoloLosDatosEditables() {
        Usuario donante = new Usuario();
        donante.setId(7L);
        mockIngreso.setUsuario(donante);
        when(ingresoRepository.findById(1L)).thenReturn(Optional.of(mockIngreso));
        when(ingresoRepository.save(any(Ingreso.class))).thenAnswer(i -> i.getArgument(0));
        Ingreso input = new Ingreso();
        input.setMonto(new BigDecimal("300.00"));
        input.setDescripcion("Ingreso actualizado");
        input.setFecha(LocalDate.of(2024, 6, 1));
        input.setTipo("Subvención");
        input.setCuenta("740");

        Ingreso result = ingresoService.updateIngresos(1L, input);

        assertEquals(1L, result.getId());
        assertEquals("Ingreso actualizado", result.getDescripcion());
        assertEquals(new BigDecimal("300.00"), result.getMonto());
        assertEquals("740", result.getCuenta());
        assertSame(mockOng, result.getOng(), "La ONG del ingreso no debe cambiar");
        assertSame(donante, result.getUsuario(), "La persona que donó no debe cambiar");
        verify(ingresoRepository, times(1)).save(mockIngreso);
    }

    @Test
    public void testUpdateIngresoNoExistenteOConDatosInvalidos() {
        when(ingresoRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> ingresoService.updateIngresos(9L, mockIngreso));

        when(ingresoRepository.findById(1L)).thenReturn(Optional.of(mockIngreso));
        Ingreso sinFecha = new Ingreso();
        sinFecha.setDescripcion("x");
        sinFecha.setMonto(new BigDecimal("5"));
        sinFecha.setTipo("OTROS");
        assertThrows(IllegalArgumentException.class, () -> ingresoService.updateIngresos(1L, sinFecha));
        verify(ingresoRepository, never()).save(any());
    }

    @Test
    public void testDeleteIngreso() {
        ingresoService.deleteIngreso(1L);
        verify(ingresoRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testCreateIngreso() {
        when(ingresoRepository.save(mockIngreso)).thenReturn(mockIngreso);
        Ingreso result = ingresoService.createIngreso(mockIngreso);

        assertNotNull(result);
        assertEquals("Ingreso de prueba", result.getDescripcion());
        verify(ingresoRepository, times(1)).save(mockIngreso);
    }


}
