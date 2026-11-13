package com.utp.sautex.vistas.dto;

import com.utp.sautex.modelos.entidades.Producto;

public record AlertaStockDTO(
        Integer id,
        String nombre,
        String categoria,
        Integer stock
) {

    public static AlertaStockDTO desde(Producto producto) {
        return new AlertaStockDTO(
                producto.getIdProducto(),
                producto.getNombre(),
                producto.getCategoria(),
                producto.getStock()
        );
    }
}
