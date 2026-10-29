package com.tfg.ong.service;

/** Código postal español: 5 cifras; las dos primeras son el código de provincia (01-52). */
public final class CodigoPostal {

    private CodigoPostal() {
    }

    /** Quita espacios. Devuelve null si el valor está vacío. */
    public static String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.replaceAll("\\s", "");
        return limpio.isEmpty() ? null : limpio;
    }

    public static boolean esValido(String valor) {
        if (valor == null || !valor.matches("[0-9]{5}")) {
            return false;
        }
        int provincia = Integer.parseInt(valor.substring(0, 2));
        return provincia >= 1 && provincia <= 52;
    }

    /** Código de provincia (2 cifras) a partir del código postal, o null si no es válido. */
    public static String provincia(String valor) {
        return esValido(valor) ? valor.substring(0, 2) : null;
    }
}
