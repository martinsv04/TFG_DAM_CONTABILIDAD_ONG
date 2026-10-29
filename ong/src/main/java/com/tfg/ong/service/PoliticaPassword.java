package com.tfg.ong.service;

import java.nio.charset.StandardCharsets;

/** Reglas mínimas para una contraseña nueva. BCrypt solo usa los primeros 72 bytes, por eso se pone ese tope. */
public final class PoliticaPassword {

    public static final int MINIMO = 8;

    private PoliticaPassword() {
    }

    public static void validar(String password) {
        if (password == null || password.length() < MINIMO) {
            throw new IllegalArgumentException("La contraseña debe tener al menos " + MINIMO + " caracteres");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("La contraseña es demasiado larga (máximo 72 caracteres)");
        }
        boolean letra = password.chars().anyMatch(Character::isLetter);
        boolean numero = password.chars().anyMatch(Character::isDigit);
        if (!letra || !numero) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos una letra y un número");
        }
    }
}
