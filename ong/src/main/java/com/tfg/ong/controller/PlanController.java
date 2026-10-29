package com.tfg.ong.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Suscripcion;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.OngRepository;
import com.tfg.ong.repository.UsuarioRepository;
import com.tfg.ong.security.AuthUser;
import com.tfg.ong.service.PlanService;
import com.tfg.ong.service.PlanService.Estado;
import com.tfg.ong.service.StripeClient;
import com.tfg.ong.service.StripeClient.StripeException;

/** Plan y facturación del administrador (solo ADMIN, ver SecurityConfig). */
@RestController
@RequestMapping("/api/plan")
public class PlanController {

    public record OngPlan(Long id, String nombre, boolean activa) {
    }

    public record Precio(Long importe, String moneda) {
    }

    public record Limites(int ongs, int miembros, int proyectos, int subvenciones, int movimientos, int facturas) {
    }

    public record PlanDto(String plan, String origen, long diasPrueba, String finPeriodo, boolean cancelaAlFinal,
                          boolean pagoPendiente, boolean tieneSuscripcion, Long ongActivaId, List<OngPlan> ongs,
                          PlanService.Uso uso, Limites limites, boolean stripeConfigurado,
                          Precio precioMensual, Precio precioAnual) {
    }

    public record Intervalo(String intervalo) {
    }

    public record OngActiva(Long ongId) {
    }

    private final PlanService planService;
    private final StripeClient stripe;
    private final OngRepository ongRepository;
    private final UsuarioRepository usuarioRepository;
    private final String urlFrontend;

    public PlanController(PlanService planService, StripeClient stripe, OngRepository ongRepository,
                          UsuarioRepository usuarioRepository,
                          @Value("${app.frontend-url:http://localhost:3000}") String urlFrontend) {
        this.planService = planService;
        this.stripe = stripe;
        this.ongRepository = ongRepository;
        this.usuarioRepository = usuarioRepository;
        this.urlFrontend = urlFrontend.replaceAll("/+$", "");
    }

    @GetMapping
    public PlanDto ver(@AuthenticationPrincipal AuthUser caller) {
        Long adminId = caller.id();
        Suscripcion s = planService.suscripcionDe(adminId);
        Estado e = PlanService.calcular(s, java.time.LocalDate.now());
        Long activa = planService.ongActiva(adminId);
        List<Ong> ongs = ongRepository.findByAdminId(adminId);

        return new PlanDto(
                e.pro() ? "PRO" : "BASICO", e.origen().name(), e.diasPrueba(),
                e.finPeriodo() == null ? null : e.finPeriodo().toString(), e.cancelaAlFinal(), e.pagoPendiente(),
                s.getStripeCustomerId() != null, activa,
                ongs.stream().map(o -> new OngPlan(o.getId(), o.getNombre(), o.getId().equals(activa))).toList(),
                planService.uso(adminId),
                new Limites(PlanService.MAX_ONGS, PlanService.MAX_MIEMBROS, PlanService.MAX_PROYECTOS,
                        PlanService.MAX_SUBVENCIONES, PlanService.MAX_MOVIMIENTOS_ANIO, PlanService.MAX_FACTURAS_ANIO),
                stripe.configurado(), precio(stripe.precio(stripe.precioMensual())), precio(stripe.precio(stripe.precioAnual())));
    }

    private static Precio precio(JsonNode p) {
        if (p == null || p.path("unit_amount").isMissingNode() || p.path("unit_amount").isNull()) {
            return null;
        }
        return new Precio(p.path("unit_amount").asLong(), p.path("currency").asText("eur"));
    }

    @PostMapping("/checkout")
    public ResponseEntity<Object> checkout(@RequestBody Intervalo cuerpo, @AuthenticationPrincipal AuthUser caller) {
        if (!stripe.configurado()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Los pagos todavía no están configurados"));
        }
        boolean anual = "ANUAL".equalsIgnoreCase(cuerpo.intervalo());
        if (!anual && !"MENSUAL".equalsIgnoreCase(cuerpo.intervalo())) {
            return ResponseEntity.badRequest().body(Map.of("message", "El intervalo debe ser MENSUAL o ANUAL"));
        }
        Suscripcion s = planService.suscripcionDe(caller.id());
        String email = usuarioRepository.findById(caller.id()).map(Usuario::getEmail).orElse(null);
        try {
            String url = stripe.crearCheckout(anual ? stripe.precioAnual() : stripe.precioMensual(), caller.id(), email,
                    s.getStripeCustomerId(), urlFrontend + "/plan?pago=ok", urlFrontend + "/plan?pago=cancelado");
            return ResponseEntity.ok(Map.of("url", url));
        } catch (StripeException e) {
            return ResponseEntity.status(502).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/portal")
    public ResponseEntity<Object> portal(@AuthenticationPrincipal AuthUser caller) {
        Suscripcion s = planService.suscripcionDe(caller.id());
        if (s.getStripeCustomerId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Todavía no tienes ninguna suscripción"));
        }
        try {
            return ResponseEntity.ok(Map.of("url", stripe.crearPortal(s.getStripeCustomerId(), urlFrontend + "/plan")));
        } catch (StripeException e) {
            return ResponseEntity.status(502).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/ong-activa")
    public ResponseEntity<Object> ongActiva(@RequestBody OngActiva cuerpo, @AuthenticationPrincipal AuthUser caller) {
        try {
            planService.cambiarOngActiva(caller.id(), cuerpo.ongId());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
