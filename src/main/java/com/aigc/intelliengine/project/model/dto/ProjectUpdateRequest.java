package com.aigc.intelliengine.project.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "项目更新请求")
public class ProjectUpdateRequest {
    @Size(max = 100)
    @Schema(description = "项目名称")
    private String name;

    @Size(max = 2000)
    @Schema(description = "项目描述")
    private String description;

    @Size(max = 500)
    @Schema(description = "封面图URL")
    private String coverUrl;

    @Schema(description = "项目状态: ACTIVE, ARCHIVED")
    private String status;

    @Schema(description = "可见性: PRIVATE, PUBLIC")
    private String visibility;
}
