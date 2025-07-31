package org.example.Exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UsuarioException.NoEncontrado.class)
    public ResponseEntity<Object> handleUsuarioNoEncontrado(UsuarioException.NoEncontrado ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UsuarioException.EmailDuplicado.class)
    public ResponseEntity<Object> handleEmailDuplicado(UsuarioException.EmailDuplicado ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UsuarioException.RolNoEncontrado.class)
    public ResponseEntity<Object> handleRolNoEncontrado(UsuarioException.RolNoEncontrado ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ProductoException.NoEncontrado.class)
    public ResponseEntity<Object> handleProductoNoEncontrado(ProductoException.NoEncontrado ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ProductoException.CategoriaInvalida.class)
    public ResponseEntity<Object> handleCategoriaInvalida(ProductoException.CategoriaInvalida ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ProductoException.ValorInvalido.class)
    public ResponseEntity<Object> handleValorInvalido(ProductoException.ValorInvalido ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Credenciales incorrectas: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", request.getRequestURI());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleUsernameNotFound(UsernameNotFoundException ex, HttpServletRequest request) {
        log.warn("Intento de login con credenciales inválidas: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", request.getRequestURI());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Object> handleExpiredToken(ExpiredJwtException ex, HttpServletRequest request) {
        log.warn("Token JWT expirado: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Token de autenticación expirado", request.getRequestURI());
    }

    @ExceptionHandler({MalformedJwtException.class, UnsupportedJwtException.class, SignatureException.class})
    public ResponseEntity<Object> handleInvalidToken(JwtException ex, HttpServletRequest request) {
        log.warn("Token JWT inválido: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Token de autenticación inválido", request.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "No tiene permisos para acceder a este recurso", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("Error interno no controlado en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ha ocurrido un error interno", request.getRequestURI());
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);
        return new ResponseEntity<>(body, status);
    }
}
