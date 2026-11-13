package com.utp.sautex.vistas.dto;

public record LoginResponseDTO(
        boolean exitoso,
        String mensaje,
        String token,
        String rol,
        UsuarioDTO usuario
) {

    public static LoginResponseDTO exito(String mensaje, String token, String rol, UsuarioDTO usuario) {
        return new LoginResponseDTO(true, mensaje, token, rol, usuario);
    }

    public static LoginResponseDTO fallo(String mensaje) {
        return new LoginResponseDTO(false, mensaje, null, null, null);
    }
}
