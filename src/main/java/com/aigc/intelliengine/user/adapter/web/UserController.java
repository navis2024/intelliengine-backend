package com.aigc.intelliengine.user.adapter.web;

import com.aigc.intelliengine.common.redis.RateLimiterService;
import com.aigc.intelliengine.common.redis.TokenBlacklistService;
import com.aigc.intelliengine.common.result.ApiResponse;
import com.aigc.intelliengine.common.result.PageResult;
import com.aigc.intelliengine.common.security.JwtUtil;
import com.aigc.intelliengine.user.adapter.web.request.UserLoginRequest;
import com.aigc.intelliengine.user.adapter.web.request.UserRegisterRequest;
import com.aigc.intelliengine.user.adapter.web.request.UserUpdateRequest;
import com.aigc.intelliengine.user.adapter.web.response.LoginResponse;
import com.aigc.intelliengine.user.adapter.web.response.UserVO;
import com.aigc.intelliengine.user.app.service.UserAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户模块控制器
 * <p>
 * 提供用户注册、登录、信息管理等功能接口
 * 接口路径: /api/v1/users/**
 *
 * @author 智擎开发团队
 * @version 1.0.0
 * @since 2024
 */
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "用户管理 - 注册/登录/用户信息管理")
public class UserController {

    private final UserAppService userAppService;
    private final TokenBlacklistService tokenBlacklistService;
    private final RateLimiterService rateLimiterService;
    private final JwtUtil jwtUtil;

    // ==================== 认证接口 (无需登录) ====================

    /**
     * 用户注册
     * <p>
     * 接口限流：同一IP每分5次
     *
     * @param request 注册请求参数
     * @return 用户信息
     */
    @PostMapping("/register")
    @Operation(
            summary = "用户注册",
            description = "新用户注册，用户名6-20位(字母数字下划线)，密码6-20位"
    )
    public ApiResponse<UserVO> register(
            @Valid @RequestBody UserRegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        // 接口限流：同一IP每分5次
        String clientIp = getClientIp(httpRequest);
        if (!rateLimiterService.tryAcquire("user:register:" + clientIp, 5, 60)) {
            return ApiResponse.error(429, "操作太频繁，请稍后再试");
        }

        UserVO user = userAppService.register(request);
        return ApiResponse.success(user);
    }

    /**
     * 用户登录
     * <p>
     * 接口限流：同一IP每分10次，超限后锁定5分钟
     *
     * @param request 登录请求参数
     * @return Token和用户信息
     */
    @PostMapping("/login")
    @Operation(
            summary = "用户登录",
            description = "用户登录，支持用户名/邮箱/手机号+密码方式"
    )
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody UserLoginRequest request,
            HttpServletRequest httpRequest
    ) {
        // 接口限流：同一IP每分10次
        String clientIp = getClientIp(httpRequest);
        if (!rateLimiterService.tryAcquire("user:login:" + clientIp, 10, 60)) {
            return ApiResponse.error(429, "登录次数过多，请5分钟后再试");
        }

        LoginResponse response = userAppService.login(request);
        return ApiResponse.success(response);
    }

    /**
     * 用户登出
     * <p>
     * 将Token加入Redis黑名单，使Token立即失效
     *
     * @param httpRequest HTTP请求
     * @return 登出结果
     */
    @PostMapping("/logout")
    @Operation(
            summary = "用户登出",
            description = "用户登出，Token将立即失效"
    )
    public ApiResponse<Void> logout(HttpServletRequest httpRequest) {
        // 获取Token
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // 获取Token剩余有效期
            Long expiration = jwtUtil.getExpirationDateFromToken(token);
            if (expiration != null && expiration > 0) {
                // 将Token加入黑名单，过期时间与Token一致
                tokenBlacklistService.addToBlacklist(token, expiration);
                return ApiResponse.success("登出成功", null);
            }
        }
        return ApiResponse.error(400, "无效的Token");
    }

    // ==================== 用户管理接口 (需要登录) ====================

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "获取用户详情",
            description = "根据用户ID获取详细信息"
    )
    public ApiResponse<UserVO> getUserById(
            @Parameter(description = "用户ID", required = true)
            @PathVariable String id
    ) {
        UserVO user = userAppService.getUserById(id);
        return ApiResponse.success(user);
    }

    /**
     * 更新用户信息
     *
     * @param id      用户ID
     * @param request 更新请求参数
     * @return 更新后的用户信息
     */
    @PutMapping("/me")
    @Operation(
            summary = "更新当前用户信息",
            description = "更新当前登录用户的邮箱、手机号、头像等信息"
    )
    public ApiResponse<UserVO> updateCurrentUser(
            @Valid @RequestBody UserUpdateRequest request,
            HttpServletRequest httpRequest
    ) {
        // 从request属性中获取当前登录用户ID（由JWT过滤器设置）
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.error(401, "未登录");
        }
        UserVO user = userAppService.updateUser(String.valueOf(userId), request);
        return ApiResponse.success(user);
    }

    /**
     * 用户列表查询
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param keyword  搜索关键词
     * @return 分页用户列表
     */
    @GetMapping
    @Operation(
            summary = "用户列表",
            description = "分页查询用户列表，支持按用户名/邮箱搜索"
    )
    public ApiResponse<PageResult<UserVO>> listUsers(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,

            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") Integer pageSize,

            @Parameter(description = "搜索关键词（用户名/邮箱）")
            @RequestParam(required = false) String keyword
    ) {
        PageResult<UserVO> pageResult = userAppService.listUsers(pageNum, pageSize, keyword);
        return ApiResponse.success(pageResult);
    }

    /**
     * 获取客户端IP地址
     * <p>
     * 从X-Forwarded-For头或远程地址获取真实IP
     *
     * @param request HTTP请求
     * @return IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}
