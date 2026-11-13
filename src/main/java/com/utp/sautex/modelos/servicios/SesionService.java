package com.utp.sautex.modelos.servicios;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SesionService {

    private record Sesion(Integer usuarioId, Instant expira) {
    }

    private final ConcurrentHashMap<String, Sesion> sesiones = new ConcurrentHashMap<>();

    public String crear(Integer usuarioId) {
        String token = "TK-" + UUID.randomUUID();
        sesiones.put(token, new Sesion(usuarioId, Instant.now().plus(4, ChronoUnit.HOURS)));
        return token;
    }

    public void revocar(String autorizacion) {
        if (autorizacion != null && autorizacion.startsWith("Bearer ")) {
            sesiones.remove(autorizacion.substring(7).trim());
        }
    }

    public Integer identificar(String autorizacion) {
        if (autorizacion == null || !autorizacion.startsWith("Bearer ")) {
            return null;
        }
        String token = autorizacion.substring(7).trim();
        Sesion sesion = sesiones.get(token);
        if (sesion == null) {
            return null;
        }
        if (!Instant.now().isBefore(sesion.expira())) {
            sesiones.remove(token);
            return null;
        }
        return sesion.usuarioId();
    }
}
