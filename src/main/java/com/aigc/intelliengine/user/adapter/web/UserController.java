package com.aigc.intelliengine.user.adapter.web;

import com.aigc.intelliengine.common.result.ApiResponse;
import com.aigc.intelliengine.common.result.PageResult;
import com.aigc.intelliengine.user.adapter.web.request.UserLoginRequest;
import com.aigc.intelliengine.user.adapter.web.request.UserRegisterRequest;
import com.aigc.intelliengine.user.adapter.web.request.UserUpdateRequest;
import com.aigc.intelliengine.user.adapter.web.response.LoginResponse;
import com.aigc.intelliengine.user.adapter.web.response.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
@Tag(name = "User", description = "用户管理 - 注册/登录/用户信息管理")
public class UserController {

    // ==================== 认证接口 (无需登录) ====================

    /**
     * 用户注册
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
            @Valid @RequestBody UserRegisterRequest request
    ) {
        // TODO: 调用应用层服务处理注册逻辑
        UserVO user = createMockUser(request.getUsername(), request.getEmail(), request.getPhone());
        return ApiResponse.success(user);
    }

    /**
     * 用户登录
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
            @Valid @RequestBody UserLoginRequest request
    ) {
        // TODO: 调用应用层服务处理登录逻辑
        LoginResponse response = new LoginResponse();
        response.setToken("mock_token_" + UUID.randomUUID());
        response.setExpiresIn(7200);
        response.setUser(createMockUser(request.getUsername(), null, null));
        return ApiResponse.success(response);
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
        // TODO: 调用应用层服务查询用户
        UserVO user = createMockUser("user_" + id, "user@example.com", "13800138000");
        user.setId(id);
        return ApiResponse.success(user);
    }

    /**
     * 更新用户信息
     *
     * @param id      用户ID
     * @param request 更新请求参数
     * @return 更新后的用户信息
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "更新用户信息",
            description = "更新用户邮箱、手机号、头像等信息"
    )
    public ApiResponse<UserVO> updateUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable String id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        // TODO: 调用应用层服务更新用户信息
        UserVO user = createMockUser("user_" + id, request.getEmail(), request.getPhone());
        user.setId(id);
        user.setAvatar(request.getAvatar());
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
        // TODO: 调用应用层服务查询用户列表
        List<UserVO> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(createMockUser("user" + i, "user" + i + "@example.com", "1380013800" + i));
        }

        PageResult<UserVO> pageResult = PageResult.of(list, 100L, pageNum, pageSize);
        return ApiResponse.success(pageResult);
    }

    // ==================== 私有方法 ====================

    /**
     * 创建模拟用户数据（用于测试）
     */
    private UserVO createMockUser(String username, String email, String phone) {
        UserVO user = new UserVO();
        user.setId(String.valueOf(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE));
        user.setUsername(username);
        user.setEmail(email != null ? email : username + "@example.com");
        user.setPhone(phone != null ? phone : "13800138000");
        user.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + username);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return user;
    }
}
