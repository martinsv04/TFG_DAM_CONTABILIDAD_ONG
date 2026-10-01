package com.tfg.ong.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.tfg.ong.model.DetalleFactura;
import com.tfg.ong.model.Factura;
import com.tfg.ong.model.Gasto;
import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Ong;
import com.tfg.ong.repository.FacturaRepository;
import com.tfg.ong.repository.GastoRepository;
import com.tfg.ong.repository.IngresoRepository;

import jakarta.transaction.Transactional;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;

    private final IngresoRepository ingresoRepository;

    private final GastoRepository gastoRepository;

    public FacturaService(FacturaRepository facturaRepository, IngresoRepository ingresoRepository, GastoRepository gastoRepository) {
        this.facturaRepository = facturaRepository;
        this.ingresoRepository = ingresoRepository;
        this.gastoRepository = gastoRepository;
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


    /**
     * Genera la factura de un movimiento (ingreso o gasto) ya registrado.
     * El número, la fecha y el importe los fija el servidor a partir del propio movimiento.
     *
     * @param tipo "INGRESO" o "GASTO"
     */
    @Transactional
    public Factura generarDesdeMovimiento(Long idOng, String tipo, Long idMovimiento) {
        if (tipo == null || idMovimiento == null) {
            throw new IllegalArgumentException("Hay que indicar el tipo de movimiento y cuál es");
        }

        Factura factura = new Factura();
        Ong ong;
        BigDecimal monto;
        String descripcion;
        String prefijo;

        switch (tipo.toUpperCase()) {
            case "INGRESO" -> {
                Ingreso ingreso = ingresoRepository.findById(idMovimiento)
                        .orElseThrow(() -> new IllegalArgumentException("El ingreso no existe"));
                if (ingreso.getOng() == null || !idOng.equals(ingreso.getOng().getId())) {
                    throw new IllegalArgumentException("El ingreso no pertenece a esta ONG");
                }
                if (facturaRepository.existsByIngresoId(idMovimiento)) {
                    throw new IllegalArgumentException("Este ingreso ya tiene una factura");
                }
                factura.setIngreso(ingreso);
                factura.setUsuario(ingreso.getUsuario());
                ong = ingreso.getOng();
                monto = ingreso.getMonto();
                descripcion = ingreso.getDescripcion();
                prefijo = "F-";
            }
            case "GASTO" -> {
                Gasto gasto = gastoRepository.findById(idMovimiento)
                        .orElseThrow(() -> new IllegalArgumentException("El gasto no existe"));
                if (gasto.getOng() == null || !idOng.equals(gasto.getOng().getId())) {
                    throw new IllegalArgumentException("El gasto no pertenece a esta ONG");
                }
                if (facturaRepository.existsByGastoId(idMovimiento)) {
                    throw new IllegalArgumentException("Este gasto ya tiene una factura");
                }
                factura.setGasto(gasto);
                ong = gasto.getOng();
                monto = gasto.getMonto();
                descripcion = gasto.getDescripcion();
                prefijo = "G-";
            }
            default -> throw new IllegalArgumentException("El tipo debe ser INGRESO o GASTO");
        }

        if (monto == null) {
            throw new IllegalArgumentException("El movimiento no tiene importe");
        }

        factura.setNumero(prefijo + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        factura.setFecha(LocalDate.now());
        factura.setOng(ong);
        factura.setTotal(monto.setScale(2, RoundingMode.HALF_UP));

        DetalleFactura detalle = new DetalleFactura();
        detalle.setFactura(factura);
        detalle.setDescripcion(descripcion == null || descripcion.isBlank() ? "Sin descripción" : descripcion);
        detalle.setCantidad(1);
        detalle.setPrecio(monto);
        detalle.setIva(BigDecimal.ZERO);
        factura.setDetalles(new ArrayList<>(List.of(detalle)));

        return facturaRepository.save(factura);
    }

    public void deleteFactura(Long id) {
        facturaRepository.deleteById(id);
    }

}
