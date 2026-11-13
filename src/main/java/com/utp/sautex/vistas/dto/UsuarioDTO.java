package com.utp.sautex.vistas.dto;

import com.utp.sautex.modelos.entidades.Usuario;

public record UsuarioDTO(
        Integer id,
        String nombre,
        String apellido,
        String correo,
        String telefono,
        String direccion,
        String foto,
        String estado,
        String rol
) {

    public static UsuarioDTO desde(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                usuario.getDireccion(),
                usuario.getFoto(),
                usuario.getEstado(),
                usuario.getRol() != null ? usuario.getRol().getNombre() : null
        );
    }
}
