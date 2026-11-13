# SAUTEX E.I.R.L. - Backend

API REST para la aplicación móvil de SAUTEX E.I.R.L. Desarrollada con Spring Boot, SQLite y JSON.

## Requisitos

- Java 21
- Maven 3.9 o superior (o usar el wrapper incluido)
- Android Studio con emulador o un celular con Expo Go

## Ejecución

Desde la carpeta del proyecto:

```powershell
.\mvnw.cmd spring-boot:run
```

Si ya tienes Maven instalado también puedes usar:

```powershell
mvn spring-boot:run
```

El servidor arranca en `http://localhost:8080`.

La base de datos se crea automáticamente en `basedatos/sautex.db` la primera vez que se ejecuta el proyecto. Si necesitas reiniciar los datos, elimina ese archivo y vuelve a arrancar.

## Cuentas de prueba

| Rol | Correo | Contraseña |
| --- | --- | --- |
| Administrador | admin@sautex.com | admin123 |
| Almacén | almacen@sautex.com | almacen123 |
| Cliente | cliente@sautex.com | cliente123 |

Las contraseñas se guardan cifradas con BCrypt. Las cuentas se crean automáticamente al iniciar la aplicación.

## Autenticación

El inicio de sesión devuelve un token:

```json
{
  "exitoso": true,
  "mensaje": "Bienvenido a SAUTEX",
  "token": "TK-...",
  "rol": "admin",
  "usuario": {
    "id": 1,
    "nombre": "Admin",
    "apellido": "SAUTEX",
    "correo": "admin@sautex.com",
    "telefono": "999000000",
    "direccion": "Oficina SAUTEX",
    "foto": null,
    "estado": "Activo",
    "rol": "admin"
  }
}
```

Las rutas protegidas reciben el token en el encabezado:

```
Authorization: Bearer TK-...
```

Las sesiones se guardan en memoria y duran 4 horas. Al reiniciar el servidor se cierran todas las sesiones.

## Endpoints

### Autenticación

| Método | Ruta | Descripción |
| --- | --- | --- |
| POST | `/api/auth/login` | Inicia sesión con correo y contraseña |
| POST | `/api/auth/registro` | Registra un cliente nuevo |
| POST | `/api/auth/logout` | Cierra la sesión actual |

### Perfil

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/api/perfil` | Datos del usuario autenticado |
| PUT | `/api/perfil` | Actualiza nombre, apellido, teléfono, dirección y foto |
| PUT | `/api/perfil/clave` | Cambia la contraseña del usuario autenticado |

### Usuarios (solo administrador)

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/api/usuarios` | Lista todos los usuarios |
| POST | `/api/usuarios` | Crea un usuario |
| PUT | `/api/usuarios/{id}` | Actualiza un usuario |
| DELETE | `/api/usuarios/{id}` | Elimina un usuario |

El correo no se puede modificar desde ningún rol.

### Productos

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/api/productos` | Lista productos activos. Acepta `categoria` y `buscar` |
| GET | `/api/productos/categorias` | Lista las categorías disponibles |
| GET | `/api/productos/{id}` | Detalle de un producto |
| POST | `/api/productos` | Crea un producto (admin o almacén) |
| PUT | `/api/productos/{id}` | Actualiza un producto (admin o almacén) |
| DELETE | `/api/productos/{id}` | Da de baja un producto (admin o almacén) |

### Almacén (admin o almacén)

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/api/almacen/resumen` | Totales, categorías, alertas y últimos movimientos |
| POST | `/api/almacen/entradas` | Registra una entrada y aumenta el stock |
| GET | `/api/almacen/movimientos` | Kardex de entradas y salidas. Acepta `productoId` y `tipo` |

### Pedidos

| Método | Ruta | Descripción |
| --- | --- | --- |
| POST | `/api/pedidos` | Registra un pedido y descuenta stock |
| GET | `/api/pedidos/mios` | Pedidos del cliente autenticado |
| GET | `/api/pedidos` | Lista todos los pedidos (admin o almacén) |
| GET | `/api/pedidos/{id}` | Detalle de un pedido |
| PATCH | `/api/pedidos/{id}/estado` | Cambia el estado (admin o almacén) |

### Reportes (solo administrador)

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/api/reportes/resumen` | Ventas, pedidos, clientes, productos y stock bajo |
| GET | `/api/reportes/categorias` | Ventas por categoría. Acepta `periodo=semana`, `mes` o `anio` |

## Ejemplos de uso

### Iniciar sesión

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/auth/login `
  -ContentType "application/json" `
  -Body '{"correo":"admin@sautex.com","clave":"admin123"}'
```

### Listar productos

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8080/api/productos
```

### Registrar un pedido

```powershell
$token = "TK-..."
$cuerpo = @{
  items = @(
    @{ productoId = 1; cantidad = 2 },
    @{ productoId = 2; cantidad = 1 }
  )
  metodoPago = "Yape / Plin"
  tipoEntrega = "Delivery"
  direccionEnvio = "Av. Los Hilos 245, Lima"
  comprobanteTipo = "Boleta"
  comprobanteDocumento = "12345678"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/pedidos `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $token" } `
  -Body $cuerpo
```

El pedido calcula el subtotal, el IGV del 18 % y el total. Si no hay stock suficiente, responde con un error.

## Conexión desde la aplicación móvil

- Emulador de Android Studio: `http://10.0.2.2:8080`
- Celular físico con Expo Go: `http://IP_DE_LA_PC:8080` (por ejemplo `http://192.168.1.25:8080`)
- Navegador o Postman en la misma PC: `http://localhost:8080`

La PC y el celular deben estar en la misma red. Si el celular no conecta, revisa el firewall de Windows.

## Estructura del proyecto

```
src/main/java/com/utp/sautex
├── SautexBackendApplication.java
├── configuracion
│   ├── CorsConfig.java
│   ├── DatosIniciales.java
│   └── ManejadorErrores.java
├── controladores
│   ├── AuthController.java
│   ├── PedidoController.java
│   ├── PerfilController.java
│   ├── ProductoController.java
│   ├── ReporteController.java
│   └── UsuarioController.java
├── modelos
│   ├── entidades
│   ├── repositorios
│   └── servicios
└── vistas/dto
```

## Estado actual de la aplicación móvil

- Navegación con Stack y Bottom Tabs por rol.
- Inicio de sesión, registro, perfil y cambio de contraseña conectados al backend.
- Usuarios administrados solo desde el backend.
- Recuperación de contraseña solo local y visual; no cambia la clave del servidor.
- CRUD de usuarios desde el panel del administrador.
- Panel de almacén con resumen real de productos, stock, valor y movimientos.
- Inventario con agregar, editar y eliminar productos.
- Registro de entradas y kardex de entradas y salidas.
- Catálogo, carrito e historial de compras todavía usan datos locales.

### Pendiente

1. Conectar el catálogo, el carrito y el historial con `/api/productos` y `/api/pedidos`.
2. Conectar el panel del administrador con `/api/reportes` y `/api/pedidos`.
3. Reemplazar los datos visuales restantes por información del backend.


## Notas

- SQLite no necesita instalar ningún servidor de base de datos.
- La carpeta `basedatos` debe existir antes de arrancar. En el repositorio se mantiene con un archivo `.gitkeep`.
- Las imágenes de productos nuevos se guardan como data URI base64 en el campo `imagen`. Las imágenes iniciales usan nombres de archivo de `assets`.
- El kardex registra entradas manuales, salidas por pedidos y ajustes de stock al editar un producto.
- La eliminación de productos es lógica: el producto deja de mostrarse en el catálogo, pero se conserva en la base de datos.
