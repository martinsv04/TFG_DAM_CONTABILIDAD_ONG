package com.tfg.ong.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.tfg.ong.model.DetalleFactura;
import com.tfg.ong.model.Factura;
import com.tfg.ong.model.Gasto;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.DetalleFacturaRepository;
import com.tfg.ong.repository.FacturaRepository;
import com.tfg.ong.repository.GastoRepository;
import com.tfg.ong.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class GastoService {

    private final GastoRepository gastoRepository;

    private final FacturaRepository facturaRepository;

    private final DetalleFacturaRepository detalleFacturaRepository;

    private final UsuarioRepository usuarioRepository;

    public GastoService(GastoRepository gastoRepository, 
                        FacturaRepository facturaRepository, 
                        DetalleFacturaRepository detalleFacturaRepository,
                        UsuarioRepository usuarioRepository) {
        this.gastoRepository = gastoRepository;
        this.facturaRepository = facturaRepository;
        this.detalleFacturaRepository = detalleFacturaRepository;
        this.usuarioRepository = usuarioRepository;
    }

      public List<Gasto> getAllGastos() {
        return gastoRepository.findAll();
    }

    public Gasto getGastoById(Long id) {
        Optional<Gasto> gasto = gastoRepository.findById(id);
        return gasto.orElse(null);
    }

    public Gasto createGasto(Gasto gasto) {
        return gastoRepository.save(gasto);
    }

    public Gasto updateGastos(Long id, Gasto gasto) {
        gasto.setId(id);
        return gastoRepository.save(gasto);
    }

    public void deleteGasto(Long id) {
        gastoRepository.deleteById(id);
    }

    @Transactional
    public Gasto createGastoConFactura(Gasto gasto, Long idUsuario) {

        Usuario usuario = null;
        if (idUsuario != null) {
            usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + idUsuario));
        }

        Ong ong = gasto.getOng();
        if (ong == null) {
            throw new IllegalArgumentException("La ONG asociada al gasto no puede ser nula");
        }

        Gasto gastoGuardado = gastoRepository.save(gasto);

        String numeroFactura = "G-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Factura factura = new Factura();
        factura.setNumero(numeroFactura);
        factura.setFecha(LocalDate.now());
        factura.setOng(ong);
        factura.setUsuario(usuario);
        factura.setTotal(gasto.getMonto());

        factura = facturaRepository.save(factura);

        DetalleFactura detalle = new DetalleFactura();
        detalle.setFactura(factura);
        detalle.setDescripcion(gasto.getDescripcion());
        detalle.setCantidad(1);
        detalle.setPrecio(gasto.getMonto());
        detalle.setIva(BigDecimal.ZERO);

        detalleFacturaRepository.save(detalle);

        return gastoGuardado;
    }



}
