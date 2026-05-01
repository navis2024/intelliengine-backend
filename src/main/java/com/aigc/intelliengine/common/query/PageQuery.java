package com.aigc.intelliengine.common.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分页查询基类
 */
@Data
public class PageQuery {
    @Schema(description = "页码，从1开始", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}
