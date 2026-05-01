package com.aigc.intelliengine.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 通用分页响应封装类
 * 
 * 用于列表接口的分页数据返回
 * 
 * @param <T> 列表数据类型
 * @author 智摩开发团队
 * @version 1.0.0
 * @since 2024
 */
@Data
@Schema(description = "通用分页响应")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    @Schema(description = "数据列表")
    private List<T> list;

    /**
     * 总记录数
     */
    @Schema(description = "总记录数", example = "100")
    private Long total;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码", example = "1")
    private Integer pageNum;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "10")
    private Integer pageSize;

    /**
     * 总页数
     */
    @Schema(description = "总页数", example = "10")
    private Integer pages;

    /**
     * 私有构造方法
     */
    private PageResult() {
    }

    /**
     * 构造分页结果
     *
     * @param list     数据列表
     * @param total    总记录数
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     */
    public PageResult(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        // 计算总页数: (total + pageSize - 1) / pageSize
        this.pages = (int) ((total + pageSize - 1) / pageSize);
    }

    /**
     * 构造分页结果（自动计算总页数）
     *
     * @param list     数据列表
     * @param total    总记录数
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     * @return PageResult
     */
    public static <T> PageResult<T> of(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        return new PageResult<>(list, total, pageNum, pageSize);
    }

    /**
     * 构造空分页结果
     *
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     * @return PageResult
     */
    public static <T> PageResult<T> empty(Integer pageNum, Integer pageSize) {
        return new PageResult<>(Collections.emptyList(), 0L, pageNum, pageSize);
    }

    /**
     * 构造空分页结果（默认页码页大小）
     *
     * @return PageResult
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>(Collections.emptyList(), 0L, 1, 10);
    }

    /**
     * 判断是否为空分页
     *
     * @return true如果列表为空
     */
    public boolean isEmpty() {
        return list == null || list.isEmpty();
    }

    /**
     * 判断是否有下一页
     *
     * @return true如果当前页不是最后一页
     */
    public boolean hasNext() {
        return pageNum < pages;
    }

    /**
     * 判断是否有上一页
     *
     * @return true如果当前页不是第一页
     */
    public boolean hasPrevious() {
        return pageNum > 1;
    }
}
