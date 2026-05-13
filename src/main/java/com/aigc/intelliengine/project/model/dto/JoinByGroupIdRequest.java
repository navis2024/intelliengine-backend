package com.aigc.intelliengine.project.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "通过组ID加入项目请求")
public class JoinByGroupIdRequest {
    @NotBlank(message = "组ID不能为空")
    @Schema(description = "项目组ID", example = "GROUP_ABC123")
    private String groupId;
}
