package com.tfg.ong.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Antes el número de factura era único en toda la base de datos; ahora lo es dentro de cada ONG
 * (restricción compuesta «id_ong + numero», que crea Hibernate). Hibernate no borra el índice antiguo,
 * así que al arrancar se elimina si existe. Es idempotente: si ya no está, no hace nada.
 */
@Component
public class MigracionFacturas implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MigracionFacturas.class);

    private final JdbcTemplate jdbc;

    public MigracionFacturas(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            // Solo hace falta en bases MySQL antiguas; en una base nueva (por ejemplo PostgreSQL) no existe ese índice
            String producto = jdbc.execute((ConnectionCallback<String>) c -> c.getMetaData().getDatabaseProductName());
            if (producto == null || !(producto.contains("MySQL") || producto.contains("MariaDB"))) {
                return;
            }
            List<String> indices = jdbc.queryForList(
                    "SELECT index_name FROM information_schema.statistics "
                            + "WHERE table_schema = DATABASE() AND table_name = 'facturas' "
                            + "AND non_unique = 0 AND index_name <> 'PRIMARY' "
                            + "GROUP BY index_name HAVING COUNT(*) = 1 AND MAX(column_name) = 'numero'",
                    String.class);
            for (String indice : indices) {
                if (indice != null && indice.matches("[A-Za-z0-9_$]+")) {
                    jdbc.execute("ALTER TABLE facturas DROP INDEX `" + indice + "`");
                    log.info("Eliminado el índice único antiguo «{}» de facturas.numero", indice);
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo revisar el índice único de facturas.numero: {}", e.getMessage());
        }
    }
}
