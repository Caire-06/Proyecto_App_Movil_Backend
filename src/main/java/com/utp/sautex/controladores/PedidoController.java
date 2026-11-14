package com.utp.sautex.controladores;

import com.utp.sautex.modelos.entidades.Usuario;
import com.utp.sautex.modelos.servicios.PedidoService;
import com.utp.sautex.modelos.servicios.PedidoService.CambiarEstadoRequest;
import com.utp.sautex.modelos.servicios.PedidoService.CrearPedidoRequest;
import com.utp.sautex.modelos.servicios.PedidoService.PedidoResponse;
import com.utp.sautex.modelos.servicios.Seguridad;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidos;
    private final Seguridad seguridad;

    public PedidoController(PedidoService pedidos, Seguridad seguridad) {
        this.pedidos = pedidos;
        this.seguridad = seguridad;
    }

    /** Genera un pedido a partir de la compra del usuario autenticado. */
    @PostMapping
    public ResponseEntity<PedidoResponse> generar(
            @RequestHeader(value = "Authorization", required = false) String autorizacion,
            @Valid @RequestBody CrearPedidoRequest request) {
        Usuario usuario = seguridad.actual(autorizacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidos.generar(request, usuario));
    }

    /** Pedidos del usuario autenticado. */
    @GetMapping("/mis-pedidos")
    public List<PedidoResponse> misPedidos(
            @RequestHeader(value = "Authorization", required = false) String autorizacion) {
        Usuario usuario = seguridad.actual(autorizacion);
        return pedidos.listarDeUsuario(usuario.getIdUsuario());
    }

    /** Todos los pedidos (admin / almacen). Filtro opcional: ?estado=en camino | entregado */
    @GetMapping
    public List<PedidoResponse> listar(
            @RequestHeader(value = "Authorization", required = false) String autorizacion,
            @RequestParam(required = false) String estado) {
        seguridad.conRol(autorizacion, "admin", "almacen");
        return pedidos.listarTodos(estado);
    }

    /** Detalle de un pedido: el propietario o admin / almacen. */
    @GetMapping("/{id}")
    public PedidoResponse obtener(
            @RequestHeader(value = "Authorization", required = false) String autorizacion,
            @PathVariable Integer id) {
        Usuario usuario = seguridad.actual(autorizacion);
        return pedidos.obtener(id, usuario, !esPersonal(usuario));
    }

    /** Cambia el estado del pedido (admin / almacen). Body: {"estado": "entregado"} */
    @PatchMapping("/{id}/estado")
    public PedidoResponse cambiarEstado(
            @RequestHeader(value = "Authorization", required = false) String autorizacion,
            @PathVariable Integer id,
            @Valid @RequestBody CambiarEstadoRequest request) {
        seguridad.conRol(autorizacion, "admin", "almacen");
        return pedidos.cambiarEstado(id, request.estado());
    }

    private boolean esPersonal(Usuario usuario) {
        String rol = usuario.getRol() != null ? usuario.getRol().getNombre() : "";
        return "admin".equalsIgnoreCase(rol) || "almacen".equalsIgnoreCase(rol);
    }
}

