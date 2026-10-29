package com.tfg.ong.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.tfg.ong.dto.TransparenciaDTO;
import com.tfg.ong.dto.TransparenciaPublicaDTO;
import com.tfg.ong.security.AuthUser;
import com.tfg.ong.security.OngAccessService;
import com.tfg.ong.service.TransparenciaService;

@RestController
public class TransparenciaController {

    private final TransparenciaService transparenciaService;
    private final OngAccessService ongAccess;

    public TransparenciaController(TransparenciaService transparenciaService, OngAccessService ongAccess) {
        this.transparenciaService = transparenciaService;
        this.ongAccess = ongAccess;
    }

    /** Informe interno (ADMIN y CONTABLE de la ONG). */
    @GetMapping("/api/transparencia/ong/{idOng}")
    public TransparenciaDTO informe(@PathVariable Long idOng, @RequestParam(required = false) Integer anio,
                                    @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);
        return transparenciaService.calcular(idOng, anio);
    }

    /** Publica o despublica la página pública de transparencia (solo ADMIN, ver SecurityConfig). */
    @PutMapping("/api/transparencia/ong/{idOng}/publicar")
    public ResponseEntity<Object> publicar(@PathVariable Long idOng, @RequestBody Map<String, Boolean> cuerpo,
                                           @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);
        boolean publica = Boolean.TRUE.equals(cuerpo.get("publica"));
        transparenciaService.publicar(idOng, publica);
        return ResponseEntity.ok(Map.of("publica", publica));
    }

    /** Página pública, sin sesión. Responde 404 si la ONG no ha publicado su transparencia. */
    @GetMapping("/api/publico/transparencia/{idOng}")
    public ResponseEntity<TransparenciaPublicaDTO> publica(@PathVariable Long idOng, @RequestParam(required = false) Integer anio) {
        TransparenciaPublicaDTO dto = transparenciaService.publica(idOng, anio);
        return dto == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(dto);
    }
}
