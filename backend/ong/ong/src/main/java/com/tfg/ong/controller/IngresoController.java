package com.tfg.ong.controller;

import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Ong;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.repository.OngRepository;
import com.tfg.ong.service.IngresoService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ingresos")
public class IngresoController {

    private final IngresoService ingresoService;

    private final IngresoRepository ingresoRepository;

    private final OngRepository ongRepository;

    public IngresoController(IngresoService ingresoService, IngresoRepository ingresoRepository, OngRepository ongRepository) {
        this.ingresoService = ingresoService;
        this.ingresoRepository = ingresoRepository;
        this.ongRepository = ongRepository;
    }

    @GetMapping
    public List<Ingreso> getAllIngresos() {
        return ingresoService.getAllIngresos();
    }

    @GetMapping("/{id}")
    public Ingreso getIngresoById(@PathVariable Long id) {

        return ingresoService.getIngresoById(id);

    }

    @PutMapping("/{id}")
    public Ingreso updateIngresos(@PathVariable Long id, @RequestBody Ingreso ingreso) {

        return ingresoService.updateIngresos(id, ingreso);

    }

    @DeleteMapping("/{id}")
    public void deleteIngreso(@PathVariable Long id) {

        ingresoService.deleteIngreso(id);

    }

    @GetMapping("/ong/{idOng}")
    public List<Ingreso> getIngresosByOng(@PathVariable Long idOng) {

        return ingresoRepository.findByOngId(idOng);

    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> crearIngresoConFactura(@RequestBody Map<String, Object> payload) {

        try {
            String descripcion = (String) payload.get("descripcion");
            Double monto = Double.parseDouble(payload.get("monto").toString());
            String tipo = (String) payload.get("tipo");
            String fechaStr = (String) payload.get("fecha");
            Long idOng = Long.parseLong(payload.get("id_ong").toString());

            Long idUsuario = null;
            if (payload.get("id_usuario") != null) {
                idUsuario = Long.parseLong(payload.get("id_usuario").toString());
            }

            Ong ong = ongRepository.findById(idOng)
                    .orElseThrow(() -> new RuntimeException("ONG no encontrada"));

            Ingreso ingreso = new Ingreso();
            ingreso.setDescripcion(descripcion);
            ingreso.setMonto(BigDecimal.valueOf(monto));
            ingreso.setTipo(tipo);
            ingreso.setFecha(LocalDate.parse(fechaStr));
            ingreso.setOng(ong);

            Ingreso ingresoGuardado = ingresoService.createIngresoConFactura(ingreso, idUsuario);

            Long facturaId = ingresoGuardado.getId();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ingreso registrado correctamente");
            response.put("facturaId", facturaId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }

    }

}
