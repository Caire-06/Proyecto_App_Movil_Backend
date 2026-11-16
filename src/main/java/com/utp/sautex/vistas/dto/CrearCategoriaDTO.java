package com.utp.sautex.vistas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearCategoriaDTO(
        @NotBlank(message = "El nombre de la categoría es obligatorio")
        @Size(max = 50, message = "El nombre no debe superar los 50 caracteres")
        String nombre,

        @Size(max = 200, message = "La descripción no debe superar los 200 caracteres")
        String descripcion
) {
}
