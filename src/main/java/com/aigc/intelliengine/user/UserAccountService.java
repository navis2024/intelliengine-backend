package com.aigc.intelliengine.user;

import cn.hutool.crypto.digest.BCrypt;
import com.aigc.intelliengine.common.exception.BusinessException;
import com.aigc.intelliengine.common.model.PageResult;
import com.aigc.intelliengine.common.redis.MultiLevelCacheService;
import com.aigc.intelliengine.common.security.JwtUtil;
import com.aigc.intelliengine.common.security.UserSession;
import com.aigc.intelliengine.user.model.dto.LoginRequest;
import com.aigc.intelliengine.user.model.dto.RegisterRequest;
import com.aigc.intelliengine.user.model.dto.UserUpdateRequest;
import com.aigc.intelliengine.user.model.entity.UserAccount;
import com.aigc.intelliengine.user.model.vo.LoginVO;
import com.aigc.intelliengine.user.model.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAccountService {

    private static final String SESSION_PREFIX = "session:user:";

    private final UserAccountMapper userAccountMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;
    private final MultiLevelCacheService cacheService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public UserVO register(RegisterRequest request) {
        if (!UserAccount.validateUsernameFormat(request.getUsername())) {
            throw new BusinessException("用户名格式不正确，要求6-20位字母数字下划线");
        }
        if (userAccountMapper.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已被使用");
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (!UserAccount.validateEmailFormat(request.getEmail())) {
                throw new BusinessException("邮箱格式不正确");
            }
            if (userAccountMapper.existsByEmail(request.getEmail())) {
                throw new BusinessException("邮箱已被使用");
            }
        }
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            if (!UserAccount.validatePhoneFormat(request.getPhone())) {
                throw new BusinessException("手机号格式不正确");
            }
            if (userAccountMapper.existsByPhone(request.getPhone())) {
                throw new BusinessException("手机号已被使用");
            }
        }

        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setPasswordHash(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setNickname(request.getNickname());
        user.setStatus(UserAccount.STATUS_NORMAL);
        user.setUserType("PERSONAL");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userAccountMapper.insert(user);

        return toVO(user);
    }

    public LoginVO login(LoginRequest request) {
        UserAccount user = userAccountMapper.selectByUsername(request.getUsername());
        if (user == null) {
            user = userAccountMapper.selectByEmail(request.getUsername());
        }
        if (user == null) {
            user = userAccountMapper.selectByPhone(request.getUsername());
        }
        if (user == null) {
            throw new BusinessException("账号或密码错误");
        }
        if (!user.canLogin()) {
            throw new BusinessException("账号状态异常，无法登录");
        }

        String storedHash = userAccountMapper.selectPasswordByUsername(user.getUsername());
        if (storedHash == null || !BCrypt.checkpw(request.getPassword(), storedHash)) {
            throw new BusinessException("账号或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        userAccountMapper.updateLastLoginTime(user.getId());

        // Write shared session to Redis (enables multi-instance session sharing)
        try {
            UserSession session = new UserSession(user.getId(), user.getUsername(),
                    System.currentTimeMillis(), token);
            String sessionJson = objectMapper.writeValueAsString(session);
            stringRedisTemplate.opsForValue().set(SESSION_PREFIX + user.getId(),
                    sessionJson, 604800, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Failed to write Redis session for user {}", user.getId(), e);
        }

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setExpiresIn(604800);
        vo.setUser(toVO(user));
        return vo;
    }

    public UserVO getUserById(Long userId) {
        return cacheService.getOrLoad("user:" + userId,
                () -> {
                    UserAccount user = userAccountMapper.selectById(userId);
                    if (user == null) throw new BusinessException("用户不存在");
                    return toVO(user);
                }, 10);
    }

    @Transactional
    public UserVO updateUser(Long userId, UserUpdateRequest request) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (!UserAccount.validateEmailFormat(request.getEmail())) {
                throw new BusinessException("邮箱格式不正确");
            }
            if (userAccountMapper.existsByEmail(request.getEmail())) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (!UserAccount.validatePhoneFormat(request.getPhone())) {
                throw new BusinessException("手机号格式不正确");
            }
            if (userAccountMapper.existsByPhone(request.getPhone())) {
                throw new BusinessException("手机号已被其他用户使用");
            }
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatarUrl(request.getAvatar());
        }
        user.setUpdatedAt(LocalDateTime.now());
        userAccountMapper.updateById(user);
        return toVO(user);
    }

    public PageResult<UserVO> listUsers(Integer pageNum, Integer pageSize, String keyword) {
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(UserAccount::getUsername, keyword).or().like(UserAccount::getEmail, keyword);
        }
        wrapper.orderByDesc(UserAccount::getCreatedAt);
        Page<UserAccount> page = userAccountMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords().stream().map(this::toVO).toList(),
                page.getTotal(), pageNum, pageSize);
    }

    private UserVO toVO(UserAccount user) {
        if (user == null) return null;
        UserVO vo = new UserVO();
        vo.setId(String.valueOf(user.getId()));
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatarUrl());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreatedAt());
        vo.setUpdateTime(user.getUpdatedAt());
        return vo;
    }
}
