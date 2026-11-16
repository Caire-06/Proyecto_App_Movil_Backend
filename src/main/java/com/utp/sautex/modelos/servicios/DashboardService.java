package com.utp.sautex.modelos.servicios;

import com.utp.sautex.modelos.entidades.Pedido;
import com.utp.sautex.modelos.entidades.Producto;
import com.utp.sautex.modelos.entidades.Usuario;
import com.utp.sautex.modelos.repositorios.PedidoRepository;
import com.utp.sautex.modelos.repositorios.ProductoRepository;
import com.utp.sautex.modelos.repositorios.UsuarioRepository;
import com.utp.sautex.vistas.dto.AlertaStockDTO;
import com.utp.sautex.vistas.dto.DashboardDTO;
import com.utp.sautex.vistas.dto.ResumenAlmacenDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final PedidoRepository pedidos;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;
    private final AlmacenService almacen;

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public DashboardService(
            PedidoRepository pedidos,
            ProductoRepository productos,
            UsuarioRepository usuarios,
            AlmacenService almacen) {

        this.pedidos = pedidos;
        this.productos = productos;
        this.usuarios = usuarios;
        this.almacen = almacen;
    }

    @Transactional(readOnly = true)
    public DashboardDTO obtenerResumen() {

        List<Pedido> listaPedidos =
                pedidos.findAllByOrderByFechaGeneracionDesc();

        BigDecimal totalVendido = listaPedidos.stream()
                .map(Pedido::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<DashboardDTO.VentaRecienteDTO> ventasRecientes =
                listaPedidos.stream()
                        .limit(5)
                        .map(pedido -> {
                            Usuario usuario = pedido.getUsuario();

                            String cliente = usuario == null
                                    ? "Cliente no disponible"
                                    : (usuario.getNombre() + " "
                                            + usuario.getApellido()).trim();

                            String fecha = pedido.getFechaGeneracion() == null
                                    ? ""
                                    : pedido.getFechaGeneracion()
                                            .format(FORMATO_FECHA);

                            return new DashboardDTO.VentaRecienteDTO(
                                    pedido.getIdPedido(),
                                    cliente,
                                    fecha,
                                    pedido.getTotal(),
                                    pedido.getEstado()
                            );
                        })
                        .toList();

        Map<Integer, Producto> productosPorId = new LinkedHashMap<>();
        productos.findAll().forEach(producto -> productosPorId.put(producto.getIdProducto(), producto));

        Map<Integer, Integer> unidadesPorProducto = new LinkedHashMap<>();
        for (Pedido pedido : listaPedidos) {
            List<Integer> ids = pedido.getListaIdProductos();
            List<Integer> cantidades = pedido.getListaCantidades();
            for (int i = 0; i < ids.size(); i++) {
                Integer idProducto = ids.get(i);
                int cantidad = i < cantidades.size() ? cantidades.get(i) : 0;
                if (idProducto == null || cantidad <= 0 || !productosPorId.containsKey(idProducto)) {
                    continue;
                }
                unidadesPorProducto.merge(idProducto, cantidad, Integer::sum);
            }
        }

        List<DashboardDTO.ProductoMasVendidoDTO> productosMasVendidos =
                unidadesPorProducto.entrySet().stream()
                        .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                        .limit(3)
                        .map(entrada -> new DashboardDTO.ProductoMasVendidoDTO(
                                entrada.getKey(),
                                productosPorId.get(entrada.getKey()).getNombre(),
                                entrada.getValue()))
                        .toList();

        ResumenAlmacenDTO resumenAlmacen = almacen.resumen();

        return new DashboardDTO(
                totalVendido,
                pedidos.count(),
                productos.countByActivoTrue(),
                usuarios.countByEstadoIgnoreCase("Activo"),
                ventasRecientes,
                productosMasVendidos,
                resumenAlmacen.alertas()
        );
    }
}