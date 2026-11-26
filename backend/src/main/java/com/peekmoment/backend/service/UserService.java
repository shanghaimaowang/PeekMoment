package com.peekmoment.backend.service;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.peekmoment.backend.auth.UserContext;
import com.peekmoment.backend.common.BusinessException;
import com.peekmoment.backend.common.ResultCode;
import com.peekmoment.backend.dto.PasswordRequest;
import com.peekmoment.backend.dto.UserVO;
import com.peekmoment.backend.dto.UserUpdateRequest;
import com.peekmoment.backend.entity.User;
import com.peekmoment.backend.mapper.UserMapper;
import com.peekmoment.backend.util.TimeUtil;

import cn.hutool.crypto.digest.BCrypt;
import lombok.RequiredArgsConstructor;

/**
 * 用户资料：查看、修改、改密。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public UserVO me(Long userId) {
        return toVO(requireUser(userId));
    }

    public UserVO update(Long userId, UserUpdateRequest req) {
        User user = requireUser(userId);
        if (req.nickname() != null) {
            user.setNickname(req.nickname());
        }
        if (req.avatarUrl() != null) {
            user.setAvatarUrl(req.avatarUrl());
        }
        userMapper.updateById(user);
        return toVO(user);
    }

    public void changePassword(Long userId, PasswordRequest req) {
        User user = requireUser(userId);
        if (!BCrypt.checkpw(req.oldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "原密码错误");
        }
        user.setPasswordHash(BCrypt.hashpw(req.newPassword()));
        userMapper.updateById(user);
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private UserVO toVO(User user) {
        return new UserVO(user.getId(), user.getUsername(), user.getNickname(),
                user.getAvatarUrl(), user.getStatus(), TimeUtil.toMillis(user.getCreatedAt()));
    }
}
