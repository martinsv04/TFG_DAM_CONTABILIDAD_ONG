package com.tfg.ong.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Limita los intentos fallidos de inicio de sesión: tras {@value #MAXIMO} fallos seguidos con la misma clave
 * (IP + correo) se bloquean nuevos intentos durante 15 minutos. Se guarda en memoria, así que se reinicia
 * al reiniciar el servidor, lo cual es suficiente para frenar adivinaciones de contraseña.
 */
public class IntentosLogin {

    public static final int MAXIMO = 5;
    static final Duration VENTANA = Duration.ofMinutes(15);
    private static final int LIMITE_REGISTROS = 5000;

    private static final class Registro {
        int fallos;
        Instant primerFallo;
        Instant bloqueadoHasta;
    }

    private final Clock reloj;
    private final Map<String, Registro> registros = new HashMap<>();

    public IntentosLogin() {
        this(Clock.systemUTC());
    }

    public IntentosLogin(Clock reloj) {
        this.reloj = reloj;
    }

    /** Segundos que faltan para poder volver a intentarlo; 0 si no hay bloqueo. */
    public synchronized long segundosBloqueado(String clave) {
        Registro r = registros.get(clave);
        if (r == null) {
            return 0;
        }
        Instant ahora = reloj.instant();
        if (r.bloqueadoHasta != null && ahora.isBefore(r.bloqueadoHasta)) {
            return Math.max(1, Duration.between(ahora, r.bloqueadoHasta).toSeconds());
        }
        if (caducado(r, ahora)) {
            registros.remove(clave);
        }
        return 0;
    }

    public synchronized void fallo(String clave) {
        Instant ahora = reloj.instant();
        if (registros.size() >= LIMITE_REGISTROS) {
            registros.values().removeIf(r -> caducado(r, ahora));
        }
        Registro r = registros.get(clave);
        if (r == null || caducado(r, ahora)) {
            r = new Registro();
            r.primerFallo = ahora;
            registros.put(clave, r);
        }
        r.fallos++;
        if (r.fallos >= MAXIMO) {
            r.bloqueadoHasta = ahora.plus(VENTANA);
        }
    }

    public synchronized void exito(String clave) {
        registros.remove(clave);
    }

    private static boolean caducado(Registro r, Instant ahora) {
        if (r.bloqueadoHasta != null) {
            return !ahora.isBefore(r.bloqueadoHasta);
        }
        return !ahora.isBefore(r.primerFallo.plus(VENTANA));
    }
}
