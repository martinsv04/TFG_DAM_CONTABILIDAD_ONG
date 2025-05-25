package com.tfg.ong.controller;

import com.tfg.ong.model.Factura;
import com.tfg.ong.repository.FacturaRepository;
import com.tfg.ong.service.FacturaService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private final FacturaService facturaService;

    private final FacturaRepository facturaRepository;

    public FacturaController(FacturaService facturaService, FacturaRepository facturaRepository) {
        this.facturaService = facturaService;
        this.facturaRepository = facturaRepository;
    }

    @GetMapping
    public List<Factura> getAllFacturas() {

        return facturaService.getAllFacturas();

    }

    @GetMapping("/{id}")
    public Factura getFacturaById(@PathVariable Long id) {

        return facturaService.getFacturaById(id);

    }

    @PostMapping
    public Factura createFactura(@RequestBody Factura factura) {

        return facturaService.createFactura(factura);

    }

    @PutMapping("/{id}")
    public Factura updateFactura(@PathVariable Long id, @RequestBody Factura factura) {

        return facturaService.updateFactura(id, factura);

    }

    @DeleteMapping("/{id}")
    public void deleteFactura(@PathVariable Long id) {

        facturaService.deleteFactura(id);

    }

    @GetMapping("/ong/{idOng}")
    public List<Factura> getFacturasByOng(@PathVariable Long idOng) {

        return facturaRepository.findByOngId(idOng);
        
    }

}
