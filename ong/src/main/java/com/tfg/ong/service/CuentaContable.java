package com.tfg.ong.service;

/** Validación del código de cuenta contable (plan de contabilidad de entidades sin fines lucrativos). */
public final class CuentaContable {

    private CuentaContable() {
    }

    /** Devuelve el código sin espacios, o null si está vacío; lanza IllegalArgumentException si no es numérico (3 a 7 cifras). */
    public static String normalizar(String cuenta) {
        if (cuenta == null || cuenta.isBlank()) {
            return null;
        }
        String c = cuenta.trim();
        if (!c.matches("\\d{3,7}")) {
            throw new IllegalArgumentException("La cuenta contable debe ser un código numérico de 3 a 7 cifras (por ejemplo 720)");
        }
        return c;
    }
}
