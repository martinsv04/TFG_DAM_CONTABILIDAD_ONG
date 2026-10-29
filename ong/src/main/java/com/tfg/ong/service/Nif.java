package com.tfg.ong.service;

import java.util.regex.Pattern;

/** Utilidades para NIF / NIE / CIF: normalización y comprobación de formato (no valida la letra de control). */
public final class Nif {

    // DNI (8 cifras + letra), NIE (X, Y o Z + 7 cifras + letra) o CIF (letra + 7 cifras + dígito o letra de control)
    private static final Pattern FORMATO = Pattern.compile(
            "^([0-9]{8}[A-Z]|[XYZ][0-9]{7}[A-Z]|[ABCDEFGHJNPQRSUVW][0-9]{7}[0-9A-J])$");

    private Nif() {
    }

    /** Quita espacios y guiones y pasa a mayúsculas. Devuelve null si el valor está vacío. */
    public static String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.replaceAll("[\\s\\p{Z}\\p{C}\\-\u2010-\u2015.]", "").toUpperCase();
        return limpio.isEmpty() ? null : limpio;
    }

    public static boolean esValido(String valor) {
        return valor != null && FORMATO.matcher(valor).matches();
    }
}
