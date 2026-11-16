package com.utp.sautex.modelos.servicios;

import com.utp.sautex.modelos.entidades.Categoria;
import com.utp.sautex.modelos.repositorios.CategoriaRepository;
import com.utp.sautex.vistas.dto.CategoriaDTO;
import com.utp.sautex.vistas.dto.CrearCategoriaDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categorias;

    public CategoriaService(CategoriaRepository categorias) {
        this.categorias = categorias;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDTO> listar() {
        return categorias.findByActivoTrueOrderByNombreAsc().stream()
                .map(CategoriaDTO::desde)
                .toList();
    }

    @Transactional
    public CategoriaDTO crear(CrearCategoriaDTO dto) {
        String nombre = dto.nombre() == null ? "" : dto.nombre().trim();
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
        if (categorias.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion(dto.descripcion() == null ? null : dto.descripcion().trim());
        categoria.setActivo(true);
        return CategoriaDTO.desde(categorias.save(categoria));
    }
}
