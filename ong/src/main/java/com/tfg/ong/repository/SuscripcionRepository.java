package com.tfg.ong.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tfg.ong.model.Suscripcion;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

    Optional<Suscripcion> findByUsuarioId(Long usuarioId);

    Optional<Suscripcion> findByStripeCustomerId(String stripeCustomerId);

    Optional<Suscripcion> findByStripeSubscriptionId(String stripeSubscriptionId);
}
