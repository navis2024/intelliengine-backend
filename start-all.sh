#!/bin/bash
# ============================================================
# 智擎 IntelliEngine — 一键启动脚本
# 用法: bash start-all.sh
# ============================================================
set -e

echo "=== 智擎 IntelliEngine 启动 ==="

# 1. Docker services
echo "[1/4] Starting Docker services (MySQL, Redis, RabbitMQ, MinIO)..."
docker-compose up -d mysql redis rabbitmq minio
echo "  Waiting for services to be healthy..."
sleep 10

# 2. Backend
echo "[2/4] Starting backend (Spring Boot)..."
mvn spring-boot:run -q &
BACKEND_PID=$!
echo "  Backend PID: $BACKEND_PID"
sleep 15

# 3. Frontend
echo "[3/4] Starting frontend (Vite dev server)..."
cd /e/front_ZQ/intelliengine-frontend
npm run dev &
FRONTEND_PID=$!
echo "  Frontend PID: $FRONTEND_PID"
sleep 5

# 4. Seed test data
echo "[4/4] Seeding test data..."
docker exec -i intelliengine-mysql mysql -uroot -pIntelliEngine@2025 intelliengine < sql/init.sql 2>/dev/null
docker exec -i intelliengine-mysql mysql -uroot -pIntelliEngine@2025 intelliengine < sql/test_data.sql 2>/dev/null
echo "  Test data seeded"

echo ""
echo "=== 启动完成 ==="
echo "  前端:    http://localhost:3000"
echo "  后端:    http://localhost:8081/api"
echo "  Swagger: http://localhost:8081/api/swagger-ui.html"
echo "  MinIO:   http://localhost:9001 (minioadmin/IntelliEngine@2025)"
echo ""
echo "  测试用户: dev_zhang / test123456"
echo ""
echo "  停止: kill $BACKEND_PID $FRONTEND_PID && docker-compose down"
echo ""
wait
