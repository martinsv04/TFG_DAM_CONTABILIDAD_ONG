package com.tfg.ong.service;

import com.tfg.ong.dto.CertificadoDTO;
import com.tfg.ong.dto.DonanteResumenDTO;
import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.repository.OngRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificadoServiceTest {

    @Mock
    private IngresoRepository ingresoRepository;

    @Mock
    private OngRepository ongRepository;

    @InjectMocks
    private CertificadoService certificadoService;

    private Ong ong;
    private Usuario donante;

    @BeforeEach
    void preparar() {
        ong = new Ong();
        ong.setId(1L);
        ong.setNombre("Manos Solidarias");
        ong.setNif("G12345678");
        ong.setDireccion("Calle Mayor 12, Sevilla");
        ong.setRepresentante("Laura Admin");
        lenient().when(ongRepository.findById(1L)).thenReturn(Optional.of(ong));

        donante = new Usuario();
        donante.setId(5L);
        donante.setNombre("Carlos Ruiz");
        donante.setNifCif("12345678Z");
        donante.setDireccion("Av. de la Paz 3, Madrid");
    }

    private Ingreso ingreso(Long id, String tipo, String monto, String fecha, Usuario usuario) {
        Ingreso i = new Ingreso();
        i.setId(id);
        i.setOng(ong);
        i.setTipo(tipo);
        i.setMonto(new BigDecimal(monto));
        i.setFecha(LocalDate.parse(fecha));
        i.setDescripcion("Donación " + id);
        i.setUsuario(usuario);
        return i;
    }

    @Test
    void certificadoDeUnaDonacion() {
        when(ingresoRepository.findById(10L)).thenReturn(Optional.of(ingreso(10L, "DONACIÓN", "100.5", "2026-03-01", donante)));

        CertificadoDTO c = certificadoService.certificadoDonacion(1L, 10L);

        assertEquals("CD-2026-1-10", c.numero());
        assertEquals("DONACION", c.tipo());
        assertEquals(new BigDecimal("100.50"), c.importeTotal());
        assertEquals("G12345678", c.entidad().nif());
        assertEquals("12345678Z", c.donante().nif());
        assertEquals(1, c.donaciones().size());
    }

    @Test
    void donacionAnonimaNoTieneCertificado() {
        when(ingresoRepository.findById(10L)).thenReturn(Optional.of(ingreso(10L, "DONACIÓN", "50", "2026-03-01", null)));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> certificadoService.certificadoDonacion(1L, 10L));
        assertTrue(e.getMessage().contains("anónima"));
    }

    @Test
    void ingresoQueNoEsDonacionSeRechaza() {
        when(ingresoRepository.findById(10L)).thenReturn(Optional.of(ingreso(10L, "SUBVENCIÓN", "5000", "2026-03-01", donante)));

        assertThrows(IllegalArgumentException.class, () -> certificadoService.certificadoDonacion(1L, 10L));
    }

    @Test
    void faltanDatosDeLaOng() {
        ong.setNif(null);
        when(ingresoRepository.findById(10L)).thenReturn(Optional.of(ingreso(10L, "DONACIÓN", "50", "2026-03-01", donante)));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> certificadoService.certificadoDonacion(1L, 10L));
        assertTrue(e.getMessage().contains("Editar ONG"));
    }

    @Test
    void faltanDatosDelDonante() {
        donante.setNifCif(" ");
        donante.setDireccion(null);
        when(ingresoRepository.findById(10L)).thenReturn(Optional.of(ingreso(10L, "DONACIÓN", "50", "2026-03-01", donante)));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> certificadoService.certificadoDonacion(1L, 10L));
        assertTrue(e.getMessage().contains("NIF"));
        assertTrue(e.getMessage().contains("dirección"));
    }

    @Test
    void donacionDeOtraOngSeRechaza() {
        Ingreso i = ingreso(10L, "DONACIÓN", "50", "2026-03-01", donante);
        Ong otra = new Ong();
        otra.setId(2L);
        i.setOng(otra);
        when(ingresoRepository.findById(10L)).thenReturn(Optional.of(i));

        assertThrows(IllegalArgumentException.class, () -> certificadoService.certificadoDonacion(1L, 10L));
    }

    @Test
    void certificadoAnualSumaLasDonacionesDelDonante() {
        Usuario otro = new Usuario();
        otro.setId(6L);
        when(ingresoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any())).thenReturn(List.of(
                ingreso(10L, "DONACIÓN", "100", "2026-01-10", donante),
                ingreso(11L, "Donación", "25.25", "2026-06-10", donante),
                ingreso(12L, "DONACIÓN", "999", "2026-07-10", otro),
                ingreso(13L, "CUOTAS", "40", "2026-08-10", donante),
                ingreso(14L, "DONACIÓN", "10", "2026-09-10", null)));

        CertificadoDTO c = certificadoService.certificadoAnual(1L, 5L, 2026);

        assertEquals("CDA-2026-1-5", c.numero());
        assertEquals(new BigDecimal("125.25"), c.importeTotal());
        assertEquals(2, c.donaciones().size());
    }

    @Test
    void certificadoAnualSinDonacionesLanzaError() {
        when(ingresoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any())).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> certificadoService.certificadoAnual(1L, 5L, 2026));
    }

    @Test
    void resumenDeDonantesAgrupaYMarcaDatosIncompletos() {
        Usuario incompleto = new Usuario();
        incompleto.setId(6L);
        incompleto.setNombre("Ana");
        when(ingresoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any())).thenReturn(List.of(
                ingreso(10L, "DONACIÓN", "100", "2026-01-10", donante),
                ingreso(11L, "DONACIÓN", "50", "2026-02-10", donante),
                ingreso(12L, "DONACIÓN", "300", "2026-03-10", incompleto),
                ingreso(13L, "DONACIÓN", "20", "2026-04-10", null)));

        List<DonanteResumenDTO> resumen = certificadoService.resumenDonantes(1L, 2026);

        assertEquals(2, resumen.size());
        assertEquals("Ana", resumen.get(0).nombre());           // el que más donó va primero
        assertFalse(resumen.get(0).datosCompletos());
        assertEquals(new BigDecimal("150.00"), resumen.get(1).totalAnio());
        assertEquals(2, resumen.get(1).numDonaciones());
        assertTrue(resumen.get(1).datosCompletos());
    }

    @Test
    void reconoceElTipoDonacionConOSinTilde() {
        assertTrue(CertificadoService.esDonacion(ingreso(1L, "DONACIÓN", "1", "2026-01-01", null)));
        assertTrue(CertificadoService.esDonacion(ingreso(1L, " donacion ", "1", "2026-01-01", null)));
        assertFalse(CertificadoService.esDonacion(ingreso(1L, "CUOTAS", "1", "2026-01-01", null)));
        assertFalse(CertificadoService.esDonacion(ingreso(1L, null, "1", "2026-01-01", null)));
    }
}
