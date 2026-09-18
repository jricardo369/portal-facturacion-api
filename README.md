# API Facturación — Portal de Facturación

API REST con Spring Boot 3.5 + Java 21, arquitectura hexagonal, JWT, MySQL y Flyway.

## 1. Requisitos previos

- Java 21, Maven 3.9+
- MySQL 8.x corriendo en el host (no en Docker)
- Docker Desktop (solo para ejecutar la API)
- `JWT_SECRET` de al menos 32 caracteres (256 bits recomendados con 64+ caracteres)

## 2. Instalación y configuración de MySQL

```sql
CREATE DATABASE IF NOT EXISTS facturacion CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'facturacion'@'%' IDENTIFIED BY 'facturacion';
GRANT ALL PRIVILEGES ON facturacion.* TO 'facturacion'@'%';
FLUSH PRIVILEGES;
```

Las tablas se crean con Flyway (`V1__create_usuarios_table.sql`).

## 3. Variables de entorno

Ver `.env.example`. Copia a `.env` para Docker Compose:

```bash
cp .env.example .env
# edita JWT_SECRET con un valor propio, p. ej. generado con:
# openssl rand -base64 48
```

**Nota**: Las credenciales de FuDo y correo ya NO van en `.env`. Están en el archivo externo `/Users/joser.vazquez/config-apps/portal-facturacion/configuracion-general.yml`.

| Variable | Descripción | Default |
|---|---|---|
| DB_HOST | Host MySQL. En Docker Desktop Mac/Win usa `host.docker.internal` | `host.docker.internal` |
| DB_PORT | Puerto MySQL | `3306` |
| DB_NAME | Base de datos | `facturacion` |
| DB_USERNAME / DB_PASSWORD | Credenciales | — |
| JWT_SECRET | Clave HMAC (≥32 chars, no versionar) | — (obligatoria) |
| JWT_EXPIRATION_MS | Expiración JWT (15-30 min) | `1800000` |
| JWT_ISSUER / JWT_AUDIENCE | Emisor/audiencia esperados | `portal-facturacion` |
| CORS_ALLOWED_ORIGINS | Orígenes permitidos (coma) | `http://localhost:3000` |
| SERVER_PORT | Puerto API | `8080` |

## 4. Configuración externa (única fuente de credenciales)

Todas las credenciales (SMTP, FuDo) viven **solo** en un archivo externo. El proyecto no contiene ninguna credencial.

**Ruta del archivo**: `/Users/joser.vazquez/config-apps/portal-facturacion/configuracion-general.yml`

```yaml
app:
  fudo:
    base-url: https://api.fu.do/v1alpha1
    auth-url: https://auth.fu.do/api
    api-key: TU_API_KEY
    api-secret: TU_API_SECRET
    timeout-ms: 8000
    sale-include: items.product,payments.paymentMethod,customer,table
  correo:
    host: smtp.gmail.com
    port: 587
    username: tu@correo.com
    password: tu-password
    auth: true
    starttls: true
    timeout-ms: 8000
    ruta-logo: http://localhost:8080/api/v1/images/logo.png
```

**Variables de entorno** usadas en el código (sin defaults en el proyecto): `CORREO_HOST`, `CORREO_PORT`, `CORREO_USERNAME`, `CORREO_PASSWORD`, `CORREO_AUTH`, `CORREO_STARTTLS`, `CORREO_TIMEOUT_MS`, `CORREO_RUTA_LAYOUTS`, `CORREO_RUTA_LOGO`, `FUDO_API_KEY`, `FUDO_API_SECRET`, `FUDO_API_BASE_URL`, `FUDO_AUTH_URL`, `FUDO_TIMEOUT_MS`.

## 5. Ejecución local con Maven

```bash
export DB_HOST=localhost DB_NAME=facturacion DB_USERNAME=facturacion DB_PASSWORD=facturacion
export JWT_SECRET='clave-local-de-al-menos-32-caracteres-123456'
# Asegurate de que exista el archivo configuracion-general.yml en la ruta configurada en application-local.yml
./mvnw spring-boot:run
```

El perfil `local` importa `configuracion-general.yml` desde `/Users/joser.vazquez/config-apps/portal-facturacion/configuracion-general.yml`.

## 6. Ejecución con Docker

Solo se levanta la API; MySQL queda fuera (host):

```bash
./mvnw clean test
docker compose up --build
```

El perfil `prod` importa `configuracion-general.yml` desde `/opt/tomcat/assets/configuracion-general.yml`. El archivo se monta como volumen desde el host:

```yaml
volumes:
  - /Users/joser.vazquez/config-apps/portal-facturacion/configuracion-general.yml:/opt/tomcat/assets/configuracion-general.yml:ro
```

**No copies el archivo dentro del proyecto.** Docker solo necesita el volumen montado para leer las credenciales.

Health: `GET http://localhost:8080/actuator/health`

## 7. Migraciones Flyway

Automáticas al arrancar (`spring.flyway.enabled=true`, `ddl-auto: validate`).
Crear nueva migración en `src/main/resources/db/migration/V<N>__descripcion.sql`.

## 8. Endpoint de login

```http
POST /api/v1/auth/login
Content-Type: application/json

{"usuario":"admin","password":"admin1234"}
```

Respuesta `200`:

```json
{"token":"<jwt>","tokenType":"Bearer","id":1,"usuario":"admin","tipo":"ADMIN"}
```

El JWT incluye `sub`, `uid`, `tipo`, `iss`, `aud`, `iat`, `exp`.

## 9. Ejemplos CRUD

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"usuario":"admin","password":"admin1234"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['token'])")

curl -H "Authorization: Bearer $TOKEN" "localhost:8080/api/v1/usuarios?page=0&size=20"
curl -H "Authorization: Bearer $TOKEN" localhost:8080/api/v1/usuarios/1
curl -X POST -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"usuario":"nuevo","password":"secreto123","nombre":"Nuevo","tipo":"OPERADOR"}' \
  localhost:8080/api/v1/usuarios
curl -X PUT -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"usuario":"nuevo","nombre":"Nuevo editado"}' localhost:8080/api/v1/usuarios/1
curl -X DELETE -H "Authorization: Bearer $TOKEN" localhost:8080/api/v1/usuarios/1
```

Filtros: `?usuario=adm&nombre=juan&tipo=ADMIN`. Solo `ADMIN` puede crear/actualizar/eliminar;
`ADMIN, OPERADOR, CONSULTA` pueden listar/consultar. Códigos: `200, 201, 204, 400, 401, 403, 404, 409`.

## 10. Envío del token JWT

```http
Authorization: Bearer <token>
```

Sin token → `401`; token válido sin rol suficiente → `403`.

## 11. Consulta de tickets (FuDo) — portal de bienvenida

Endpoint **público** (sin JWT), pensado para el portal `bienvenida-facturacion`:

```http
GET /api/v1/tickets/{numeroTicket}?transaccion=&rfc=&codigoPostal=
```

Ejemplo:

```bash
curl localhost:8080/api/v1/tickets/123
curl "localhost:8080/api/v1/tickets/123?transaccion=20&rfc=XAXX010101000&codigoPostal=06600"
```

Respuesta `200` (normalizada desde `GET /sales/{id}` de FuDo):

```json
{
  "numeroTicket": "123",
  "total": 1350.50,
  "subtotal": 1350.50,
  "estado": "CLOSED",
  "tipoVenta": "EAT-IN",
  "fechaEmision": "2026-09-01T10:00:00Z",
  "cliente": "Paula",
  "mesa": "Mesa 1",
  "facturable": true,
  "items": [{ "id": "10", "descripcion": "Cafe Latte", "cantidad": 2 }],
  "pagos": [{ "id": "20", "metodo": "Cash", "monto": 1350.50 }]
}
```

Códigos: `200` ok · `400` folio con formato inválido · `404` ticket inexistente · `502` FuDo no configurado / no disponible. Requiere `FUDO_API_KEY` + `FUDO_API_SECRET`; el token FuDo (`POST https://auth.fu.do/api`) se cachea hasta su `exp` y se renueva ante `401`.

## 12. Credenciales de prueba

No se crea usuario inicial por defecto (evita secretos versionados). Para pruebas locales crea uno vía SQL con BCrypt o arranca los tests (`application-test.yml` usa H2 + `UsuarioFlujoIT` que siembra `admin/admin1234` en memoria).

Generar hash BCrypt de ejemplo:

```bash
python3 -c "import bcrypt;print(bcrypt.hashpw(b'admin1234',bcrypt.gensalt()).decode())"
```

```sql
INSERT INTO usuarios (usuario,password,nombre,tipo,activo,created_at,updated_at)
VALUES ('admin','<HASH_BCRYPT>','Administrador','ADMIN',TRUE,NOW(),NOW());
```

## Supuestos y decisiones

- `tipo` → autoridad `ROLE_<TIPO>` (`ADMIN`, `OPERADOR`, `CONSULTA`).
- `DELETE` es baja lógica (`activo=false`); inactivos no listan ni loguean (login → `401`).
- CSRF deshabilitado por API stateless con Bearer (documentado en `SecurityConfig`); CORS explícito por propiedades.
- JWT valida firma HMAC, `iss` esperado y expiración; algoritmo fijado en servidor (no `alg` del token).
- Actuator expone solo `health,info`; health incluye chequeo DB.
- `adapter.out.mail` es NoOp; `adapter.out.file` es stub pendiente.
- Tests: unitarios (servicios, JWT), `@WebMvcTest` (controladores, authz), integración H2 (flujo completo), Testcontainers MySQL condicional (se omite sin Docker), ArchUnit (domain/application no dependen de adapters/frameworks).
- Tickets/FuDo: el folio del portal se mapea al `id` de venta (`sale`) de FuDo (`GET /sales/{id}?include=items.product,payments.paymentMethod,customer,table`, base `https://api.fu.do/v1alpha1`, ref `https://dev.fu.do/api`). Solo ventas `CLOSED` se marcan `facturable=true`; `transaccion/rfc/codigoPostal` son opcionales de trazabilidad (la validación estricta contra el ticket impreso queda pendiente de confirmar el campo FuDo). `GET /api/v1/tickets/**` es público para el portal de bienvenida (sin login).

## Docker: iniciar y detener

```bash
# Iniciar contenedores
docker compose up --build

# Iniciar en segundo plano
docker compose up --build -d

# Detener y eliminar contenedores
docker compose down

# Detener contenedores sin borrar volúmenes/redes
docker compose stop
```

Para ver logs:

```bash
docker compose logs -f api
```
