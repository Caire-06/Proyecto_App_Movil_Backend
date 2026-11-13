package com.utp.sautex.controladores;

import com.utp.sautex.modelos.entidades.Usuario;
import com.utp.sautex.modelos.servicios.Seguridad;
import com.utp.sautex.modelos.servicios.UsuarioService;
import com.utp.sautex.vistas.dto.ActualizarUsuarioDTO;
import com.utp.sautex.vistas.dto.CrearUsuarioDTO;
import com.utp.sautex.vistas.dto.UsuarioDTO;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarios;
    private final Seguridad seguridad;

    public UsuarioController(UsuarioService usuarios, Seguridad seguridad) {
        this.usuarios = usuarios;
        this.seguridad = seguridad;
    }

    @GetMapping
    public List<UsuarioDTO> listar(@RequestHeader(value = "Authorization", required = false) String autorizacion) {
        seguridad.conRol(autorizacion, "admin");
        return usuarios.listar();
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> crear(@RequestHeader(value = "Authorization", required = false) String autorizacion,
                                            @Valid @RequestBody CrearUsuarioDTO dto) {
        seguridad.conRol(autorizacion, "admin");
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarios.crear(dto));
    }

    @PutMapping("/{id}")
    public UsuarioDTO actualizar(@RequestHeader(value = "Authorization", required = false) String autorizacion,
                                 @PathVariable Integer id,
                                 @Valid @RequestBody ActualizarUsuarioDTO dto) {
        seguridad.conRol(autorizacion, "admin");
        return usuarios.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@RequestHeader(value = "Authorization", required = false) String autorizacion,
                                         @PathVariable Integer id) {
        Usuario admin = seguridad.conRol(autorizacion, "admin");
        usuarios.eliminar(id, admin.getIdUsuario());
        return ResponseEntity.noContent().build();
    }
}
