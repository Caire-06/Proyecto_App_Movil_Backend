package com.utp.sautex.vistas.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardDTO(
        BigDecimal totalVendido,
        long cantidadPedidos,
        long cantidadProductos,
        long cantidadUsuarios,
        List<VentaRecienteDTO> ventasRecientes,
        List<ProductoMasVendidoDTO> productosMasVendidos,
        List<AlertaStockDTO> alertasInventario
) {

    public record VentaRecienteDTO(
            Integer idPedido,
            String cliente,
            String fecha,
            BigDecimal total,
            String estado
    ) {}

    public record ProductoMasVendidoDTO(
            Integer idProducto,
            String nombre,
            Integer vendidos
    ) {}
}