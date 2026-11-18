package cl.duoc.huerto.usuarios.dto;

import jakarta.validation.constraints.NotBlank;

// DTO (objeto de transferencia) para el inicio de sesión: solo trae correo y clave.
// Es un "record": Java genera solo el constructor y los métodos correo() y clave().
public record LoginRequest(
        @NotBlank String correo,
        @NotBlank String clave
) {
}
