package com.utp.sautex.vistas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El formato del correo no es válido")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        String clave
) {
}
