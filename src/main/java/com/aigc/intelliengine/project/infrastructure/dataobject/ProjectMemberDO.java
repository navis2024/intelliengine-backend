package com.aigc.intelliengine.project.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目成员数据对象(Project Member Data Object)
 * <p>
 * 对应数据库表: project_member
 *
 * @author 智擎开发团队
 * @since 2024
 */
@Data
@TableName("project_member")
public class ProjectMemberDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色: OWNER/ADMIN/MEMBER/VIEWER
     */
    private String role;

    /**
     * 加入时间
     */
    private LocalDateTime joinedAt;
}
