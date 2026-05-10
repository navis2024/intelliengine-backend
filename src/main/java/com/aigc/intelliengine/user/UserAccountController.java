package com.aigc.intelliengine.user;

import com.aigc.intelliengine.common.model.ApiResponse;
import com.aigc.intelliengine.common.model.PageResult;
import com.aigc.intelliengine.common.redis.RateLimiterService;
import com.aigc.intelliengine.common.redis.TokenBlacklistService;
import com.aigc.intelliengine.common.security.UserContextHolder;
import com.aigc.intelliengine.common.security.JwtUtil;
import com.aigc.intelliengine.user.model.dto.LoginRequest;
import com.aigc.intelliengine.user.model.dto.RegisterRequest;
import com.aigc.intelliengine.user.model.dto.UserUpdateRequest;
import com.aigc.intelliengine.user.model.vo.LoginVO;
import com.aigc.intelliengine.user.model.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "用户管理 - 注册/登录/用户信息管理")
public class UserAccountController {

    private final UserAccountService userAccountService;
    private final TokenBlacklistService tokenBlacklistService;
    private final RateLimiterService rateLimiterService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册，用户名6-20位(字母数字下划线)，密码6-20位")
    public ApiResponse<UserVO> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        if (!rateLimiterService.tryAcquire("user:register:" + clientIp, 5, 60)) {
            return ApiResponse.error(429, "操作太频繁，请稍后再试");
        }
        return ApiResponse.success(userAccountService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录，支持用户名/邮箱/手机号+密码方式")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        if (!rateLimiterService.tryAcquire("user:login:" + clientIp, 10, 60)) {
            return ApiResponse.error(429, "登录次数过多，请5分钟后再试");
        }
        return ApiResponse.success(userAccountService.login(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出，Token将立即失效")
    public ApiResponse<Void> logout(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long expiration = jwtUtil.getExpirationDateFromToken(token);
            if (expiration != null && expiration > 0) {
                tokenBlacklistService.addToBlacklist(token, expiration);
                return ApiResponse.success("登出成功", null);
            }
        }
        return ApiResponse.error(400, "无效的Token");
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前用户信息", description = "从Token中获取当前登录用户的详细信息")
    public ApiResponse<UserVO> getCurrentUser() {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) return ApiResponse.error(401, "未登录");
        return ApiResponse.success(userAccountService.getUserById(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取详细信息")
    public ApiResponse<UserVO> getUserById(@Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        return ApiResponse.success(userAccountService.getUserById(id));
    }

    @PutMapping("/me")
    @Operation(summary = "更新当前用户信息", description = "更新当前登录用户的邮箱、手机号、头像等信息")
    public ApiResponse<UserVO> updateCurrentUser(@Valid @RequestBody UserUpdateRequest request) {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) return ApiResponse.error(401, "未登录");
        return ApiResponse.success(userAccountService.updateUser(userId, request));
    }

    @GetMapping
    @Operation(summary = "用户列表", description = "分页查询用户列表，支持按用户名/邮箱搜索")
    public ApiResponse<PageResult<UserVO>> listUsers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        return ApiResponse.success(userAccountService.listUsers(pageNum, pageSize, keyword));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
