package com.utp.sautex.vistas.dto;

import com.utp.sautex.modelos.entidades.Categoria;

public record CategoriaDTO(
        Integer id,
        String nombre,
        String descripcion,
        Boolean activo
) {

    public static CategoriaDTO desde(Categoria categoria) {
        return new CategoriaDTO(
                categoria.getIdCategoria(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getActivo()
        );
    }
}
