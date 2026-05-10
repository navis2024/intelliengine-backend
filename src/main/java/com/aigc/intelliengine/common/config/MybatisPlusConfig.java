package com.aigc.intelliengine.common.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus配置类
 * 
 * 功能说明:
 * - 配置Mapper接口扫描路径
 * - 启用MyBatis Plus功能
 * 
 * 扫描路径: com.aigc.intelliengine.**.infrastructure.mapper
 * 
 * @author 智擎开发团队
 * @version 1.0.0
 * @since 2024
 */
@Configuration
@MapperScan("com.aigc.intelliengine")
public class MybatisPlusConfig {
    // MyBatis Plus配置在application.yml中
}
