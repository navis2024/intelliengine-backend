package com.aigc.intelliengine.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 项目创建请求DTO
 *
 * @author 智擎开发团队
 * @since 2024
 */
@Data
@Schema(description = "项目创建请求")
public class ProjectCreateRequest {

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称最多100个字符")
    @Schema(description = "项目名称", required = true, example = "我的创意项目")
    private String name;

    @Size(max = 2000, message = "项目描述最多2000个字符")
    @Schema(description = "项目描述", example = "这是一个AIGC视频创作项目")
    private String description;

    @Size(max = 500, message = "封面图URL最多500个字符")
    @Schema(description = "封面图URL", example = "https://example.com/cover.jpg")
    private String coverUrl;

    @Schema(description = "可见性: PRIVATE(私有), PUBLIC(公开)", example = "PRIVATE")
    private String visibility;
}
