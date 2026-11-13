package com.utp.sautex.controladores;

import com.utp.sautex.modelos.servicios.AuthService;
import com.utp.sautex.modelos.servicios.SesionService;
import com.utp.sautex.vistas.dto.LoginRequestDTO;
import com.utp.sautex.vistas.dto.LoginResponseDTO;
import com.utp.sautex.vistas.dto.RegistroRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;
    private final SesionService sesiones;

    public AuthController(AuthService auth, SesionService sesiones) {
        this.auth = auth;
        this.sesiones = sesiones;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        LoginResponseDTO respuesta = auth.iniciarSesion(dto);
        if (respuesta.exitoso()) {
            return ResponseEntity.ok(respuesta);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(respuesta);
    }

    @PostMapping("/registro")
    public ResponseEntity<LoginResponseDTO> registro(@Valid @RequestBody RegistroRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.registrar(dto));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String autorizacion) {
        sesiones.revocar(autorizacion);
        return ResponseEntity.noContent().build();
    }
}
