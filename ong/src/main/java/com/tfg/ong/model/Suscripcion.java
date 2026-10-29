package com.tfg.ong.model;

import java.time.LocalDate;

import jakarta.persistence.*;

/**
 * Plan de un administrador (cubre todas las ONG que administra). Todos empiezan con una prueba del plan Pro;
 * después pasan al plan Básico salvo que tengan una suscripción de pago en Stripe.
 */
@Entity
@Table(name = "suscripciones")
public class Suscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_usuario", nullable = false, unique = true)
    private Long usuarioId;

    @Column(name = "prueba_hasta", nullable = false)
    private LocalDate pruebaHasta;

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;

    // Estado de la suscripción en Stripe: active, trialing, past_due, canceled, unpaid...
    private String estado;

    @Column(name = "periodo_fin")
    private LocalDate periodoFin;

    @Column(name = "cancelar_al_final", nullable = false)
    private boolean cancelarAlFinal;

    // ONG que sigue activa en el plan Básico (null = la más antigua)
    @Column(name = "ong_activa_id")
    private Long ongActivaId;

    public Suscripcion() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public LocalDate getPruebaHasta() { return pruebaHasta; }
    public void setPruebaHasta(LocalDate pruebaHasta) { this.pruebaHasta = pruebaHasta; }

    public String getStripeCustomerId() { return stripeCustomerId; }
    public void setStripeCustomerId(String stripeCustomerId) { this.stripeCustomerId = stripeCustomerId; }

    public String getStripeSubscriptionId() { return stripeSubscriptionId; }
    public void setStripeSubscriptionId(String stripeSubscriptionId) { this.stripeSubscriptionId = stripeSubscriptionId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDate getPeriodoFin() { return periodoFin; }
    public void setPeriodoFin(LocalDate periodoFin) { this.periodoFin = periodoFin; }

    public boolean isCancelarAlFinal() { return cancelarAlFinal; }
    public void setCancelarAlFinal(boolean cancelarAlFinal) { this.cancelarAlFinal = cancelarAlFinal; }

    public Long getOngActivaId() { return ongActivaId; }
    public void setOngActivaId(Long ongActivaId) { this.ongActivaId = ongActivaId; }
}
