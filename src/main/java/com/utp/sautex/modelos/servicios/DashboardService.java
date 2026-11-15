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
import java.util.List;

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

        List<DashboardDTO.ProductoMasVendidoDTO> productosMasVendidos =
                productos.findByActivoTrueOrderByVendidosDesc()
                        .stream()
                        .limit(5)
                        .map(producto ->
                                new DashboardDTO.ProductoMasVendidoDTO(
                                        producto.getIdProducto(),
                                        producto.getNombre(),
                                        producto.getVendidos()
                                ))
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