package com.utp.sautex.configuracion;

import com.utp.sautex.modelos.entidades.Producto;
import com.utp.sautex.modelos.entidades.Rol;
import com.utp.sautex.modelos.entidades.Usuario;
import com.utp.sautex.modelos.repositorios.MovimientoInventarioRepository;
import com.utp.sautex.modelos.repositorios.ProductoRepository;
import com.utp.sautex.modelos.repositorios.RolRepository;
import com.utp.sautex.modelos.repositorios.UsuarioRepository;
import com.utp.sautex.modelos.servicios.AlmacenService;
import com.utp.sautex.modelos.servicios.ClaveService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class DatosIniciales implements CommandLineRunner {

    private final RolRepository roles;
    private final UsuarioRepository usuarios;
    private final ProductoRepository productos;
    private final MovimientoInventarioRepository movimientos;
    private final AlmacenService almacen;
    private final ClaveService claves;

    public DatosIniciales(RolRepository roles, UsuarioRepository usuarios, ProductoRepository productos,
                          MovimientoInventarioRepository movimientos, AlmacenService almacen,
                          ClaveService claves) {
        this.roles = roles;
        this.usuarios = usuarios;
        this.productos = productos;
        this.movimientos = movimientos;
        this.almacen = almacen;
        this.claves = claves;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Rol rolAdmin = buscarRol("admin", "Administrador del sistema");
        Rol rolAlmacen = buscarRol("almacen", "Encargado de almacén");
        Rol rolCliente = buscarRol("cliente", "Cliente de la tienda");

        buscarUsuario("Admin", "SAUTEX", "admin@sautex.com", "999000000",
                "Oficina SAUTEX", "admin123", rolAdmin);
        buscarUsuario("Almacen", "SAUTEX", "almacen@sautex.com", "999000001",
                "Almacén SAUTEX", "almacen123", rolAlmacen);
        buscarUsuario("Carmen", "Ríos", "cliente@sautex.com", "987654321",
                "Av. Los Hilos 245, Lima", "cliente123", rolCliente);

        if (productos.count() == 0) {
            crearProductos();
        }
        if (movimientos.count() == 0) {
            productos.findAll().stream()
                    .filter(producto -> producto.getStock() != null && producto.getStock() > 0)
                    .forEach(producto -> almacen.registrarEntradaInicial(
                            producto, producto.getStock(), "Stock inicial"));
        }
    }

    private Rol buscarRol(String nombre, String descripcion) {
        return roles.findByNombre(nombre)
                .orElseGet(() -> roles.save(new Rol(nombre, descripcion)));
    }

    private Usuario buscarUsuario(String nombre, String apellido, String correo, String telefono,
                                  String direccion, String clave, Rol rol) {
        return usuarios.findByCorreoIgnoreCase(correo).orElseGet(() -> {
            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setCorreo(correo);
            usuario.setTelefono(telefono);
            usuario.setDireccion(direccion);
            usuario.setClave(claves.encriptar(clave));
            usuario.setEstado("Activo");
            usuario.setRol(rol);
            return usuarios.save(usuario);
        });
    }

    private void crearProductos() {
        producto("Hilo Poliéster 150D", "Cono x 5 kg", "12.50", 120, 6, "Poliéster",
                "hilo-polieter150.jpg", "Hilo de poliéster de alta resistencia para costura industrial.");
        producto("Hilo Nylon Industrial", "Cono x 5 kg", "15.00", 80, 2, "Nylon",
                "hilo-nylon.jpg", "Hilo de nylon resistente para tejidos técnicos y acabados.");
        producto("Hilo Poliéster 300D", "Cono x 8 kg", "18.50", 45, 3, "Poliéster",
                "hilo-poliester300.jpg", "Hilo de mayor grosor para costuras reforzadas.");
        producto("Hilo Texturizado 75D", "Bobina x 3 kg", "9.80", 100, 5, "Texturizados",
                "hilo-texturizado75.jpg", "Hilo texturizado de textura suave para prendas ligeras.");
        producto("Hilo Algodón 20/2", "Cono x 1 kg", "14.00", 60, 0, "Algodón",
                "hilo-algodon.jpg", "Hilo de algodón peinado para confección de prendas.");
        producto("Hilo Poliéster 250D", "Cono x 6 kg", "16.50", 35, 0, "Poliéster",
                "hilo-poliester250.jpg", "Hilo de poliéster con excelente resistencia a la tracción.");
        producto("Hilo Nylon 210D", "Cono x 4 kg", "13.90", 8, 0, "Nylon",
                "hilo-nylon210.jpg", "Hilo de nylon para aplicaciones de alta exigencia.");
        producto("Hilo Texturizado 150D", "Bobina x 4 kg", "11.20", 25, 0, "Texturizados",
                "hilo-texturizado150.jpg", "Hilo texturizado con acabado uniforme para bordado.");
    }

    private void producto(String nombre, String unidad, String precio, int stock, int vendidos,
                          String categoria, String imagen, String descripcion) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setUnidad(unidad);
        producto.setPrecio(new BigDecimal(precio));
        producto.setStock(stock);
        producto.setVendidos(vendidos);
        producto.setCategoria(categoria);
        producto.setImagen(imagen);
        producto.setDescripcion(descripcion);
        producto.setActivo(true);
        productos.save(producto);
    }
}
