package com.tfg.ong.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Cliente mínimo de la API de Stripe (sin la librería oficial): crea sesiones de pago de suscripción y del
 * portal de cliente, consulta precios y verifica la firma de los webhooks.
 */
@Component
public class StripeClient {

    public static class StripeException extends RuntimeException {
        public StripeException(String mensaje) {
            super(mensaje);
        }
    }

    private static final String BASE = "https://api.stripe.com/v1";
    private static final long TOLERANCIA_SEGUNDOS = 300;
    private static final long CACHE_PRECIOS_MS = 10 * 60 * 1000L;

    private final String claveSecreta;
    private final String secretoWebhook;
    private final String precioMensual;
    private final String precioAnual;
    private final ObjectMapper json = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final Map<String, Object[]> cachePrecios = new ConcurrentHashMap<>();

    public StripeClient(@Value("${stripe.secret-key:}") String claveSecreta,
                        @Value("${stripe.webhook-secret:}") String secretoWebhook,
                        @Value("${stripe.price-mensual:}") String precioMensual,
                        @Value("${stripe.price-anual:}") String precioAnual) {
        this.claveSecreta = claveSecreta;
        this.secretoWebhook = secretoWebhook;
        this.precioMensual = precioMensual;
        this.precioAnual = precioAnual;
    }

    public boolean configurado() {
        return !claveSecreta.isBlank() && !precioMensual.isBlank() && !precioAnual.isBlank();
    }

    public String precioMensual() {
        return precioMensual;
    }

    public String precioAnual() {
        return precioAnual;
    }

    public boolean webhookConfigurado() {
        return !secretoWebhook.isBlank();
    }

    // ---------------------------------------------------------------- llamadas a Stripe

    /** Crea una sesión de Stripe Checkout para suscribirse; devuelve la URL a la que enviar al usuario. */
    public String crearCheckout(String precio, Long adminId, String email, String clienteStripe,
                                String urlOk, String urlCancelado) {
        Map<String, String> f = new LinkedHashMap<>();
        f.put("mode", "subscription");
        f.put("line_items[0][price]", precio);
        f.put("line_items[0][quantity]", "1");
        f.put("success_url", urlOk);
        f.put("cancel_url", urlCancelado);
        f.put("client_reference_id", String.valueOf(adminId));
        f.put("subscription_data[metadata][usuario_id]", String.valueOf(adminId));
        f.put("allow_promotion_codes", "true");
        if (clienteStripe != null && !clienteStripe.isBlank()) {
            f.put("customer", clienteStripe);
        } else if (email != null && !email.isBlank()) {
            f.put("customer_email", email);
        }
        return peticion("POST", "/checkout/sessions", f).path("url").asText();
    }

    /** Crea una sesión del portal de cliente (cambiar tarjeta, ver facturas, cancelar). */
    public String crearPortal(String clienteStripe, String urlVuelta) {
        Map<String, String> f = new LinkedHashMap<>();
        f.put("customer", clienteStripe);
        f.put("return_url", urlVuelta);
        return peticion("POST", "/billing_portal/sessions", f).path("url").asText();
    }

    /** Importe (en céntimos), moneda e intervalo de un precio; null si no se puede consultar. Se guarda 10 minutos. */
    public JsonNode precio(String idPrecio) {
        if (idPrecio == null || idPrecio.isBlank() || claveSecreta.isBlank()) {
            return null;
        }
        Object[] guardado = cachePrecios.get(idPrecio);
        if (guardado != null && System.currentTimeMillis() - (Long) guardado[0] < CACHE_PRECIOS_MS) {
            return (JsonNode) guardado[1];
        }
        try {
            JsonNode precio = peticion("GET", "/prices/" + idPrecio, null);
            cachePrecios.put(idPrecio, new Object[]{System.currentTimeMillis(), precio});
            return precio;
        } catch (StripeException e) {
            return null;
        }
    }

    private JsonNode peticion(String metodo, String ruta, Map<String, String> formulario) {
        if (claveSecreta.isBlank()) {
            throw new StripeException("Stripe no está configurado");
        }
        try {
            HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(BASE + ruta))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + claveSecreta);
            if ("POST".equals(metodo)) {
                b.header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(codificar(formulario)));
            } else {
                b.GET();
            }
            HttpResponse<String> r = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
            JsonNode cuerpo = json.readTree(r.body());
            if (r.statusCode() >= 400) {
                throw new StripeException(cuerpo.path("error").path("message").asText("Error de Stripe"));
            }
            return cuerpo;
        } catch (IOException e) {
            throw new StripeException("No se pudo contactar con Stripe");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new StripeException("Llamada a Stripe interrumpida");
        }
    }

    static String codificar(Map<String, String> formulario) {
        return formulario.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    // ---------------------------------------------------------------- webhook

    /** Comprueba la cabecera Stripe-Signature del aviso recibido. */
    public boolean firmaValida(String cuerpo, String cabecera, long ahoraSegundos) {
        return firmaValida(cuerpo, cabecera, secretoWebhook, ahoraSegundos);
    }

    static boolean firmaValida(String cuerpo, String cabecera, String secreto, long ahoraSegundos) {
        if (cuerpo == null || cabecera == null || secreto == null || secreto.isBlank()) {
            return false;
        }
        String marca = null;
        java.util.List<String> firmas = new java.util.ArrayList<>();
        for (String parte : cabecera.split(",")) {
            String[] kv = parte.trim().split("=", 2);
            if (kv.length != 2) {
                continue;
            }
            if (kv[0].equals("t")) {
                marca = kv[1];
            } else if (kv[0].equals("v1")) {
                firmas.add(kv[1]);
            }
        }
        if (marca == null || firmas.isEmpty()) {
            return false;
        }
        long t;
        try {
            t = Long.parseLong(marca);
        } catch (NumberFormatException e) {
            return false;
        }
        if (Math.abs(ahoraSegundos - t) > TOLERANCIA_SEGUNDOS) {
            return false;
        }
        String esperada = hmac(secreto, marca + "." + cuerpo);
        for (String f : firmas) {
            if (MessageDigest.isEqual(esperada.getBytes(StandardCharsets.UTF_8), f.getBytes(StandardCharsets.UTF_8))) {
                return true;
            }
        }
        return false;
    }

    static String hmac(String secreto, String datos) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(datos.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }
}
