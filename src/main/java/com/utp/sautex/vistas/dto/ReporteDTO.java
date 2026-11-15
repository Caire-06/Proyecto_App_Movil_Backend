package com.utp.sautex.vistas.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReporteDTO(
        BigDecimal totalVentas,
        long cantidadPedidos,
        BigDecimal ticketPromedio,
        String productoMasVendido,
        List<CategoriaReporteDTO> ventasPorCategoria
) {
    public record CategoriaReporteDTO(
            String nombre,
            long cantidad,
            BigDecimal porcentaje
    ) {
    }
}
