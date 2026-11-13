package com.utp.sautex.modelos.repositorios;

import com.utp.sautex.modelos.entidades.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Integer> {

    List<MovimientoInventario> findAllByOrderByFechaDesc();

    List<MovimientoInventario> findByTipoIgnoreCaseOrderByFechaDesc(String tipo);

    List<MovimientoInventario> findByProductoIdOrderByFechaDesc(Integer productoId);

    List<MovimientoInventario> findByProductoIdAndTipoIgnoreCaseOrderByFechaDesc(Integer productoId, String tipo);

    List<MovimientoInventario> findTop5ByOrderByFechaDesc();
}
