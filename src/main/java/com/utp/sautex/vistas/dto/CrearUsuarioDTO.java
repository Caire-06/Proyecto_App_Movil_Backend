package com.utp.sautex.vistas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearUsuarioDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, max = 80, message = "El apellido debe tener entre 2 y 80 caracteres")
        String apellido,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El formato del correo no es válido")
        String correo,

        @Size(max = 15, message = "El teléfono no debe superar los 15 dígitos")
        String telefono,

        @Size(max = 200, message = "La dirección no debe superar los 200 caracteres")
        String direccion,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String clave,

        @NotBlank(message = "El rol es obligatorio")
        String rol
) {
}
