package com.utp.sautex.controladores;

import com.utp.sautex.modelos.servicios.ProductoService;
import com.utp.sautex.modelos.servicios.Seguridad;
import com.utp.sautex.vistas.dto.ActualizarProductoDTO;
import com.utp.sautex.vistas.dto.CrearProductoDTO;
import com.utp.sautex.vistas.dto.ProductoDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productos;
    private final Seguridad seguridad;

    public ProductoController(ProductoService productos, Seguridad seguridad) {
        this.productos = productos;
        this.seguridad = seguridad;
    }

    @GetMapping
    public List<ProductoDTO> listar(@RequestParam(required = false) String categoria,
                                    @RequestParam(required = false) String buscar) {
        return productos.listar(categoria, buscar);
    }

    @GetMapping("/categorias")
    public List<String> categorias() {
        return productos.categorias();
    }

    @PostMapping
    public ResponseEntity<ProductoDTO> crear(@RequestHeader(value = "Authorization", required = false) String autorizacion,
                                             @Valid @RequestBody CrearProductoDTO dto) {
        seguridad.conRol(autorizacion, "admin", "almacen");
        return ResponseEntity.status(HttpStatus.CREATED).body(productos.crear(dto));
    }

    @PutMapping("/{id}")
    public ProductoDTO actualizar(@RequestHeader(value = "Authorization", required = false) String autorizacion,
                                  @PathVariable Integer id,
                                  @Valid @RequestBody ActualizarProductoDTO dto) {
        seguridad.conRol(autorizacion, "admin", "almacen");
        return productos.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@RequestHeader(value = "Authorization", required = false) String autorizacion,
                                         @PathVariable Integer id) {
        seguridad.conRol(autorizacion, "admin", "almacen");
        productos.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
