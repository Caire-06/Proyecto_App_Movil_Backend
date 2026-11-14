package com.utp.sautex.modelos.servicios;

import com.utp.sautex.modelos.entidades.MovimientoInventario;
import com.utp.sautex.modelos.entidades.Pedido;
import com.utp.sautex.modelos.entidades.Producto;
import com.utp.sautex.modelos.entidades.Usuario;
import com.utp.sautex.modelos.repositorios.MovimientoInventarioRepository;
import com.utp.sautex.modelos.repositorios.PedidoRepository;
import com.utp.sautex.modelos.repositorios.ProductoRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PedidoService {

    /** IGV vigente en Perú (18%). */
    public static final BigDecimal TASA_IGV = new BigDecimal("0.18");

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PedidoRepository pedidos;
    private final ProductoRepository productos;
    private final MovimientoInventarioRepository movimientos;

    public PedidoService(PedidoRepository pedidos, ProductoRepository productos,
                         MovimientoInventarioRepository movimientos) {
        this.pedidos = pedidos;
        this.productos = productos;
        this.movimientos = movimientos;
    }

    // ------------------------------------------------------------------
    // DTOs del flujo de pedidos
    // ------------------------------------------------------------------

    public record ItemPedidoRequest(
            @NotNull(message = "Seleccione un producto")
            Integer productoId,

            @NotNull(message = "Ingrese la cantidad")
            @Min(value = 1, message = "La cantidad debe ser mayor a cero")
            Integer cantidad
    ) {
    }

    public record CrearPedidoRequest(
            @NotEmpty(message = "Debe seleccionar al menos un producto")
            List<@Valid @NotNull(message = "Producto inválido") ItemPedidoRequest> productos
    ) {
    }

    public record CambiarEstadoRequest(
            @NotBlank(message = "Ingrese el estado del pedido")
            String estado
    ) {
    }

    public record ItemPedidoResponse(
            Integer productoId,
            String nombre,
            String imagen,
            Integer cantidad,
            BigDecimal precioUnitario,
            BigDecimal importe
    ) {
    }

    public record PedidoResponse(
            Integer idPedido,
            Integer idUsuario,
            String cliente,
            String fechaGeneracion,
            String estado,
            List<Integer> idProductos,
            List<ItemPedidoResponse> productos,
            BigDecimal subtotal,
            BigDecimal igv,
            BigDecimal total
    ) {
    }

    // ------------------------------------------------------------------
    // Operaciones
    // ------------------------------------------------------------------

    @Transactional
    public PedidoResponse generar(CrearPedidoRequest request, Usuario usuario) {
        if (request == null || request.productos() == null || request.productos().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un producto");
        }

        // Agrupa productos repetidos sumando sus cantidades (manteniendo el orden).
        Map<Integer, Integer> cantidadesPorProducto = new LinkedHashMap<>();
        for (ItemPedidoRequest item : request.productos()) {
            if (item == null || item.productoId() == null) {
                throw new IllegalArgumentException("Seleccione un producto");
            }
            if (item.cantidad() == null || item.cantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
            }
            cantidadesPorProducto.merge(item.productoId(), item.cantidad(), Integer::sum);
        }

        List<Integer> ids = new ArrayList<>();
        List<Integer> cantidades = new ArrayList<>();
        List<Producto> comprados = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        // Validación completa antes de modificar el stock.
        for (Map.Entry<Integer, Integer> entrada : cantidadesPorProducto.entrySet()) {
            Producto producto = productos.findById(entrada.getKey())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Producto no encontrado (id " + entrada.getKey() + ")"));

            if (!producto.getActivo()) {
                throw new IllegalArgumentException("El producto '" + producto.getNombre() + "' no está disponible");
            }
            if (producto.getStock() < entrada.getValue()) {
                throw new IllegalArgumentException("Stock insuficiente para '" + producto.getNombre()
                        + "'. Disponible: " + producto.getStock());
            }

            ids.add(producto.getIdProducto());
            cantidades.add(entrada.getValue());
            comprados.add(producto);
            subtotal = subtotal.add(producto.getPrecio().multiply(BigDecimal.valueOf(entrada.getValue())));
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal igv = subtotal.multiply(TASA_IGV).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv);

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setFechaGeneracion(LocalDateTime.now());
        pedido.setEstado(Pedido.ESTADO_EN_CAMINO);
        pedido.setListaIdProductos(ids);
        pedido.setListaCantidades(cantidades);
        pedido.setSubtotal(subtotal);
        pedido.setIgv(igv);
        pedido.setTotal(total);
        Pedido guardado = pedidos.save(pedido);

        // Descuenta stock, suma vendidos y registra la salida en el inventario.
        for (int i = 0; i < comprados.size(); i++) {
            Producto producto = comprados.get(i);
            int cantidad = cantidades.get(i);
            int stockAnterior = producto.getStock();

            producto.setStock(stockAnterior - cantidad);
            producto.setVendidos((producto.getVendidos() == null ? 0 : producto.getVendidos()) + cantidad);
            productos.save(producto);

            registrarSalida(producto, cantidad, stockAnterior, guardado.getIdPedido(), usuario);
        }

        return aRespuesta(guardado);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarDeUsuario(Integer idUsuario) {
        return pedidos.findByUsuarioIdUsuarioOrderByFechaGeneracionDesc(idUsuario).stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarTodos(String estado) {
        List<Pedido> lista;
        if (estado == null || estado.isBlank()) {
            lista = pedidos.findAllByOrderByFechaGeneracionDesc();
        } else {
            lista = pedidos.findByEstadoOrderByFechaGeneracionDesc(normalizarEstado(estado));
        }
        return lista.stream().map(this::aRespuesta).toList();
    }

    /**
     * Obtiene un pedido. Si {@code soloPropietario} es verdadero, valida que el pedido
     * pertenezca al usuario indicado.
     */
    @Transactional(readOnly = true)
    public PedidoResponse obtener(Integer idPedido, Usuario usuario, boolean soloPropietario) {
        Pedido pedido = buscarEntidad(idPedido);
        if (soloPropietario && !pedido.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permisos para ver este pedido");
        }
        return aRespuesta(pedido);
    }

    @Transactional
    public PedidoResponse cambiarEstado(Integer idPedido, String estado) {
        Pedido pedido = buscarEntidad(idPedido);
        pedido.setEstado(normalizarEstado(estado));
        return aRespuesta(pedidos.save(pedido));
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private Pedido buscarEntidad(Integer idPedido) {
        return pedidos.findById(idPedido)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
    }

    private String normalizarEstado(String estado) {
        String normalizado = estado == null ? "" : estado.trim().toLowerCase();
        if (!Pedido.ESTADOS_VALIDOS.contains(normalizado)) {
            throw new IllegalArgumentException("El estado solo puede ser 'en camino' o 'entregado'");
        }
        return normalizado;
    }

    private void registrarSalida(Producto producto, int cantidad, int stockAnterior,
                                 Integer idPedido, Usuario usuario) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProductoId(producto.getIdProducto());
        movimiento.setProductoNombre(producto.getNombre());
        movimiento.setTipo("SALIDA");
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockNuevo(producto.getStock());
        movimiento.setReferencia("Pedido #" + idPedido);
        movimiento.setObservacion("Venta generada por compra del cliente");
        movimiento.setFecha(LocalDateTime.now());
        if (usuario != null) {
            movimiento.setUsuarioId(usuario.getIdUsuario());
            movimiento.setUsuarioNombre(nombreCompleto(usuario));
        }
        movimientos.save(movimiento);
    }

    private PedidoResponse aRespuesta(Pedido pedido) {
        List<Integer> ids = pedido.getListaIdProductos();
        List<Integer> cantidades = pedido.getListaCantidades();

        Map<Integer, Producto> mapa = new LinkedHashMap<>();
        productos.findAllById(ids).forEach(producto -> mapa.put(producto.getIdProducto(), producto));

        List<ItemPedidoResponse> items = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            Integer id = ids.get(i);
            int cantidad = i < cantidades.size() ? cantidades.get(i) : 1;
            Producto producto = mapa.get(id);
            BigDecimal precio = producto != null ? producto.getPrecio() : null;
            items.add(new ItemPedidoResponse(
                    id,
                    producto != null ? producto.getNombre() : "Producto no disponible",
                    producto != null ? producto.getImagen() : null,
                    cantidad,
                    precio,
                    precio != null ? precio.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP) : null
            ));
        }

        Usuario usuario = pedido.getUsuario();
        return new PedidoResponse(
                pedido.getIdPedido(),
                usuario != null ? usuario.getIdUsuario() : null,
                usuario != null ? nombreCompleto(usuario) : null,
                pedido.getFechaGeneracion() != null ? pedido.getFechaGeneracion().format(FORMATO_FECHA) : null,
                pedido.getEstado(),
                ids,
                items,
                pedido.getSubtotal(),
                pedido.getIgv(),
                pedido.getTotal()
        );
    }

    private String nombreCompleto(Usuario usuario) {
        String nombre = usuario.getNombre() == null ? "" : usuario.getNombre();
        String apellido = usuario.getApellido() == null ? "" : usuario.getApellido();
        return (nombre + " " + apellido).trim();
    }
}

