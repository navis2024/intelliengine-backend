package com.aigc.intelliengine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 配置类
 * 
 * 配置API文档信息和JWT认证方式
 * 
 * 访问地址: http://localhost:8081/api/swagger-ui.html
 * API JSON: http://localhost:8081/api/v3/api-docs
 * 
 * @author 智摩开发团队
 * @version 1.0.0
 * @since 2024
 */
@Configuration
public class SwaggerConfig {

    /**
     * 配置OpenAPI文档信息
     * 
     * @return OpenAPI配置实例
     */
    @Bean
    public OpenAPI customOpenAPI() {
        // 安全方案: JWT Bearer Token
        SecurityScheme securityScheme = new SecurityScheme()
                .name("BearerAuth")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("在下方输入JWT Token，格式: eyJhbGciOiJIUzI1NiIs...");

        // 安全要求
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("BearerAuth");

        return new OpenAPI()
                // API基本信息
                .info(new Info()
                        .title("智摩 (IntelliEngine) API")
                        .description("""
                                AIGC资产管理与协作平台 - 后端API文档
                                
                                ## 主要模块
                                - **User**: 用户管理 (注册/登录/信息管理)
                                - **Project**: 项目管理 (创建/成员/设置)
                                - **Asset**: 资产管理 (上传/版本/元数据)
                                - **Review**: 审阅管理 (审批/批注/时间线)
                                - **Market**: 市场交易 (模板/购买/下载)
                                
                                ## 认证说明
                                1. 先调用 `登录接口` 获取JWT Token
                                2. 点击右上角 **Authorize** 按钮
                                3. 输入 `Bearer {your_token}` 或只输入 `{your_token}`
                                4. 点击 **Authorize** 按钮保存
                                5. 测试其他需要认证的接口
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("智摩开发团队")
                                .email("dev@intelliengine.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                // 添加安全配置
                .addSecurityItem(securityRequirement)
                .schemaRequirement("BearerAuth", securityScheme);
    }
}
