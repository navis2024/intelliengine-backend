# 智擎 (IntelliEngine) 后端服务

[![Java](https://img.shields.io/badge/Java-17-blue)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-orange)](https://www.mysql.com/)

智擎是一个AIGC资产管理与协作平台，提供项目管理、资产版本控制、视频审批、模板市场等核心功能。

## 技术栈

- **Java 17** - 主要编程语言
- **Spring Boot 3.2** - 应用框架
- **Spring Security + JWT** - 认证授权
- **MyBatis Plus 3.5.5** - ORM框架
- **MySQL 8.0** - 数据库
- **Redis** - 缓存与会话
- **COLA 4.0** - 分层架构
- **Swagger/OpenAPI** - API文档
- **Docker** - 容器化部署

## 项目结构

采用COLA分层架构：

```
com.aigc.intelliengine
├── user              # 用户模块
├── project           # 项目管理模块
├── asset             # 资产管理模块
├── review            # 审批批注模块
├── market            # 市场交易模块
└── common            # 公共组件
```

## 模块统计

| 模块 | 文件数 | 功能概述 |
|------|--------|---------|
| user | 13 | 用户注册、登录、信息管理 |
| project | 15 | 项目创建、成员管理 |
| asset | 13 | 资产上传、版本控制 |
| review | 14 | 视频批注、评论回复 |
| market | 14 | 模板市场、订单管理 |
| common | 7 | 全局异常、统一响应 |

**总计: 82个Java源文件 + 3个单元测试类**

## 快速开始

### 方式一：Docker部署（推荐）

```bash
# 1. 克隆项目
git clone https://github.com/navis2024/intelliengine-backend.git
cd intelliengine-backend

# 2. 一键部署
./deploy.sh start

# 3. 访问服务
# API文档: http://localhost:8081/api/swagger-ui.html
# MySQL: localhost:3307
# Redis: localhost:6379
```

部署脚本命令：
```bash
./deploy.sh start    # 启动服务
./deploy.sh stop     # 停止服务
./deploy.sh restart  # 重启服务
./deploy.sh logs     # 查看日志
./deploy.sh status   # 查看状态
```

### 方式二：本地运行

#### 1. 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0
- Docker（可选）

### 2. 数据库配置
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/intelliengine
    username: root
    password: IntelliEngine@2025
```

### 3. 运行项目
```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run

# 访问 Swagger UI
http://localhost:8081/api/swagger-ui.html
```

## API接口

| 模块 | 接口前缀 | 主要接口 |
|------|---------|---------|
| 用户 | `/api/v1/users` | 注册、登录、修改用户信息 |
| 项目 | `/api/v1/projects` | 创建、查询、更新、删除项目 |
| 资产 | `/api/v1/assets` | 上传、版本管理、查询 |
| 审批 | `/api/v1/reviews` | 创建批注、查询评论 |
| 市场 | `/api/v1/market` | 模板列表、下单 |

## 数据库表结构

主要表：
- `user_account` - 用户账户
- `project_info` - 项目信息
- `project_member` - 项目成员
- `asset_info` - 资产信息
- `asset_version` - 资产版本
- `review_comment` - 审批评论
- `review_reply` - 评论回复
- `market_template` - 市场模板
- `market_order` - 订单信息

## 开发规范

- 使用COLA分层架构
- 所有代码需有详细中文注释
- Mapper层使用MyBatis注解SQL
- Controller层使用Swagger注解
- JWT Token认证保护API

## 单元测试

```bash
# 运行所有测试
mvn test

# 查看测试报告
target/surefire-reports/
```

测试覆盖：
- **JwtUtilTest** - JWT工具类测试（5个用例）
- **UserAppServiceTest** - 用户服务测试（2个用例）
- **ProjectAppServiceTest** - 项目服务测试（2个用例）

## GitHub仓库

https://github.com/navis2024/intelliengine-backend

## 版本历史

- `v1.1.0` - 添加JWT认证、单元测试、Docker部署
- `v1.0.0` - 初始版本，完成5个核心模块
