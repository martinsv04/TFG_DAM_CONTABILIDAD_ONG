package com.tfg.ong.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfg.ong.model.Suscripcion;
import com.tfg.ong.repository.SuscripcionRepository;

class StripeTest {

    private static final String SECRETO = "whsec_prueba";
    private final ObjectMapper json = new ObjectMapper();

    // ------------------------------------------------------------ firma del webhook

    private static String cabecera(String cuerpo, long t, String secreto) {
        return "t=" + t + ",v1=" + StripeClient.hmac(secreto, t + "." + cuerpo);
    }

    @Test
    void aceptaUnaFirmaValida() {
        String cuerpo = "{\"id\":\"evt_1\"}";
        assertTrue(StripeClient.firmaValida(cuerpo, cabecera(cuerpo, 1000, SECRETO), SECRETO, 1010));
    }

    @Test
    void rechazaFirmasFalsasCuerposAlteradosYAvisosAntiguos() {
        String cuerpo = "{\"id\":\"evt_1\"}";
        String buena = cabecera(cuerpo, 1000, SECRETO);

        assertFalse(StripeClient.firmaValida(cuerpo, cabecera(cuerpo, 1000, "otro_secreto"), SECRETO, 1010));
        assertFalse(StripeClient.firmaValida(cuerpo + " ", buena, SECRETO, 1010));
        assertFalse(StripeClient.firmaValida(cuerpo, buena, SECRETO, 1000 + 301));
        assertFalse(StripeClient.firmaValida(cuerpo, null, SECRETO, 1010));
        assertFalse(StripeClient.firmaValida(cuerpo, "basura", SECRETO, 1010));
        assertFalse(StripeClient.firmaValida(cuerpo, buena, "", 1010));
    }

    @Test
    void codificaElFormularioParaStripe() {
        String f = StripeClient.codificar(new java.util.LinkedHashMap<>(Map.of("line_items[0][price]", "price_1")));
        assertEquals("line_items%5B0%5D%5Bprice%5D=price_1", f);
    }

    // ------------------------------------------------------------ avisos

    private StripeWebhookService servicio(SuscripcionRepository repo, Suscripcion s) {
        PlanService plan = mock(PlanService.class);
        when(plan.suscripcionDe(1L)).thenReturn(s);
        when(repo.save(any(Suscripcion.class))).thenAnswer(i -> i.getArgument(0));
        return new StripeWebhookService(repo, plan);
    }

    @Test
    void elCheckoutCompletadoEnlazaElClienteYLaSuscripcion() throws Exception {
        SuscripcionRepository repo = mock(SuscripcionRepository.class);
        Suscripcion s = new Suscripcion();
        s.setUsuarioId(1L);

        JsonNode evento = json.readTree("""
                {"type":"checkout.session.completed","data":{"object":{
                  "mode":"subscription","client_reference_id":"1","customer":"cus_1",
                  "subscription":"sub_1","payment_status":"paid"}}}""");

        assertTrue(servicio(repo, s).procesar(evento));

        assertEquals("cus_1", s.getStripeCustomerId());
        assertEquals("sub_1", s.getStripeSubscriptionId());
        assertEquals("active", s.getEstado());
    }

    @Test
    void laActualizacionDeSuscripcionCambiaEstadoYFinDePeriodo() throws Exception {
        SuscripcionRepository repo = mock(SuscripcionRepository.class);
        Suscripcion s = new Suscripcion();
        s.setUsuarioId(1L);
        s.setStripeSubscriptionId("sub_1");
        when(repo.findByStripeSubscriptionId("sub_1")).thenReturn(Optional.of(s));
        StripeWebhookService servicio = servicio(repo, s);

        // 2026-12-01T00:00:00Z, en los items (API reciente)
        JsonNode evento = json.readTree("""
                {"type":"customer.subscription.updated","data":{"object":{
                  "id":"sub_1","customer":"cus_1","status":"past_due","cancel_at_period_end":true,
                  "items":{"data":[{"current_period_end":1796083200}]}}}}""");

        assertTrue(servicio.procesar(evento));

        assertEquals("past_due", s.getEstado());
        assertTrue(s.isCancelarAlFinal());
        assertNotNull(s.getPeriodoFin());
        assertEquals(2026, s.getPeriodoFin().getYear());
    }

    @Test
    void laCancelacionPasaLaSuscripcionACanceled() throws Exception {
        SuscripcionRepository repo = mock(SuscripcionRepository.class);
        Suscripcion s = new Suscripcion();
        s.setUsuarioId(1L);
        s.setStripeSubscriptionId("sub_1");
        s.setEstado("active");
        when(repo.findByStripeSubscriptionId("sub_1")).thenReturn(Optional.of(s));
        StripeWebhookService servicio = servicio(repo, s);

        servicio.procesar(json.readTree("""
                {"type":"customer.subscription.deleted","data":{"object":{
                  "id":"sub_1","customer":"cus_1","status":"canceled","current_period_end":1796083200}}}"""));

        assertEquals("canceled", s.getEstado());
        s.setPruebaHasta(LocalDate.of(2026, 1, 1));
        assertFalse(PlanService.calcular(s, LocalDate.of(2027, 1, 1)).pro());
    }

    @Test
    void ignoraLosAvisosDeUnaSuscripcionAntiguaYLosEventosDesconocidos() throws Exception {
        SuscripcionRepository repo = mock(SuscripcionRepository.class);
        Suscripcion s = new Suscripcion();
        s.setUsuarioId(1L);
        s.setStripeSubscriptionId("sub_nueva");
        s.setEstado("active");
        when(repo.findByStripeCustomerId("cus_1")).thenReturn(Optional.of(s));
        StripeWebhookService servicio = servicio(repo, s);

        servicio.procesar(json.readTree("""
                {"type":"customer.subscription.deleted","data":{"object":{
                  "id":"sub_vieja","customer":"cus_1","status":"canceled"}}}"""));
        assertEquals("active", s.getEstado());
        assertEquals("sub_nueva", s.getStripeSubscriptionId());

        assertFalse(servicio.procesar(json.readTree("{\"type\":\"invoice.paid\",\"data\":{\"object\":{}}}")));
    }
}
