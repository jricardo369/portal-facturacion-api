# Plan final: local con Spring Boot, producción en EC2 con Docker + GitHub Actions

## 1. Estrategia

| Ambiente | Cómo corre | Perfil | Config |
|---|---|---|---|
| Local | `./mvnw spring-boot:run` directo, como cualquier app Spring Boot | `local` (por defecto en `application.yml`) | `application-local.yml` + variables de tu `.env` |
| Producción (EC2) | Imagen Docker desde GHCR, orquestada con `docker-compose.prod.yml` + Caddy (HTTPS automático) | `prod` (`application-prod.yml`, forzado por `ENV` en el `Dockerfile`) | `.env.prod` que vive **solo en el servidor** |

Regla de oro: la imagen siempre arranca con perfil `prod`; en local nunca se usa Docker para el API.

## 2. Archivos del plan

- `src/main/resources/application-prod.yml` — datasource/JPA/Flyway/CORS/JWT/Fudo solo por variables de entorno, sin secretos quemados.
- `Dockerfile` — se agregó `ENV SPRING_PROFILES_ACTIVE=prod`. Multi-stage, usuario no-root y healthcheck sin cambios.
- `docker-compose.prod.yml` — servicio `api` (imagen `ghcr.io/<owner>/portal-facturacion-api:${TAG}`, `env_file: .env.prod`, puerto interno 8080) + servicio `caddy` (80/443, certificado Let's Encrypt automático).
- `Caddyfile` — `{$APP_DOMAIN} { reverse_proxy api:8080 }`. `APP_DOMAIN` sale del `.env.prod`.
- `.env.prod.example` — plantilla de las 15 variables. Copiar a `.env.prod` en el servidor y rellenar.
- `.github/workflows/deploy.yml` — pipeline: `push` a `main` o tag `v*.*.*` → `./mvnw -B verify` → build/push a GHCR (`:sha-xxxx` y `:latest`; en tag `v1.2.3` además `:1.2.3`) → deploy por SSH al EC2 (`pull` + `up -d`).

## 3. Lo que se necesita en el EC2 (una sola vez)

1. Instancia con Docker Engine + plugin compose y git:
   - Ubuntu: `sudo apt-get update && sudo apt-get install -y docker.io docker-compose-plugin git && sudo usermod -aG docker $USER`
   - Amazon Linux 2023: `sudo dnf install -y docker git && sudo systemctl enable --now docker && sudo usermod -aG docker ec2-user` (+ plugin compose según AMI)
2. MySQL gestionado por ti en el mismo servidor, con base y usuario creados:
   ```sql
   CREATE DATABASE IF NOT EXISTS facturacion_db CHARACTER SET utf8mb4;
   CREATE USER 'facturacion'@'%' IDENTIFIED BY 'PASSWORD_SEGURA';
   GRANT ALL ON facturacion_db.* TO 'facturacion'@'%';
   ```
   El usuario `root`/`%` o el host `host.docker.internal` debe aceptar conexiones desde el contenedor (con `extra_hosts` ya incluido en el compose).
3. DNS: registro `A` de `api.tudominio.com` → IP elástica del EC2.
4. Security Group: abrir `80` y `443` al mundo, `22` solo a tu IP. **No abrir `8080`**.
5. Carpeta de despliegue:
   ```
   sudo mkdir -p /opt/portal-facturacion-api && sudo chown $USER:$USER /opt/portal-facturacion-api
   cd /opt/portal-facturacion-api
   git clone <tu-repo> .
   cp .env.prod.example .env.prod   # y rellena valores reales
   ```
6. Si el repo GitHub es privado: `echo <GHCR_PAT> | docker login ghcr.io -u <tu-usuario> --password-stdin` (el workflow lo repite en cada deploy).
7. Secrets en GitHub (repo → Settings → Secrets → Actions): `EC2_HOST`, `EC2_USER`, `EC2_SSH_KEY`, `EC2_PORT` (22), `GHCR_USER`, `GHCR_PAT` (PAT clásico con scope `read:packages`; si el repo es público puedes omitir login).
8. Generar `JWT_SECRET` de mínimo 32 caracteres aleatorios:
   ```
   openssl rand -base64 48
   ```

## 4. Cómo probar en local (Spring Boot, sin Docker)

```bash
# 1. MySQL local arriba y variables listas (copia .env.example a .env y ajústalo)
# 2. Correr
./mvnw spring-boot:run
# 3. Verificar
curl http://localhost:8080/actuator/health
# 4. Login y prueba con token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usuario":"admin","password":"Admin123!"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['token'])")
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/clientes?size=1"
```

Notas: el perfil activo por defecto es `local`; el seed crea `admin/Admin123!` (cámbialo tras el primer login). El `JWT_SECRET` local debe ser el mismo en cada arranque y de ≥32 caracteres, o los tokens dejarán de validar al reiniciar.

## 5. Cómo subir a producción

```bash
# Opción A (automática, recomendada): merge/push a main o tag de versión
git tag v1.0.0 && git push origin v1.0.0
# El workflow compila, testea, sube la imagen y despliega al EC2 solo.

# Opción B (manual, si Actions falla o quieres control total)
docker build -t ghcr.io/<owner>/portal-facturacion-api:1.0.0 .
docker push ghcr.io/<owner>/portal-facturacion-api:1.0.0
# En el EC2:
cd /opt/portal-facturacion-api
TAG=1.0.0 IMAGE_OWNER=<owner> docker compose -f docker-compose.prod.yml pull
TAG=1.0.0 IMAGE_OWNER=<owner> docker compose -f docker-compose.prod.yml up -d
```

Verificación en prod:
```bash
curl https://api.tudominio.com/actuator/health
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs --tail=50 api
```

Rollback: `TAG=<versión-anterior> ... up -d` con el tag previo (GHCR conserva los tags viejos).

## 6. Diagnóstico de los errores 401 vistos en este proyecto

- Causa 1 (encontrada en logs): `FacturacionService` atrapaba `ClienteNoEncontradoException` dentro de un método `@Transactional`, lo que marcaba la transacción como `rollback-only` y rompía la respuesta. **Corregido**: el método orquestador ya no abre transacción (cada consulta usa la suya).
- Causa 2: token expirado (dura 30 min, `JWT_EXPIRATION_MS`) o firmado con otro `JWT_SECRET` (login en docker vs API en IDE). Siempre genera el token con login **fresco** contra el **mismo** API al que llamas, y usa el mismo `JWT_SECRET` en todos lados.
