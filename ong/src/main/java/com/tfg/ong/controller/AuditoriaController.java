package com.tfg.ong.controller;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.ong.security.AuthUser;
import com.tfg.ong.security.OngAccessService;
import com.tfg.ong.service.AuditoriaService;

/** Registro de actividad de una ONG. Solo ADMIN (regla en SecurityConfig). */
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final OngAccessService ongAccess;
    private final AuditoriaService auditoriaService;

    public AuditoriaController(OngAccessService ongAccess, AuditoriaService auditoriaService) {
        this.ongAccess = ongAccess;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/ong/{idOng}")
    public ResponseEntity<Object> listar(@PathVariable Long idOng,
                                         @RequestParam(required = false) String entidad,
                                         @RequestParam(required = false) String accion,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                         @RequestParam(defaultValue = "0") int pagina,
                                         @RequestParam(defaultValue = "25") int tamano,
                                         @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);
        try {
            return ResponseEntity.ok(auditoriaService.buscar(idOng, entidad, accion, desde, hasta, pagina, tamano));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
