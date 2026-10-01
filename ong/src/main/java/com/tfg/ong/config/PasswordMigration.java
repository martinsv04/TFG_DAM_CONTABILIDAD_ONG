package com.tfg.ong.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Al arrancar, convierte a BCrypt las contraseñas que aún estén en texto plano en la base de datos.
 * Es idempotente: una contraseña que ya es un hash BCrypt (empieza por "$2") no se vuelve a tocar,
 * así que se puede ejecutar en cada arranque sin riesgo.
 *
 * Usa SQL directo sobre la tabla usuarios (solo lee y escribe la columna password) en lugar de JPA,
 * para no cargar ONGs, reportes ni otras relaciones y no depender del estado del resto de tablas.
 */
@Component
public class PasswordMigration implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PasswordMigration.class);

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public PasswordMigration(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        int migradas = 0;

        List<Map<String, Object>> filas = jdbcTemplate.queryForList("SELECT id, password FROM usuarios");
        for (Map<String, Object> fila : filas) {
            Object id = fila.get("id");
            Object valor = fila.get("password");
            String password = valor != null ? valor.toString() : null;
            if (password != null && !password.isBlank() && !password.startsWith("$2")) {
                jdbcTemplate.update("UPDATE usuarios SET password = ? WHERE id = ?",
                        passwordEncoder.encode(password), id);
                migradas++;
            }
        }

        if (migradas > 0) {
            log.info("Contraseñas migradas a BCrypt: {}", migradas);
        }
    }
}
