package com.peekmoment.backend.service;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.peekmoment.backend.auth.JwtUtil;
import com.peekmoment.backend.common.BusinessException;
import com.peekmoment.backend.common.ResultCode;
import com.peekmoment.backend.dto.LoginRequest;
import com.peekmoment.backend.dto.LoginResponse;
import com.peekmoment.backend.dto.RegisterRequest;
import com.peekmoment.backend.entity.Admin;
import com.peekmoment.backend.entity.User;
import com.peekmoment.backend.mapper.AdminMapper;
import com.peekmoment.backend.mapper.UserMapper;

import cn.hutool.crypto.digest.BCrypt;
import lombok.RequiredArgsConstructor;

/**
 * 双端登录注册：密码 BCrypt 校验，token 用 role 区分 user / admin。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final AdminMapper adminMapper;
    private final JwtUtil jwtUtil;

    public LoginResponse register(RegisterRequest req) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.username()));
        if (count > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名已存在");
        }

        User user = new User();
        user.setUsername(req.username());
        user.setPasswordHash(BCrypt.hashpw(req.password()));
        user.setNickname(req.nickname());
        user.setStatus(1);
        user.setDeleted(0);
        userMapper.insert(user);

        return buildUserResponse(user, jwtUtil.issue(user.getId(), JwtUtil.ROLE_USER));
    }

    public LoginResponse login(LoginRequest req) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.username()));
        if (user == null || !BCrypt.checkpw(req.password(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }
        return buildUserResponse(user, jwtUtil.issue(user.getId(), JwtUtil.ROLE_USER));
    }

    public LoginResponse adminLogin(LoginRequest req) {
        Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, req.username()));
        if (admin == null || !BCrypt.checkpw(req.password(), admin.getPasswordHash())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名或密码错误");
        }
        LoginResponse.UserInfo info = new LoginResponse.UserInfo(
                admin.getId(), admin.getUsername(), admin.getNickname(), null);
        return new LoginResponse(jwtUtil.issue(admin.getId(), JwtUtil.ROLE_ADMIN), info);
    }

    private LoginResponse buildUserResponse(User user, String token) {
        LoginResponse.UserInfo info = new LoginResponse.UserInfo(
                user.getId(), user.getUsername(), user.getNickname(), user.getAvatarUrl());
        return new LoginResponse(token, info);
    }
}
