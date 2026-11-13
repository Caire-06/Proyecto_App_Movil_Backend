package com.utp.sautex.modelos.repositorios;

import com.utp.sautex.modelos.entidades.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    boolean existsByUsuarioIdUsuario(Integer idUsuario);
}
