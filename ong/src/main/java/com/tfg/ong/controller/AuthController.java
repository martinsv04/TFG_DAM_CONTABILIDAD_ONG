package com.tfg.ong.controller;

import com.tfg.ong.model.LoginRequest;
import com.tfg.ong.model.LoginResponse;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.UsuarioRepository;
import com.tfg.ong.security.IntentosLogin;
import com.tfg.ong.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    // Frena los intentos de adivinar contraseñas (5 fallos seguidos bloquean 15 minutos)
    private final IntentosLogin intentos = new IntentosLogin();

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {

        if (loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
            return credencialesIncorrectas();
        }

        String clave = request.getRemoteAddr() + "|" + loginRequest.getEmail().trim().toLowerCase();

        long espera = intentos.segundosBloqueado(clave);
        if (espera > 0) {
            long minutos = (espera + 59) / 60;
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", String.valueOf(espera))
                    .body(Map.of("message", "Demasiados intentos fallidos. Vuelve a intentarlo en " + minutos
                            + (minutos == 1 ? " minuto." : " minutos.")));
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(loginRequest.getEmail());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
                intentos.exito(clave);
                String token = jwtService.generateToken(usuario);
                LoginResponse response = new LoginResponse(usuario.getId(), usuario.getRol().toString(), token);
                return ResponseEntity.ok(response);
            }
        }

        intentos.fallo(clave);
        return credencialesIncorrectas();
    }

    private static ResponseEntity<Object> credencialesIncorrectas() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Correo o contraseña incorrectos"));
    }

}
