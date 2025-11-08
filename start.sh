#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "[repast] 构建镜像..."
docker compose build

echo "[repast] 启动服务..."
docker compose up -d

echo "[repast] 查看服务状态..."
docker compose ps

echo "[repast] 前端: http://localhost:8081"
echo "[repast] 后端: http://localhost:8080"
echo "[repast] MySQL: localhost:3306 (root/123456, DB=member_system)"