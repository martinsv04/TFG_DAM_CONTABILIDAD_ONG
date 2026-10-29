package com.tfg.ong.controller;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfg.ong.service.StripeClient;
import com.tfg.ong.service.StripeWebhookService;

/** Recibe los avisos de Stripe. Es público (lo llama Stripe), pero solo acepta los que llevan una firma válida. */
@RestController
@RequestMapping("/api/stripe")
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    private final StripeClient stripe;
    private final StripeWebhookService servicio;
    private final ObjectMapper json = new ObjectMapper();

    public StripeWebhookController(StripeClient stripe, StripeWebhookService servicio) {
        this.stripe = stripe;
        this.servicio = servicio;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> recibir(@RequestBody String cuerpo,
                                          @RequestHeader(value = "Stripe-Signature", required = false) String firma) {
        if (!stripe.firmaValida(cuerpo, firma, Instant.now().getEpochSecond())) {
            return ResponseEntity.badRequest().body("Firma no válida");
        }
        try {
            JsonNode evento = json.readTree(cuerpo);
            servicio.procesar(evento);
        } catch (Exception e) {
            // 500 para que Stripe reintente el aviso más tarde
            log.error("Error al procesar un aviso de Stripe", e);
            return ResponseEntity.internalServerError().body("Error");
        }
        return ResponseEntity.ok("ok");
    }
}
