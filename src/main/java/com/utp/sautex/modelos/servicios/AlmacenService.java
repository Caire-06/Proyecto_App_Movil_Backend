package com.utp.sautex.modelos.servicios;

import com.utp.sautex.modelos.entidades.MovimientoInventario;
import com.utp.sautex.modelos.entidades.Producto;
import com.utp.sautex.modelos.entidades.Usuario;
import com.utp.sautex.modelos.repositorios.MovimientoInventarioRepository;
import com.utp.sautex.modelos.repositorios.ProductoRepository;
import com.utp.sautex.vistas.dto.AlertaStockDTO;
import com.utp.sautex.vistas.dto.CategoriaAlmacenDTO;
import com.utp.sautex.vistas.dto.CrearEntradaDTO;
import com.utp.sautex.vistas.dto.MovimientoInventarioDTO;
import com.utp.sautex.vistas.dto.ResumenAlmacenDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class AlmacenService {

    private static final int LIMITE_STOCK_BAJO = 10;

    private final ProductoRepository productos;
    private final MovimientoInventarioRepository movimientos;

    public AlmacenService(ProductoRepository productos, MovimientoInventarioRepository movimientos) {
        this.productos = productos;
        this.movimientos = movimientos;
    }

    @Transactional(readOnly = true)
    public ResumenAlmacenDTO resumen() {
        List<Producto> activos = productos.findByActivoTrueOrderByNombreAsc();

        long stockTotal = activos.stream()
                .mapToLong(Producto::getStock)
                .sum();

        BigDecimal valorInventario = activos.stream()
                .map(producto -> producto.getPrecio().multiply(BigDecimal.valueOf(producto.getStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AlertaStockDTO> alertas = activos.stream()
                .filter(producto -> producto.getStock() <= LIMITE_STOCK_BAJO)
                .sorted(Comparator.comparingInt(Producto::getStock)
                        .thenComparing(Producto::getNombre))
                .map(AlertaStockDTO::desde)
                .toList();

        Map<String, List<Producto>> porCategoria = activos.stream()
                .collect(Collectors.groupingBy(this::categoriaSegura, TreeMap::new, Collectors.toList()));

        List<CategoriaAlmacenDTO> categorias = porCategoria.entrySet().stream()
                .map(entrada -> new CategoriaAlmacenDTO(
                        entrada.getKey(),
                        entrada.getValue().size(),
                        entrada.getValue().stream().mapToLong(Producto::getStock).sum()
                ))
                .toList();

        List<MovimientoInventarioDTO> ultimosMovimientos = movimientos.findTop5ByOrderByFechaDesc().stream()
                .map(MovimientoInventarioDTO::desde)
                .toList();

        return new ResumenAlmacenDTO(
                activos.size(),
                stockTotal,
                valorInventario,
                alertas.size(),
                categorias,
                alertas,
                ultimosMovimientos
        );
    }

    @Transactional
    public MovimientoInventarioDTO registrarEntrada(CrearEntradaDTO dto, Usuario usuario) {
        Producto producto = productos.findById(dto.productoId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        if (!producto.getActivo()) {
            throw new IllegalArgumentException("El producto no está disponible");
        }
        if (dto.cantidad() == null || dto.cantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }

        int stockAnterior = producto.getStock();
        producto.setStock(stockAnterior + dto.cantidad());
        productos.save(producto);

        return guardarMovimiento(
                producto,
                "ENTRADA",
                dto.cantidad(),
                stockAnterior,
                producto.getStock(),
                referencia(dto.referencia(), "Entrada manual"),
                limpiar(dto.observacion()),
                usuario
        );
    }

    public void registrarEntradaInicial(Producto producto, int cantidad, String referencia) {
        if (cantidad <= 0) {
            return;
        }
        guardarMovimiento(producto, "ENTRADA", cantidad, 0, cantidad,
                referencia(referencia, "Stock inicial"), "Carga inicial del producto", null);
    }

    public void registrarAjuste(Producto producto, String tipo, int cantidad, int stockAnterior,
                                int stockNuevo, String referencia) {
        guardarMovimiento(producto, tipo, cantidad, stockAnterior, stockNuevo,
                referencia(referencia, "Ajuste de inventario"), "Ajuste desde la edición del producto", null);
    }

    @Transactional(readOnly = true)
    public List<MovimientoInventarioDTO> listarMovimientos(Integer productoId, String tipo) {
        String tipoNormalizado = tipo == null ? "" : tipo.trim().toUpperCase();
        boolean filtrarTipo = tipoNormalizado.equals("ENTRADA") || tipoNormalizado.equals("SALIDA");

        List<MovimientoInventario> lista;
        if (productoId != null && filtrarTipo) {
            lista = movimientos.findByProductoIdAndTipoIgnoreCaseOrderByFechaDesc(productoId, tipoNormalizado);
        } else if (productoId != null) {
            lista = movimientos.findByProductoIdOrderByFechaDesc(productoId);
        } else if (filtrarTipo) {
            lista = movimientos.findByTipoIgnoreCaseOrderByFechaDesc(tipoNormalizado);
        } else {
            lista = movimientos.findAllByOrderByFechaDesc();
        }

        return lista.stream().map(MovimientoInventarioDTO::desde).toList();
    }

    private MovimientoInventarioDTO guardarMovimiento(Producto producto, String tipo, int cantidad,
                                                       int stockAnterior, int stockNuevo,
                                                       String referencia, String observacion,
                                                       Usuario usuario) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProductoId(producto.getIdProducto());
        movimiento.setProductoNombre(producto.getNombre());
        movimiento.setTipo(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockNuevo(stockNuevo);
        movimiento.setReferencia(referencia);
        movimiento.setObservacion(observacion);
        movimiento.setFecha(LocalDateTime.now());

        if (usuario != null) {
            movimiento.setUsuarioId(usuario.getIdUsuario());
            movimiento.setUsuarioNombre((usuario.getNombre() + " " + usuario.getApellido()).trim());
        }

        return MovimientoInventarioDTO.desde(movimientos.save(movimiento));
    }

    private String categoriaSegura(Producto producto) {
        String categoria = producto.getCategoria();
        return categoria == null || categoria.isBlank() ? "Sin categoría" : categoria.trim();
    }

    private String limpiar(String valor) {
        return valor == null ? null : valor.trim();
    }

    private String referencia(String valor, String porDefecto) {
        String limpio = limpiar(valor);
        return limpio == null || limpio.isEmpty() ? porDefecto : limpio;
    }
}
