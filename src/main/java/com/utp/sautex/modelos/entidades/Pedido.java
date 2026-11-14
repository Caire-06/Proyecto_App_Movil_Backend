package com.utp.sautex.modelos.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "pedidos")
public class Pedido {

    public static final String ESTADO_EN_CAMINO = "en camino";
    public static final String ESTADO_ENTREGADO = "entregado";
    public static final List<String> ESTADOS_VALIDOS = List.of(ESTADO_EN_CAMINO, ESTADO_ENTREGADO);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer idPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_generacion")
    private LocalDateTime fechaGeneracion;

    @Column(name = "estado", length = 20)
    private String estado;

    /** IDs de los productos comprados separados por coma. Ej: "1,4,7" */
    @Column(name = "id_productos", length = 500)
    private String idProductos;

    /** Cantidades compradas, en el mismo orden que id_productos. Ej: "2,1,3" */
    @Column(name = "cantidades", length = 500)
    private String cantidades;

    @Column(name = "subtotal", precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "igv", precision = 12, scale = 2)
    private BigDecimal igv;

    @Column(name = "total", precision = 12, scale = 2)
    private BigDecimal total;

    public Pedido() {
    }

    @PrePersist
    @PreUpdate
    private void validarAntesDeGuardar() {
        if (fechaGeneracion == null) {
            fechaGeneracion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = ESTADO_EN_CAMINO;
        }
        if (!ESTADOS_VALIDOS.contains(estado)) {
            throw new IllegalArgumentException("Estado de pedido no válido: " + estado
                    + ". Valores permitidos: 'en camino' o 'entregado'");
        }
    }

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        String normalizado = estado == null ? null : estado.trim().toLowerCase();
        if (normalizado == null || !ESTADOS_VALIDOS.contains(normalizado)) {
            throw new IllegalArgumentException("El estado solo puede ser 'en camino' o 'entregado'");
        }
        this.estado = normalizado;
    }

    public String getIdProductos() {
        return idProductos;
    }

    public void setIdProductos(String idProductos) {
        this.idProductos = idProductos;
    }

    public String getCantidades() {
        return cantidades;
    }

    public void setCantidades(String cantidades) {
        this.cantidades = cantidades;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getIgv() {
        return igv;
    }

    public void setIgv(BigDecimal igv) {
        this.igv = igv;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    /** Lista de IDs de productos del pedido. */
    public List<Integer> getListaIdProductos() {
        return aLista(idProductos);
    }

    public void setListaIdProductos(List<Integer> ids) {
        this.idProductos = aTexto(ids);
    }

    /** Lista de cantidades (mismo orden que los IDs de productos). */
    public List<Integer> getListaCantidades() {
        return aLista(cantidades);
    }

    public void setListaCantidades(List<Integer> lista) {
        this.cantidades = aTexto(lista);
    }

    private static List<Integer> aLista(String texto) {
        if (texto == null || texto.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(texto.split(","))
                .map(String::trim)
                .filter(valor -> !valor.isEmpty())
                .map(Integer::valueOf)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static String aTexto(List<Integer> lista) {
        if (lista == null || lista.isEmpty()) {
            return null;
        }
        return lista.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}
