package com.tfg.ong.controller;

import com.tfg.ong.model.DetalleFactura;
import com.tfg.ong.service.DetalleFacturaService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detallefacturas")
public class DetalleFacturaController {

    private final DetalleFacturaService detalleFacturaService;

    public DetalleFacturaController(DetalleFacturaService detalleFacturaService) {
        this.detalleFacturaService = detalleFacturaService;
    }
    
    @GetMapping
    public List<DetalleFactura> getAllDetalleFacturas() {

        return detalleFacturaService.getAllDetalleFacturas();

    }

    @GetMapping("/{id}")
    public DetalleFactura getDetalleFacturaById(@PathVariable Long id) {

        return detalleFacturaService.getDetalleFacturaById(id);

    }

    @PostMapping
    public DetalleFactura createDetalle(@RequestBody DetalleFactura detalleFactura) {

        return detalleFacturaService.createDetalleFactura(detalleFactura);

    }

    @PutMapping("/{id}")
    public DetalleFactura updateDetalle(@PathVariable Long id, @RequestBody DetalleFactura detalleFactura) {

        return detalleFacturaService.updateDetalleFacturas(id, detalleFactura);

    }

    @DeleteMapping("/{id}")
    public void deleteDetalle(@PathVariable Long id) {

        detalleFacturaService.deleteDetalleFactura(id);
        
    }
}
