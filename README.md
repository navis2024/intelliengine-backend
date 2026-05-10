# 智擎 (IntelliEngine) — AIGC 资产管理与协作平台

[![Java](https://img.shields.io/badge/Java-17-blue)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3-green)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-orange)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-Caching%20%7C%20Lock%20%7C%20Session-red)](https://redis.io/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Async%20Queue-orange)](https://www.rabbitmq.com/)
[![MinIO](https://img.shields.io/badge/MinIO-Object%20Storage-blue)](https://min.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

面向 AIGC 创作者的资产管理平台，支持视频/图片等资产的版本管理、AI 元数据标注、智能分析报告、模板市场交易。

## 架构总览

```
┌──────────────────────────────────────────────────────────────┐
│                       Nginx :80                              │
│                  /api → backend :8081                        │
│                  /    → frontend  (static)                    │
└──────────────────────────────────────────────────────────────┘
         │                    │              │
    ┌────▼────┐          ┌────▼────┐   ┌─────▼──────┐
    │ Vue 3   │          │ Spring  │   │  Docker    │
    │ Tailwind│          │ Boot 3.2│   │  Compose   │
    │ Pinia   │          │ COLA    │   │  Stack     │
    └─────────┘          └───┬─────┘   └────────────┘
                             │
    ┌────────────────────────┼──────────────────────────┐
    │                        │                          │
┌───▼────┐  ┌────────┐  ┌───▼────┐  ┌──────────┐  ┌───▼────┐
│ MySQL  │  │ Redis  │  │ MinIO  │  │ RabbitMQ │  │FFmpeg  │
│  8.0   │  │Cache/  │  │Object  │  │  Async   │  │Frame   │
│ :3307  │  │Lock/   │  │Storage │  │ Extract  │  │Extract │
│        │  │Session │  │ :9000  │  │  Queue   │  │        │
└────────┘  └────────┘  └────────┘  └──────────┘  └────────┘
```

## 技术栈

| 层次 | 技术 | 说明 |
|------|------|------|
| 后端框架 | Spring Boot 3.2 + Java 17 | COLA 分层架构 |
| ORM | MyBatis-Plus 3.5.5 | Lambda QueryWrapper + 分页 |
| 认证 | Spring Security + JWT (jjwt 0.12) | Token 黑名单 + ThreadLocal 会话 |
| 数据库 | MySQL 8.0 | 15 张表，Docker 部署 |
| 缓存 | Redis + Caffeine L1/L2 | 多级缓存 + 热点 Key 检测 |
| 分布式锁 | Redis + Lua 脚本 | UUID 持有者校验，原子释放 |
| 消息队列 | RabbitMQ (Spring AMQP) | 异步视频抽帧 + 死信队列 |
| 对象存储 | MinIO SDK 8.5 | 预签名 URL 上传 |
| 视频处理 | FFmpeg | I-frame 提取，自动检测路径 |
| LLM | OpenAI 兼容接口 | 策略模式，可替换 |
| 前端 | Vue 3 + TypeScript + Tailwind | Pinia 状态管理 |
| 文档 | SpringDoc OpenAPI 2.3 | Swagger UI |
| 部署 | Docker Compose | MySQL + Redis + RabbitMQ + MinIO + Nginx |

## 项目结构

```
ZQ_plat/src/main/java/com/aigc/intelliengine/
├── user/           # 用户模块 — 注册/登录/信息管理
├── project/        # 项目管理 — CRUD/成员管理/权限校验
├── asset/          # 资产管理 — 上传/版本控制/MinIO 存储
├── review/         # 审批批注 — 按帧批注/评论/回复
├── market/         # 模板市场 — 发布/购买/收藏
├── agent/          # AI Agent — AI视频元数据/抽帧/Prompt库/报告/数据采集
├── notification/   # 通知模块 — 站内消息
└── common/
    ├── config/     # RabbitMQ / Cache 配置
    ├── redis/      # MultiLevelCache / DistributedLock / HotKeyDetector / RateLimiter
    ├── security/   # JwtUtil / JwtAuthFilter / UserContextHolder / MembershipValidator
    ├── controller/ # HealthController
    ├── model/      # PageResult / ApiResponse
    └── exception/  # BusinessException / GlobalExceptionHandler

front_ZQ/intelliengine-frontend/src/
├── views/          # Dashboard / Projects / Assets / Market / Agent / DraftBoard
├── api/            # 后端 API 封装 (axios)
├── types/          # TypeScript 类型定义
└── stores/         # Pinia 状态管理
```

## 核心亮点

### 1. Redis 五种场景深度应用
- **分布式锁**：Lua 脚本原子释放 + UUID 持有者校验，防止误删他人锁
- **多级缓存**：Caffeine L1 + Redis L2，穿透/击穿/雪崩三重防护，热点 Key 滑动窗口自动发现
- **Session 共享**：Redis 存储用户会话（7天 TTL），ThreadLocal 上下文传递，多实例无状态
- **限流器**：基于 Redis 的 API 限流
- **Token 黑名单**：JWT 登出后加入 Redis 黑名单

### 2. 数据可见性集中鉴权
- `MembershipValidator` 统一入口：`requireMembership` / `requireProjectOwner` / `requireAssetAccess`
- 资产所有权多态：USER 类型仅创建者可访问，PROJECT 类型组成员可访问
- 10 个安全漏洞一次性封堵

### 3. LLM 策略模式
- `PromptAnalysisService` 自动选择 OpenAI / Mock 策略
- `llm.enabled=true` 时激活真实 LLM 分析（OpenAI 兼容接口）
- LLM 不可用时自动回退基础增强

### 4. RabbitMQ 异步抽帧
- `POST /v1/agent/videos` → 入库 → 发 MQ 消息 → 立即返回 200
- `VideoFrameWorker` 异步消费，提取 I-frame，失败 3 次进死信队列
- HTTP 线程不再阻塞在 FFmpeg 进程上

## 快速开始

### Docker 一键部署

```bash
cd ZQ_plat
docker compose up -d
# 服务将在 localhost:8081 启动
```

Docker Compose 包含：MySQL 8.0、Redis 7、RabbitMQ 3.12、MinIO、Nginx、Backend

### 本地开发

**环境要求**：JDK 17+ / Maven 3.8+ / Node 18+

```bash
# 1. 启动中间件
docker compose up -d mysql redis rabbitmq minio

# 2. 初始化数据库
docker exec -i intelliengine-mysql mysql -uroot -pIntelliEngine@2025 intelliengine < sql/init.sql

# 3. 启动后端
cd ZQ_plat
mvn spring-boot:run

# 4. 启动前端
cd front_ZQ/intelliengine-frontend
npm install && npm run dev
```

访问：
- 前端：http://localhost:5173
- Swagger：http://localhost:8081/api/swagger-ui.html
- MinIO 控制台：http://localhost:9001

## API 概览

| 模块 | 前缀 | 主要端点 |
|------|------|---------|
| 用户 | `/api/v1/users` | register, login, profile, update |
| 项目 | `/api/v1/projects` | CRUD, join/{groupId}, members |
| 资产 | `/api/v1/assets` | upload, versions, linkToProject, diff |
| 评审 | `/api/v1/reviews` | comments, replies, status |
| 市场 | `/api/v1/market` | templates, orders, favorites |
| Agent | `/api/v1/agent` | ai-videos, frames, tasks, reports, prompts |
| 健康 | `/api/health` | status, cache/stats, cache/hot-keys |

## 数据库表（15张）

| 模块 | 表名 |
|------|------|
| 用户 | `user_account` |
| 项目 | `project_info`, `project_member` |
| 资产 | `asset_info`, `asset_version` |
| 评审 | `review_comment`, `review_reply` |
| 市场 | `market_template`, `market_order`, `market_order_item`, `market_favorite` |
| Agent | `prompt_library`, `asset_ai_video`, `video_frame`, `agent_report`, `agent_report_template`, `agent_data_task`, `agent_data_record` |
| 通知 | `notification` |

## 测试

```bash
mvn test
# 73 个单元测试，覆盖 user/asset/market/agent/common 模块
```

## 版本

- `v1.2.0` — Agent 模块、Redis 深度集成、LLM 接入、RabbitMQ 异步、数据可见性
- `v1.1.0` — JWT 认证、Docker 部署
- `v1.0.0` — 初始版本，5 个核心模块
