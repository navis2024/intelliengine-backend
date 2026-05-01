package com.aigc.intelliengine.project.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目领域实体(Project Entity)
 * <p>
 * 位于领域层，代表项目领域的核心业务概念
 * 不依赖任何技术框架，不依赖数据库存储方式
 *
 * @author 智擎开发团队
 * @since 2024
 */
@Data
public class Project {

    /**
     * 项目ID
     */
    private String id;

    /**
     * 项目编码
     */
    private String projectCode;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 封面图URL
     */
    private String coverUrl;

    /**
     * 创建者ID
     */
    private String ownerId;

    /**
     * 项目状态: ACTIVE, ARCHIVED, DELETED
     */
    private String status;

    /**
     * 可见性: PRIVATE, PUBLIC
     */
    private String visibility;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标志
     */
    private Integer deleted;

    /**
     * 业务方法：检查项目是否活跃
     */
    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }

    /**
     * 业务方法：检查项目是否公开
     */
    public boolean isPublic() {
        return "PUBLIC".equals(this.visibility);
    }

    /**
     * 业务方法：归档项目
     */
    public void archive() {
        this.status = "ARCHIVED";
    }

    /**
     * 业务方法：恢复项目
     */
    public void activate() {
        this.status = "ACTIVE";
    }
}
