package com.tfg.ong.controller;

import com.tfg.ong.security.AuthUser;
import com.tfg.ong.security.OngAccessService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.tfg.ong.model.DetalleFactura;
import com.tfg.ong.service.DetalleFacturaService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detallefacturas")
public class DetalleFacturaController {

    private final OngAccessService ongAccess;

    private final DetalleFacturaService detalleFacturaService;

    public DetalleFacturaController(DetalleFacturaService detalleFacturaService, OngAccessService ongAccess) {
        this.ongAccess = ongAccess;
        this.detalleFacturaService = detalleFacturaService;
    }
    
    @GetMapping
    public List<DetalleFactura> getAllDetalleFacturas(@AuthenticationPrincipal AuthUser caller) {

        return ongAccess.filtrar(caller, detalleFacturaService.getAllDetalleFacturas(),
                detalle -> detalle.getFactura() != null ? detalle.getFactura().getOng() : null);

    }

    @GetMapping("/{id}")
    public DetalleFactura getDetalleFacturaById(@PathVariable Long id, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarDetalleFactura(caller, id);


        return detalleFacturaService.getDetalleFacturaById(id);

    }

    @PostMapping
    public DetalleFactura createDetalle(@RequestBody DetalleFactura detalleFactura, @AuthenticationPrincipal AuthUser caller) {
        if (detalleFactura.getFactura() != null) {
            ongAccess.verificarFactura(caller, detalleFactura.getFactura().getId());
        }


        return detalleFacturaService.createDetalleFactura(detalleFactura);

    }

    @PutMapping("/{id}")
    public DetalleFactura updateDetalle(@PathVariable Long id, @RequestBody DetalleFactura detalleFactura, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarDetalleFactura(caller, id);


        return detalleFacturaService.updateDetalleFacturas(id, detalleFactura);

    }

    @DeleteMapping("/{id}")
    public void deleteDetalle(@PathVariable Long id, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarDetalleFactura(caller, id);


        detalleFacturaService.deleteDetalleFactura(id);
        
    }
}
