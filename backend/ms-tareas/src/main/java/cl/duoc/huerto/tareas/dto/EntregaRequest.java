package cl.duoc.huerto.tareas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// Lo que envía el estudiante al entregar una tarea: su comentario, las rutas de sus fotos y quién es.
public record EntregaRequest(
        @NotNull Long estudianteId,
        @NotBlank(message = "El comentario es obligatorio") String comentario,
        @NotEmpty(message = "Debe adjuntar al menos una foto") List<String> fotos,
        @NotNull Long fechaRealizacion
) {
}
