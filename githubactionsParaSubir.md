# Guía: subir API + Web a EC2 con GitHub Actions

Arquitectura actual: dos repos (`portal-facturacion-api`, `portal-facturacion-web`), cada uno publica su
imagen en GHCR y despliega en el **mismo EC2**. El repo del API es el **orquestador**: su
`docker-compose.prod.yml` levanta `api` + `web` + `caddy`, y Caddy reparte por path en un solo dominio.

```
push a master ─▶ Actions (build/test/push GHCR) ─▶ SSH al EC2 ─▶ pull + up -d ─▶ Caddy :80/:443
   /api/*, /actuator/*, /v3/*, /swagger-ui* ─▶ api:8080 (Spring Boot)
   / (resto)                                 ─▶ web:80   (Angular/nginx)
```

Puertos internos: `api` expone `8080`, `web` escucha `80` (cada contenedor tiene su propia red, no chocan).
Solo Caddy publica puertos al host (`80`/`443`). Nada de `:8080` público por diseño.

---

## 1. Configuración en GitHub (por repo, los secrets NO se comparten)

En **cada** repo (`...-api` y `...-web`): `Settings → Secrets and variables → Actions → Secrets`:

| Secret        | Valor                                                        | Ejemplo                                     |
|---------------|--------------------------------------------------------------|---------------------------------------------|
| `EC2_HOST`    | IP elástica o DNS del EC2                                    | `54.215.12.132`                             |
| `EC2_USER`    | Usuario SSH (`ubuntu` en Ubuntu, `ec2-user` en Amazon Linux) | `ubuntu`                                    |
| `EC2_SSH_KEY` | Clave privada `.pem` **completa** (con primera y última línea, sin passphrase) | `-----BEGIN RSA PRIVATE KEY----- ...` |
| `EC2_PORT`    | Puerto SSH                                                   | `22`                                        |
| `GHCR_USER`   | Tu usuario GitHub en minúsculas (dueño del PAT)              | `jricardo369`                               |
| `GHCR_PAT`    | PAT clásico con scope `read:packages`                        | `ghp_xxxx...`                               |

Crear el PAT: GitHub → foto → `Settings → Developer settings → Personal access tokens → Tokens (classic) →
Generate new token (classic)` → scope `read:packages` (agrega `write:packages` solo si harás `docker push`
manual desde tu PC). Probarlo en local:

```bash
echo "TU_PAT" | docker login ghcr.io -u TU_USUARIO --password-stdin
docker pull ghcr.io/tu-usuario/portal-facturacion-api:latest
```

Además, en cada repo: `Settings → Actions → General → Workflow permissions` → `Read and write permissions`
(si no, el `docker push` a GHCR con `GITHUB_TOKEN` falla con `denied`). Si la imagen GHCR es pública,
`GHCR_USER/GHCR_PAT` pueden omitirse (el workflow usa `GITHUB_TOKEN` como respaldo, pero ese token expira
al terminar el run y no sirve para pulls posteriores en el EC2).

---

## 2. Configuración en el EC2 (una sola vez)

```bash
# 1. Docker + compose + git (Ubuntu)
sudo apt-get update && sudo apt-get install -y docker.io docker-compose-plugin git
sudo usermod -aG docker $USER   # re-login después

# 2. MySQL en el mismo servidor + usuario para los contenedores.
#    OJO: el contenedor llega con IP 172.18.0.x vía host.docker.internal,
#    el usuario debe ser 'facturacion'@'%' (sin acento), no solo localhost:
sudo mysql -e "CREATE DATABASE IF NOT EXISTS facturacion_db CHARACTER SET utf8mb4;"
sudo mysql -e "CREATE USER IF NOT EXISTS 'facturacion'@'%' IDENTIFIED BY 'PASSWORD_SEGURA';"
sudo mysql -e "GRANT ALL ON facturacion_db.* TO 'facturacion'@'%'; FLUSH PRIVILEGES;"

# 3. Security Group: 80 y 443 al mundo, 22 solo a tu IP. NO abrir 8080.

# 4. Carpeta orquestadora (repo del API) + carpeta de configs/ffuera del repo)
sudo mkdir -p /opt/portal-facturacion-api /opt/portal-facturacion-configs/html-mails
sudo chown $USER:$USER /opt/portal-facturacion-api /opt/portal-facturacion-configs /opt/portal-facturacion-configs/html-mails
cd /opt/portal-facturacion-api && git clone <url-del-repo-api> .

# 5. Config externa (NO versionada). El yml: copia de configuracion-general.example.yml
cp configuracion-general.example.yml /opt/portal-facturacion-configs/configuracion-general.yml
chmod 600 /opt/portal-facturacion-configs/configuracion-general.yml
nano /opt/portal-facturacion-configs/configuracion-general.yml
# revisa: spring.datasource.url (host.docker.internal:3306), username SIN acento,
# password, app.jwt.secret (≥32 caracteres aleatorios), cors, fudo, correo.

# 6. Infra (copia de .env.prod.example). TAG/WEB_TAG los pone cada workflow;
#    aquí mandan IMAGE_OWNER/WEB_IMAGE_OWNER (minúsculas) y APP_DOMAIN (SIN http://).
cp .env.prod.example /opt/portal-facturacion-configs/.env.prod
chmod 600 /opt/portal-facturacion-configs/.env.prod
nano /opt/portal-facturacion-configs/.env.prod
# IMAGE_OWNER=jricardo369
# TAG=latest
# WEB_IMAGE_OWNER=jricardo369
# WEB_TAG=latest
# APP_DOMAIN=ec2-54-215-12-132.us-west-1.compute.amazonaws.com  (temporal; ver §6 HTTPS)

# 7. Layouts de correo (viven SOLO en el servidor, editables sin rebuild):
#    subir correo_factura.html a /opt/portal-facturacion-configs/html-mails/
chmod 755 /opt/portal-facturacion-configs /opt/portal-facturacion-configs/html-mails
chmod 644 /opt/portal-facturacion-configs/html-mails/*.html
# (el contenedor corre como usuario 'app' no-root: necesita o+rx en dirs y o+r en archivos)

# 8. Login GHCR en el servidor (para pulls manuales; el workflow lo repite en cada deploy)
echo "TU_PAT" | docker login ghcr.io -u TU_USUARIO --password-stdin
```

La `.pem` vive en tu Mac (`~/.ssh/*.pem`, `chmod 400`), nunca en el repo ni en el EC2. En Actions va
pegada completa en `EC2_SSH_KEY`.

---

## 3. Qué hace el workflow del API (`.github/workflows/deploy.yml`)

Disparadores: `push` a `master`, tags `v*.*.*`, o manual (`workflow_dispatch` con input `tag` para rollback).

**Job `build-test-push`** (runner `ubuntu-latest`, `packages: write`):
1. `checkout` + `setup-java` (Temurin 21, caché maven).
2. `./mvnw -B verify` — compila y corre tests; si falla, no hay imagen ni deploy.
3. `docker/setup-buildx-action` + login a GHCR con `GITHUB_TOKEN`.
4. Paso `meta`: nombre de imagen en minúsculas (`tr` porque GHCR lo exige); tag = versión del tag `v*`,
   o input manual, o `sha-<7>` del commit. Emite `outputs.tag/image`.
5. `docker/build-push-action`: construye el `Dockerfile` multi-stage y publica
   `ghcr.io/<owner>/portal-facturacion-api:<tag>` y `:latest`.

**Job `deploy`** (vía `appleboy/ssh-action`):
1. `cd /opt/portal-facturacion-api`, `git fetch/checkout` del SHA exacto que disparó el run.
2. `docker login ghcr.io` con `GHCR_PAT` (o `GITHUB_TOKEN` de respaldo).
3. Exporta `TAG` (del build) y `IMAGE_OWNER` (minúsculas) — **tienen prioridad** sobre `.env.prod`;
   `APP_DOMAIN` sale del `.env.prod` del servidor. Falla rápido si falta ese archivo.
4. `docker compose --env-file /opt/portal-facturacion-configs/.env.prod -f docker-compose.prod.yml`
   `pull` + `up -d --force-recreate` (todo: api, web, caddy) + `ps`.
5. Loop 12×10s: `docker exec portal-facturacion-api wget ... /actuator/health | grep '"status":"UP"'`.
   Si a los 12 intentos no hay `UP`, vuelca `logs api` y falla el workflow (`exit 1`).
6. `docker image prune -af` (limpieza).

**Si hay que mover algo del API:** cambiar rama/tags en `on:`; versión Java en `setup-java`;
`IMAGE_NAME`/registry en `env:`; comandos del deploy en `script:` (p. ej. `pull api caddy` para no tocar la web);
ruta del compose o del `.env.prod` en `ENV_FILE`/`COMPOSE`; umbrales del loop de salud.

---

## 4. Qué hace el workflow de la Web (`.github/workflows/deploy.yml`)

Igual estructura, diferencias:
1. Usa `setup-node` con `.nvmrc` (Node 22) y verifica `npm ci + npm run build -- --configuration production`
   antes de construir la imagen (si Angular no compila, no hay push).
2. `Dockerfile` multi-stage: `node:22-alpine` compila → `nginx:alpine` sirve
   `dist/portal-facturacion-web/browser` en el `:80` (con `nginx.conf`: SPA fallback, no-cache a
   `index.html/ngsw*`, cache 1 año a assets con hash, `listen [::]:80` para IPv6).
3. Publica `ghcr.io/<owner>/portal-facturacion-web:<tag>` y `:latest`.
4. En el deploy **no** clona el repo web en el EC2: entra a `/opt/portal-facturacion-api`
   (orquestador), actualiza a `origin/master`, exporta `WEB_TAG`/`WEB_IMAGE_OWNER` y corre
   `pull web` + `up -d --force-recreate web caddy`.
5. Verifica con `docker exec portal-facturacion-web wget http://127.0.0.1:80/ | grep "<html"`
   (se usa `127.0.0.1` porque `localhost` resuelve a IPv6 `::1` y daba `Connection refused`).

**Si hay que mover algo de la web:** versión Node (`.nvmrc` + `node-version-file`); nombre de imagen
(sale de `github.repository`); a qué servicios hacer `pull/up` en el `script:`; chequeo de salud.

**Orden de primer deploy:** web primero (crea la imagen en GHCR; su deploy fallará hasta que el compose
del API tenga el servicio `web`), luego API (levanta todo junto). Después el orden ya no importa.

**Frontend y API base:** la web usa ruta relativa `/api/v1` (`src/app/shared/api-base.ts`): en dev la
resuelve `proxy.conf.json` (`/api → localhost:18080/8080`), en prod Caddy (`/api/* → api:8080`). Sin CORS.

---

## 5. Operación diaria

```bash
# Desplegar: solo push a master del repo que cambió (web primero si es la primera vez)
git push origin master
# Actions del repo → run verde → "API healthy" / "WEB healthy"

# Rollback a tag manual: Actions → build-push-deploy → Run workflow → tag = sha-xxxx o versión

# Estado y logs en el EC2:
cd /opt/portal-facturacion-api
docker compose --env-file /opt/portal-facturacion-configs/.env.prod -f docker-compose.prod.yml ps
docker compose --env-file /opt/portal-facturacion-configs/.env.prod -f docker-compose.prod.yml logs --tail=50 api
docker exec portal-facturacion-api wget -qO- http://localhost:8080/actuator/health; echo   # {"status":"UP"}
docker exec portal-facturacion-web wget -qO- http://127.0.0.1:80/ | head -c 60             # <html...

# Verificación pública (http temporal, ver §6):
curl -f http://<APP_DOMAIN>/actuator/health
curl -f http://<APP_DOMAIN>/
```

---

## 6. Errores típicos ya vistos (y su fix)

| Síntoma | Causa | Fix |
|---|---|---|
| `Access denied for user 'facturación'@'172...'` (MySQL 1045), health `DOWN`, loop 12/12 | Usuario con acento o password distinta entre MySQL y `configuracion-general.yml`; usuario sin `@'%'` | `username: facturacion` (sin `ó`), igualar password, `CREATE/ALTER USER 'facturacion'@'%'` + `GRANT` |
| `missing server host` en `ssh-action` | Falta `EC2_HOST` en **ese** repo (los secrets no se comparten) | Agregar los 6 secrets en cada repo |
| `...-web:latest: not found` / `...-api:latest: not found` en `pull` | Primera vez (imagen aún no publicada) o `*_IMAGE_OWNER` con placeholder `tu-organizacion*` | Push del repo dueño primero; poner dueños reales en `.env.prod` |
| `dependency web failed to start / unhealthy`, `wget: can't connect ... ([::1]:80)` | `localhost` resolvió a IPv6 y nginx solo escuchaba IPv4 | `listen [::]:80` en `nginx.conf` + healthchecks por `127.0.0.1` (ya aplicado) |
| Caddy `Restarting`: `Unexpected next token after '{'` | Bloques `handle X { ... }` en una línea (Caddyfile no los admite) | Bloques multilínea (ya aplicado) |
| Caddy `NXDOMAIN looking up A` | `APP_DOMAIN` sin registro DNS | Crear registro `A → IP elástica` y esperar propagación (Caddy reintenta solo) |
| `ps` muestra contenedores de hace horas tras el deploy | `up -d` sin recrear | Se agregó `--force-recreate` a ambos workflows |
| `https://<ec2-host>:8080/...` no abre | Por diseño: `8080` solo `expose` interno, y el API sirve `http` (TLS lo termina Caddy) | Usar `http(s)://<APP_DOMAIN>/...` sin `:8080`; SG sin `8080` |

Para distinguir un 404: el de negocio (ej. ticket sin factura → `FacturaNoEncontradaException` →
`GlobalExceptionHandler` → ProblemDetail `Resource not found`) trae JSON con `detail`; el de Caddy es texto
plano; el de nginx es HTML. Mirar el cuerpo (`F12 → Network → Response`) antes de asumir ruteo.

---

## 7. Si se requiere HTTPS (estado actual: HTTP temporal)

**Por qué HTTP hoy:** el `Caddyfile` empieza con `http://{$APP_DOMAIN}` a propósito. Let's Encrypt
**prohíbe** emitir certificados para `*.compute.amazonaws.com` (`rejectedIdentifier`), así que con el
hostname del EC2 el HTTPS es imposible. Todo funciona en `http` plano (tokens legibles en red: solo
aceptable en pruebas).

**Pasos para HTTPS con dominio propio:**
1. Registrar/dominar un dominio (ej. `tudominio.com`) y crear el registro DNS:
   `Tipo A | tudominio.com → <IP elástica del EC2>` (y `www` si aplica). Verificar:
   `dig +short tudominio.com` debe dar la IP.
2. En el EC2: `APP_DOMAIN=tudominio.com` (apex, **sin** `http://`) en
   `/opt/portal-facturacion-configs/.env.prod`. SG con `80`+`443` abiertos al mundo (el challenge
   http-01 de Let's Encrypt entra por el `80`).
3. En el repo API, `Caddyfile`: quitar el prefijo `http://` (dejar `{$APP_DOMAIN} {`, borrando el
   comentario TEMPORAL), commit + push. El deploy recrea Caddy y este obtiene el certificado solo
   (reintenta cada 60/120s; ver con `logs caddy`: `certificate obtained successfully`).
4. Validar: `curl -f https://tudominio.com/actuator/health` y `https://tudominio.com/`. Caddy redirige
   solo el `http→https`.
5. Opcional después: endurecer headers TLS en Caddy, renovar es automático; si se cambia de IP,
   actualizar el `A` y nada más.
