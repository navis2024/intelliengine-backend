package com.aigc.intelliengine.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 通用API响应封装类
 * 
 * 统一所有接口的响应格式，包含状态码、消息、数据和时间戳
 * 
 * @param <T> 响应数据类型
 * @author 智摩开发团队
 * @version 1.0.0
 * @since 2024
 */
@Data
@Schema(description = "通用API响应")
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 业务状态码
     * 200 - 成功
     * 400 - 请求参数错误
     * 401 - 未授权
     * 403 - 禁止访问
     * 404 - 资源不存在
     * 500 - 服务器内部错误
     */
    @Schema(description = "业务状态码", example = "200")
    private Integer code;

    /**
     * 提示信息
     */
    @Schema(description = "提示信息", example = "操作成功")
    private String message;

    /**
     * 响应数据
     */
    @Schema(description = "响应数据")
    private T data;

    /**
     * 时间戳(毫秒)
     */
    @Schema(description = "时间戳(毫秒)", example = "1704067200000")
    private Long timestamp;

    /**
     * 私有构造方法
     */
    private ApiResponse() {
        this.timestamp = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 私有构造方法
     */
    private ApiResponse(Integer code, String message, T data) {
        this();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ==================== 成功响应 ====================

    /**
     * 成功响应（无数据）
     *
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "操作成功", null);
    }

    /**
     * 成功响应（有数据）
     *
     * @param data 响应数据
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data);
    }

    /**
     * 成功响应（自定义消息）
     *
     * @param message 自定义消息
     * @param data    响应数据
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    // ==================== 失败响应 ====================

    /**
     * 失败响应
     *
     * @param message 错误消息
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null);
    }

    /**
     * 失败响应（指定状态码）
     *
     * @param code    状态码
     * @param message 错误消息
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 参数错误响应
     *
     * @param message 错误消息
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(400, message, null);
    }

    /**
     * 未授权响应
     *
     * @param message 错误消息
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>(401, message, null);
    }

    /**
     * 禁止访问响应
     *
     * @param message 错误消息
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return new ApiResponse<>(403, message, null);
    }

    /**
     * 资源不存在响应
     *
     * @param message 错误消息
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(404, message, null);
    }

    // ==================== 快捷方法 ====================

    /**
     * 判断是否成功
     *
     * @return true如果状态码为200
     */
    public boolean isSuccess() {
        return code != null && code == 200;
    }
}
