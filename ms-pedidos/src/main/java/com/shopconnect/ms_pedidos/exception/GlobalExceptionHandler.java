package com.shopconnect.ms_pedidos.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * MANEJADOR GLOBAL DE EXCEPCIONES
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * @RestControllerAdvice intercepta excepciones de TODOS los @RestController
 * de la aplicación y devuelve respuestas JSON estructuradas.
 *
 * Sin este handler, un error 500 mostraría el stack trace completo de Java
 * al cliente (mala práctica de seguridad y experiencia de usuario).
 *
 * Con este handler, el cliente recibe un JSON limpio:
 * {
 *   "timestamp": "2025-06-01T14:30:00",
 *   "status": 400,
 *   "error": "Errores de validación",
 *   "campos": { "nombre": "El nombre es obligatorio" }
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja errores de validación @Valid.
     *
     * Se activa cuando el @RequestBody del Controller falla las validaciones
     * @NotBlank, @NotNull, @Size, @Positive, @Email, etc.
     * Spring lanza MethodArgumentNotValidException automáticamente antes de
     * llegar al método del Controller.
     *
     * Respuesta: 400 Bad Request con detalle de cada campo inválido.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidaciones(
            MethodArgumentNotValidException ex) {

        log.warn("[GlobalExceptionHandler] Error de validación");

        // Extraer errores por campo
        Map<String, String> erroresPorCampo = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            // error.getField(): nombre del campo Java (ej: "nombre", "precioBase")
            // error.getDefaultMessage(): el mensaje del @NotBlank(message="...")
            erroresPorCampo.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now().toString());
        respuesta.put("status", 400);
        respuesta.put("error", "Errores de validación");
        respuesta.put("campos", erroresPorCampo);

        return ResponseEntity.badRequest().body(respuesta);
    }

    /**
     * Maneja IllegalArgumentException (reglas de negocio violadas).
     * Ejemplos: número duplicado, nombre de tipo duplicado, amenidad repetida.
     * Respuesta: 409 Conflict.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarConflicto(
            IllegalArgumentException ex, WebRequest request) {

        log.warn("[GlobalExceptionHandler] Conflicto: {}", ex.getMessage());

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now().toString());
        respuesta.put("status", 409);
        respuesta.put("error", "Conflicto");
        respuesta.put("mensaje", ex.getMessage());
        respuesta.put("path", request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(respuesta);
    }

    /**
     * Maneja RuntimeException (recursos no encontrados, tipos no válidos, etc.).
     * Respuesta: 404 Not Found.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> manejarRuntime(
            RuntimeException ex, WebRequest request) {

        log.error("[GlobalExceptionHandler] RuntimeException: {}", ex.getMessage());

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now().toString());
        respuesta.put("status", 404);
        respuesta.put("error", "Recurso no encontrado");
        respuesta.put("mensaje", ex.getMessage());
        respuesta.put("path", request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
    }

    /**
     * Captura cualquier excepción no manejada por los handlers anteriores.
     * Respuesta: 500 Internal Server Error con mensaje genérico (no exponemos internals).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarGeneral(
            Exception ex, WebRequest request) {

        log.error("[GlobalExceptionHandler] Error inesperado: {}", ex.getMessage(), ex);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now().toString());
        respuesta.put("status", 500);
        respuesta.put("error", "Error interno del servidor");
        respuesta.put("mensaje", "Ocurrió un error inesperado. Contacte al administrador.");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
    }
}
