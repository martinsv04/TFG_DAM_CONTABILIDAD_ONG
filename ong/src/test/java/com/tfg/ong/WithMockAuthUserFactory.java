package com.tfg.ong;

import com.tfg.ong.security.AuthUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

public class WithMockAuthUserFactory implements WithSecurityContextFactory<WithMockAuthUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockAuthUser anotacion) {
        Long ongId = anotacion.ongId() > 0 ? anotacion.ongId() : null;
        AuthUser principal = new AuthUser(anotacion.id(), anotacion.email(), anotacion.rol(), ongId);

        Authentication autenticacion = UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + anotacion.rol().name())));

        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacion);
        return contexto;
    }
}
