package com.utp.sautex.modelos.repositorios;

import com.utp.sautex.modelos.entidades.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    List<Producto> findByActivoTrueOrderByNombreAsc();

    List<Producto> findByActivoTrueAndCategoriaIgnoreCaseOrderByNombreAsc(String categoria);
}
