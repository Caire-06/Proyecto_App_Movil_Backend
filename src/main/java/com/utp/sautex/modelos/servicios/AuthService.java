package com.utp.sautex.modelos.servicios;

import com.utp.sautex.modelos.entidades.Rol;
import com.utp.sautex.modelos.entidades.Usuario;
import com.utp.sautex.modelos.repositorios.RolRepository;
import com.utp.sautex.modelos.repositorios.UsuarioRepository;
import com.utp.sautex.vistas.dto.LoginRequestDTO;
import com.utp.sautex.vistas.dto.LoginResponseDTO;
import com.utp.sautex.vistas.dto.RegistroRequestDTO;
import com.utp.sautex.vistas.dto.UsuarioDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final ClaveService claves;
    private final SesionService sesiones;

    public AuthService(UsuarioRepository usuarios, RolRepository roles, ClaveService claves, SesionService sesiones) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.claves = claves;
        this.sesiones = sesiones;
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO iniciarSesion(LoginRequestDTO dto) {
        Usuario usuario = usuarios.findByCorreoIgnoreCase(normalizar(dto.correo())).orElse(null);
        if (usuario == null || !claves.verificar(dto.clave(), usuario.getClave())) {
            return LoginResponseDTO.fallo("Correo o contraseña incorrectos");
        }
        if (!"Activo".equalsIgnoreCase(usuario.getEstado())) {
            return LoginResponseDTO.fallo("La cuenta se encuentra inactiva");
        }
        String rol = usuario.getRol() != null ? usuario.getRol().getNombre() : "cliente";
        String token = sesiones.crear(usuario.getIdUsuario());
        return LoginResponseDTO.exito("Bienvenido a SAUTEX", token, rol, UsuarioDTO.desde(usuario));
    }

    @Transactional
    public LoginResponseDTO registrar(RegistroRequestDTO dto) {
        String correo = normalizar(dto.correo());
        if (usuarios.existsByCorreoIgnoreCase(correo)) {
            throw new IllegalArgumentException("Ese correo ya está registrado");
        }
        Rol rol = roles.findByNombre("cliente")
                .orElseThrow(() -> new IllegalArgumentException("El rol cliente no está configurado"));

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.nombre().trim());
        usuario.setApellido(dto.apellido().trim());
        usuario.setCorreo(correo);
        usuario.setTelefono(dto.telefono());
        usuario.setDireccion(dto.direccion());
        usuario.setClave(claves.encriptar(dto.clave()));
        usuario.setEstado("Activo");
        usuario.setRol(rol);
        usuario = usuarios.save(usuario);

        String token = sesiones.crear(usuario.getIdUsuario());
        return LoginResponseDTO.exito("Registro completado", token, rol.getNombre(), UsuarioDTO.desde(usuario));
    }

    private String normalizar(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase();
    }
}
