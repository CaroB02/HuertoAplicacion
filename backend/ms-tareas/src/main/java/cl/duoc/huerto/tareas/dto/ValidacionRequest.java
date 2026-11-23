package cl.duoc.huerto.tareas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Lo que envía el docente al revisar una entrega: "Validada" o "Rechazada" y su comentario.
public record ValidacionRequest(
        @NotNull Long validadorId,
        @NotBlank String resultado,
        String comentario,
        @NotNull Long fecha
) {
}
