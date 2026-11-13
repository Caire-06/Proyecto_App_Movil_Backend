package com.utp.sautex.vistas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CrearProductoDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no debe superar los 150 caracteres")
        String nombre,

        @Size(max = 50, message = "La unidad no debe superar los 50 caracteres")
        String unidad,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
        BigDecimal precio,

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,

        @Size(max = 50, message = "La categoría no debe superar los 50 caracteres")
        String categoria,

        @Size(max = 2000000, message = "La imagen es demasiado grande")
        String imagen,

        @Size(max = 500, message = "La descripción no debe superar los 500 caracteres")
        String descripcion
) {
}
