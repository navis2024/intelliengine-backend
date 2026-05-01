package com.aigc.intelliengine.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目视图对象(Project VO)
 *
 * @author 智擎开发团队
 * @since 2024
 */
@Data
@Schema(description = "项目信息")
public class ProjectVO {

    @Schema(description = "项目ID", example = "1001")
    private String id;

    @Schema(description = "项目编码", example = "PROJ_20240101_001")
    private String projectCode;

    @Schema(description = "项目名称", example = "我的创意项目")
    private String name;

    @Schema(description = "项目描述")
    private String description;

    @Schema(description = "封面图URL")
    private String coverUrl;

    @Schema(description = "创建者ID", example = "1")
    private String ownerId;

    @Schema(description = "项目状态: ACTIVE, ARCHIVED", example = "ACTIVE")
    private String status;

    @Schema(description = "可见性: PRIVATE, PUBLIC", example = "PRIVATE")
    private String visibility;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
