package com.tfg.ong.controller;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.ong.service.ContactoService;

import jakarta.servlet.http.HttpServletRequest;

/** Formulario de contacto de la web: es público (no requiere sesión). */
@RestController
@RequestMapping("/api/contacto")
public class ContactoController {

    private static final int MAX_POR_HORA = 5;
    private static final long UNA_HORA_MS = 3_600_000L;

    private final ContactoService contactoService;

    // Marcas de tiempo de los envíos recientes de cada dirección IP (límite sencillo contra abusos)
    private final Map<String, Deque<Long>> envios = new ConcurrentHashMap<>();

    public ContactoController(ContactoService contactoService) {
        this.contactoService = contactoService;
    }

    private boolean superaLimite(String ip) {
        long ahora = System.currentTimeMillis();
        Deque<Long> marcas = envios.computeIfAbsent(ip, k -> new ArrayDeque<>());
        synchronized (marcas) {
            while (!marcas.isEmpty() && ahora - marcas.peekFirst() > UNA_HORA_MS) {
                marcas.pollFirst();
            }
            if (marcas.size() >= MAX_POR_HORA) {
                return true;
            }
            marcas.addLast(ahora);
            return false;
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> enviar(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        // Campo trampa «web»: una persona no lo ve ni lo rellena; si viene con texto es un bot y se descarta sin avisar
        Object trampa = payload.get("web");
        if (trampa != null && !trampa.toString().isBlank()) {
            return ResponseEntity.ok(Map.of("message", "Mensaje recibido"));
        }

        if (superaLimite(request.getRemoteAddr())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Has enviado demasiados mensajes seguidos. Inténtalo de nuevo más tarde."));
        }

        try {
            contactoService.registrar(texto(payload.get("nombre")), texto(payload.get("empresa")),
                    texto(payload.get("email")), texto(payload.get("mensaje")));
            return ResponseEntity.ok(Map.of("message", "Mensaje recibido"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", String.valueOf(e.getMessage())));
        }
    }

    private static String texto(Object valor) {
        return valor == null ? null : valor.toString();
    }
}
