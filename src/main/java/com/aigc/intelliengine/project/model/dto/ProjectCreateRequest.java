package com.aigc.intelliengine.project.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "项目创建请求")
public class ProjectCreateRequest {
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100)
    @Schema(description = "项目名称", required = true, example = "我的创意项目")
    private String name;

    @Size(max = 2000)
    @Schema(description = "项目描述", example = "这是一个AIGC视频创作项目")
    private String description;

    @Size(max = 500)
    @Schema(description = "封面图URL")
    private String coverUrl;

    @Schema(description = "可见性: PRIVATE, PUBLIC", example = "PRIVATE")
    private String visibility;
}
