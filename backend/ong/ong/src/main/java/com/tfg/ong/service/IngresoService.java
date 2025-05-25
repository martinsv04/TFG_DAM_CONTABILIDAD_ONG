package com.tfg.ong.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.tfg.ong.model.DetalleFactura;
import com.tfg.ong.model.Factura;
import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.DetalleFacturaRepository;
import com.tfg.ong.repository.FacturaRepository;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class IngresoService {
    
    
    private final IngresoRepository ingresoRepository;

   
    private final FacturaRepository facturaRepository;

    
    private final DetalleFacturaRepository detalleFacturaRepository;

    
    private final UsuarioRepository usuarioRepository;

    public IngresoService(IngresoRepository ingresoRepository, 
                          FacturaRepository facturaRepository, 
                          DetalleFacturaRepository detalleFacturaRepository,
                          UsuarioRepository usuarioRepository) {
        this.ingresoRepository = ingresoRepository;
        this.facturaRepository = facturaRepository;
        this.detalleFacturaRepository = detalleFacturaRepository;
        this.usuarioRepository = usuarioRepository;
    }

      public List<Ingreso> getAllIngresos() {
        return ingresoRepository.findAll();
    }

    public Ingreso getIngresoById(Long id) {
        Optional<Ingreso> ingreso = ingresoRepository.findById(id);
        return ingreso.orElse(null);
    }

    public Ingreso createIngreso(Ingreso ingreso) {
        return ingresoRepository.save(ingreso);
    }

    public Ingreso updateIngresos(Long id, Ingreso ingreso) {
        ingreso.setId(id);
        return ingresoRepository.save(ingreso);
    }

    public void deleteIngreso(Long id) {
        ingresoRepository.deleteById(id);
    }

    @Transactional
    public Ingreso createIngresoConFactura(Ingreso ingreso, Long idUsuario) {

        Usuario usuario = null;
            if (idUsuario != null) {
                usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + idUsuario));
            }

        Ong ong = ingreso.getOng();
            if (ong == null) {
                throw new IllegalArgumentException("La ONG asociada al ingreso no puede ser nula");
            }

        Ingreso ingresoGuardado = ingresoRepository.save(ingreso);

        String numeroFactura = "F-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Factura factura = new Factura();
        factura.setNumero(numeroFactura);
        factura.setFecha(LocalDate.now());
        factura.setOng(ong);
        factura.setUsuario(usuario);
        factura.setTotal(ingreso.getMonto());

        factura = facturaRepository.save(factura);

        DetalleFactura detalle = new DetalleFactura();
        detalle.setFactura(factura);
        detalle.setDescripcion(ingreso.getDescripcion());
        detalle.setCantidad(1);
        detalle.setPrecio(ingreso.getMonto());
        detalle.setIva(BigDecimal.ZERO);

        detalleFacturaRepository.save(detalle);

        return ingresoGuardado;
    }


}
