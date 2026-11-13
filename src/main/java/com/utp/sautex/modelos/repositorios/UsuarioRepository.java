package com.utp.sautex.modelos.repositorios;

import com.utp.sautex.modelos.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    List<Usuario> findAllByOrderByNombreAsc();

    long countByRolNombre(String nombre);
}
