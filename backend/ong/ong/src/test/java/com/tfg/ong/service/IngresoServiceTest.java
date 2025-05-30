
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

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private DetalleFacturaRepository detalleFacturaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private IngresoService ingresoService;

    private Ingreso mockIngreso;
    private Ong mockOng;
    private Usuario mockUsuario;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        mockOng = new Ong();
        mockOng.setId(1L);

        mockUsuario = new Usuario();
        mockUsuario.setId(1L);

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
    public void testUpdateIngreso() {
        when(ingresoRepository.save(any(Ingreso.class))).thenReturn(mockIngreso);
        Ingreso input = new Ingreso();
        input.setMonto(new BigDecimal("300.00"));
        input.setDescripcion("Ingreso actualizado");
        input.setFecha(LocalDate.of(2024, 6, 1));
        input.setTipo("Subvención");
        input.setOng(mockOng);

        Ingreso result = ingresoService.updateIngresos(1L, input);

        assertEquals(1L, result.getId());
        verify(ingresoRepository, times(1)).save(input);
    }

    @Test
    public void testDeleteIngreso() {
        ingresoService.deleteIngreso(1L);
        verify(ingresoRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testCreateIngresoConFactura() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(ingresoRepository.save(any(Ingreso.class))).thenReturn(mockIngreso);
        when(facturaRepository.save(any(Factura.class))).thenAnswer(i -> i.getArgument(0));

        Ingreso result = ingresoService.createIngresoConFactura(mockIngreso, 1L);

        assertNotNull(result);
        assertEquals("Ingreso de prueba", result.getDescripcion());
        verify(detalleFacturaRepository, times(1)).save(any(DetalleFactura.class));
    }

    @Test
    public void TestCreateIngresoConFactura_SinUsuario() {
        when(ingresoRepository.save(any(Ingreso.class))).thenReturn(mockIngreso);
        when(facturaRepository.save(any(Factura.class))).thenAnswer(i -> i.getArgument(0));

        Ingreso result = ingresoService.createIngresoConFactura(mockIngreso, null);

        assertNotNull(result);
        verify(usuarioRepository, times(0)).findById(any());
        verify(detalleFacturaRepository, times(1)).save(any(DetalleFactura.class));
    }

    @Test
    public void TestCreateIngresoConFactura_UsuarioNoExiste() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                ingresoService.createIngresoConFactura(mockIngreso, 1L));

        assertEquals("Usuario no encontrado con ID: 1", exception.getMessage());
    }

    @Test
    public void TestCreateIngresoConFactura_OngNula() {
        mockIngreso.setOng(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ingresoService.createIngresoConFactura(mockIngreso, null));

        assertEquals("La ONG asociada al ingreso no puede ser nula", exception.getMessage());
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
