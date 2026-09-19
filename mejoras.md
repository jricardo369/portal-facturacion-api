# Mejoras detectadas en la auditoria de `portal-facturacion-api`

Auditoria de estandares, arquitectura y seguridad (Java 21 / Spring Boot / hexagono).
Se documenta todo lo encontrado excepto las actualizaciones de versiones de librerias, que ya se aplicaron en `pom.xml`.

Prioridades: 🔴 Critica / 🟠 Alta / 🟡 Media / 🔵 Baja

---

## 1. Seguridad

### 1.1 🔴 Secretos y credenciales commiteados en el repositorio

- `src/main/resources/application-local.yml` define password de BD `Ricardo16` y un `JWT_SECRET` por defecto en claro.
- `docker-compose.yml` expone los mismos defaults (`DB_PASSWORD`, `JWT_SECRET`).
- `V2__seed_admin_user.sql` crea el usuario `admin` con password conocido `Admin123!` y sin mecanismo que fuerce el cambio tras el primer login.

**Accion sugerida**

- Eliminar valores por defecto secretos; la aplicacion debe fallar al arrancar si faltan las variables de entorno.
- Rotar/eliminar el JWT secret por defecto.
- Implementar "cambio de contrasena obligatorio en primer login" para el seed, o eliminar el seed y crear el admin por script/secret gestionado.

### 1.2 🔴 `JWT_SECRET` invalido en `.env.example`

`.env.example` usa `JWT_SECRET=OsoDespierto2026` (15 caracteres). jjwt exige una clave de al menos 256 bits (32 bytes) para HS256; con ese valor la aplicacion **no arranca** (`WeakKeyException`).

**Accion sugerida**

- Usar un secreto de, al menos, 32 caracteres aleatorios y documentarlo en `README`/`.env.example` como ejemplo no funcional.

### 1.3 🔴 Endpoints de facturacion publicos, sin autenticacion ni rate limit

- `SecurityConfig` permite sin token: `GET /api/v1/facturacion/datos-factura`, `GET /api/v1/facturacion/factura`, `POST /api/v1/facturacion/facturar`, `POST /api/v1/facturacion/enviar-correo`, `POST /api/v1/facturacion/reenviar-factura`.
- `JwtAuthenticationFilter.shouldNotFilter()` excluye los mismos paths sin validar el token.

**Impacto**

- Consulta de tickets/facturas de terceros sin autenticacion.
- Cualquier persona puede disparar el envio de correos a direcciones arbitrarias (abuso de email bombing).

**Accion sugerida**

- Requerir autenticacion con rol (`ADMIN`/`OPERADOR`/`CONSULTA`) en estos endpoints.
- Agregar rate limiting (p. ej. `Bucket4j`, `Resilience4j`, control de concurrencia) y verificar el dominio/remitente del correo.

### 1.4 🔴 `/facturar` acepta datos arbitrarios del cliente

`FacturacionController.facturar()` recibe `DatosFacturaResponse` como request body sin validacion y con datos de importes, impuestos y `facturable` controlados por el cliente. Un atacante puede grabar facturas con totales arbitrarios.

**Accion sugerida**

- Crear un DTO de request dedicado y validado (`DatosFacturaRequest`) solo con identificadores (RFC, numeroTicket).
- Que el servidor recalcule totales/impuestos desde la consulta real del ticket (FuDo) y del RFC, y valide consistencia.

### 1.5 🟠 Exposicion de directorios del filesystem en `/api/v1/images/**`

`WebConfig` registra handlers `file:src/main/webapp/images/` y `file:src/main/resources/static/images/`, y `SecurityConfig` los deja publicos (`permitAll`). Riesgo de path traversal y exposicion accidental de archivos del servidor.

**Accion sugerida**

- Servir imagenes desde `classpath:/static/images/` (protegidas o sin contenido sensible) y eliminar las rutas `file:` mientras no sean estrictamente necesarias.
- Si se mantienen, restringir a roles y validar el nombre contra path traversal.

### 1.6 🟠 JWT: no se valida `audience` ni el estado del usuario en BD

- `JwtTokenAdapter.parse()` valida firma e `issuer` pero no `audience`.
- El filtro construye el `Authentication` desde los claims; si un usuario se desactiva o elimina, sus tokens siguen validos hasta expirar.

**Accion sugerida**

- Validar `audience` en el parser.
- Consultar la vigencia del usuario por request (cache corto) o reducir `JWT_EXPIRATION_MS` y soportar lista de revocacion.

### 1.7 🟠 Sin rate limiting ni bloqueo por intentos en `/auth/login`

El login no tiene throttling: exposicion a fuerza bruta de credenciales.

**Accion sugerida**

- Rate limiting por IP/usuario + retraso progresivo/bloqueo temporal tras N fallos.

### 1.8 🟡 `/actuator/info` publico con env habilitado

`management.info.env.enabled=true` expone informacion del entorno vía `/actuator/info` sin autenticacion.

**Accion sugerida**

- Desactivar `management.info.env.enabled` o proteger el endpoint.

### 1.9 🔵 Headers de seguridad HTTP ausentes

No se establecen `Strict-Transport-Security`, `X-Content-Type-Options`, `Cache-Control` ni CSP.

**Accion sugerida**

- Configurar headers de seguridad (idealmente HSTS exacerbado por Caddy).

---

## 2. Estandares y arquitectura

### 2.1 🟠 Falta de handlers de error especificos

`GlobalExceptionHandler` no cubre:

- `HttpMessageNotReadableException` (JSON invalido)
- `MethodArgumentTypeMismatchException` (parametros mal tipados)
- `NoHandlerFoundException` / 404 de ruta
- `DataIntegrityViolationException` (conflictos de BD no mapeados a excepciones de dominio)

**Accion sugerida**

- Agregar handlers y un catch-all para `Exception` que no filtre detalles internos.

### 2.2 🟡 Parametro `sort` sin whitelist de campos

En `ClienteRepositoryAdapter`, `FacturaRepositoryAdapter` y `UsuarioRepositoryAdapter`, el nombre de la propiedad se toma del request. No permite inyeccion, pero falla o expone campos no previstos.

**Accion sugerida**

- Whitelist de campos de ordenamiento validos y direccion explicita.

### 2.3 🟡 `enviarTextoPlano` envia HTML y la excepcion de correo se silencia

- `SmtpCorreoAdapter` usa `helper.setText(contenido, true)` (HTML) aunque la API se llama "TextoPlano".
- Las excepciones del envio solo se loguean; no hay reintentos ni cola; el endpoint responde "enviado" sin garantia de entrega.

**Accion sugerida**

- Renombrar a `enviarHtml` o usar `false` si debe ser texto plano.
- Evaluar reintentos, persistencia de pendientes o confirmaciones entregadas.

### 2.4 🟡 Codigo muerto / incompleto

- `LocalFileStorageAdapter.guardar()` lanza `UnsupportedOperationException` (sin implementar).
- `UsuarioMantenimientoScheduler.depurar()` solo loguea; no hace mantenimiento.
- `EnviarFacturaRequest` y `ReenviarFacturaRequest` son identicos (duplicacion).

**Accion sugerida**

- Implementar, eliminar o marcar explicitamente como backlog.

### 2.5 🟡 Duplicidad documental de `FacturacionController`

Existen dos controladores en `/api/v1/facturacion` (`FacturacionController` y `EnvioFacturaController`) con responsabilidades parcialmente repetidas y cohesion baja.

**Accion sugerida**

- Unificar en un solo controlador o separar por dominio claro.

### 2.6 🟡 CI sin analisis estatico ni cobertura

El workflow de GitHub solo ejecuta `verify`. No hay `spotless`/`checkstyle`, ni reporte de cobertura (JaCoCo), ni dependabot activo para el pom.

**Accion sugerida**

- Agregar formateo objetivo (p. ej. Spotless) y JaCoCo para medir/supervisar cobertura.
- El workflow despliega solo en la rama `main`, mientras el repo usa `master` (revisar).

### 2.7 🔵 Cobertura de reglas de arquitectura minima

`ArquitecturaTest` tiene solo 3 reglas (domain/adapter). Faltan reglas para: `application` -> `port.in` usados via interfaces, `adapter.in` -> usa `application`, narquitectura de paquetes para `configuration`.

**Accion sugerida**

- Ampliar el test de arquitectura para blindar las capas faltantes.

### 2.8 🔵 Recovery y resiliencia de FuDo

`FudoAuthClient` cachea el token en memoria sin jitter; `FudoTicketAdapter` reintenta solo una vez ante 401. No hay timeouts explícitos configurados en `RestClient` (el resto depende de defaults).

**Accion sugerida**

- Configurar timeouts de conexion/lectura en `RestClient` y considerar reintentos con backoff ante 5xx/red.

---

## 3. Resumen por prioridad

| Prioridad | Cantidad | Items |
|---|---|---|
| 🔴 Critica | 4 | 1.1, 1.2, 1.3, 1.4 |
| 🟠 Alta | 3 | 1.5, 1.6, 1.7, 2.1 |
| 🟡 Media | 6 | 1.8, 2.2, 2.3, 2.4, 2.5, 2.6 |
| 🔵 Baja | 2 | 1.9, 2.7, 2.8 |

> Nota: las actualizaciones de versiones (Spring Boot 3.5.16, springdoc 2.8.17, jjwt 0.13.0, Testcontainers 1.21.4, ArchUnit 1.4.2, H2 a scope `test`) ya fueron aplicadas y verificadas en `pom.xml`; no estan incluidas en este documento.