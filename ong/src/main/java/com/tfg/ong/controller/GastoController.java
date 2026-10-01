package com.tfg.ong.controller;

import com.tfg.ong.security.AuthUser;
import com.tfg.ong.security.OngAccessService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.AccessDeniedException;
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

    private final OngAccessService ongAccess;

    private final GastoService gastoService;

    private final OngRepository ongRepository;

    private final GastoRepository gastoRepository;

    public GastoController(GastoService gastoService, OngRepository ongRepository, GastoRepository gastoRepository, OngAccessService ongAccess) {
        this.ongAccess = ongAccess;
        this.gastoService = gastoService;
        this.ongRepository = ongRepository;
        this.gastoRepository = gastoRepository;
    }

    @GetMapping
    public List<Gasto> getAllGastos(@AuthenticationPrincipal AuthUser caller) {

        return ongAccess.filtrar(caller, gastoService.getAllGastos(), Gasto::getOng);

    }

    @GetMapping("/{id}")
    public Gasto getGastoById(@PathVariable Long id, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarGasto(caller, id);


        return gastoService.getGastoById(id);

    }

    @PutMapping("/{id}")
    public Gasto updateGasto(@PathVariable Long id, @RequestBody Gasto gasto, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarGasto(caller, id);


        return gastoService.updateGastos(id, gasto);

    }

    @DeleteMapping("/{id}")
    public void deleteGasto(@PathVariable Long id, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarGasto(caller, id);


        gastoService.deleteGasto(id);

    }

    @GetMapping("/ong/{idOng}")
    public List<Gasto> getGastosByOng(@PathVariable Long idOng, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);


        return gastoRepository.findByOngId(idOng);

    }

   @PostMapping
    public ResponseEntity<Map<String, Object>> crearGasto(@RequestBody Map<String, Object> payload, @AuthenticationPrincipal AuthUser caller) {

        try {
            String descripcion = (String) payload.get("descripcion");
            Double monto = Double.parseDouble(payload.get("monto").toString());
            String categoriaStr = (String) payload.get("categoria");
            String fechaStr = (String) payload.get("fecha");

            Long idOng = Long.parseLong(payload.get("id_ong").toString());

            // Solo se puede registrar en la ONG propia
            ongAccess.verificarOng(caller, idOng);


            Ong ong = ongRepository.findById(idOng)
                    .orElseThrow(() -> new RuntimeException("ONG no encontrada"));

            Gasto gasto = new Gasto();
            gasto.setDescripcion(descripcion);
            gasto.setMonto(BigDecimal.valueOf(monto));
            gasto.setCategoria(Categoria.valueOf(categoriaStr));
            gasto.setFecha(LocalDate.parse(fechaStr));
            gasto.setOng(ong);

            Gasto gastoGuardado = gastoService.createGasto(gasto);


            Map<String, Object> response = new HashMap<>();
            response.put("message", "Gasto registrado correctamente");
            response.put("id", gastoGuardado.getId());

            return ResponseEntity.ok(response);

        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
        
    }

}
