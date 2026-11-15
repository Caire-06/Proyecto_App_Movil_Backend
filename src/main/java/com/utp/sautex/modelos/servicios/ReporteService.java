package com.utp.sautex.modelos.servicios;

import com.utp.sautex.modelos.entidades.Pedido;
import com.utp.sautex.modelos.entidades.Producto;
import com.utp.sautex.modelos.repositorios.PedidoRepository;
import com.utp.sautex.modelos.repositorios.ProductoRepository;
import com.utp.sautex.vistas.dto.ReporteDTO;
import com.utp.sautex.vistas.dto.ReporteDTO.CategoriaReporteDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    private final PedidoRepository pedidos;
    private final ProductoRepository productos;

    public ReporteService(
            PedidoRepository pedidos,
            ProductoRepository productos) {
        this.pedidos = pedidos;
        this.productos = productos;
    }

    @Transactional(readOnly = true)
    public ReporteDTO obtenerResumen(String periodo) {

        LocalDate hoy = LocalDate.now();
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicio;

        switch (periodo.toLowerCase()) {
            case "semana" ->
                inicio = hoy.minusDays(6).atStartOfDay();

            case "mes" ->
                inicio = hoy.withDayOfMonth(1).atStartOfDay();

            case "año", "anio" ->
                inicio = hoy.withDayOfYear(1).atStartOfDay();

            default ->
                throw new IllegalArgumentException(
                        "El período debe ser semana, mes o año");
        }

        List<Pedido> pedidosPeriodo =
                pedidos.findAllByOrderByFechaGeneracionDesc()
                        .stream()
                        .filter(p -> p.getFechaGeneracion() != null)
                        .filter(p -> !p.getFechaGeneracion().isBefore(inicio))
                        .filter(p -> !p.getFechaGeneracion().isAfter(ahora))
                        .toList();

        BigDecimal totalVentas = pedidosPeriodo.stream()
                .map(Pedido::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long cantidadPedidos = pedidosPeriodo.size();

        BigDecimal ticketPromedio = cantidadPedidos == 0
                ? BigDecimal.ZERO
                : totalVentas.divide(
                        BigDecimal.valueOf(cantidadPedidos),
                        2,
                        RoundingMode.HALF_UP);

        Map<Integer, Integer> unidadesPorProducto = new HashMap<>();

        for (Pedido pedido : pedidosPeriodo) {

            List<Integer> ids = pedido.getListaIdProductos();
            List<Integer> cantidades = pedido.getListaCantidades();

            int limite = Math.min(ids.size(), cantidades.size());

            for (int i = 0; i < limite; i++) {
                Integer id = ids.get(i);
                Integer cantidad = cantidades.get(i);

                if (id != null && cantidad != null && cantidad > 0) {
                    unidadesPorProducto.merge(id, cantidad, Integer::sum);
                }
            }
        }

        Set<Integer> idsProductos = unidadesPorProducto.keySet();

        Map<Integer, Producto> productosPorId =
                productos.findAllById(idsProductos)
                        .stream()
                        .collect(Collectors.toMap(
                                Producto::getIdProducto,
                                producto -> producto));

        String productoMasVendido = unidadesPorProducto.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> {
                    Producto producto = productosPorId.get(entry.getKey());
                    return producto == null
                            ? "Producto no disponible"
                            : producto.getNombre();
                })
                .orElse("Sin ventas");

        Map<String, Integer> unidadesPorCategoria = new LinkedHashMap<>();

        for (Map.Entry<Integer, Integer> entry :
                unidadesPorProducto.entrySet()) {

            Producto producto = productosPorId.get(entry.getKey());

            String categoria = producto == null
                    || producto.getCategoria() == null
                    || producto.getCategoria().isBlank()
                    ? "Sin categoría"
                    : producto.getCategoria().trim();

            unidadesPorCategoria.merge(
                    categoria,
                    entry.getValue(),
                    Integer::sum);
        }

        long totalUnidades = unidadesPorProducto.values()
                .stream()
                .mapToLong(Integer::longValue)
                .sum();

        List<CategoriaReporteDTO> ventasPorCategoria =
                new ArrayList<>();

        unidadesPorCategoria.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue()
                        .reversed())
                .forEach(entry -> {

                    BigDecimal porcentaje = totalUnidades == 0
                            ? BigDecimal.ZERO
                            : BigDecimal.valueOf(entry.getValue())
                                    .multiply(BigDecimal.valueOf(100))
                                    .divide(
                                            BigDecimal.valueOf(totalUnidades),
                                            2,
                                            RoundingMode.HALF_UP);

                    ventasPorCategoria.add(
                            new CategoriaReporteDTO(
                                    entry.getKey(),
                                    entry.getValue(),
                                    porcentaje));
                });

        return new ReporteDTO(
                totalVentas,
                cantidadPedidos,
                ticketPromedio,
                productoMasVendido,
                ventasPorCategoria);
    }
}
