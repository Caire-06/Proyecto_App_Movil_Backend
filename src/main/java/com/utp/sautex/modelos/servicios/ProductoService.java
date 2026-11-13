package com.utp.sautex.modelos.servicios;

import com.utp.sautex.modelos.entidades.Producto;
import com.utp.sautex.modelos.repositorios.ProductoRepository;
import com.utp.sautex.vistas.dto.ActualizarProductoDTO;
import com.utp.sautex.vistas.dto.CrearProductoDTO;
import com.utp.sautex.vistas.dto.ProductoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productos;
    private final AlmacenService almacen;

    public ProductoService(ProductoRepository productos, AlmacenService almacen) {
        this.productos = productos;
        this.almacen = almacen;
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listar(String categoria, String buscar) {
        List<Producto> lista;

        if (categoria == null || categoria.isBlank() || categoria.equalsIgnoreCase("todos")) {
            lista = productos.findByActivoTrueOrderByNombreAsc();
        } else {
            lista = productos.findByActivoTrueAndCategoriaIgnoreCaseOrderByNombreAsc(categoria.trim());
        }

        String texto = buscar == null ? "" : buscar.trim().toLowerCase();

        return lista.stream()
                .filter(producto -> texto.isEmpty() || producto.getNombre().toLowerCase().contains(texto))
                .map(ProductoDTO::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> categorias() {
        return productos.findAll().stream()
                .map(Producto::getCategoria)
                .filter(categoria -> categoria != null && !categoria.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    @Transactional
    public ProductoDTO crear(CrearProductoDTO dto) {
        Producto producto = new Producto();
        producto.setNombre(dto.nombre().trim());
        producto.setUnidad(dto.unidad());
        producto.setPrecio(dto.precio());
        producto.setStock(dto.stock());
        producto.setCategoria(dto.categoria());
        producto.setImagen(dto.imagen());
        producto.setDescripcion(dto.descripcion());
        producto.setVendidos(0);
        producto.setActivo(true);
        Producto guardado = productos.save(producto);
        almacen.registrarEntradaInicial(guardado, guardado.getStock(), "Stock inicial");
        return ProductoDTO.desde(guardado);
    }

    @Transactional
    public ProductoDTO actualizar(Integer id, ActualizarProductoDTO dto) {
        Producto producto = buscarEntidad(id);
        Integer stockAnterior = producto.getStock();

        if (dto.nombre() != null) {
            producto.setNombre(dto.nombre().trim());
        }
        if (dto.unidad() != null) {
            producto.setUnidad(dto.unidad());
        }
        if (dto.precio() != null) {
            producto.setPrecio(dto.precio());
        }
        if (dto.stock() != null) {
            producto.setStock(dto.stock());
        }
        if (dto.categoria() != null) {
            producto.setCategoria(dto.categoria());
        }
        if (dto.imagen() != null) {
            producto.setImagen(dto.imagen());
        }
        if (dto.descripcion() != null) {
            producto.setDescripcion(dto.descripcion());
        }
        if (dto.activo() != null) {
            producto.setActivo(dto.activo());
        }

        Producto guardado = productos.save(producto);

        if (dto.stock() != null && stockAnterior != null && !dto.stock().equals(stockAnterior)) {
            int diferencia = dto.stock() - stockAnterior;
            almacen.registrarAjuste(
                    guardado,
                    diferencia > 0 ? "ENTRADA" : "SALIDA",
                    Math.abs(diferencia),
                    stockAnterior,
                    dto.stock(),
                    "Ajuste de inventario"
            );
        }

        return ProductoDTO.desde(guardado);
    }

    @Transactional
    public void eliminar(Integer id) {
        Producto producto = buscarEntidad(id);
        producto.setActivo(false);
        productos.save(producto);
    }

    @Transactional(readOnly = true)
    public Producto buscarEntidad(Integer id) {
        return productos.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
    }
}
