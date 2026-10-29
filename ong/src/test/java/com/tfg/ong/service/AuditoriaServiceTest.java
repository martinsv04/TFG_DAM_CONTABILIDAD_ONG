package com.tfg.ong.service;

import com.tfg.ong.model.RegistroAuditoria;
import com.tfg.ong.model.Rol;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.AuditoriaRepository;
import com.tfg.ong.repository.UsuarioRepository;
import com.tfg.ong.security.AuthUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private AuditoriaRepository auditoriaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AuditoriaService service;

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private void autenticar() {
        AuthUser usuario = new AuthUser(7L, "laura@ong.org", Rol.ADMIN, 1L);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
    }

    @Test
    void registraConElUsuarioAutenticado() {
        autenticar();
        Usuario laura = new Usuario();
        laura.setNombre("Laura Admin");
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(laura));

        service.registrar(1L, "INGRESO", 5L, RegistroAuditoria.CREAR, "«Donación» · 100,00 €");

        ArgumentCaptor<RegistroAuditoria> captor = ArgumentCaptor.forClass(RegistroAuditoria.class);
        verify(auditoriaRepository).save(captor.capture());
        RegistroAuditoria r = captor.getValue();
        assertEquals("Laura Admin", r.getUsuarioNombre());
        assertEquals(7L, r.getUsuarioId());
        assertEquals("ADMIN", r.getUsuarioRol());
        assertEquals(1L, r.getOngId());
        assertEquals("INGRESO", r.getEntidad());
        assertEquals(5L, r.getEntidadId());
        assertNotNull(r.getFecha());
    }

    @Test
    void sinUsuarioAutenticadoSeAnotaComoSistema() {
        service.registrar(1L, "GASTO", 2L, RegistroAuditoria.ELIMINAR, "x");

        ArgumentCaptor<RegistroAuditoria> captor = ArgumentCaptor.forClass(RegistroAuditoria.class);
        verify(auditoriaRepository).save(captor.capture());
        assertEquals("Sistema", captor.getValue().getUsuarioNombre());
        assertNull(captor.getValue().getUsuarioId());
    }

    @Test
    void recortaLasDescripcionesLargas() {
        service.registrar(1L, "GASTO", 2L, RegistroAuditoria.CREAR, "a".repeat(1000));

        ArgumentCaptor<RegistroAuditoria> captor = ArgumentCaptor.forClass(RegistroAuditoria.class);
        verify(auditoriaRepository).save(captor.capture());
        assertEquals(600, captor.getValue().getDescripcion().length());
    }

    @Test
    void siFallaElGuardadoNoSePropagaElError() {
        when(auditoriaRepository.save(any())).thenThrow(new RuntimeException("BD caída"));

        assertDoesNotThrow(() -> service.registrar(1L, "GASTO", 2L, RegistroAuditoria.CREAR, "x"));
    }

    @Test
    void buscaConFiltrosYLimitaElTamano() {
        when(auditoriaRepository.buscar(eq(1L), eq("INGRESO"), eq("CREAR"), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new RegistroAuditoria())));

        AuditoriaService.Pagina p = service.buscar(1L, "ingreso", "crear", LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-31"), -3, 5000);

        assertEquals(1, p.contenido().size());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(auditoriaRepository).buscar(eq(1L), eq("INGRESO"), eq("CREAR"), eq(LocalDate.parse("2026-01-01").atStartOfDay()),
                eq(LocalDate.parse("2026-02-01").atStartOfDay()), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(100, captor.getValue().getPageSize());
    }

    @Test
    void rechazaFiltrosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> service.buscar(1L, "OTRA", null, null, null, 0, 25));
        assertThrows(IllegalArgumentException.class, () -> service.buscar(1L, null, "BORRAR", null, null, 0, 25));
        verify(auditoriaRepository, never()).buscar(any(), any(), any(), any(), any(), any());
    }
}
