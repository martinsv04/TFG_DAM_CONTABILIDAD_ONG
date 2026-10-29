package com.tfg.ong.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.tfg.ong.model.MiembroGobierno;
import com.tfg.ong.security.AuthUser;
import com.tfg.ong.security.OngAccessService;
import com.tfg.ong.service.GobiernoService;

/** Órgano de gobierno y equipo de dirección. Lectura para el equipo de la ONG; cambios solo ADMIN (SecurityConfig). */
@RestController
@RequestMapping("/api/gobierno")
public class GobiernoController {

    private final GobiernoService gobiernoService;
    private final OngAccessService ongAccess;

    public GobiernoController(GobiernoService gobiernoService, OngAccessService ongAccess) {
        this.gobiernoService = gobiernoService;
        this.ongAccess = ongAccess;
    }

    @GetMapping("/ong/{idOng}")
    public List<MiembroGobierno> listar(@PathVariable Long idOng, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);
        return gobiernoService.listar(idOng);
    }

    @PostMapping("/ong/{idOng}")
    public ResponseEntity<Object> crear(@PathVariable Long idOng, @RequestBody MiembroGobierno datos,
                                        @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);
        try {
            return ResponseEntity.ok(gobiernoService.crear(idOng, datos));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", String.valueOf(e.getMessage())));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> actualizar(@PathVariable Long id, @RequestBody MiembroGobierno datos,
                                             @AuthenticationPrincipal AuthUser caller) {
        verificar(caller, id);
        try {
            return ResponseEntity.ok(gobiernoService.actualizar(id, datos));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", String.valueOf(e.getMessage())));
        }
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal AuthUser caller) {
        verificar(caller, id);
        gobiernoService.eliminar(id);
    }

    // Si la persona no existe también se deniega, para no revelar qué ids existen
    private void verificar(AuthUser caller, Long id) {
        Long idOng = gobiernoService.ongDe(id);
        if (idOng == null) {
            throw new AccessDeniedException("No tienes acceso a esta ONG");
        }
        ongAccess.verificarOng(caller, idOng);
    }
}
