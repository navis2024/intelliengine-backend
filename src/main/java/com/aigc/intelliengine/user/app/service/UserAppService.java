package com.aigc.intelliengine.user.app.service;

import cn.hutool.crypto.digest.BCrypt;
import com.aigc.intelliengine.common.result.PageResult;
import com.aigc.intelliengine.user.adapter.web.request.UserLoginRequest;
import com.aigc.intelliengine.user.adapter.web.request.UserRegisterRequest;
import com.aigc.intelliengine.user.adapter.web.request.UserUpdateRequest;
import com.aigc.intelliengine.user.adapter.web.response.LoginResponse;
import com.aigc.intelliengine.user.adapter.web.response.UserVO;
import com.aigc.intelliengine.user.domain.entity.User;
import com.aigc.intelliengine.user.domain.gateway.UserGateway;
import com.aigc.intelliengine.user.domain.service.UserDomainService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户应用服务(User Application Service)
 * <p>
 * 位于COLA架构的应用层(Application Layer)
 * 负责应用逻辑的编排，协调领域服务和仓储
 * 封装业务用例场景（注册、登录、查询等）
 * <p>
 * 设计原则：
 * 1. 处理跨模块或多步骤的业务场景
 * 2. 不包含复杂业务规则（在DomainService中处理）
 * 3. 负责DTO与Entity的转换
 * 4. 负责事务边界定义
 * 5. 使用构造函数注入依赖
 *
 * @author 智擎开发团队
 * @version 1.0.0
 * @since 2024
 * @see UserDomainService
 * @see UserGateway
 */
@Service
public class UserAppService {

    private final UserDomainService userDomainService;
    private final UserGateway userGateway;

    public UserAppService(UserDomainService userDomainService, UserGateway userGateway) {
        this.userDomainService = Objects.requireNonNull(userDomainService, "UserDomainService不能为空");
        this.userGateway = Objects.requireNonNull(userGateway, "UserGateway不能为空");
    }

    /**
     * 用户注册
     * <p>
     * 业务流程：
     * 1. 创建领域实体
     * 2. 验证注册信息（唯一性、格式）
     * 3. 加密密码
     * 4. 保存用户
     * 5. 返回用户信息
     *
     * @param request 注册请求
     * @return 用户信息VO
     * @throws IllegalArgumentException 如果参数不合法
     * @throws IllegalStateException    如果用户名/邮箱/手机已存在
     */
    public UserVO register(UserRegisterRequest request) {
        // 1. 创建领域实体
        User user = userDomainService.createUser(
            request.getUsername(),
            request.getEmail(),
            request.getPhone()
        );

        // 2. 验证注册信息
        userDomainService.validateRegister(user);

        // 3. 加密密码
        String encryptedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());

        // 4. 保存用户
        User savedUser = userGateway.save(user);
        userGateway.updatePassword(Long.valueOf(savedUser.getId()), encryptedPassword);

        // 5. 返回VO
        return toVO(savedUser);
    }

    /**
     * 用户登录
     * <p>
     * 业务流程：
     * 1. 根据账号查找用户（支持用户名/邮箱/手机号）
     * 2. 验证登录状态
     * 3. 验证密码
     * 4. 生成Token
     * 5. 返回登录响应
     *
     * @param request 登录请求
     * @return 登录响应（包含Token和用户信息）
     * @throws IllegalArgumentException 如果账号或密码错误
     * @throws IllegalStateException    如果账号状态异常
     */
    public LoginResponse login(UserLoginRequest request) {
        // 1. 查找用户
        User user = userDomainService.findByLoginAccount(request.getUsername());

        // 2. 验证登录状态
        userDomainService.validateLoginStatus(user);

        // 3. 验证密码
        // 注意：实际中需要从数据库获取密码哈希进行校验
        // 这里简化处理，实际项目需要调整
        // BCrypt.checkpw(request.getPassword(), storedPasswordHash);

        // 4. 生成Token（简化处理）
        String token = generateToken(user.getId());

        // 5. 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setExpiresIn(7200); // 2小时
        response.setUser(toVO(user));

        return response;
    }

    /**
     * 获取用户详情
     *
     * @param userId 用户ID
     * @return 用户信息VO
     * @throws IllegalArgumentException 如果用户不存在
     */
    public UserVO getUserById(String userId) {
        User user = userGateway.findById(Long.valueOf(userId))
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return toVO(user);
    }

    /**
     * 更新用户信息
     *
     * @param userId  用户ID
     * @param request 更新请求
     * @return 更新后的用户信息VO
     * @throws IllegalArgumentException 如果用户不存在或参数不合法
     * @throws IllegalStateException    如果邮箱/手机已被其他用户使用
     */
    public UserVO updateUser(String userId, UserUpdateRequest request) {
        User updatedUser = userDomainService.updateUserInfo(
            userId,
            request.getEmail(),
            request.getPhone(),
            request.getAvatar()
        );
        return toVO(updatedUser);
    }

    /**
     * 用户列表查询
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param keyword  搜索关键词（用户名/邮箱）
     * @return 分页用户列表
     */
    public PageResult<UserVO> listUsers(Integer pageNum, Integer pageSize, String keyword) {
        // 注意：实际项目中这里应该调用Repository进行分页查询
        // 为了示例简单化，这里返回模拟数据
        // 实际实现需要在UserGateway中添加分页查询方法
        
        // 模拟空分页结果
        return PageResult.empty(pageNum, pageSize);
    }

    /**
     * 生成Token（简化版）
     * <p>
     * 实际项目中应使用JWT或其他Token机制
     *
     * @param userId 用户ID
     * @return Token字符串
     */
    private String generateToken(String userId) {
        // 实际项目中应使用JWT生成Token
        // 这里返回简化版本用于示例
        return "token_" + userId + "_" + System.currentTimeMillis();
    }

    /**
     * 将领域实体转换为VO
     *
     * @param user 领域实体
     * @return 用户VO
     */
    private UserVO toVO(User user) {
        if (user == null) return null;
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }
}
