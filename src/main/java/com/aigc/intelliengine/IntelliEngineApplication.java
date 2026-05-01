package com.aigc.intelliengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智摩应用启动类
 * 
 * 该类是智摩后端应用的唯一入口，负责启动Spring Boot容器
 * 
 * @SpringBootApplication 是一个组合注解，包含：
 * - @Configuration: 标记该类为配置类
 * - @EnableAutoConfiguration: 启用Spring Boot自动配置
 * - @ComponentScan: 自动扫描当前包(com.aigc.intelliengine)及其子包下的所有组件
 * 
 * 扫描范围：com.aigc.intelliengine 及其所有子包
 * 包含模块：user, project, asset, review, market
 * 
 * @author 智摩开发团队
 * @version 1.0.0
 * @since 2024
 */
@SpringBootApplication(scanBasePackages = "com.aigc.intelliengine")
public class IntelliEngineApplication {

    /**
     * 应用程序主入口方法
     * 
     * 运行流程：
     * 1. 创建Spring应用上下文
     * 2. 扫描并加载所有Bean定义
     * 3. 启动内嵌Tomcat服务器
     * 4. 监听配置的端口(默认8080)
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(IntelliEngineApplication.class, args);
        System.out.println("══════════════════════════════════════════════════════════════════════════════════");
        System.out.println("╔════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║            智摩(IntelliEngine) 后端服务已启动成功            ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════════════════╣");
        System.out.println("║  API文档: http://localhost:8081/api/swagger-ui.html            ║");
        System.out.println("║  健康检查: http://localhost:8081/api/actuator/health          ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════╝");
        System.out.println("══════════════════════════════════════════════════════════════════════════════════");
    }
}
