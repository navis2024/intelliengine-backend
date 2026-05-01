package com.aigc.intelliengine.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 项目更新请求DTO
 *
 * @author 智擎开发团队
 * @since 2024
 */
@Data
@Schema(description = "项目更新请求")
public class ProjectUpdateRequest {

    @Size(max = 100, message = "项目名称最多100个字符")
    @Schema(description = "项目名称", example = "更新后的项目名")
    private String name;

    @Size(max = 2000, message = "项目描述最多2000个字符")
    @Schema(description = "项目描述")
    private String description;

    @Size(max = 500, message = "封面图URL最多500个字符")
    @Schema(description = "封面图URL")
    private String coverUrl;

    @Schema(description = "可见性: PRIVATE, PUBLIC")
    private String visibility;
}
