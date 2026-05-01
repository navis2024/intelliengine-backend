package com.aigc.intelliengine.project.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目数据对象(Project Data Object)
 * <p>
 * 对应数据库表: project_info
 * 位于基础设施层，直接与数据库表project_info映射
 *
 * @author 智擎开发团队
 * @since 2024
 */
@Data
@TableName("project_info")
public class ProjectDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目编码，唯一约束
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
    private Long ownerId;

    /**
     * 项目状态: ACTIVE/ARCHIVED/DELETED
     */
    private String status;

    /**
     * 可见性: PRIVATE/PUBLIC
     */
    private String visibility;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标志
     */
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
