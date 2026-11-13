package com.utp.sautex.modelos.servicios;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ClaveService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String encriptar(String clave) {
        return encoder.encode(clave);
    }

    public boolean verificar(String clave, String claveEncriptada) {
        if (clave == null || claveEncriptada == null) {
            return false;
        }
        return encoder.matches(clave, claveEncriptada);
    }
}
