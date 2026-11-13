package com.utp.sautex.vistas.dto;

import com.utp.sautex.modelos.entidades.MovimientoInventario;

import java.time.format.DateTimeFormatter;

public record MovimientoInventarioDTO(
        Integer id,
        Integer productoId,
        String productoNombre,
        String tipo,
        Integer cantidad,
        Integer stockAnterior,
        Integer stockNuevo,
        String referencia,
        String observacion,
        Integer usuarioId,
        String usuarioNombre,
        String fecha
) {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static MovimientoInventarioDTO desde(MovimientoInventario movimiento) {
        return new MovimientoInventarioDTO(
                movimiento.getIdMovimiento(),
                movimiento.getProductoId(),
                movimiento.getProductoNombre(),
                movimiento.getTipo(),
                movimiento.getCantidad(),
                movimiento.getStockAnterior(),
                movimiento.getStockNuevo(),
                movimiento.getReferencia(),
                movimiento.getObservacion(),
                movimiento.getUsuarioId(),
                movimiento.getUsuarioNombre(),
                movimiento.getFecha().format(FORMATO_FECHA)
        );
    }
}
