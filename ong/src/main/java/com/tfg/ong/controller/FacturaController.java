package com.tfg.ong.controller;

import com.tfg.ong.security.AuthUser;
import com.tfg.ong.security.OngAccessService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.tfg.ong.model.Factura;
import com.tfg.ong.repository.FacturaRepository;
import com.tfg.ong.service.FacturaService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private final OngAccessService ongAccess;

    private final FacturaService facturaService;

    private final FacturaRepository facturaRepository;

    public FacturaController(FacturaService facturaService, FacturaRepository facturaRepository, OngAccessService ongAccess) {
        this.ongAccess = ongAccess;
        this.facturaService = facturaService;
        this.facturaRepository = facturaRepository;
    }

    @GetMapping
    public List<Factura> getAllFacturas(@AuthenticationPrincipal AuthUser caller) {

        return ongAccess.filtrar(caller, facturaService.getAllFacturas(), Factura::getOng);

    }

    @GetMapping("/{id}")
    public Factura getFacturaById(@PathVariable Long id, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarFactura(caller, id);


        return facturaService.getFacturaById(id);

    }

    @PostMapping
    public Factura createFactura(@RequestBody Factura factura, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, factura.getOng() != null ? factura.getOng().getId() : null);


        return facturaService.createFactura(factura);

    }

    @PutMapping("/{id}")
    public Factura updateFactura(@PathVariable Long id, @RequestBody Factura factura, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarFactura(caller, id);


        return facturaService.updateFactura(id, factura);

    }

    @DeleteMapping("/{id}")
    public void deleteFactura(@PathVariable Long id, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarFactura(caller, id);


        facturaService.deleteFactura(id);

    }

    @GetMapping("/ong/{idOng}")
    public List<Factura> getFacturasByOng(@PathVariable Long idOng, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);


        return facturaRepository.findByOngId(idOng);
        
    }

    /** Cuerpo para generar una factura: tipo ("INGRESO" o "GASTO") y el id del movimiento. */
    public record FacturaDeMovimiento(String tipo, Long movimientoId) {}

    @PostMapping("/ong/{idOng}/generar")
    public ResponseEntity<?> generarFactura(@PathVariable Long idOng, @RequestBody FacturaDeMovimiento body,
                                            @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);

        try {
            Factura creada = facturaService.generarDesdeMovimiento(idOng, body.tipo(), body.movimientoId());
            return ResponseEntity.ok(Map.of("id", creada.getId(), "numero", creada.getNumero(), "total", creada.getTotal()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

}
