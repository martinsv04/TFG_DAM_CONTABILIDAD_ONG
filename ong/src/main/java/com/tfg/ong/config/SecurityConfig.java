package com.tfg.ong.config;

import com.tfg.ong.repository.UsuarioRepository;
import com.tfg.ong.security.JwtAuthFilter;
import com.tfg.ong.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Datos económicos: solo ADMIN y CONTABLE (ADMIN para modificar o borrar)
    private static final String[] ECONOMICO = {
            "/api/ingresos/**",
            "/api/gastos/**",
            "/api/facturas/**",
            "/api/detallefacturas/**",
            "/api/reportes/**",
            "/api/certificados/**",
            "/api/modelo182/**",
            "/api/proyectos/**",
            "/api/subvenciones/**"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtService jwtService,
                                                   UsuarioRepository usuarioRepository) throws Exception {

        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtService, usuarioRepository);

        http
                // API REST sin sesiones ni cookies: no hace falta CSRF
                .csrf(csrf -> csrf.disable())
                // Reutiliza la configuración CORS de WebConfig
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        // Formulario de contacto de la web (público)
                        .requestMatchers(HttpMethod.POST, "/api/contacto").permitAll()
                        // Avisos de Stripe: públicos, pero el controlador solo acepta los que llevan firma válida
                        .requestMatchers(HttpMethod.POST, "/api/stripe/webhook").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        // ONGs: cualquier usuario autenticado puede ver sus datos y miembros
                        .requestMatchers(HttpMethod.GET, "/api/ongs/usuario/*", "/api/ongs/*", "/api/ongs/*/miembros").authenticated()
                        // Resto de operaciones sobre ONGs (crear, editar, borrar, listar todas): solo ADMIN
                        .requestMatchers("/api/ongs/**").hasRole("ADMIN")

                        // Gestión de usuarios (alta, cambio de rol, borrado...): solo ADMIN
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                        // Plan y facturación: solo ADMIN
                        .requestMatchers("/api/plan/**").hasRole("ADMIN")

                        // Registro de actividad: solo ADMIN
                        .requestMatchers("/api/auditoria/**").hasRole("ADMIN")

                        // Las donaciones las crea el DONANTE (además de ADMIN y CONTABLE)
                        .requestMatchers(HttpMethod.POST, "/api/ingresos").hasAnyRole("ADMIN", "CONTABLE", "DONANTE")

                        // El donante consulta su propio certificado anual
                        .requestMatchers(HttpMethod.GET, "/api/certificados/mi-anual").hasRole("DONANTE")

                        .requestMatchers(HttpMethod.PUT, ECONOMICO).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, ECONOMICO).hasRole("ADMIN")
                        .requestMatchers(ECONOMICO).hasAnyRole("ADMIN", "CONTABLE")

                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
