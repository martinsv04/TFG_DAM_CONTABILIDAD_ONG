
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

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private DetalleFacturaRepository detalleFacturaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private GastoService gastoService;

    private Gasto mockGasto;
    private Ong mockOng;
    private Usuario mockUsuario;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        mockOng = new Ong();
        mockOng.setId(1L);

        mockUsuario = new Usuario();
        mockUsuario.setId(1L);

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
    public void testUpdateGasto() {
        when(gastoRepository.save(any(Gasto.class))).thenReturn(mockGasto);
        Gasto input = new Gasto();
        input.setMonto(new BigDecimal("180.00"));
        input.setDescripcion("Actualizado");
        input.setFecha(LocalDate.of(2024, 6, 1));
        input.setCategoria(Categoria.OTROS);
        input.setOng(mockOng);

        Gasto result = gastoService.updateGastos(1L, input);

        assertEquals(1L, result.getId());
        verify(gastoRepository, times(1)).save(input);
    }

    @Test
    public void testDeleteGasto() {
        gastoService.deleteGasto(1L);
        verify(gastoRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testCreateGastoConFactura() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(gastoRepository.save(any(Gasto.class))).thenReturn(mockGasto);
        when(facturaRepository.save(any(Factura.class))).thenAnswer(i -> i.getArgument(0));

        Gasto result = gastoService.createGastoConFactura(mockGasto, 1L);

        assertNotNull(result);
        assertEquals("Gasto de prueba", result.getDescripcion());
        verify(detalleFacturaRepository, times(1)).save(any(DetalleFactura.class));
    }

    @Test
    public void testCreateGastoConFactura_SinUsuario() {
        when(gastoRepository.save(any(Gasto.class))).thenReturn(mockGasto);
        when(facturaRepository.save(any(Factura.class))).thenAnswer(i -> i.getArgument(0));

        Gasto result = gastoService.createGastoConFactura(mockGasto, null);

        assertNotNull(result);
        verify(usuarioRepository, times(0)).findById(any());
        verify(detalleFacturaRepository, times(1)).save(any(DetalleFactura.class));
    }

    @Test
    public void testCreateGastoConFactura_UsuarioNoExiste() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                gastoService.createGastoConFactura(mockGasto, 1L));

        assertEquals("Usuario no encontrado con ID: 1", exception.getMessage());
    }

    @Test
    public void testCreateGastoConFactura_OngNula() {
        mockGasto.setOng(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                gastoService.createGastoConFactura(mockGasto, null));

        assertEquals("La ONG asociada al gasto no puede ser nula", exception.getMessage());
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
