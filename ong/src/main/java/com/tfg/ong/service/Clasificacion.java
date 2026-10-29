package com.tfg.ong.service;

/**
 * Validación de los datos con los que se clasifican ingresos (origen y financiador) para el informe de
 * transparencia. Un valor vacío significa "sin indicar": el informe lo deduce.
 */
public final class Clasificacion {

    public static final String PUBLICO = "PUBLICO";
    public static final String PRIVADO = "PRIVADO";
    public static final int MAX_FINANCIADOR = 120;

    private Clasificacion() {
    }

    /** PUBLICO, PRIVADO o null si viene vacío; cualquier otro valor es un error. */
    public static String origen(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String v = valor.trim().toUpperCase();
        if (!PUBLICO.equals(v) && !PRIVADO.equals(v)) {
            throw new IllegalArgumentException("El origen de la financiación debe ser público o privado");
        }
        return v;
    }

    /** Nombre del financiador sin espacios sobrantes, o null si viene vacío. */
    public static String financiador(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String v = valor.trim();
        if (v.length() > MAX_FINANCIADOR) {
            throw new IllegalArgumentException("El nombre del financiador no puede superar " + MAX_FINANCIADOR + " caracteres");
        }
        return v;
    }
}
