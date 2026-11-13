package com.utp.sautex.modelos.servicios;

import com.utp.sautex.modelos.entidades.Usuario;
import com.utp.sautex.modelos.repositorios.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@Component
public class Seguridad {

    private final SesionService sesiones;
    private final UsuarioRepository usuarios;

    public Seguridad(SesionService sesiones, UsuarioRepository usuarios) {
        this.sesiones = sesiones;
        this.usuarios = usuarios;
    }

    public Usuario actual(String autorizacion) {
        Integer id = sesiones.identificar(autorizacion);
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida o expirada");
        }
        return usuarios.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }

    public Usuario conRol(String autorizacion, String... rolesPermitidos) {
        Usuario usuario = actual(autorizacion);
        String rol = usuario.getRol() != null ? usuario.getRol().getNombre() : "";
        boolean permitido = Arrays.stream(rolesPermitidos).anyMatch(rol::equalsIgnoreCase);
        if (!permitido) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta acción");
        }
        return usuario;
    }
}
