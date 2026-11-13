package com.utp.sautex.configuracion;

import com.utp.sautex.vistas.dto.MensajeDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MensajeDTO> errorNegocio(IllegalArgumentException excepcion) {
        return ResponseEntity.badRequest().body(new MensajeDTO(excepcion.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MensajeDTO> errorValidacion(MethodArgumentNotValidException excepcion) {
        String mensaje = excepcion.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Los datos enviados no son válidos");
        return ResponseEntity.badRequest().body(new MensajeDTO(mensaje));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MensajeDTO> errorFormato(HttpMessageNotReadableException excepcion) {
        return ResponseEntity.badRequest().body(new MensajeDTO("El cuerpo de la solicitud no es válido"));
    }
}
