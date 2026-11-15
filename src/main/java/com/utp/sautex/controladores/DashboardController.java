package com.utp.sautex.controladores;

import com.utp.sautex.modelos.servicios.DashboardService;
import com.utp.sautex.modelos.servicios.Seguridad;
import com.utp.sautex.vistas.dto.DashboardDTO;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboard;
    private final Seguridad seguridad;

    public DashboardController(
            DashboardService dashboard,
            Seguridad seguridad) {

        this.dashboard = dashboard;
        this.seguridad = seguridad;
    }

    @GetMapping("/resumen")
    public DashboardDTO resumen(
            @RequestHeader(value = "Authorization", required = false)
            String autorizacion) {

        seguridad.conRol(autorizacion, "admin");

        return dashboard.obtenerResumen();
    }
}