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
  entity/          — Entidades JPA
  repository/      — Interfaces JpaRepository (sin impl personalizada para consultas básicas)
  service/         — Interfaces de servicio que extienden CrudService<T, ID>
  service/impl/    — Clases concretas que extienden AbstractCrudService<T, ID>
  controller/      — @RestController con @RequestMapping("/api/v1/...")
```

Módulos: `catalogo`, `pedido`, `pago`, `solicitud`, `usuario`

### Abstracción CRUD (`service/`)
- `CrudService<T, ID>` — interfaz con `listar`, `obtener`, `crear`, `actualizar`, `eliminar`
- `AbstractCrudService<T, ID>` — provee la implementación completa; las subclases deben implementar `getLog()` y `asignarId(T, ID)` (asigna la PK a la entidad antes de guardar)

Todas las implementaciones de servicio siguen este patrón:
```java
@Service @Slf4j
public class FooServiceImpl extends AbstractCrudService<Foo, Integer> implements FooService {
    public FooServiceImpl(FooRepository repo) { super(repo, "Foo"); }
    @Override protected Logger getLog() { return log; }
    @Override protected void asignarId(Foo e, Integer id) { e.setId(id); }
}
```

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