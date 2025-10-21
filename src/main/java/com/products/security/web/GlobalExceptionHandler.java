package com.products.security.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(LockedException.class)
  public ResponseEntity<Map<String,Object>> handleLocked(LockedException ex) {
    return ResponseEntity.status(423).body(Map.of(
        "status", 423,
        "error", "Usuario bloqueado"
    ));
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<Map<String,Object>> handleBadCreds(BadCredentialsException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
        "status", 401,
        "error", "Credenciales inválidas"
    ));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String,Object>> handleValidation(MethodArgumentNotValidException ex) {
    return ResponseEntity.badRequest().body(Map.of(
        "status", 400,
        "error", "Datos inválidos"
    ));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String,Object>> handleIllegalArg(IllegalArgumentException ex) {
    return ResponseEntity.badRequest().body(Map.of(
        "status", 400,
        "error", ex.getMessage()
    ));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String,Object>> handleGeneric(Exception ex) {
    return ResponseEntity.status(500).body(Map.of(
        "status", 500,
        "error", "Error interno"
    ));
  }
}
