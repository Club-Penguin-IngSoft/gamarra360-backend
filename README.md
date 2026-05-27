# Gamarra 360° — Backend

Backend Spring Boot del proyecto **Gamarra 360°** (PUCP — Ingeniería de Software, equipo Club Penguin).

Este repositorio contiene el módulo `catalogo` enfocado en **CU-07 (Navegación del Catálogo Público)** y **CU-08 (Detalle de Producto y Búsqueda por Relevancia)** — sección 4 del backlog del proyecto.

---

## Stack

| Componente | Versión |
|---|---|
| Java | **17** |
| Spring Boot | **3.2.5** |
| Maven | wrapper incluido (`mvnw`) |
| MySQL | 8.x (local + AWS RDS) |
| Lombok | última estable |

---

## Estructura del módulo `catalogo`

Sigue la arquitectura monolítica modular por capas definida en `CLAUDE.md §3-4`:

```
src/main/java/com/gamarra360/
├── GamarraApplication.java
├── config/
│   └── CorsConfig.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ErrorRespuestaDto.java
│   └── RecursoNoEncontradoException.java
├── usuario/                       ← minimal — solo para FK
│   └── entity/
│       ├── Usuario.java
│       ├── Comerciante.java
│       └── Tienda.java
└── catalogo/                      ← módulo principal
    ├── controller/
    │   └── ProductoController.java
    ├── service/
    │   ├── ProductoService.java
    │   └── ProductoServiceImpl.java
    ├── repository/
    │   ├── ProductoRepository.java
    │   └── CategoriaRepository.java
    ├── entity/
    │   ├── Producto.java
    │   ├── Categoria.java
    │   ├── Color.java
    │   ├── Talla.java
    │   ├── VarianteProducto.java
    │   ├── ImagenProducto.java
    │   ├── EspecificacionProducto.java
    │   └── DescuentoVolumen.java
    ├── dto/
    │   ├── ProductoDto.java
    │   ├── VarianteProductoDto.java
    │   ├── EspecificacionProductoDto.java
    │   ├── DescuentoVolumenDto.java
    │   ├── CategoriaDto.java
    │   └── FiltrosCatalogoDto.java
    └── mapper/
        └── ProductoMapper.java
```

---

## Endpoints

Base URL (local): `http://localhost:8080/api/v1`

| Método | Ruta | Descripción | CU |
|---|---|---|---|
| GET | `/productos` | Lista catálogo público con filtros + búsqueda por relevancia | CU-07/08 |
| GET | `/productos/{id}` | Detalle completo del producto (variantes, imágenes, specs, descuentos) | CU-08 |
| GET | `/productos/tienda/{idTienda}` | Productos de una tienda específica | CU-07 |

### Query params soportados en `GET /productos`

```
?q=polo                          → búsqueda por palabras clave (relevancia)
?categorias=HOMBRE&categorias=MUJER  → filtro por categoría (multi)
?tipoServicio=PERSONALIZABLE     → COMPRA_DIRECTA | PERSONALIZABLE
?precioMin=20&precioMax=100      → rango de precios
?color=Negro                     → filtro por color
?tallas=S&tallas=M               → filtro por tallas (multi)
```

### Ejemplos curl

```bash
# Listar todo el catálogo
curl http://localhost:8080/api/v1/productos

# Buscar "polo"
curl "http://localhost:8080/api/v1/productos?q=polo"

# Filtrar por HOMBRE
curl "http://localhost:8080/api/v1/productos?categorias=HOMBRE"

# Detalle del producto 13 (casaca biker premium)
curl http://localhost:8080/api/v1/productos/13
```

---

## Setup local

### Pre-requisitos

- Java 17 (JDK)
- MySQL 8.x corriendo en `localhost:3306`
- Maven (o usa el wrapper `./mvnw`)

### Pasos

1. **Crear la BD** ejecutando `BD/schema_clean.sql`:
   ```bash
   mysql -u root -p < ../BD/schema_clean.sql
   ```

2. **Configurar credenciales** (opcional, valores por defecto `root/root`):
   ```bash
   # PowerShell
   $env:DB_USER="root"
   $env:DB_PASSWORD="tu_password"

   # Bash
   export DB_USER=root
   export DB_PASSWORD=tu_password
   ```

3. **Levantar la aplicación**:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

   La primera vez carga `data.sql` con productos de prueba (13 productos).

4. **Probar**: abre `http://localhost:8080/api/v1/productos` en el navegador.

---

## Setup AWS RDS MySQL (futuro)

Cuando esté lista la BD en AWS, exportar variables de entorno:

```bash
export DB_URL="jdbc:mysql://<endpoint-rds>.amazonaws.com:3306/gamarra360?useSSL=true&serverTimezone=America/Lima"
export DB_USER="<usuario_rds>"
export DB_PASSWORD="<password_rds>"

./mvnw spring-boot:run -Dspring-boot.run.profiles=aws
```

**IMPORTANTE**: en AWS NO se ejecuta `data.sql` (el perfil `aws` tiene `spring.sql.init.mode: never`). La data real ya debe existir en RDS.

---

## Conexión con el frontend

El frontend (`Frontend/`) tiene su `apiClient` configurado con base URL `http://localhost:8080/api/v1` por defecto. Para que funcione end-to-end:

1. Levantar backend: `./mvnw spring-boot:run -Dspring-boot.run.profiles=local` (puerto 8080)
2. Levantar frontend: `npm run dev` desde `Frontend/` (puerto 5173)
3. CORS ya está configurado en `CorsConfig.java` para permitir `http://localhost:5173`

Cuando el frontend quiera apuntar al backend real (no a los mocks), basta con descomentar las llamadas a `apiClient.get(...)` en `Frontend/src/services/catalogoService.ts` y eliminar las funciones mock.

---

## Notas técnicas

### Sobre el schema BD

- El schema (`schema_clean.sql`) tiene **27 tablas**. Este módulo (`catalogo`) usa **10 tablas**: `productos`, `variantes_producto`, `categorias`, `colores`, `tallas`, `producto_categoria`, `imagenes_producto`, `especificaciones`, `descuentos_volumen`, y por FK también `tiendas`, `comerciantes`, `usuarios`.

- Las **17 tablas restantes** (carritos, solicitudes, cotizaciones, personalizaciones, pedidos, pagos, etc.) son responsabilidad de otros equipos / módulos.

- `spring.jpa.hibernate.ddl-auto: validate` → Hibernate solo verifica que las entidades coincidan con el schema. **NO modifica** la BD.

### Sobre la búsqueda por relevancia (CU-08, RF-22/23)

Algoritmo de scoring actual (en `ProductoServiceImpl`):

- **+10 pts** si la palabra está en `productos.nombre`
- **+5 pts** si está en `productos.descripcion`
- **+5 pts** si está en `tiendas.nombre_comercial`
- **+3 pts** si está en `categorias.nombre_categoria`

Multi-palabra: cada palabra suma score independientemente.

**Migración futura**: cuando crezca el volumen, reemplazar por `MATCH AGAINST` con índices `FULLTEXT` en MySQL. Cambio aislado al `ProductoRepository`.

### Sobre exclusión de comerciantes no aprobados (CU-07, RF-20/21)

El método `ProductoRepository.findCatalogoPublico()` ya aplica el JOIN con condiciones:

```sql
WHERE producto.activo = TRUE
  AND tienda.verificada = TRUE
  AND comerciante.verificado = TRUE
  AND usuario.activo = TRUE
```

Esto garantiza que NUNCA aparecen en el catálogo público productos de:
- Comerciantes con `verificado = false` (no aprobados por admin)
- Usuarios con `activo = false` (cuenta desactivada)
- Tiendas con `verificada = false`
- Productos con `activo = false`

---

## Equipo

Módulo 4 (Exploración y Búsqueda) — **John & Claudia** (fullstack).

Otros módulos están a cargo de otros integrantes del equipo Club Penguin (10 personas).
