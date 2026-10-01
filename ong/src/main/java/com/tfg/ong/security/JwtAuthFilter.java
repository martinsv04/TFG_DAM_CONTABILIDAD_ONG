package com.tfg.ong.security;

import com.tfg.ong.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee la cabecera "Authorization: Bearer <token>", valida el token y, si es correcto,
 * carga el usuario desde la base de datos. Se usa el rol de la base de datos (no el del token)
 * para que un cambio de rol o el borrado de un usuario tenga efecto inmediato.
 *
 * No es un @Component a propósito: se crea dentro de SecurityConfig para que solo se ejecute
 * dentro de la cadena de seguridad y no una segunda vez como filtro de servlet.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String cabecera = request.getHeader("Authorization");

        if (cabecera != null && cabecera.startsWith(PREFIJO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String token = cabecera.substring(PREFIJO.length()).trim();

            jwtService.extractUserId(token)
                    .flatMap(usuarioRepository::findById)
                    .ifPresent(usuario -> {
                        AuthUser principal = new AuthUser(usuario.getId(), usuario.getEmail(), usuario.getRol(),
                                usuario.getOng() != null ? usuario.getOng().getId() : null);
                        UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())));
                        autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(autenticacion);
                    });
        }

        filterChain.doFilter(request, response);
    }
}
