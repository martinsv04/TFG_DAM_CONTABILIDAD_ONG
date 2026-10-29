
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

public class GastoServiceTest {

    @Mock
    private GastoRepository gastoRepository;

    @InjectMocks
    private GastoService gastoService;

    private Gasto mockGasto;
    private Ong mockOng;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        mockOng = new Ong();
        mockOng.setId(1L);

        mockGasto = new Gasto();
        mockGasto.setId(1L);
        mockGasto.setMonto(new BigDecimal("150.00"));
        mockGasto.setFecha(LocalDate.of(2024, 5, 1));
        mockGasto.setDescripcion("Gasto de prueba");
        mockGasto.setCategoria(Categoria.TRANSPORTE);
        mockGasto.setOng(mockOng);
    }

    @Test
    public void testGetAllGastos() {
        gastoService.getAllGastos();
        verify(gastoRepository, times(1)).findAll();
    }

    @Test
    public void testGetGastoById() {
        when(gastoRepository.findById(1L)).thenReturn(Optional.of(mockGasto));
        Gasto result = gastoService.getGastoById(1L);
        assertNotNull(result);
        assertEquals("Gasto de prueba", result.getDescripcion());
    }

    @Test
    public void testUpdateGastoCambiaSoloLosDatosEditables() {
        when(gastoRepository.findById(1L)).thenReturn(Optional.of(mockGasto));
        when(gastoRepository.save(any(Gasto.class))).thenAnswer(i -> i.getArgument(0));
        Gasto input = new Gasto();
        input.setMonto(new BigDecimal("180.00"));
        input.setDescripcion("  Actualizado ");
        input.setFecha(LocalDate.of(2024, 6, 1));
        input.setCategoria(Categoria.OTROS);
        input.setCuenta(" 623 ");

        Gasto result = gastoService.updateGastos(1L, input);

        assertEquals(1L, result.getId());
        assertEquals("Actualizado", result.getDescripcion());
        assertEquals(new BigDecimal("180.00"), result.getMonto());
        assertEquals(Categoria.OTROS, result.getCategoria());
        assertEquals("623", result.getCuenta());
        assertSame(mockOng, result.getOng(), "La ONG del gasto no debe cambiar");
        verify(gastoRepository, times(1)).save(mockGasto);
    }

    @Test
    public void testUpdateGastoNoExistenteOConDatosInvalidos() {
        when(gastoRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> gastoService.updateGastos(9L, mockGasto));

        when(gastoRepository.findById(1L)).thenReturn(Optional.of(mockGasto));
        Gasto sinImporte = new Gasto();
        sinImporte.setDescripcion("x");
        sinImporte.setFecha(LocalDate.of(2024, 6, 1));
        sinImporte.setCategoria(Categoria.OTROS);
        sinImporte.setMonto(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, () -> gastoService.updateGastos(1L, sinImporte));
        verify(gastoRepository, never()).save(any());
    }

    @Test
    public void testDeleteGasto() {
        gastoService.deleteGasto(1L);
        verify(gastoRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testCreateGasto() {
        when(gastoRepository.save(mockGasto)).thenReturn(mockGasto);

        Gasto result = gastoService.createGasto(mockGasto);

        assertNotNull(result);
        assertEquals("Gasto de prueba", result.getDescripcion());
        verify(gastoRepository, times(1)).save(mockGasto);
    }

}
