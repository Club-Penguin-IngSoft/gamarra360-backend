# CLAUDE.md

Este archivo proporciona orientación a Claude Code (claude.ai/code) al trabajar con el código de este repositorio.

## Comandos

```bash
# Compilar
mvn clean install

# Ejecutar localmente
mvn spring-boot:run

# Ejecutar todos los tests
mvn test

# Ejecutar un test específico
mvn test -Dtest=NombreClaseTest

# Empaquetar sin ejecutar tests
mvn package -DskipTests
```


## Arquitectura

### Paquete base
`pe.com.gamarra360.backend`

### Módulos de dominio
Cada módulo vive en su propio sub-paquete y sigue la misma estructura:

```
<modulo>/
  entity/      — Entidades JPA
  repository/  — Interfaces JpaRepository (sin impl personalizada para consultas básicas)
  service/     — Clases @Service concretas con métodos listar/obtener/crear/actualizar/eliminar
  controller/  — @RestController con @RequestMapping("/api/v1/...")
```

Módulos: `catalogo`, `pedido`, `pago`, `solicitud`, `usuario`

### Patrón de servicio (`service/`)
Cada servicio es una clase `@Service @Slf4j` directamente en el paquete `service/` (sin capa de interfaz ni sub-paquete `impl/`). Patrón estándar:

```java
@Service
@Slf4j
public class FooService {
    private final FooRepository repository;
    public FooService(FooRepository repository) { this.repository = repository; }

    public List<Foo> listar() { ... }
    public Foo obtener(Integer id) { ... }
    public Foo crear(Foo entidad) { ... }
    public Foo actualizar(Integer id, Foo entidad) { entidad.setId(id); ... }
    public void eliminar(Integer id) { ... }
}
```

`AuthService` en el módulo `usuario` es la excepción: ya era una clase concreta y no se toca.

### Herencia de usuario (`usuario/`)
`Usuario` es la entidad JPA base con `InheritanceType.JOINED`. `Admin`, `Cliente` y `Comerciante` tienen cada uno su propia tabla unida por `usuario_id`. `CustomUserDetailsService` carga un `Usuario` por email para Spring Security.

### Flujo de seguridad
1. Toda solicitud (excepto `/api/v1/auth/**` y `/actuator/health`) requiere un Bearer JWT.
2. `JwtAuthenticationFilter` extrae y valida el token mediante `JwtService`, luego popula el `SecurityContext`.
3. `SecurityConfig` usa sesiones sin estado (`SessionCreationPolicy.STATELESS`) y `@EnableMethodSecurity` para autorización a nivel de método.

### Manejo de excepciones (`exception/`)
`GlobalExceptionHandler` (@RestControllerAdvice) mapea:
- `RecursoNoEncontradoException` → 404
- `DatosInvalidosException` / `MethodArgumentNotValidException` → 400
- `AccessDeniedException` → 403
- `Exception` → 500

Siempre lanzar estas excepciones tipadas desde los servicios en lugar de retornar respuestas de error directamente desde los controladores.

### Enums (`enums/`)
Enums de dominio compartidos entre módulos: `RolEnum`, `ProveedorAuth`, `EstadoPago`, `EstadoPedido`, `EstadoSolicitud`, `TipoEntrega`, `TipoTrabajo`.