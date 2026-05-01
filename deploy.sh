#!/bin/bash
# 智擎(IntelliEngine) 快速部署脚本
# 使用方式: ./deploy.sh [start|stop|restart|logs|status]

set -e

# 配置
COMPOSE_FILE="docker-compose.yml"
PROJECT_NAME="intelliengine"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印帮助信息
print_help() {
    echo -e "${GREEN}智擎(IntelliEngine) 快速部署脚本${NC}"
    echo ""
    echo "用法: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  start    - 启动所有服务"
    echo "  stop     - 停止所有服务"
    echo "  restart  - 重启所有服务"
    echo "  logs     - 查看后端日志"
    echo "  status   - 查看服务状态"
    echo "  build    - 重新构建并启动"
    echo "  clean    - 清理所有数据和容器"
}

# 启动服务
start_services() {
    echo -e "${GREEN}正在启动智擎服务...${NC}"
    docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME up -d
    echo -e "${GREEN}服务启动成功！${NC}"
    echo ""
    echo "API 文档: http://localhost:8081/api/swagger-ui.html"
    echo "MySQL: localhost:3307"
    echo "Redis: localhost:6379"
}

# 停止服务
stop_services() {
    echo -e "${YELLOW}正在停止智擎服务...${NC}"
    docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME down
    echo -e "${GREEN}服务已停止${NC}"
}

# 查看日志
view_logs() {
    docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME logs -f backend
}

# 查看状态
view_status() {
    echo -e "${GREEN}服务状态:${NC}"
    docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME ps
}

# 重新构建
build_services() {
    echo -e "${YELLOW}正在重新构建...${NC}"
    docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME down
    docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME build --no-cache
    docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME up -d
    echo -e "${GREEN}构建并启动成功！${NC}"
}

# 清理数据
clean_all() {
    echo -e "${RED}警告: 此操作将删除所有数据和容器！${NC}"
    read -p "确认继续? (yes/no): " confirm
    if [ "$confirm" = "yes" ]; then
        docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME down -v
        docker system prune -f
        echo -e "${GREEN}清理完成${NC}"
    else
        echo "已取消"
    fi
}

# 主逻辑
case "${1:-start}" in
    start)
        start_services
        ;;
    stop)
        stop_services
        ;;
    restart)
        stop_services
        sleep 2
        start_services
        ;;
    logs)
        view_logs
        ;;
    status)
        view_status
        ;;
    build)
        build_services
        ;;
    clean)
        clean_all
        ;;
    *)
        print_help
        exit 1
        ;;
esac
