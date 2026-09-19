# Mejoras de seguridad para endpoints publicos sin token (`/api/v1/facturacion/*`)

Contexto: el portal permite solicitar factura sin usuario/contraseña, por lo que ciertos
endpoints se exponen publicamente. Este documento recopila las medidas para reducir el
riesgo de esos endpoints y quedan pendientes de implementar/revisar.

Prioridades: 🔴 Critica / 🟠 Alta / 🟡 Media / 🔵 Baja

Estado: [ ] pendiente  [ ] en progreso  [ ] implementado

---

## 1. Modelo de amenaza de los endpoints publicos

| Endpoint | Riesgo | Impacto |
|---|---|---|
| `GET /api/v1/facturacion/datos-factura` | Enumeracion de folios / fuga de datos del ticket | Consulta de tickets de terceros |
| `GET /api/v1/facturacion/factura` | Enumeracion de folios / fuga de facturas | Consulta de facturas de terceros |
| `POST /api/v1/facturacion/facturar` | Integridad de datos | Facturas con montos/fechas arbitrarios |
| `POST /api/v1/facturacion/enviar-correo` | Email bombing | Envio de correos a direcciones arbitrarias |
| `POST /api/v1/facturacion/reenviar-factura` | Email bombing | Envio de correos a direcciones arbitrarias |
| `GET /api/v1/images/**` | Path traversal / exposicion de FS | Lectura de archivos del servidor |

Puntos de entrada actuales:
- `SecurityConfig.java:43-45` -> matchers publicos
- `JwtAuthenticationFilter.java:53-62` -> paths excluidos del filtro JWT

---

## 2. Estrategia por capas

### Capa 1 - Reducir superficie 🔵
- [ ] Eliminar los handlers `file:` de `WebConfig.java:12-16` (imagenes solo desde `classpath:/static/images/`).
- [ ] No exponer swagger-ui / actuador de forma publica si no se requiere.

### Capa 2 - Endurecer la "credencial" del usuario publico 🟠
El numero de ticket se genera en otro sistema (FuDo) e imprime en el ticket; si es corto/secuencial
es adivinable. Mitigaciones:

- [ ] Prueba de posesion: exigir `numeroTicket` + `transaccion` (y que coincida con el ticket en
      FuDo) en `GET datos-factura`, `GET factura`, `POST facturar`, `POST enviar-correo` y
      `POST reenviar-factura`.
      - Ubicacion sugerida: `FacturacionService.obtenerDatosFactura` / `facturar` y
        `EnvioFacturaService`.
      - Error generico en caso de no coincidir (no confirmar existencia del folio).
- [ ] (Futuro) Si el ticket no imprime `transaccion`, evaluar emitir codigo publico aleatorio
      no adivinable (`TKT-XXXX-XXXX-XXXX-XXXX`) mapeado al folio, para usarlo como credencial
      en endpoints publicos.

### Capa 3 - Minimizar input e integridad server-side 🔴
- [ ] `POST /facturar`: dejar de aceptar `DatosFacturaResponse` (hoy `FacturacionController.java:49-53`).
      Crear `SolicitarFacturacionRequest` validado con:
      `numeroTicket`, `transaccion`, `rfc`, `razonSocial`, `codigoPostal`, `correoElectronico`,
      `regimenFiscal`, `usoFactura` (+ resto de datos fiscales).
- [ ] El servidor recalcula subtotal/impuestos/total y `facturable` desde el ticket de FuDo;
      no confiar en montos del cliente.
- [ ] Rechazar tickets ya facturados y tickets no cerrados (`facturable = false`).

### Capa 4 - Limitar el envio de correo 🟠
- [ ] `enviar-correo` / `reenviar-factura`: no confiar solo en el correo digitado.
      Enviar UNICAMENTE al correo del titular cuando exista, o validar propiedad.
- [ ] Tope por ticket/factura (p. ej. max 3 envios/dia + cooldown) con persistencia en BD.
- [ ] Requerir `transaccion` (prueba de posesion) tambien en estos endpoints.

### Capa 5 - Anti-bot / anti-abuso 🟠
- [ ] Rate limiting por IP:
      - Caddy (`rate_limit`) en el edge (`Caddyfile`, se usa `caddy:2-alpine` en
        `docker-compose.prod.yml:24`).
      - Bucket4j en la app (`com.bucket4j:bucket4j-core`) para los paths publicos
        (defensa en profundidad, aplica aunque no haya Caddy).
- [ ] CAPTCHA invisible (reCAPTCHA v3 / hCaptcha) en `POST facturar`, `enviar-correo` y
      `reenviar-factura`.
      - Props: `app.recaptcha.site-key` / `app.recaptcha.secret` (estilo `JwtProperties` en `configuration/`).
      - Sin configurar -> fail-closed en prod, libre en dev.
- [ ] Limitar tamano de body en POST publicos (props de Tomcat) y timeouts.

### Capa 6 - Perimetral / hardening 🔵
- [ ] Headers de seguridad (HSTS, X-Content-Type-Options, CSP) en Caddy.
- [ ] Mensajes de error genericos (evitar oraculo de existencia).
- [ ] (Opcional, mayor friccion) Magic link / OTP por correo antes de timbrar/descargar una factura.

---

## 3. Archivos afectados (proyeccion)

| Archivo | Cambio |
|---|---|
| `pom.xml` | Agregar `bucket4j-core` (y `recaptcha` si se usa API propia) |
| `configuration/SecurityConfig.java` | Restringir paths publicos; headers de seguridad |
| `adapter/in/web/controller/FacturacionController.java` | `facturar` con nuevo DTO; validacion `@Valid` |
| `adapter/in/web/request/SolicitarFacturacionRequest.java` | Nuevo DTO con bean validation |
| `adapter/in/web/controller/EnvioFacturaController.java` | Requerir `transaccion`; limites de envio |
| `application/service/FacturacionService.java` | Prueba de posesion + recalculo de montos |
| `application/service/EnvioFacturaService.java` | Tope/cooldown de envios por ticket |
| `configuration/WebConfig.java` | Quitar handlers `file:` |
| `configuration/RecaptchaProperties.java` | Nuevas props (opcional CAPTCHA) |
| `adapter/in/security/../RateLimitCapFilter.java` | Filtro Bucket4j para paths publicos |
| `Caddyfile` | Directiva `rate_limit` y headers |
| `src/main/resources/application-*.yml` | Props de bucket/captcha/limites |

---

## 4. Pendientes de confirmacion

- [ ] Confirmar si el ticket impreso muestra `transaccion` (o un segundo dato verificable tipo RFC/CP).
- [ ] Decidir que datos fiscales debe capturar el portal para clientes nuevos
      (alta con lo digitado vs alta previa obligatoria).
- [ ] Definir topes: envios por ticket/dia y rango de rate limit por IP.

## 5. Checklist de despliegue

- [ ] Reavisar que ningun secreto quede en repos (ver `mejoras.md` 1.1 y 1.2).
- [ ] Verificar que los endpoints publicos restantes mantienen funcionamiento en portal.
- [ ] Pruebas: unit tests de posesion/rate limit/captcha + prueba manual en prod (preview).