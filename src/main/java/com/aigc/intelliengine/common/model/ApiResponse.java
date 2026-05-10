package com.aigc.intelliengine.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Schema(description = "通用API响应")
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "业务状态码", example = "200")
    private Integer code;
    @Schema(description = "提示信息", example = "操作成功")
    private String message;
    @Schema(description = "响应数据")
    private T data;
    @Schema(description = "时间戳(毫秒)", example = "1704067200000")
    private Long timestamp;

    private ApiResponse() {
        this.timestamp = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private ApiResponse(Integer code, String message, T data) {
        this();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success() { return new ApiResponse<>(200, "操作成功", null); }
    public static <T> ApiResponse<T> success(T data) { return new ApiResponse<>(200, "操作成功", data); }
    public static <T> ApiResponse<T> success(String message, T data) { return new ApiResponse<>(200, message, data); }
    public static <T> ApiResponse<T> error(String message) { return new ApiResponse<>(500, message, null); }
    public static <T> ApiResponse<T> error(Integer code, String message) { return new ApiResponse<>(code, message, null); }
    public static <T> ApiResponse<T> badRequest(String message) { return new ApiResponse<>(400, message, null); }
    public static <T> ApiResponse<T> unauthorized(String message) { return new ApiResponse<>(401, message, null); }
    public static <T> ApiResponse<T> forbidden(String message) { return new ApiResponse<>(403, message, null); }
    public static <T> ApiResponse<T> notFound(String message) { return new ApiResponse<>(404, message, null); }
    public boolean isSuccess() { return code != null && code == 200; }
}
