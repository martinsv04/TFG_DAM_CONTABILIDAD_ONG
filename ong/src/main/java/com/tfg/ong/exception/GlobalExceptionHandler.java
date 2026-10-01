package com.tfg.ong.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Sin permisos: debe ser un 403, no un 500
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        return new ResponseEntity<>(errorBody(ex), HttpStatus.FORBIDDEN);
    }

    // Datos de entrada no válidos (por ejemplo, contraseña vacía al crear un usuario)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(errorBody(ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        ex.printStackTrace();

        return new ResponseEntity<>(errorBody(ex), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Map<String, Object> errorBody(Exception ex) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", LocalDateTime.now());
        errorBody.put("error", ex.getClass().getSimpleName());
        errorBody.put("message", ex.getMessage());
        return errorBody;
    }
}
