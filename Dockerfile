# 智擎后端服务 Dockerfile
# 基于 Spring Boot 3.2 + Java 17

# ==================== 构建阶段 ====================
FROM eclipse-temurin:17-jdk-alpine AS builder

# 工作目录
WORKDIR /app

# 先复制 Maven 配置文件（利用缓存）
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

# 下载依赖（如果 pom.xml 未改变，这一层会被缓存）
RUN ./mvnw dependency:go-offline -B 2>/dev/null || true

# 复制源代码
COPY src ./src

# 构建应用（跳过测试以加速构建）
RUN ./mvnw clean package -DskipTests -B

# ==================== 运行阶段 ====================
FROM eclipse-temurin:17-jre-alpine

# 安装必要的运行时依赖
RUN apk add --no-cache curl tzdata

# 设置时区
ENV TZ=Asia/Shanghai

# 创建非 root 用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 工作目录
WORKDIR /app

# 从构建阶段复制 jar 文件
COPY --from=builder /app/target/*.jar app.jar

# 设置文件权限
RUN chown appuser:appgroup app.jar

# 切换到非 root 用户
USER appuser

# 暴露端口
EXPOSE 8081

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/api/health || exit 1

# 启动命令
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=prod", \
    "-jar", \
    "app.jar"]
