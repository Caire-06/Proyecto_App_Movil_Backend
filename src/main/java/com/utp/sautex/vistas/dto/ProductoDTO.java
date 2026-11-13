package com.utp.sautex.vistas.dto;

import com.utp.sautex.modelos.entidades.Producto;

import java.math.BigDecimal;

public record ProductoDTO(
        Integer id,
        String nombre,
        String unidad,
        BigDecimal precio,
        Integer stock,
        Integer vendidos,
        String categoria,
        String imagen,
        String descripcion,
        Boolean activo
) {

    public static ProductoDTO desde(Producto producto) {
        return new ProductoDTO(
                producto.getIdProducto(),
                producto.getNombre(),
                producto.getUnidad(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getVendidos(),
                producto.getCategoria(),
                producto.getImagen(),
                producto.getDescripcion(),
                producto.getActivo()
        );
    }
}
