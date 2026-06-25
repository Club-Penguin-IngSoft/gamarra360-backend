#!/usr/bin/env bash
# deploy.sh — Despliegue del backend Gamarra 360° en AWS EC2
#
# Uso:
#   chmod +x deploy.sh
#   ./deploy.sh
#
# Variables de entorno requeridas (o edita la sección de configuración):
#   EC2_HOST   — IP pública o DNS de la instancia EC2
#   EC2_USER   — Usuario SSH (ej. ubuntu, ec2-user)
#   EC2_KEY    — Ruta al archivo .pem con la clave privada
#   APP_PORT   — Puerto donde corre el backend (default: 8080)
#   DEPLOY_DIR — Directorio en EC2 donde se despliega (default: /opt/gamarra360)

set -euo pipefail

# ── Configuración ──────────────────────────────────────────────────────────────
EC2_HOST="${EC2_HOST:-ingechow.clsfbay9pwbr.us-east-1.rds.amazonaws.com}"   # Reemplazar con IP de EC2
EC2_USER="${EC2_USER:-ubuntu}"
EC2_KEY="${EC2_KEY:-~/.ssh/gamarra360.pem}"
APP_PORT="${APP_PORT:-8080}"
DEPLOY_DIR="${DEPLOY_DIR:-/opt/gamarra360}"
JAR_NAME="gamarra360-backend.jar"
SERVICE_NAME="gamarra360"

# ── Colores ────────────────────────────────────────────────────────────────────
GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
info()    { echo -e "${GREEN}[INFO]${NC} $1"; }
warning() { echo -e "${YELLOW}[WARN]${NC} $1"; }
error()   { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }

# ── Validaciones previas ───────────────────────────────────────────────────────
[ -f "$EC2_KEY" ] || error "Clave SSH no encontrada: $EC2_KEY"
command -v mvn &>/dev/null || error "Maven no está instalado"
command -v ssh &>/dev/null || error "SSH no está disponible"

# ── 1. Build del JAR ──────────────────────────────────────────────────────────
info "Compilando proyecto Spring Boot..."
mvn clean package -DskipTests -q
JAR_PATH=$(find target -name "*.jar" ! -name "*sources*" | head -1)
[ -z "$JAR_PATH" ] && error "No se encontró el JAR en target/"
info "JAR generado: $JAR_PATH"

# ── 2. Preparar directorio en EC2 ─────────────────────────────────────────────
info "Preparando directorio en EC2 ($EC2_HOST)..."
ssh -i "$EC2_KEY" -o StrictHostKeyChecking=no "$EC2_USER@$EC2_HOST" \
  "sudo mkdir -p $DEPLOY_DIR && sudo chown $EC2_USER:$EC2_USER $DEPLOY_DIR"

# ── 3. Subir JAR ──────────────────────────────────────────────────────────────
info "Subiendo JAR al servidor..."
scp -i "$EC2_KEY" -o StrictHostKeyChecking=no \
  "$JAR_PATH" "$EC2_USER@$EC2_HOST:$DEPLOY_DIR/$JAR_NAME"

# ── 4. Crear/actualizar servicio systemd ──────────────────────────────────────
info "Configurando servicio systemd..."
ssh -i "$EC2_KEY" -o StrictHostKeyChecking=no "$EC2_USER@$EC2_HOST" "
sudo tee /etc/systemd/system/${SERVICE_NAME}.service > /dev/null <<'SERVICE'
[Unit]
Description=Gamarra 360 Backend
After=network.target

[Service]
User=${EC2_USER}
WorkingDirectory=${DEPLOY_DIR}
ExecStart=/usr/bin/java -jar ${DEPLOY_DIR}/${JAR_NAME} --server.port=${APP_PORT}
SuccessExitStatus=143
Restart=always
RestartSec=10
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=${SERVICE_NAME}
EnvironmentFile=-${DEPLOY_DIR}/.env

[Install]
WantedBy=multi-user.target
SERVICE
sudo systemctl daemon-reload
sudo systemctl enable ${SERVICE_NAME}
"

# ── 5. Reiniciar servicio ──────────────────────────────────────────────────────
info "Reiniciando servicio..."
ssh -i "$EC2_KEY" -o StrictHostKeyChecking=no "$EC2_USER@$EC2_HOST" \
  "sudo systemctl restart $SERVICE_NAME"

# ── 6. Verificar salud ────────────────────────────────────────────────────────
info "Esperando que el servicio arranque (30s)..."
sleep 30
HEALTH=$(ssh -i "$EC2_KEY" -o StrictHostKeyChecking=no "$EC2_USER@$EC2_HOST" \
  "curl -sf http://localhost:${APP_PORT}/actuator/health || echo 'DOWN'")

if echo "$HEALTH" | grep -q '"status":"UP"'; then
  info "Backend desplegado correctamente en http://$EC2_HOST:$APP_PORT"
else
  warning "El servicio puede no haber arrancado aún. Revisa: sudo journalctl -u $SERVICE_NAME -n 50"
fi
