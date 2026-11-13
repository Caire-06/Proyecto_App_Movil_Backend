package com.utp.sautex.vistas.dto;

import jakarta.validation.constraints.Size;

public record ActualizarPerfilDTO(
        @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres")
        String nombre,

        @Size(min = 2, max = 80, message = "El apellido debe tener entre 2 y 80 caracteres")
        String apellido,

        @Size(max = 15, message = "El teléfono no debe superar los 15 dígitos")
        String telefono,

        @Size(max = 200, message = "La dirección no debe superar los 200 caracteres")
        String direccion,

        @Size(max = 500, message = "La foto no debe superar los 500 caracteres")
        String foto
) {
}
