package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UserAuthResponse;
import com.example.demo.mapper.SysUserMapper;
import com.example.demo.model.SysUser;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(SysUserMapper sysUserMapper, BCryptPasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserAuthResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        if (sysUserMapper.findByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        String phone = trimToNull(request.getPhone());
        if (phone != null && sysUserMapper.countByPhone(phone) > 0) {
            throw new IllegalArgumentException("手机号已存在");
        }

        String email = trimToNull(request.getEmail());
        if (email != null && sysUserMapper.countByEmail(email) > 0) {
            throw new IllegalArgumentException("邮箱已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword().trim()));
        user.setNickname(request.getNickname().trim());
        user.setPhone(phone);
        user.setEmail(email);
        user.setStatus(1);
        sysUserMapper.insert(user);

        return new UserAuthResponse(user.getId(), user.getUsername(), user.getNickname());
    }

    @Transactional
    public UserAuthResponse login(LoginRequest request) {
        String username = request.getUsername().trim();
        SysUser user = sysUserMapper.findByUsername(username);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new IllegalArgumentException("账号已被禁用");
        }

        sysUserMapper.updateLastLoginAt(user.getId(), LocalDateTime.now());
        return new UserAuthResponse(user.getId(), user.getUsername(), user.getNickname());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
