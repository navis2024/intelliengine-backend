package com.aigc.intelliengine.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
@Schema(description = "通用分页响应")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "数据列表")
    private List<T> list;
    @Schema(description = "总记录数", example = "100")
    private Long total;
    @Schema(description = "当前页码", example = "1")
    private Integer pageNum;
    @Schema(description = "每页大小", example = "10")
    private Integer pageSize;
    @Schema(description = "总页数", example = "10")
    private Integer pages;

    private PageResult() {}

    public PageResult(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (int) ((total + pageSize - 1) / pageSize);
    }

    public static <T> PageResult<T> of(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        return new PageResult<>(list, total, pageNum, pageSize);
    }
    public static <T> PageResult<T> empty(Integer pageNum, Integer pageSize) {
        return new PageResult<>(Collections.emptyList(), 0L, pageNum, pageSize);
    }
    public static <T> PageResult<T> empty() { return new PageResult<>(Collections.emptyList(), 0L, 1, 10); }
    public boolean isEmpty() { return list == null || list.isEmpty(); }
    public boolean hasNext() { return pageNum < pages; }
    public boolean hasPrevious() { return pageNum > 1; }
}
