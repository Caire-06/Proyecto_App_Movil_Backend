package com.utp.sautex.controladores;

import com.utp.sautex.modelos.servicios.ReporteService;
import com.utp.sautex.modelos.servicios.Seguridad;
import com.utp.sautex.vistas.dto.ReporteDTO;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reportes;
    private final Seguridad seguridad;

    public ReporteController(
            ReporteService reportes,
            Seguridad seguridad) {
        this.reportes = reportes;
        this.seguridad = seguridad;
    }

    @GetMapping("/resumen")
    public ReporteDTO obtenerResumen(
            @RequestHeader(value = "Authorization", required = false)
            String autorizacion,
            @RequestParam(defaultValue = "mes") String periodo) {

        seguridad.conRol(autorizacion, "admin");

        return reportes.obtenerResumen(periodo);
    }
}
