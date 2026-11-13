package com.utp.sautex.controladores;

import com.utp.sautex.modelos.entidades.Usuario;
import com.utp.sautex.modelos.servicios.AlmacenService;
import com.utp.sautex.modelos.servicios.Seguridad;
import com.utp.sautex.vistas.dto.CrearEntradaDTO;
import com.utp.sautex.vistas.dto.MovimientoInventarioDTO;
import com.utp.sautex.vistas.dto.ResumenAlmacenDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/almacen")
public class AlmacenController {

    private final AlmacenService almacen;
    private final Seguridad seguridad;

    public AlmacenController(AlmacenService almacen, Seguridad seguridad) {
        this.almacen = almacen;
        this.seguridad = seguridad;
    }

    @GetMapping("/resumen")
    public ResumenAlmacenDTO resumen(@RequestHeader(value = "Authorization", required = false) String autorizacion) {
        seguridad.conRol(autorizacion, "admin", "almacen");
        return almacen.resumen();
    }

    @PostMapping("/entradas")
    public ResponseEntity<MovimientoInventarioDTO> registrarEntrada(
            @RequestHeader(value = "Authorization", required = false) String autorizacion,
            @Valid @RequestBody CrearEntradaDTO dto) {
        Usuario usuario = seguridad.conRol(autorizacion, "admin", "almacen");
        return ResponseEntity.status(HttpStatus.CREATED).body(almacen.registrarEntrada(dto, usuario));
    }

    @GetMapping("/movimientos")
    public List<MovimientoInventarioDTO> movimientos(
            @RequestHeader(value = "Authorization", required = false) String autorizacion,
            @RequestParam(required = false) Integer productoId,
            @RequestParam(required = false) String tipo) {
        seguridad.conRol(autorizacion, "admin", "almacen");
        return almacen.listarMovimientos(productoId, tipo);
    }
}
