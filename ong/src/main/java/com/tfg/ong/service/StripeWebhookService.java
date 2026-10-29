package com.tfg.ong.service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.tfg.ong.model.Suscripcion;
import com.tfg.ong.repository.SuscripcionRepository;

/** Aplica a la base de datos los avisos (webhooks) de Stripe. Es idempotente: repetir un aviso no cambia nada. */
@Service
public class StripeWebhookService {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookService.class);

    private final SuscripcionRepository suscripcionRepository;
    private final PlanService planService;

    public StripeWebhookService(SuscripcionRepository suscripcionRepository, PlanService planService) {
        this.suscripcionRepository = suscripcionRepository;
        this.planService = planService;
    }

    /** @return true si el evento era de un tipo que se trata (aunque no hiciera falta cambiar nada) */
    public boolean procesar(JsonNode evento) {
        String tipo = evento.path("type").asText();
        JsonNode objeto = evento.path("data").path("object");
        if ("checkout.session.completed".equals(tipo)) {
            checkoutCompletado(objeto);
            return true;
        }
        if (tipo.startsWith("customer.subscription.")
                && ("created".equals(tipo.substring(22)) || "updated".equals(tipo.substring(22)) || "deleted".equals(tipo.substring(22)))) {
            suscripcionActualizada(objeto, tipo.endsWith("deleted"));
            return true;
        }
        return false;
    }

    private void checkoutCompletado(JsonNode sesion) {
        if (!"subscription".equals(sesion.path("mode").asText())) {
            return;
        }
        Long adminId = parseId(sesion.path("client_reference_id").asText(null));
        if (adminId == null) {
            log.warn("Checkout de Stripe sin client_reference_id válido");
            return;
        }
        Suscripcion s = planService.suscripcionDe(adminId);
        s.setStripeCustomerId(textoONulo(sesion.path("customer")));
        s.setStripeSubscriptionId(textoONulo(sesion.path("subscription")));
        // El estado definitivo llega con customer.subscription.*; mientras tanto, un pago completado cuenta como activo
        String pago = sesion.path("payment_status").asText();
        if ("paid".equals(pago) || "no_payment_required".equals(pago)) {
            s.setEstado("active");
        }
        suscripcionRepository.save(s);
    }

    private void suscripcionActualizada(JsonNode sub, boolean eliminada) {
        String idSub = textoONulo(sub.path("id"));
        String cliente = textoONulo(sub.path("customer"));
        String estado = eliminada ? "canceled" : sub.path("status").asText();

        Optional<Suscripcion> encontrada = idSub == null ? Optional.empty() : suscripcionRepository.findByStripeSubscriptionId(idSub);
        if (encontrada.isEmpty() && cliente != null) {
            encontrada = suscripcionRepository.findByStripeCustomerId(cliente);
        }
        if (encontrada.isEmpty()) {
            Long adminId = parseId(sub.path("metadata").path("usuario_id").asText(null));
            if (adminId != null) {
                encontrada = Optional.of(planService.suscripcionDe(adminId));
            }
        }
        if (encontrada.isEmpty()) {
            log.warn("Aviso de Stripe de una suscripción desconocida: {}", idSub);
            return;
        }
        Suscripcion s = encontrada.get();

        // Aviso de una suscripción antigua (por ejemplo, cancelada antes de volver a suscribirse): se ignora
        boolean otra = s.getStripeSubscriptionId() != null && idSub != null && !s.getStripeSubscriptionId().equals(idSub);
        if (otra && !("active".equals(estado) || "trialing".equals(estado))) {
            return;
        }

        s.setStripeSubscriptionId(idSub);
        if (cliente != null) {
            s.setStripeCustomerId(cliente);
        }
        s.setEstado(estado);
        s.setCancelarAlFinal(sub.path("cancel_at_period_end").asBoolean(false));
        long fin = finDePeriodo(sub);
        if (fin > 0) {
            s.setPeriodoFin(Instant.ofEpochSecond(fin).atZone(ZoneId.systemDefault()).toLocalDate());
        }
        suscripcionRepository.save(s);
    }

    /** current_period_end está en la suscripción (API antigua) o en sus items (API 2025-03-31 y posteriores). */
    static long finDePeriodo(JsonNode sub) {
        long fin = sub.path("current_period_end").asLong(0);
        if (fin > 0) {
            return fin;
        }
        return sub.path("items").path("data").path(0).path("current_period_end").asLong(0);
    }

    private static String textoONulo(JsonNode nodo) {
        return nodo.isMissingNode() || nodo.isNull() || nodo.asText().isBlank() ? null : nodo.asText();
    }

    private static Long parseId(String texto) {
        try {
            return texto == null ? null : Long.valueOf(texto);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
