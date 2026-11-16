package com.utp.sautex.controladores;

import com.utp.sautex.modelos.servicios.CategoriaService;
import com.utp.sautex.modelos.servicios.Seguridad;
import com.utp.sautex.vistas.dto.CategoriaDTO;
import com.utp.sautex.vistas.dto.CrearCategoriaDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categorias;
    private final Seguridad seguridad;

    public CategoriaController(CategoriaService categorias, Seguridad seguridad) {
        this.categorias = categorias;
        this.seguridad = seguridad;
    }

    @GetMapping
    public List<CategoriaDTO> listar(@RequestHeader(value = "Authorization", required = false) String autorizacion) {
        seguridad.conRol(autorizacion, "admin", "almacen");
        return categorias.listar();
    }

    @PostMapping
    public ResponseEntity<CategoriaDTO> crear(@RequestHeader(value = "Authorization", required = false) String autorizacion,
                                              @Valid @RequestBody CrearCategoriaDTO dto) {
        seguridad.conRol(autorizacion, "admin");
        return ResponseEntity.status(HttpStatus.CREATED).body(categorias.crear(dto));
    }
}
