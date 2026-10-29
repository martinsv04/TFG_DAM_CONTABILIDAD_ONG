package com.tfg.ong.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.tfg.ong.dto.TransparenciaDTO;
import com.tfg.ong.dto.TransparenciaDTO.Parte;
import com.tfg.ong.model.*;
import com.tfg.ong.repository.*;

public class TransparenciaServiceTest {

    @Mock private IngresoRepository ingresoRepository;
    @Mock private GastoRepository gastoRepository;
    @Mock private OngRepository ongRepository;
    @Mock private MiembroGobiernoRepository gobiernoRepository;

    @InjectMocks
    private TransparenciaService servicio;

    private Ong ong;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ong = new Ong();
        ong.setId(1L);
        ong.setNombre("ONG de prueba");
        when(ongRepository.findById(1L)).thenReturn(Optional.of(ong));
    }

    private Ingreso ingreso(String monto, String tipo, String origen, String financiador, int anio) {
        Ingreso i = new Ingreso();
        i.setMonto(new BigDecimal(monto));
        i.setTipo(tipo);
        i.setOrigen(origen);
        i.setFinanciador(financiador);
        i.setFecha(LocalDate.of(anio, 3, 1));
        i.setOng(ong);
        return i;
    }

    private Gasto gasto(String monto, AreaGasto area, boolean conProyecto, int anio) {
        Gasto g = new Gasto();
        g.setMonto(new BigDecimal(monto));
        g.setCategoria(Categoria.OTROS);
        g.setArea(area);
        g.setFecha(LocalDate.of(anio, 4, 1));
        if (conProyecto) {
            g.setProyecto(new Proyecto());
        }
        return g;
    }

    private Parte parte(List<Parte> lista, String clave) {
        return lista.stream().filter(p -> p.clave().equals(clave)).findFirst().orElseThrow();
    }

    @Test
    public void origenSeDeduceDeLaSubvencionSiNoSeIndica() {
        assertEquals("PUBLICO", TransparenciaService.origenDe(ingreso("10", "SUBVENCIÓN", null, null, 2025)));
        assertEquals("PRIVADO", TransparenciaService.origenDe(ingreso("10", "DONACIÓN", null, null, 2025)));
        assertEquals("PRIVADO", TransparenciaService.origenDe(ingreso("10", "SUBVENCIÓN", "PRIVADO", null, 2025)));
    }

    @Test
    public void financiadorSeDeduceDelTipoOdeLaSubvencion() {
        assertEquals("Donaciones de particulares", TransparenciaService.financiadorDe(ingreso("10", "DONACIÓN", null, null, 2025)));
        assertEquals("Cuotas de socios", TransparenciaService.financiadorDe(ingreso("10", "CUOTAS", null, null, 2025)));
        Ingreso conSubvencion = ingreso("10", "SUBVENCIÓN", null, null, 2025);
        Subvencion s = new Subvencion();
        s.setFinanciador("Junta de Andalucía");
        conSubvencion.setSubvencion(s);
        assertEquals("Junta de Andalucía", TransparenciaService.financiadorDe(conSubvencion));
        assertEquals("Fundación X", TransparenciaService.financiadorDe(ingreso("10", "OTROS", null, " Fundación X ", 2025)));
    }

    @Test
    public void areaSeDeduceDelProyecto() {
        assertEquals(AreaGasto.MISION, TransparenciaService.areaDe(gasto("1", null, true, 2025)));
        assertEquals(AreaGasto.ADMINISTRACION, TransparenciaService.areaDe(gasto("1", null, false, 2025)));
        assertEquals(AreaGasto.CAPTACION, TransparenciaService.areaDe(gasto("1", AreaGasto.CAPTACION, true, 2025)));
    }

    @Test
    public void calculaFinanciacionPublicaYPrivada() {
        when(ingresoRepository.findByOngId(1L)).thenReturn(List.of(
                ingreso("600", "SUBVENCIÓN", "PUBLICO", "Ayuntamiento", 2025),
                ingreso("300", "DONACIÓN", null, null, 2025),
                ingreso("100", "CUOTAS", null, null, 2025),
                ingreso("999", "DONACIÓN", null, null, 2024)));
        when(gastoRepository.findByOngId(1L)).thenReturn(List.of());

        TransparenciaDTO dto = servicio.calcular(1L, 2025);

        assertEquals(2025, dto.anio());
        assertEquals(0, new BigDecimal("1000").compareTo(dto.ingresos()));
        assertEquals(0, new BigDecimal("600").compareTo(parte(dto.financiacion(), "PUBLICO").importe()));
        assertEquals(0, new BigDecimal("60.0").compareTo(parte(dto.financiacion(), "PUBLICO").porcentaje()));
        assertEquals(0, new BigDecimal("40.0").compareTo(parte(dto.financiacion(), "PRIVADO").porcentaje()));
        assertEquals("Ayuntamiento", dto.mayorFinanciador());
        assertEquals(0, new BigDecimal("60.0").compareTo(dto.mayorFinanciadorPorcentaje()));
        // Donación y cuota no tienen el origen indicado
        assertEquals(2, dto.ingresosSinClasificar());
    }

    @Test
    public void agrupaFinanciadoresIgnorandoMayusculas() {
        when(ingresoRepository.findByOngId(1L)).thenReturn(List.of(
                ingreso("100", "OTROS", "PRIVADO", "Fundación Luz", 2025),
                ingreso("50", "OTROS", "PRIVADO", "fundación luz", 2025)));
        when(gastoRepository.findByOngId(1L)).thenReturn(List.of());

        TransparenciaDTO dto = servicio.calcular(1L, 2025);

        assertEquals(1, dto.financiadores().size());
        assertEquals(0, new BigDecimal("150").compareTo(dto.financiadores().get(0).importe()));
    }

    @Test
    public void calculaGastosPorAreaYCuentaLosSinClasificar() {
        when(ingresoRepository.findByOngId(1L)).thenReturn(List.of());
        when(gastoRepository.findByOngId(1L)).thenReturn(List.of(
                gasto("700", AreaGasto.MISION, false, 2025),
                gasto("200", AreaGasto.ADMINISTRACION, false, 2025),
                gasto("100", null, false, 2025)));

        TransparenciaDTO dto = servicio.calcular(1L, 2025);

        assertEquals(3, dto.gastosPorArea().size());
        assertEquals("MISION", dto.gastosPorArea().get(0).clave());
        assertEquals(0, new BigDecimal("70.0").compareTo(parte(dto.gastosPorArea(), "MISION").porcentaje()));
        // El gasto sin área ni proyecto cuenta como administración
        assertEquals(0, new BigDecimal("30.0").compareTo(parte(dto.gastosPorArea(), "ADMINISTRACION").porcentaje()));
        assertEquals(0, BigDecimal.ZERO.compareTo(parte(dto.gastosPorArea(), "CAPTACION").importe()));
        assertEquals(1, dto.gastosSinClasificar());
    }

    @Test
    public void sinDatosNoDivideEntreCero() {
        when(ingresoRepository.findByOngId(1L)).thenReturn(List.of());
        when(gastoRepository.findByOngId(1L)).thenReturn(List.of());

        TransparenciaDTO dto = servicio.calcular(1L, null);

        assertEquals(LocalDate.now().getYear(), dto.anio());
        assertEquals(0, BigDecimal.ZERO.compareTo(dto.resultado()));
        assertNull(dto.mayorFinanciador());
        assertEquals(0, BigDecimal.ZERO.compareTo(parte(dto.financiacion(), "PUBLICO").porcentaje()));
    }

    @Test
    public void sinAnioElegidoUsaElMasRecienteConDatos() {
        when(ingresoRepository.findByOngId(1L)).thenReturn(List.of(ingreso("10", "OTROS", null, null, 2023)));
        when(gastoRepository.findByOngId(1L)).thenReturn(List.of());

        assertEquals(2023, servicio.calcular(1L, null).anio());
    }

    @Test
    public void laPaginaPublicaSoloExisteSiSePublica() {
        when(ingresoRepository.findByOngId(1L)).thenReturn(List.of());
        when(gastoRepository.findByOngId(1L)).thenReturn(List.of());
        when(gobiernoRepository.findByOngIdOrderByOrganoAscOrdenAscIdAsc(1L)).thenReturn(List.of());

        assertNull(servicio.publica(1L, null));

        servicio.publicar(1L, true);
        verify(ongRepository).save(ong);
        assertNotNull(servicio.publica(1L, null));
        assertEquals("ONG de prueba", servicio.publica(1L, null).nombre());
    }
}
