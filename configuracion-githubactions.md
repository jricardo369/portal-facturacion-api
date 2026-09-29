# Configuración GitHub Actions + EC2

Guía para dejar funcionando `.github/workflows/deploy.yml`
(`push` a `master` o tag `v*.*.*` → tests → push a GHCR → deploy por SSH al EC2).

## 1. Conceptos

| Token | Dónde se crea | Dónde se usa | Duración |
|---|---|---|---|
| `GITHUB_TOKEN` | Automático, lo genera Actions en cada run. No lo creas. | `deploy.yml:67` (push a GHCR) y `:112` (fallback pull en EC2) | Expira al terminar el run |
| `GHCR_PAT` (PAT clásico) | Tú lo generas en tu perfil de GitHub | `deploy.yml:109-110` (`docker login` en el EC2) | Hasta que lo revoques |

Nunca escribas valores reales dentro del `.yml`, solo referencias `${{ secrets.NOMBRE }}`.

## 2. Generar el Personal Access Token (PAT)

1. GitHub → foto de perfil → `Settings` → `Developer settings` → `Personal access tokens` → `Tokens (classic)`.
2. `Generate new token (classic)` → `Generate new token (classic)`.
3. Nombre: `portal-facturacion-ec2-pull`, expiración: `90 days` (o `No expiration` si es tu servidor fijo).
4. Scopes:
   - `read:packages` ← obligatorio (para `docker pull` en el EC2).
   - `write:packages` ← opcional, solo si harás `docker push` manual desde tu PC.
   - `delete:packages` ← opcional, para borrar tags viejos.
5. `Generate token` → cópialo de inmediato (solo se muestra una vez).

Probarlo en local (token de dev):

```bash
echo "TU_PAT" | docker login ghcr.io -u TU_USUARIO_GITHUB --password-stdin
docker pull ghcr.io/tu-usuario/portal-facturacion-api:latest
docker logout ghcr.io
```

Si el `pull` funciona, el PAT es válido.

## 3. Agregar los Secrets del repo

Repo → `Settings` → `Secrets and variables` → `Actions` → `Secrets` → `New repository secret`:

| Secret | Valor | Ejemplo |
|---|---|---|
| `EC2_HOST` | IP elástica o DNS del EC2 | `3.85.12.34` |
| `EC2_USER` | Usuario SSH | `ubuntu` (Ubuntu) / `ec2-user` (Amazon Linux) |
| `EC2_SSH_KEY` | Contenido **completo** del `.pem` privado | Ver abajo |
| `EC2_PORT` | Puerto SSH | `22` |
| `GHCR_USER` | Tu usuario GitHub dueño del PAT (minúsculas) | `mi-usuario` |
| `GHCR_PAT` | El token del paso 2 | `ghp_xxxx...` |

### EC2_SSH_KEY (el que más falla)

```bash
cat tu-clave.pem
```

Copia **todo**, incluyendo primera y última línea:

```
-----BEGIN RSA PRIVATE KEY-----
MIIEowIBAAKCAQEA...
...
-----END RSA PRIVATE KEY-----
```

Reglas:
- Es la clave **privada**, no la `.pub`.
- Sin passphrase (si tiene, genera otra: `ssh-keygen -t rsa -b 4096 -f nueva -N ""`).
- Sin espacios extra ni saltos de línea Windows (`\r`).
- `EC2_USER` debe coincidir con la AMI.

## 4. Permisos del workflow

Repo → `Settings` → `Actions` → `General` → `Workflow permissions`:

- Activa `Read and write permissions` + `Allow GitHub Actions to create and approve pull requests` (opcional).
- Si no, el `docker push` a GHCR con `GITHUB_TOKEN` falla con `denied`.

La primera imagen `ghcr.io/<owner>/portal-facturacion-api` se crea sola en el primer run. Si el repo es privado, déjala privada; el EC2 entra con `GHCR_PAT`.

## 5. Verificar

1. `git add/commit/push` a `master`.
2. Repo → `Actions` → run `build-push-deploy` en verde.
3. En el EC2:
   ```bash
   cd /opt/portal-facturacion-api
   docker compose -f docker-compose.prod.yml ps
   curl -f http://localhost:8080/actuator/health
   ```

## 6. Errores típicos

| Error en Actions | Causa | Fix |
|---|---|---|
| `denied: installation not allowed to Create packages` | `Workflow permissions` en solo lectura | Paso 4 |
| `no such image: ...:sha-xxxx` | Deploy de tag viejo / TAG manual mal escrito | Usa `Actions` → `Run workflow` → input `tag` correcto |
| `ssh: handshake failed / Permission denied (publickey)` | `EC2_SSH_KEY` o `EC2_USER` mal | Paso 3, revisa `.pem` completo y usuario según AMI |
| `Host key verification failed` | Cambio de IP del EC2 | Borra el secret `SSH_KNOWN_HOSTS` si lo usas, o reconecta una vez por SSH |
| `unauthorized: authentication required` en el EC2 | `GHCR_PAT` expirado o sin `read:packages` | Regenera paso 2 y actualiza el secret + `docker login` en el EC2 |
