package com.tfg.ong.controller;

import com.tfg.ong.model.Gasto;
import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Reporte;
import com.tfg.ong.model.TipoReporte;
import com.tfg.ong.repository.GastoRepository;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.service.ReporteService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    private final IngresoRepository ingresoRepository;

    private final GastoRepository gastoRepository;

    private static final String OTROS = "OTROS";

    public ReporteController(ReporteService reporteService, IngresoRepository ingresoRepository, GastoRepository gastoRepository) {
        this.reporteService = reporteService;
        this.ingresoRepository = ingresoRepository;
        this.gastoRepository = gastoRepository;
    }

    @GetMapping
    public List<Reporte> getAllReportes() {

        return reporteService.getAllReportes();

    }

    @GetMapping("/{id}")
    public Reporte getReporteById(@PathVariable Long id) {

        return reporteService.getReporteById(id);

    }

    @PutMapping("/{id}")
    public Reporte updateReporte(@PathVariable Long id, @RequestBody Reporte reporte) {

        return reporteService.updateReportes(id, reporte);

    }

    @DeleteMapping("/{id}")
    public void deleteReporte(@PathVariable Long id) {

        reporteService.deleteReporte(id);

    }

    @GetMapping("/estado-resultados/{idOng}")
    public ResponseEntity<Map<String, Object>> generarEstadoResultados(

        @PathVariable Long idOng,
        @RequestParam int anio) {

        Map<String, Double> ingresosPorTipo = new HashMap<>();
        Map<String, Double> gastosPorCategoria = new HashMap<>();

        double totalIngresos = 0;
        double totalGastos = 0;

        LocalDate inicio = LocalDate.of(anio, 1, 1);
        LocalDate fin = LocalDate.of(anio, 12, 31);

        List<Ingreso> ingresos = ingresoRepository.findByOngIdAndFechaBetween(idOng, inicio, fin);
        for (Ingreso ingreso : ingresos) {
            String tipo = ingreso.getTipo() != null ? ingreso.getTipo() : OTROS;
            double monto = ingreso.getMonto() != null ? ingreso.getMonto().doubleValue() : 0;
            ingresosPorTipo.put(tipo, ingresosPorTipo.getOrDefault(tipo, 0.0) + monto);
            totalIngresos += monto;
        }

        List<Gasto> gastos = gastoRepository.findByOngIdAndFechaBetween(idOng, inicio, fin);
        for (Gasto gasto : gastos) {
            String categoria = gasto.getCategoria() != null ? gasto.getCategoria().toString() : OTROS;
            double monto = gasto.getMonto() != null ? gasto.getMonto().doubleValue() : 0;
            gastosPorCategoria.put(categoria, gastosPorCategoria.getOrDefault(categoria, 0.0) + monto);
            totalGastos += monto;
        }

        double resultadoNeto = totalIngresos - totalGastos;

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("ingresos", ingresosPorTipo);
        resultado.put("gastos", gastosPorCategoria);
        resultado.put("totalIngresos", totalIngresos);
        resultado.put("totalGastos", totalGastos);
        resultado.put("resultadoNeto", resultadoNeto);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/balance-general/{idOng}")
    public ResponseEntity<Map<String, Object>> generarBalanceGeneral(@PathVariable Long idOng, @RequestParam int anio, @RequestParam(defaultValue = "anual") String modo, @RequestParam(defaultValue = "1") int periodo) {
    
        LocalDate inicio;
        LocalDate fin;
    
        switch (modo) {
            case "mensual":
                inicio = LocalDate.of(anio, periodo, 1);
                fin = inicio.withDayOfMonth(inicio.lengthOfMonth());
                break;
            case "trimestral":
                int mesInicio = (periodo - 1) * 3 + 1;
                inicio = LocalDate.of(anio, mesInicio, 1);
                fin = inicio.plusMonths(2).withDayOfMonth(inicio.plusMonths(2).lengthOfMonth());
                break;
            default: 
                inicio = LocalDate.of(anio, 1, 1);
                fin = LocalDate.of(anio, 12, 31);
                break;
        }
    
        List<Gasto> gastos = gastoRepository.findByOngIdAndFechaBetween(idOng, inicio, fin);
        List<Ingreso> ingresos = ingresoRepository.findAll().stream()
                .filter(i -> i.getOng() != null && i.getOng().getId().equals(idOng))
                .filter(i -> {
                    LocalDate fecha = i.getFecha();
                    return fecha != null && (fecha.isEqual(inicio) || fecha.isAfter(inicio)) && (fecha.isEqual(fin) || fecha.isBefore(fin));
                })
                .toList();
    
        double totalIngresos = ingresos.stream()
                .map(i -> i.getMonto() != null ? i.getMonto().doubleValue() : 0)
                .reduce(0.0, Double::sum);
    
        double totalGastos = gastos.stream()
                .map(g -> g.getMonto() != null ? g.getMonto().doubleValue() : 0)
                .reduce(0.0, Double::sum);
    
        double fondosNetos = totalIngresos - totalGastos;
    
        Map<String, Object> balance = new HashMap<>();
        balance.put("activos", totalIngresos);
        balance.put("pasivos", 0);
        balance.put("fondosNetos", fondosNetos);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/balance-general/{idOng}/guardar")
    public ResponseEntity<String> guardarBalanceGeneral(@PathVariable Long idOng, @RequestParam int anio, @RequestParam(defaultValue = "anual") String modo, @RequestParam(defaultValue = "1") int periodo) {

        LocalDate inicio;
        LocalDate fin;

        switch (modo) {
            case "mensual":
                inicio = LocalDate.of(anio, periodo, 1);
                fin = inicio.withDayOfMonth(inicio.lengthOfMonth());
                break;
            case "trimestral":
                int mesInicio = (periodo - 1) * 3 + 1;
                inicio = LocalDate.of(anio, mesInicio, 1);
                fin = inicio.plusMonths(2).withDayOfMonth(inicio.plusMonths(2).lengthOfMonth());
                break;
            default:
                inicio = LocalDate.of(anio, 1, 1);
                fin = LocalDate.of(anio, 12, 31);
                break;
        }

        List<Ingreso> ingresos = ingresoRepository.findByOngIdAndFechaBetween(idOng, inicio, fin);
        List<Gasto> gastos = gastoRepository.findByOngIdAndFechaBetween(idOng, inicio, fin);

        double totalIngresos = ingresos.stream()
                .map(i -> i.getMonto() != null ? i.getMonto().doubleValue() : 0)
                .reduce(0.0, Double::sum);

        double totalGastos = gastos.stream()
                .map(g -> g.getMonto() != null ? g.getMonto().doubleValue() : 0)
                .reduce(0.0, Double::sum);

        double fondosNetos = totalIngresos - totalGastos;

        Map<String, Object> contenido = new HashMap<>();
        contenido.put("activos", totalIngresos);
        contenido.put("pasivos", 0);
        contenido.put("fondosNetos", fondosNetos);

        Reporte reporte = new Reporte();
        reporte.setOng(!gastos.isEmpty() ? gastos.get(0).getOng() : ingresos.get(0).getOng());
        reporte.setTipo(TipoReporte.BALANCE_GENERAL);
        reporte.setFechaGeneracion(LocalDateTime.now());
        reporte.setContenido(contenido.toString());

        reporteService.createReporte(reporte);

        return ResponseEntity.ok("Reporte de balance general guardado correctamente");

    }

    @PostMapping("/estado-resultados/{idOng}/guardar")
    public ResponseEntity<String> guardarEstadoResultados(@PathVariable Long idOng, @RequestParam int anio) {

        LocalDate inicio = LocalDate.of(anio, 1, 1);
        LocalDate fin = LocalDate.of(anio, 12, 31);

        List<Ingreso> ingresos = ingresoRepository.findByOngIdAndFechaBetween(idOng, inicio, fin);
        List<Gasto> gastos = gastoRepository.findByOngIdAndFechaBetween(idOng, inicio, fin);

        Map<String, Double> ingresosPorTipo = new HashMap<>();
        Map<String, Double> gastosPorCategoria = new HashMap<>();
        double totalIngresos = 0;
        double totalGastos = 0;

        for (Ingreso ingreso : ingresos) {
            String tipo = ingreso.getTipo() != null ? ingreso.getTipo() : OTROS;
            double monto = ingreso.getMonto() != null ? ingreso.getMonto().doubleValue() : 0;
            ingresosPorTipo.put(tipo, ingresosPorTipo.getOrDefault(tipo, 0.0) + monto);
            totalIngresos += monto;
        }

        for (Gasto gasto : gastos) {
            String categoria = gasto.getCategoria() != null ? gasto.getCategoria().toString() : OTROS;
            double monto = gasto.getMonto() != null ? gasto.getMonto().doubleValue() : 0;
            gastosPorCategoria.put(categoria, gastosPorCategoria.getOrDefault(categoria, 0.0) + monto);
            totalGastos += monto;
        }

        double resultadoNeto = totalIngresos - totalGastos;

        Map<String, Object> contenido = new HashMap<>();
        contenido.put("ingresos", ingresosPorTipo);
        contenido.put("gastos", gastosPorCategoria);
        contenido.put("totalIngresos", totalIngresos);
        contenido.put("totalGastos", totalGastos);
        contenido.put("resultadoNeto", resultadoNeto);

        Reporte reporte = new Reporte();
        reporte.setOng(ingresos.isEmpty() ? gastos.get(0).getOng() : ingresos.get(0).getOng());
        reporte.setTipo(TipoReporte.ESTADO_RESULTADOS);
        reporte.setFechaGeneracion(LocalDateTime.now());
        reporte.setContenido(contenido.toString());

        reporteService.createReporte(reporte);

        return ResponseEntity.ok("Reporte de estado de resultados guardado correctamente");
    }

}
