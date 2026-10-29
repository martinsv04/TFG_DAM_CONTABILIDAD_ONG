package com.tfg.ong.config;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.tfg.ong.model.Rol;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.UsuarioRepository;
import com.tfg.ong.service.PoliticaPassword;

/**
 * En una base de datos nueva (sin ningún usuario) crea el primer administrador a partir de las variables
 * de entorno ADMIN_EMAIL y ADMIN_PASSWORD. Si ya hay usuarios o las variables no existen, no hace nada.
 */
@Component
public class AdminInicial implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminInicial(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                        @Value("${app.admin.email:}") String email,
                        @Value("${app.admin.password:}") String password) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return;
        }
        if (usuarioRepository.count() > 0) {
            return;
        }
        try {
            PoliticaPassword.validar(password);
        } catch (IllegalArgumentException e) {
            log.warn("No se creó el administrador inicial: {}", e.getMessage());
            return;
        }
        Usuario admin = new Usuario();
        admin.setNombre("Administrador");
        admin.setEmail(email.trim().toLowerCase());
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRol(Rol.ADMIN);
        admin.setCreadoEn(LocalDate.now());
        usuarioRepository.save(admin);
        log.info("Creado el administrador inicial {}", admin.getEmail());
    }
}
