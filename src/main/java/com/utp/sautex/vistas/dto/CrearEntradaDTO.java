package com.utp.sautex.vistas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearEntradaDTO(
        @NotNull(message = "Seleccione un producto")
        Integer productoId,

        @NotNull(message = "Ingrese la cantidad")
        @Min(value = 1, message = "La cantidad debe ser mayor a cero")
        Integer cantidad,

        @Size(max = 150, message = "La referencia no debe superar los 150 caracteres")
        String referencia,

        @Size(max = 300, message = "La observación no debe superar los 300 caracteres")
        String observacion
) {
}
