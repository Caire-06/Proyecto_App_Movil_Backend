package com.utp.sautex.modelos.servicios;

import com.utp.sautex.modelos.entidades.Rol;
import com.utp.sautex.modelos.entidades.Usuario;
import com.utp.sautex.modelos.repositorios.RolRepository;
import com.utp.sautex.modelos.repositorios.UsuarioRepository;
import com.utp.sautex.modelos.repositorios.PedidoRepository;
import com.utp.sautex.vistas.dto.ActualizarPerfilDTO;
import com.utp.sautex.vistas.dto.ActualizarUsuarioDTO;
import com.utp.sautex.vistas.dto.CambiarClaveDTO;
import com.utp.sautex.vistas.dto.CrearUsuarioDTO;
import com.utp.sautex.vistas.dto.MensajeDTO;
import com.utp.sautex.vistas.dto.UsuarioDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PedidoRepository pedidos;
    private final ClaveService claves;

    public UsuarioService(UsuarioRepository usuarios, RolRepository roles, PedidoRepository pedidos, ClaveService claves) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.pedidos = pedidos;
        this.claves = claves;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listar() {
        return usuarios.findAllByOrderByNombreAsc().stream()
                .map(UsuarioDTO::desde)
                .toList();
    }

    @Transactional
    public UsuarioDTO crear(CrearUsuarioDTO dto) {
        String correo = dto.correo().trim().toLowerCase();
        if (usuarios.existsByCorreoIgnoreCase(correo)) {
            throw new IllegalArgumentException("Ese correo ya está registrado");
        }
        Rol rol = buscarRol(dto.rol());

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.nombre().trim());
        usuario.setApellido(dto.apellido().trim());
        usuario.setCorreo(correo);
        usuario.setTelefono(dto.telefono());
        usuario.setDireccion(dto.direccion());
        usuario.setClave(claves.encriptar(dto.clave()));
        usuario.setEstado("Activo");
        usuario.setRol(rol);

        return UsuarioDTO.desde(usuarios.save(usuario));
    }

    @Transactional
    public UsuarioDTO actualizar(Integer id, ActualizarUsuarioDTO dto) {
        Usuario usuario = buscarEntidad(id);

        if (dto.nombre() != null) {
            usuario.setNombre(dto.nombre().trim());
        }
        if (dto.apellido() != null) {
            usuario.setApellido(dto.apellido().trim());
        }
        if (dto.telefono() != null) {
            usuario.setTelefono(dto.telefono());
        }
        if (dto.direccion() != null) {
            usuario.setDireccion(dto.direccion());
        }
        if (dto.rol() != null && !dto.rol().isBlank()) {
            usuario.setRol(buscarRol(dto.rol()));
        }
        if (dto.clave() != null && !dto.clave().isBlank()) {
            usuario.setClave(claves.encriptar(dto.clave()));
        }

        return UsuarioDTO.desde(usuarios.save(usuario));
    }

    @Transactional
    public void eliminar(Integer id, Integer idSolicitante) {
        Usuario usuario = buscarEntidad(id);
        if (usuario.getIdUsuario().equals(idSolicitante)) {
            throw new IllegalArgumentException("No puede eliminar su propia cuenta");
        }
        if ("admin@sautex.com".equalsIgnoreCase(usuario.getCorreo())) {
            throw new IllegalArgumentException("La cuenta principal del administrador no se puede eliminar");
        }
        if (usuario.getRol() != null && "admin".equalsIgnoreCase(usuario.getRol().getNombre())
                && usuarios.countByRolNombre("admin") <= 1) {
            throw new IllegalArgumentException("Debe existir al menos un administrador");
        }
        if (pedidos.existsByUsuarioIdUsuario(id)) {
            throw new IllegalArgumentException("El usuario tiene pedidos registrados y no se puede eliminar");
        }
        usuarios.delete(usuario);
    }

    @Transactional
    public UsuarioDTO actualizarPerfil(Integer id, ActualizarPerfilDTO dto) {
        Usuario usuario = buscarEntidad(id);

        if (dto.nombre() != null) {
            usuario.setNombre(dto.nombre().trim());
        }
        if (dto.apellido() != null) {
            usuario.setApellido(dto.apellido().trim());
        }
        if (dto.telefono() != null) {
            usuario.setTelefono(dto.telefono());
        }
        if (dto.direccion() != null) {
            usuario.setDireccion(dto.direccion());
        }
        if (dto.foto() != null) {
            usuario.setFoto(dto.foto());
        }

        return UsuarioDTO.desde(usuarios.save(usuario));
    }

    @Transactional
    public MensajeDTO cambiarClave(Integer id, CambiarClaveDTO dto) {
        Usuario usuario = buscarEntidad(id);
        if (!claves.verificar(dto.claveActual(), usuario.getClave())) {
            throw new IllegalArgumentException("Su contraseña actual no es correcta");
        }
        usuario.setClave(claves.encriptar(dto.claveNueva()));
        usuarios.save(usuario);
        return new MensajeDTO("Contraseña actualizada correctamente");
    }

    @Transactional(readOnly = true)
    public Usuario buscarEntidad(Integer id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private Rol buscarRol(String nombre) {
        String valor = nombre == null ? "cliente" : nombre.trim().toLowerCase();
        return roles.findByNombre(valor)
                .orElseThrow(() -> new IllegalArgumentException("El rol indicado no existe"));
    }
}
