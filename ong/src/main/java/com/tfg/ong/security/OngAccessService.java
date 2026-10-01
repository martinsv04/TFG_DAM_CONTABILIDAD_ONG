package com.tfg.ong.security;

import com.tfg.ong.model.DetalleFactura;
import com.tfg.ong.model.Factura;
import com.tfg.ong.model.Gasto;
import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Reporte;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.DetalleFacturaRepository;
import com.tfg.ong.repository.FacturaRepository;
import com.tfg.ong.repository.GastoRepository;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.repository.OngRepository;
import com.tfg.ong.repository.ReporteRepository;
import com.tfg.ong.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Decide si un usuario puede trabajar con los datos de una ONG concreta.
 *
 * Un usuario tiene acceso a una ONG si pertenece a ella (usuarios.id_ong) o si es su administrador (ongs.id_admin).
 * Los roles (ADMIN, CONTABLE...) se comprueban aparte en SecurityConfig; esta clase añade la segunda
 * comprobación: "tienes el rol adecuado, pero ¿es TU ONG?".
 *
 * Los métodos verificarXxx lanzan AccessDeniedException (que GlobalExceptionHandler convierte en 403).
 * Si el elemento no existe también se deniega, para no revelar qué ids existen.
 */
@Service
public class OngAccessService {

    private final OngRepository ongRepository;
    private final FacturaRepository facturaRepository;
    private final IngresoRepository ingresoRepository;
    private final GastoRepository gastoRepository;
    private final ReporteRepository reporteRepository;
    private final DetalleFacturaRepository detalleFacturaRepository;
    private final UsuarioRepository usuarioRepository;

    public OngAccessService(OngRepository ongRepository,
                            FacturaRepository facturaRepository,
                            IngresoRepository ingresoRepository,
                            GastoRepository gastoRepository,
                            ReporteRepository reporteRepository,
                            DetalleFacturaRepository detalleFacturaRepository,
                            UsuarioRepository usuarioRepository) {
        this.ongRepository = ongRepository;
        this.facturaRepository = facturaRepository;
        this.ingresoRepository = ingresoRepository;
        this.gastoRepository = gastoRepository;
        this.reporteRepository = reporteRepository;
        this.detalleFacturaRepository = detalleFacturaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** Ids de las ONGs a las que el usuario pertenece o que administra. */
    private Set<Long> ongsAccesibles(AuthUser usuario) {
        Set<Long> ids = new HashSet<>();
        if (usuario == null) {
            return ids;
        }
        if (usuario.ongId() != null) {
            ids.add(usuario.ongId());
        }
        for (Ong ong : ongRepository.findByAdminId(usuario.id())) {
            ids.add(ong.getId());
        }
        return ids;
    }

    public boolean puedeOng(AuthUser usuario, Long ongId) {
        return ongId != null && ongsAccesibles(usuario).contains(ongId);
    }

    public void verificarOng(AuthUser usuario, Long ongId) {
        if (!puedeOng(usuario, ongId)) {
            throw new AccessDeniedException("No tienes acceso a esta ONG");
        }
    }

    public void verificarFactura(AuthUser usuario, Long facturaId) {
        verificarOng(usuario, ongIdDe(facturaId,
                id -> facturaRepository.findById(id).map(Factura::getOng)));
    }

    public void verificarIngreso(AuthUser usuario, Long ingresoId) {
        verificarOng(usuario, ongIdDe(ingresoId,
                id -> ingresoRepository.findById(id).map(Ingreso::getOng)));
    }

    public void verificarGasto(AuthUser usuario, Long gastoId) {
        verificarOng(usuario, ongIdDe(gastoId,
                id -> gastoRepository.findById(id).map(Gasto::getOng)));
    }

    public void verificarReporte(AuthUser usuario, Long reporteId) {
        verificarOng(usuario, ongIdDe(reporteId,
                id -> reporteRepository.findById(id).map(Reporte::getOng)));
    }

    public void verificarDetalleFactura(AuthUser usuario, Long detalleId) {
        verificarOng(usuario, ongIdDe(detalleId,
                id -> detalleFacturaRepository.findById(id).map(DetalleFactura::getFactura).map(Factura::getOng)));
    }

    /** Un usuario puede operar sobre sí mismo o sobre los miembros de sus ONGs. */
    public void verificarUsuario(AuthUser usuario, Long usuarioId) {
        if (usuario != null && usuarioId != null && usuarioId.equals(usuario.id())) {
            return;
        }
        verificarOng(usuario, ongIdDe(usuarioId,
                id -> usuarioRepository.findById(id).map(Usuario::getOng)));
    }

    /** Deja en la lista solo los elementos que pertenecen a ONGs a las que el usuario tiene acceso. */
    public <T> List<T> filtrar(AuthUser usuario, List<T> elementos, Function<T, Ong> ongDe) {
        Set<Long> permitidas = ongsAccesibles(usuario);
        return elementos.stream()
                .filter(elemento -> {
                    Ong ong = ongDe.apply(elemento);
                    return ong != null && permitidas.contains(ong.getId());
                })
                .toList();
    }

    /** Busca la ONG del elemento con el id dado; devuelve null si el id es null o el elemento no existe. */
    private Long ongIdDe(Long id, Function<Long, Optional<Ong>> buscar) {
        if (id == null) {
            return null;
        }
        return buscar.apply(id).map(Ong::getId).orElse(null);
    }
}
