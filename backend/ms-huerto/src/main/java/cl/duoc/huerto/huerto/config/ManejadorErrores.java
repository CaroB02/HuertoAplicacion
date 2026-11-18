package cl.duoc.huerto.huerto.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

// Convierte los errores en respuestas JSON simples: {"mensaje": "..."}.
// La app revisa el código HTTP (400, 404, 409...) con isSuccessful / HttpException.
@RestControllerAdvice
public class ManejadorErrores {

    // Errores lanzados por los servicios: 404, 409, etc.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> estado(ResponseStatusException e) {
        String mensaje = e.getReason() == null ? "Error" : e.getReason();
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("mensaje", mensaje));
    }

    // Falló alguna validación (@NotBlank, @Email...): 400 Bad Request con el primer mensaje
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Datos no válidos");
        return ResponseEntity.badRequest().body(Map.of("mensaje", mensaje));
    }
}
