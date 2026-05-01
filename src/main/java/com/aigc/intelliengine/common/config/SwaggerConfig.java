package com.aigc.intelliengine.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 配置类
 * <p>
 * 配置API文档的基本信息、安全配置和服务器信息
 * 支持JWT Token认证测试
 *
 * @author 智擎开发团队
 * @version 1.0.0
 * @since 2024
 */
@Configuration
public class SwaggerConfig {

    /**
     * 安全方案名称
     */
    private static final String SECURITY_SCHEME_NAME = "JWT认证";

    /**
     * 配置OpenAPI
     * <p>
     * 设置API文档标题、版本、描述、联系方式等信息
     * 配置JWT安全方案，支持在Swagger UI中测试需要认证的接口
     *
     * @return OpenAPI配置对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API基本信息
                .info(new Info()
                        .title("智擎(IntelliEngine) API文档")
                        .version("v1.1.0")
                        .description("""
                                ## 智擎 - AIGC资产管理与协作平台
                                
                                ### 功能模块
                                - **用户管理** - 注册、登录、登出、用户信息管理
                                - **项目管理** - 项目创建、成员管理、权限控制
                                - **资产管理** - 资产上传、版本控制、分类管理
                                - **审批批注** - 视频批注、评论回复、审批流程
                                - **模板市场** - 模板浏览、购买下载、订单管理
                                
                                ### 认证说明
                                1. 调用 `/api/v1/users/login` 获取JWT Token
                                2. 在请求头中添加: `Authorization: Bearer {token}`
                                3. 点击右上角"Authorize"按钮输入Token进行测试
                                
                                ### 限流说明
                                - 登录接口: 同一IP每分钟10次
                                - 注册接口: 同一IP每分钟5次
                                
                                ### 响应格式
                                ```json
                                {
                                  "code": 200,
                                  "message": "操作成功",
                                  "data": {},
                                  "timestamp": 1704067200000
                                }
                                ```
                                """)
                        .contact(new Contact()
                                .name("智擎开发团队")
                                .email("support@intelliengine.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://github.com/navis2024/intelliengine-backend/blob/master/LICENSE")))
                // 服务器配置
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081/api")
                                .description("本地开发环境"),
                        new Server()
                                .url("http://localhost:80/api")
                                .description("Docker部署环境(Nginx)"),
                        new Server()
                                .url("https://api.intelliengine.com/api")
                                .description("生产环境")))
                // 安全配置 - JWT
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("请输入JWT Token，格式: Bearer {token}")));
    }
}
