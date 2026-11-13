package com.utp.sautex.controladores;

import com.utp.sautex.modelos.entidades.Usuario;
import com.utp.sautex.modelos.servicios.Seguridad;
import com.utp.sautex.modelos.servicios.UsuarioService;
import com.utp.sautex.vistas.dto.ActualizarPerfilDTO;
import com.utp.sautex.vistas.dto.CambiarClaveDTO;
import com.utp.sautex.vistas.dto.MensajeDTO;
import com.utp.sautex.vistas.dto.UsuarioDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final UsuarioService usuarios;
    private final Seguridad seguridad;

    public PerfilController(UsuarioService usuarios, Seguridad seguridad) {
        this.usuarios = usuarios;
        this.seguridad = seguridad;
    }

    @GetMapping
    public UsuarioDTO ver(@RequestHeader(value = "Authorization", required = false) String autorizacion) {
        return UsuarioDTO.desde(seguridad.actual(autorizacion));
    }

    @PutMapping
    public UsuarioDTO actualizar(@RequestHeader(value = "Authorization", required = false) String autorizacion,
                                 @Valid @RequestBody ActualizarPerfilDTO dto) {
        Usuario usuario = seguridad.actual(autorizacion);
        return usuarios.actualizarPerfil(usuario.getIdUsuario(), dto);
    }

    @PutMapping("/clave")
    public MensajeDTO cambiarClave(@RequestHeader(value = "Authorization", required = false) String autorizacion,
                                   @Valid @RequestBody CambiarClaveDTO dto) {
        Usuario usuario = seguridad.actual(autorizacion);
        return usuarios.cambiarClave(usuario.getIdUsuario(), dto);
    }
}
