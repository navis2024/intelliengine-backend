package com.aigc.intelliengine.project.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "项目信息")
public class ProjectVO {
    @Schema(description = "项目ID") private String id;
    @Schema(description = "项目编码") private String projectCode;
    @Schema(description = "项目名称") private String name;
    @Schema(description = "项目描述") private String description;
    @Schema(description = "封面图URL") private String coverUrl;
    @Schema(description = "创建者ID") private String ownerId;
    @Schema(description = "创建者名称") private String ownerName;
    @Schema(description = "项目状态") private String status;
    @Schema(description = "可见性") private String visibility;
    @Schema(description = "成员数量") private Integer memberCount;
    @Schema(description = "资产数量") private Long assetCount;
    @Schema(description = "创建时间") private LocalDateTime createTime;
    @Schema(description = "更新时间") private LocalDateTime updateTime;
}
