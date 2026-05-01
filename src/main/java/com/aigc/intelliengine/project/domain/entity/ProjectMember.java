package com.aigc.intelliengine.project.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目成员领域实体(Project Member Entity)
 * <p>
 * 位于领域层，代表项目成员关系
 *
 * @author 智擎开发团队
 * @since 2024
 */
@Data
public class ProjectMember {

    /**
     * 成员记录ID
     */
    private String id;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 角色: OWNER, ADMIN, MEMBER, VIEWER
     */
    private String role;

    /**
     * 加入时间
     */
    private LocalDateTime joinedAt;

    /**
     * 业务方法：检查是否是所有者
     */
    public boolean isOwner() {
        return "OWNER".equals(this.role);
    }

    /**
     * 业务方法：检查是否有管理权限
     */
    public boolean hasAdminPermission() {
        return "OWNER".equals(this.role) || "ADMIN".equals(this.role);
    }

    /**
     * 业务方法：检查是否可编辑
     */
    public boolean canEdit() {
        return "OWNER".equals(this.role) || "ADMIN".equals(this.role) || "MEMBER".equals(this.role);
    }
}
