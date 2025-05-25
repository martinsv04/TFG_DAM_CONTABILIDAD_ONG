package com.tfg.ong.controller;

import com.tfg.ong.model.Categoria;
import com.tfg.ong.model.Gasto;
import com.tfg.ong.model.Ong;
import com.tfg.ong.repository.GastoRepository;
import com.tfg.ong.repository.OngRepository;
import com.tfg.ong.service.GastoService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gastos")
public class GastoController {

    private final GastoService gastoService;

    private final OngRepository ongRepository;

    private final GastoRepository gastoRepository;

    public GastoController(GastoService gastoService, OngRepository ongRepository, GastoRepository gastoRepository) {
        this.gastoService = gastoService;
        this.ongRepository = ongRepository;
        this.gastoRepository = gastoRepository;
    }

    @GetMapping
    public List<Gasto> getAllGastos() {

        return gastoService.getAllGastos();

    }

    @GetMapping("/{id}")
    public Gasto getGastoById(@PathVariable Long id) {

        return gastoService.getGastoById(id);

    }

    @PutMapping("/{id}")
    public Gasto updateGasto(@PathVariable Long id, @RequestBody Gasto gasto) {

        return gastoService.updateGastos(id, gasto);

    }

    @DeleteMapping("/{id}")
    public void deleteGasto(@PathVariable Long id) {

        gastoService.deleteGasto(id);

    }

    @GetMapping("/ong/{idOng}")
    public List<Gasto> getGastosByOng(@PathVariable Long idOng) {

        return gastoRepository.findByOngId(idOng);

    }

   @PostMapping
    public ResponseEntity<Map<String, Object>> crearGastoConFactura(@RequestBody Map<String, Object> payload) {

        try {
            String descripcion = (String) payload.get("descripcion");
            Double monto = Double.parseDouble(payload.get("monto").toString());
            String categoriaStr = (String) payload.get("categoria");
            String fechaStr = (String) payload.get("fecha");

            Long idOng = Long.parseLong(payload.get("id_ong").toString());

            Long idUsuario = null;
            if (payload.get("id_usuario") != null) {
                idUsuario = Long.parseLong(payload.get("id_usuario").toString());
            }

            Ong ong = ongRepository.findById(idOng)
                    .orElseThrow(() -> new RuntimeException("ONG no encontrada"));

            Gasto gasto = new Gasto();
            gasto.setDescripcion(descripcion);
            gasto.setMonto(BigDecimal.valueOf(monto));
            gasto.setCategoria(Categoria.valueOf(categoriaStr));
            gasto.setFecha(LocalDate.parse(fechaStr));
            gasto.setOng(ong);

            Gasto gastoGuardado = gastoService.createGastoConFactura(gasto, idUsuario);

            Long facturaId = gastoGuardado.getId();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Gasto registrado correctamente");
            response.put("facturaId", facturaId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
        
    }

}
