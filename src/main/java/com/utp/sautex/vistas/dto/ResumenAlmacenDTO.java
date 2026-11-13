package com.utp.sautex.vistas.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResumenAlmacenDTO(
        long totalProductos,
        long stockTotal,
        BigDecimal valorInventario,
        long productosStockBajo,
        List<CategoriaAlmacenDTO> categorias,
        List<AlertaStockDTO> alertas,
        List<MovimientoInventarioDTO> ultimosMovimientos
) {
}
