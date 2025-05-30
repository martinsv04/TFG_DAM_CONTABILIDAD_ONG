package com.tfg.ong.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tfg.ong.model.Factura;
import com.tfg.ong.repository.FacturaRepository;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;

    public FacturaService(FacturaRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
    }

      public List<Factura> getAllFacturas() {
        return facturaRepository.findAll();
    }

    public Factura getFacturaById(Long id) {
        Optional<Factura> factura = facturaRepository.findById(id);
        return factura.orElse(null);
    }

    public Factura createFactura(Factura factura) {
        if (factura.getDetalles() != null) {
            factura.getDetalles().forEach(detalle -> detalle.setFactura(factura));
        }
        return facturaRepository.save(factura);
    }

    public Factura updateFactura(Long id, Factura factura) {
        factura.setId(id);
        if (factura.getDetalles() != null) {
            factura.getDetalles().forEach(detalle -> detalle.setFactura(factura));
        }
        return facturaRepository.save(factura);
    }


    public void deleteFactura(Long id) {
        facturaRepository.deleteById(id);
    }

}
