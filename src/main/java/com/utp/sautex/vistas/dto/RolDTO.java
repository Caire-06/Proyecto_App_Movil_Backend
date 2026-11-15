package com.utp.sautex.vistas.dto;

import com.utp.sautex.modelos.entidades.Rol;

public record RolDTO(
        Integer id,
        String nombre,
        String descripcion
) {
    public static RolDTO desde(Rol rol) {
        return new RolDTO(
                rol.getIdRol(),
                rol.getNombre(),
                rol.getDescripcion()
        );
    }
}