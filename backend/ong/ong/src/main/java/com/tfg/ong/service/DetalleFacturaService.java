package com.tfg.ong.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tfg.ong.model.DetalleFactura;
import com.tfg.ong.repository.DetalleFacturaRepository;

@Service
public class DetalleFacturaService {

    private final DetalleFacturaRepository detalleFacturaRepository;
    
    public DetalleFacturaService(DetalleFacturaRepository detalleFacturaRepository) {
        this.detalleFacturaRepository = detalleFacturaRepository;
    }
    
    public List<DetalleFactura> getAllDetalleFacturas() {
        return detalleFacturaRepository.findAll();
    }

    public DetalleFactura getDetalleFacturaById(Long id) {
        Optional<DetalleFactura> detalleFactura = detalleFacturaRepository.findById(id);
        return detalleFactura.orElse(null);
    }

    public DetalleFactura createDetalleFactura(DetalleFactura detalleFactura) {
        return detalleFacturaRepository.save(detalleFactura);
    }

    public DetalleFactura updateDetalleFacturas(Long id, DetalleFactura detalleFactura) {
        detalleFactura.setId(id);
        return detalleFacturaRepository.save(detalleFactura);
    }

    public void deleteDetalleFactura(Long id) {
        detalleFacturaRepository.deleteById(id);
    }

}
