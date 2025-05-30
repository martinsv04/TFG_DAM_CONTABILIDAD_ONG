package com.tfg.ong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tfg.ong.model.*;
import com.tfg.ong.repository.GastoRepository;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.service.ReporteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReporteController.class)
public class ReporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReporteService reporteService;

    @MockitoBean
    private IngresoRepository ingresoRepository;

    @MockitoBean
    private GastoRepository gastoRepository;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private Ong ong;
    private Reporte reporte;

    @BeforeEach
    public void setup() {
        ong = new Ong();
        ong.setId(1L);

        reporte = new Reporte();
        reporte.setId(1L);
        reporte.setContenido("Reporte de prueba");
        reporte.setFechaGeneracion(LocalDateTime.of(2024, 5, 1, 12, 0));
        reporte.setTipo(TipoReporte.ESTADO_RESULTADOS);
        reporte.setOng(ong);
    }

    @Test
    public void testGetAllReportes() throws Exception {
        when(reporteService.getAllReportes()).thenReturn(singletonList(reporte));

        mockMvc.perform(get("/api/reportes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].contenido").value("Reporte de prueba"));
    }

    @Test
    public void testGetReporteById() throws Exception {
        when(reporteService.getReporteById(1L)).thenReturn(reporte);

        mockMvc.perform(get("/api/reportes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").value("Reporte de prueba"));
    }

    @Test
    public void testUpdateReporte() throws Exception {
        when(reporteService.updateReportes(eq(1L), any())).thenReturn(reporte);

        mockMvc.perform(put("/api/reportes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reporte)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").value("Reporte de prueba"));
    }

    @Test
    public void testDeleteReporte() throws Exception {
        doNothing().when(reporteService).deleteReporte(1L);

        mockMvc.perform(delete("/api/reportes/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGenerarEstadoResultados_Completo() throws Exception {
        Ingreso ingreso = new Ingreso();
        ingreso.setFecha(LocalDate.of(2024, 6, 10));
        ingreso.setMonto(BigDecimal.valueOf(500));
        ingreso.setTipo("DONACIÓN");
        ingreso.setOng(ong);

        Gasto gasto = new Gasto();
        gasto.setFecha(LocalDate.of(2024, 6, 11));
        gasto.setMonto(BigDecimal.valueOf(200));
        gasto.setCategoria(Categoria.OTROS);
        gasto.setOng(ong);

        when(ingresoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of(ingreso));
        when(gastoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of(gasto));

        mockMvc.perform(get("/api/reportes/estado-resultados/1")
                        .param("anio", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingresos.DONACIÓN").value(500.0))
                .andExpect(jsonPath("$.gastos.OTROS").value(200.0))
                .andExpect(jsonPath("$.totalIngresos").value(500.0))
                .andExpect(jsonPath("$.totalGastos").value(200.0))
                .andExpect(jsonPath("$.resultadoNeto").value(300.0));
    }

    @Test
    public void testGenerarEstadoResultados_ValoresNulos() throws Exception {
        Ingreso ingreso = new Ingreso(); ingreso.setFecha(LocalDate.now()); ingreso.setOng(ong);
        Gasto gasto = new Gasto(); gasto.setFecha(LocalDate.now()); gasto.setOng(ong);

        when(ingresoRepository.findByOngIdAndFechaBetween(anyLong(), any(), any())).thenReturn(singletonList(ingreso));
        when(gastoRepository.findByOngIdAndFechaBetween(anyLong(), any(), any())).thenReturn(singletonList(gasto));

        mockMvc.perform(get("/api/reportes/estado-resultados/1").param("anio", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingresos.OTROS").value(0.0))
                .andExpect(jsonPath("$.gastos.OTROS").value(0.0));
    }

    @Test
    public void testGuardarEstadoResultados_ConGastos() throws Exception {
        Gasto gasto = new Gasto(); gasto.setMonto(BigDecimal.TEN); gasto.setOng(ong); gasto.setFecha(LocalDate.now());

        when(ingresoRepository.findByOngIdAndFechaBetween(anyLong(), any(), any())).thenReturn(List.of());
        when(gastoRepository.findByOngIdAndFechaBetween(anyLong(), any(), any())).thenReturn(List.of(gasto));

        mockMvc.perform(post("/api/reportes/estado-resultados/1/guardar")
                        .param("anio", "2024"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reporte de estado de resultados guardado correctamente"));
    }

    @Test
    public void testGuardarEstadoResultados_ConIngresos() throws Exception {
        Ingreso ingreso = new Ingreso(); ingreso.setMonto(BigDecimal.valueOf(150)); ingreso.setTipo("DONACIÓN");
        ingreso.setOng(ong); ingreso.setFecha(LocalDate.now());

        when(ingresoRepository.findByOngIdAndFechaBetween(anyLong(), any(), any())).thenReturn(List.of(ingreso));
        when(gastoRepository.findByOngIdAndFechaBetween(anyLong(), any(), any())).thenReturn(List.of());

        mockMvc.perform(post("/api/reportes/estado-resultados/1/guardar")
                        .param("anio", "2024"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reporte de estado de resultados guardado correctamente"));
    }

    @Test
    public void testGenerarBalanceGeneral_Mensual_Completo() throws Exception {
        LocalDate fecha = LocalDate.of(2024, 1, 15);

        Ingreso ingreso = new Ingreso(); ingreso.setFecha(fecha); ingreso.setMonto(BigDecimal.valueOf(600)); ingreso.setOng(ong);
        Gasto gasto = new Gasto(); gasto.setFecha(fecha); gasto.setMonto(BigDecimal.valueOf(250)); gasto.setOng(ong);

        when(ingresoRepository.findAll()).thenReturn(List.of(ingreso));
        when(gastoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any())).thenReturn(List.of(gasto));

        mockMvc.perform(get("/api/reportes/balance-general/1")
                        .param("anio", "2024")
                        .param("modo", "mensual")
                        .param("periodo", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activos").value(600.0))
                .andExpect(jsonPath("$.pasivos").value(0.0))
                .andExpect(jsonPath("$.fondosNetos").value(350.0));
    }

    @Test
    public void testGuardarBalanceGeneral_Modos() throws Exception {
        Gasto gasto = new Gasto(); gasto.setMonto(BigDecimal.TEN); gasto.setOng(ong); gasto.setFecha(LocalDate.of(2024, 1, 15));

        for (String modo : List.of("mensual", "trimestral", "anual", "otro")) {
            when(ingresoRepository.findByOngIdAndFechaBetween(anyLong(), any(), any())).thenReturn(List.of());
            when(gastoRepository.findByOngIdAndFechaBetween(anyLong(), any(), any())).thenReturn(List.of(gasto));

            mockMvc.perform(post("/api/reportes/balance-general/1/guardar")
                            .param("anio", "2024")
                            .param("modo", modo)
                            .param("periodo", "1"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Reporte de balance general guardado correctamente"));
        }
    }

    @Test
    public void testGenerarBalanceGeneral_Trimestral() throws Exception {
        LocalDate fecha = LocalDate.of(2024, 2, 15);

        Ingreso ingreso = new Ingreso(); ingreso.setFecha(fecha); ingreso.setMonto(BigDecimal.valueOf(300)); ingreso.setOng(ong);
        Gasto gasto = new Gasto(); gasto.setFecha(fecha); gasto.setMonto(BigDecimal.valueOf(100)); gasto.setOng(ong);

        when(ingresoRepository.findAll()).thenReturn(List.of(ingreso));
        when(gastoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any())).thenReturn(List.of(gasto));

        mockMvc.perform(get("/api/reportes/balance-general/1")
                        .param("anio", "2024")
                        .param("modo", "trimestral")
                        .param("periodo", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fondosNetos").value(200.0));
    }

    @Test
    public void testGuardarBalanceGeneral_Trimestral() throws Exception {
        LocalDate fecha = LocalDate.of(2024, 3, 20);

        Gasto gasto = new Gasto(); gasto.setFecha(fecha); gasto.setMonto(BigDecimal.valueOf(100)); gasto.setOng(ong);

        when(ingresoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any())).thenReturn(List.of());
        when(gastoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any())).thenReturn(List.of(gasto));

        mockMvc.perform(post("/api/reportes/balance-general/1/guardar")
                        .param("anio", "2024")
                        .param("modo", "trimestral")
                        .param("periodo", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reporte de balance general guardado correctamente"));
    }

    @Test
    public void testGenerarBalanceGeneral_DefaultModo() throws Exception {
        LocalDate fecha = LocalDate.of(2024, 5, 10);

        Ingreso ingreso = new Ingreso();
        ingreso.setFecha(fecha);
        ingreso.setMonto(BigDecimal.valueOf(1000));
        ingreso.setOng(ong);

        Ingreso ingresoSinOng = new Ingreso();
        ingresoSinOng.setFecha(fecha);
        ingresoSinOng.setMonto(BigDecimal.valueOf(500));
        ingresoSinOng.setOng(null);

        Ingreso ingresoSinFecha = new Ingreso();
        ingresoSinFecha.setOng(ong);
        ingresoSinFecha.setFecha(null);
        ingresoSinFecha.setMonto(BigDecimal.valueOf(700));

        Gasto gasto = new Gasto();
        gasto.setFecha(fecha);
        gasto.setMonto(BigDecimal.valueOf(400));
        gasto.setOng(ong);

        when(ingresoRepository.findAll()).thenReturn(List.of(ingresoSinOng, ingresoSinFecha, ingreso));
        when(gastoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of(gasto));

        mockMvc.perform(get("/api/reportes/balance-general/1")
                        .param("anio", "2024")
                        .param("modo", "noReconocido")
                        .param("periodo", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fondosNetos").value(600.0));
    }


    @Test
    public void testGenerarEstadoResultados_ConValoresNulos() throws Exception {
        Ingreso ingreso = new Ingreso();
        ingreso.setFecha(LocalDate.now());
        ingreso.setOng(ong);

        Gasto gasto = new Gasto();
        gasto.setFecha(LocalDate.now());
        gasto.setOng(ong);

        when(ingresoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of(ingreso));
        when(gastoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of(gasto));

        mockMvc.perform(get("/api/reportes/estado-resultados/1")
                        .param("anio", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingresos.OTROS").value(0.0))
                .andExpect(jsonPath("$.gastos.OTROS").value(0.0));
    }

    @Test
    public void testGenerarEstadoResultados_SinDatos() throws Exception {
        when(ingresoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(gastoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/reportes/estado-resultados/1")
                        .param("anio", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIngresos").value(0.0))
                .andExpect(jsonPath("$.totalGastos").value(0.0));
    }

    @Test
    public void testGuardarBalanceGeneral_SinDatos() throws Exception {
        when(ingresoRepository.findByOngIdAndFechaBetween(anyLong(), any(), any()))
                .thenReturn(List.of());
        when(gastoRepository.findByOngIdAndFechaBetween(anyLong(), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(post("/api/reportes/balance-general/1/guardar")
                        .param("anio", "2024")
                        .param("modo", "mensual")
                        .param("periodo", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No hay datos disponibles para generar el balance general."));
    }


    @Test
    public void testGenerarBalanceGeneral_PeriodoInvalido() throws Exception {
        mockMvc.perform(get("/api/reportes/balance-general/1")
                        .param("anio", "2024")
                        .param("modo", "mensual")
                        .param("periodo", "13"))
                .andExpect(status().isInternalServerError());

        mockMvc.perform(get("/api/reportes/balance-general/1")
                        .param("anio", "2024")
                        .param("modo", "trimestral")
                        .param("periodo", "5"))
                .andExpect(status().isInternalServerError());
    }



    @Test
    public void testGenerarEstadoResultados_ConMontosNegativos() throws Exception {
        Ingreso ingreso = new Ingreso();
        ingreso.setFecha(LocalDate.now());
        ingreso.setMonto(BigDecimal.valueOf(-100));
        ingreso.setTipo("DONACION");
        ingreso.setOng(ong);

        when(ingresoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of(ingreso));
        when(gastoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/reportes/estado-resultados/1")
                        .param("anio", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIngresos").value(-100.0));
    }

    @Test
    public void testGenerarBalanceGeneral_FechaLimite() throws Exception {
        LocalDate fecha = LocalDate.of(2024, 3, 31);

        Ingreso ingreso = new Ingreso();
        ingreso.setFecha(fecha);
        ingreso.setMonto(BigDecimal.valueOf(500));
        ingreso.setOng(ong);

        when(ingresoRepository.findAll()).thenReturn(List.of(ingreso));
        when(gastoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/reportes/balance-general/1")
                        .param("anio", "2024")
                        .param("modo", "trimestral")
                        .param("periodo", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activos").value(500.0));
    }

    @Test
    public void testGenerarBalanceGeneral_ModoAnual() throws Exception {
        Ingreso ingreso = new Ingreso();
        ingreso.setFecha(LocalDate.of(2024, 6, 15));
        ingreso.setMonto(BigDecimal.valueOf(1000));
        ingreso.setOng(ong);

        when(ingresoRepository.findAll()).thenReturn(List.of(ingreso));
        when(gastoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/reportes/balance-general/1")
                        .param("anio", "2024")
                        .param("modo", "anual"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activos").value(1000.0));
    }

    @Test
    public void testGenerarBalanceGeneral_ModoDefault() throws Exception {
        Ingreso ingreso = new Ingreso();
        ingreso.setFecha(LocalDate.of(2024, 6, 15));
        ingreso.setMonto(BigDecimal.valueOf(1000));
        ingreso.setOng(ong);

        when(ingresoRepository.findAll()).thenReturn(List.of(ingreso));
        when(gastoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/reportes/balance-general/1")
                        .param("anio", "2024")
                        .param("modo", "invalido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activos").value(1000.0));
    }

    @Test
    public void testGenerarBalanceGeneral_FechasFueraDeRango() throws Exception {
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fin = LocalDate.of(2024, 12, 31);

        Ingreso ingresoAntes = new Ingreso();
        ingresoAntes.setFecha(LocalDate.of(2023, 12, 31));
        ingresoAntes.setMonto(BigDecimal.valueOf(300));
        ingresoAntes.setOng(ong);

        Ingreso ingresoDespues = new Ingreso();
        ingresoDespues.setFecha(LocalDate.of(2025, 1, 1));
        ingresoDespues.setMonto(BigDecimal.valueOf(300));
        ingresoDespues.setOng(ong);

        Ingreso ingresoValido = new Ingreso();
        ingresoValido.setFecha(LocalDate.of(2024, 6, 15));
        ingresoValido.setMonto(BigDecimal.valueOf(400));
        ingresoValido.setOng(ong);

        Gasto gasto = new Gasto();
        gasto.setFecha(LocalDate.of(2024, 6, 15));
        gasto.setMonto(BigDecimal.valueOf(100));
        gasto.setOng(ong);

        when(ingresoRepository.findAll()).thenReturn(List.of(ingresoAntes, ingresoDespues, ingresoValido));
        when(gastoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any())).thenReturn(List.of(gasto));

        mockMvc.perform(get("/api/reportes/balance-general/1")
                        .param("anio", "2024")
                        .param("modo", "anual")
                        .param("periodo", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fondosNetos").value(300.0));
    }

    @Test
    public void testGenerarBalanceGeneral_RamasFechaExactas() throws Exception {
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fin = LocalDate.of(2024, 12, 31);

        Ingreso ingresoIgualInicio = new Ingreso();
        ingresoIgualInicio.setFecha(inicio);
        ingresoIgualInicio.setMonto(BigDecimal.valueOf(100));
        ingresoIgualInicio.setOng(ong);

        Ingreso ingresoIgualFin = new Ingreso();
        ingresoIgualFin.setFecha(fin);
        ingresoIgualFin.setMonto(BigDecimal.valueOf(200));
        ingresoIgualFin.setOng(ong);

        Ingreso ingresoDentro = new Ingreso();
        ingresoDentro.setFecha(LocalDate.of(2024, 6, 15));
        ingresoDentro.setMonto(BigDecimal.valueOf(300));
        ingresoDentro.setOng(ong);

        Ingreso ingresoAntes = new Ingreso();
        ingresoAntes.setFecha(inicio.minusDays(1));
        ingresoAntes.setMonto(BigDecimal.valueOf(999));
        ingresoAntes.setOng(ong);

        Ingreso ingresoDespues = new Ingreso();
        ingresoDespues.setFecha(fin.plusDays(1));
        ingresoDespues.setMonto(BigDecimal.valueOf(888));
        ingresoDespues.setOng(ong);

        Gasto gasto = new Gasto(); gasto.setFecha(LocalDate.of(2024, 6, 15));
        gasto.setMonto(BigDecimal.valueOf(100));
        gasto.setOng(ong);

        when(ingresoRepository.findAll()).thenReturn(
                List.of(ingresoIgualInicio, ingresoIgualFin, ingresoDentro, ingresoAntes, ingresoDespues)
        );
        when(gastoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any())).thenReturn(List.of(gasto));

        mockMvc.perform(get("/api/reportes/balance-general/1")
                        .param("anio", "2024")
                        .param("modo", "anual")
                        .param("periodo", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activos").value(600.0))
                .andExpect(jsonPath("$.fondosNetos").value(500.0));
    }

    @Test
    public void testGenerarBalanceGeneral_MontosNulosYValidos() throws Exception {
        LocalDate fecha = LocalDate.of(2024, 5, 10);
        Ingreso ingresoValido = new Ingreso();
        ingresoValido.setFecha(fecha);
        ingresoValido.setMonto(BigDecimal.valueOf(300));
        ingresoValido.setOng(ong);

        Ingreso ingresoNulo = new Ingreso();
        ingresoNulo.setFecha(fecha);
        ingresoNulo.setMonto(null);
        ingresoNulo.setOng(ong);

        Gasto gasto = new Gasto();
        gasto.setFecha(fecha);
        gasto.setMonto(BigDecimal.valueOf(100));
        gasto.setOng(ong);

        when(ingresoRepository.findAll()).thenReturn(List.of(ingresoValido, ingresoNulo));
        when(gastoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any())).thenReturn(List.of(gasto));

        mockMvc.perform(get("/api/reportes/balance-general/1")
                        .param("anio", "2024")
                        .param("modo", "anual")
                        .param("periodo", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activos").value(300.0))
                .andExpect(jsonPath("$.fondosNetos").value(200.0));
    }

    @Test
    public void testGenerarBalanceGeneral_GastosConMontoNuloYValido() throws Exception {
        LocalDate fecha = LocalDate.of(2024, 5, 10);

        Ingreso ingreso = new Ingreso();
        ingreso.setFecha(fecha);
        ingreso.setMonto(BigDecimal.valueOf(500));
        ingreso.setOng(ong);

        Gasto gastoValido = new Gasto();
        gastoValido.setFecha(fecha);
        gastoValido.setMonto(BigDecimal.valueOf(300));
        gastoValido.setOng(ong);

        Gasto gastoNulo = new Gasto();
        gastoNulo.setFecha(fecha);
        gastoNulo.setMonto(null);
        gastoNulo.setOng(ong);

        when(ingresoRepository.findAll()).thenReturn(List.of(ingreso));
        when(gastoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of(gastoValido, gastoNulo));

        mockMvc.perform(get("/api/reportes/balance-general/1")
                        .param("anio", "2024")
                        .param("modo", "anual")
                        .param("periodo", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activos").value(500.0))
                .andExpect(jsonPath("$.fondosNetos").value(200.0));
    }


    @Test
    public void testGuardarBalanceGeneral_UsaIngresoCuandoNoHayGastos() throws Exception {
        Ingreso ingreso = new Ingreso();
        ingreso.setMonto(BigDecimal.valueOf(400));
        ingreso.setOng(ong);
        ingreso.setFecha(LocalDate.of(2024, 1, 15));

        when(ingresoRepository.findByOngIdAndFechaBetween(anyLong(), any(), any())).thenReturn(List.of(ingreso));
        when(gastoRepository.findByOngIdAndFechaBetween(anyLong(), any(), any())).thenReturn(List.of());

        mockMvc.perform(post("/api/reportes/balance-general/1/guardar")
                        .param("anio", "2024")
                        .param("modo", "mensual")
                        .param("periodo", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reporte de balance general guardado correctamente"));
    }

    @Test
    public void testGuardarBalanceGeneral_IngresoConMontoNulo() throws Exception {
        Ingreso ingresoValido = new Ingreso();
        ingresoValido.setMonto(BigDecimal.valueOf(100));
        ingresoValido.setFecha(LocalDate.of(2024, 1, 15));
        ingresoValido.setOng(ong);

        Ingreso ingresoNulo = new Ingreso();
        ingresoNulo.setMonto(null);
        ingresoNulo.setFecha(LocalDate.of(2024, 1, 15));
        ingresoNulo.setOng(ong);

        Gasto gasto = new Gasto();
        gasto.setMonto(BigDecimal.valueOf(50));
        gasto.setFecha(LocalDate.of(2024, 1, 15));
        gasto.setOng(ong);

        when(ingresoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of(ingresoValido, ingresoNulo));
        when(gastoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of(gasto));

        mockMvc.perform(post("/api/reportes/balance-general/1/guardar")
                        .param("anio", "2024")
                        .param("modo", "mensual")
                        .param("periodo", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reporte de balance general guardado correctamente"));
    }

    @Test
    public void testGuardarBalanceGeneral_GastoConMontoNulo() throws Exception {
        Ingreso ingreso = new Ingreso();
        ingreso.setMonto(BigDecimal.valueOf(200));
        ingreso.setFecha(LocalDate.of(2024, 1, 15));
        ingreso.setOng(ong);

        Gasto gastoValido = new Gasto();
        gastoValido.setMonto(BigDecimal.valueOf(100));
        gastoValido.setFecha(LocalDate.of(2024, 1, 15));
        gastoValido.setOng(ong);

        Gasto gastoNulo = new Gasto();
        gastoNulo.setMonto(null);
        gastoNulo.setFecha(LocalDate.of(2024, 1, 15));
        gastoNulo.setOng(ong);

        when(ingresoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of(ingreso));
        when(gastoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of(gastoValido, gastoNulo));

        mockMvc.perform(post("/api/reportes/balance-general/1/guardar")
                        .param("anio", "2024")
                        .param("modo", "mensual")
                        .param("periodo", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reporte de balance general guardado correctamente"));
    }
    @Test
    public void testGenerarEstadoResultados_GastoConCategoriaYMontoNulos() throws Exception {
        Gasto gasto = new Gasto();
        gasto.setFecha(LocalDate.of(2024, 1, 1));
        gasto.setCategoria(null);
        gasto.setMonto(null);
        gasto.setOng(ong);

        when(ingresoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(gastoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of(gasto));

        mockMvc.perform(get("/api/reportes/estado-resultados/1")
                        .param("anio", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gastos.OTROS").value(0.0));
    }

    @Test
    public void testGuardarEstadoResultados_GastoCategoriaYMontoNull() throws Exception {
        Gasto gasto = new Gasto();
        gasto.setFecha(LocalDate.of(2024, 6, 15));
        gasto.setCategoria(null);
        gasto.setMonto(null);
        gasto.setOng(ong);

        when(ingresoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(gastoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of(gasto));

        mockMvc.perform(post("/api/reportes/estado-resultados/1/guardar")
                        .param("anio", "2024"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reporte de estado de resultados guardado correctamente"));
    }
    @Test
    public void testGuardarEstadoResultados_IngresoMontoNull() throws Exception {
        Ingreso ingreso = new Ingreso();
        ingreso.setFecha(LocalDate.of(2024, 6, 15));
        ingreso.setMonto(null);
        ingreso.setTipo("DONACIÓN");
        ingreso.setOng(ong);

        when(ingresoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of(ingreso));
        when(gastoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(post("/api/reportes/estado-resultados/1/guardar")
                        .param("anio", "2024"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reporte de estado de resultados guardado correctamente"));
    }

    @Test
    public void testGuardarEstadoResultados_GastoConCategoriaNoNula() throws Exception {
        Gasto gasto = new Gasto();
        gasto.setFecha(LocalDate.of(2024, 5, 15));
        gasto.setCategoria(Categoria.OTROS);
        gasto.setMonto(BigDecimal.valueOf(200));
        gasto.setOng(ong);

        when(ingresoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(gastoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any()))
                .thenReturn(List.of(gasto));

        mockMvc.perform(post("/api/reportes/estado-resultados/1/guardar")
                        .param("anio", "2024"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reporte de estado de resultados guardado correctamente"));
    }

}