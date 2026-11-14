package com.utp.sautex.modelos.repositorios;

import com.utp.sautex.modelos.entidades.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    boolean existsByUsuarioIdUsuario(Integer idUsuario);

    List<Pedido> findByUsuarioIdUsuarioOrderByFechaGeneracionDesc(Integer idUsuario);

    List<Pedido> findAllByOrderByFechaGeneracionDesc();

    List<Pedido> findByEstadoOrderByFechaGeneracionDesc(String estado);
}
