package com.tfg.ong.security;

import com.tfg.ong.model.Factura;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Rol;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.DetalleFacturaRepository;
import com.tfg.ong.repository.FacturaRepository;
import com.tfg.ong.repository.GastoRepository;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.repository.OngRepository;
import com.tfg.ong.repository.ReporteRepository;
import com.tfg.ong.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OngAccessServiceTest {

    private OngRepository ongRepository;
    private FacturaRepository facturaRepository;
    private UsuarioRepository usuarioRepository;
    private OngAccessService ongAccess;

    private Ong ong1;
    private Ong ong2;

    @BeforeEach
    void setUp() {
        ongRepository = mock(OngRepository.class);
        facturaRepository = mock(FacturaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);

        ongAccess = new OngAccessService(
                ongRepository,
                facturaRepository,
                mock(IngresoRepository.class),
                mock(GastoRepository.class),
                mock(ReporteRepository.class),
                mock(DetalleFacturaRepository.class),
                usuarioRepository);

        ong1 = new Ong();
        ong1.setId(1L);
        ong2 = new Ong();
        ong2.setId(2L);
    }

    private AuthUser miembroDe(Long ongId) {
        return new AuthUser(10L, "contable@example.com", Rol.CONTABLE, ongId);
    }

    @Test
    void unMiembroPuedeAccederAsuOng() {
        assertTrue(ongAccess.puedeOng(miembroDe(1L), 1L));
    }

    @Test
    void unMiembroNoPuedeAccederAOtraOng() {
        assertFalse(ongAccess.puedeOng(miembroDe(1L), 2L));
        assertThrows(AccessDeniedException.class, () -> ongAccess.verificarOng(miembroDe(1L), 2L));
    }

    @Test
    void unAdminPuedeAccederALasOngsQueAdministra() {
        AuthUser admin = new AuthUser(5L, "admin@example.com", Rol.ADMIN, null);
        when(ongRepository.findByAdminId(5L)).thenReturn(List.of(ong1, ong2));

        assertTrue(ongAccess.puedeOng(admin, 1L));
        assertTrue(ongAccess.puedeOng(admin, 2L));
        assertFalse(ongAccess.puedeOng(admin, 3L));
    }

    @Test
    void sinUsuarioOSinIdDeOngSeDeniega() {
        assertFalse(ongAccess.puedeOng(null, 1L));
        assertFalse(ongAccess.puedeOng(miembroDe(1L), null));
    }

    @Test
    void unaFacturaDeOtraOngSeDeniega() {
        Factura factura = new Factura();
        factura.setId(50L);
        factura.setOng(ong2);
        when(facturaRepository.findById(50L)).thenReturn(Optional.of(factura));

        assertThrows(AccessDeniedException.class, () -> ongAccess.verificarFactura(miembroDe(1L), 50L));
    }

    @Test
    void unaFacturaDeMiOngSePermite() {
        Factura factura = new Factura();
        factura.setId(50L);
        factura.setOng(ong1);
        when(facturaRepository.findById(50L)).thenReturn(Optional.of(factura));

        assertDoesNotThrow(() -> ongAccess.verificarFactura(miembroDe(1L), 50L));
    }

    @Test
    void unElementoQueNoExisteSeDenegaSinRevelarSiExiste() {
        when(facturaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> ongAccess.verificarFactura(miembroDe(1L), 99L));
    }

    @Test
    void unUsuarioSiempreSePuedeEditarASiMismo() {
        AuthUser yo = miembroDe(1L);

        assertDoesNotThrow(() -> ongAccess.verificarUsuario(yo, 10L));
    }

    @Test
    void unUsuarioDeOtraOngNoSePuedeTocar() {
        Usuario otro = new Usuario();
        otro.setId(20L);
        otro.setOng(ong2);
        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(otro));

        assertThrows(AccessDeniedException.class, () -> ongAccess.verificarUsuario(miembroDe(1L), 20L));
    }

    @Test
    void filtrarDejaSoloLosElementosDeMisOngs() {
        Factura propia = new Factura();
        propia.setId(1L);
        propia.setOng(ong1);
        Factura ajena = new Factura();
        ajena.setId(2L);
        ajena.setOng(ong2);
        Factura sinOng = new Factura();
        sinOng.setId(3L);

        List<Factura> resultado = ongAccess.filtrar(miembroDe(1L), List.of(propia, ajena, sinOng), Factura::getOng);

        assertEquals(List.of(propia), resultado);
    }
}
