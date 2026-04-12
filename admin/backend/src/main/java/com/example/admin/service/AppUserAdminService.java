package com.example.admin.service;

import com.example.admin.dto.AppUserCreateRequest;
import com.example.admin.dto.AppUserResponse;
import com.example.admin.dto.AppUserUpdateRequest;
import com.example.admin.mapper.AppUserMapper;
import com.example.admin.model.AppUser;
import com.example.admin.util.StringValueUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserAdminService {

    private final AppUserMapper appUserMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public List<AppUserResponse> list(String keyword) {
        return appUserMapper.findAll(StringValueUtils.trimToNull(keyword)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AppUserResponse create(AppUserCreateRequest request) {
        validateStatus(request.getStatus());
        String username = request.getUsername().trim();
        if (appUserMapper.findByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        String phone = StringValueUtils.trimToNull(request.getPhone());
        if (phone != null && appUserMapper.countByPhone(phone, null) > 0) {
            throw new IllegalArgumentException("手机号已存在");
        }

        String email = StringValueUtils.trimToNull(request.getEmail());
        if (email != null && appUserMapper.countByEmail(email, null) > 0) {
            throw new IllegalArgumentException("邮箱已存在");
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setPasswordHash(passwordEncoder.encode(request.getPassword().trim()));
        appUser.setNickname(request.getNickname().trim());
        appUser.setPhone(phone);
        appUser.setEmail(email);
        appUser.setStatus(request.getStatus());
        appUserMapper.insert(appUser);
        return toResponse(requireUser(appUser.getId()));
    }

    @Transactional
    public AppUserResponse update(Long id, AppUserUpdateRequest request) {
        validateStatus(request.getStatus());
        AppUser existing = requireUser(id);

        String phone = StringValueUtils.trimToNull(request.getPhone());
        if (phone != null && appUserMapper.countByPhone(phone, id) > 0) {
            throw new IllegalArgumentException("手机号已存在");
        }

        String email = StringValueUtils.trimToNull(request.getEmail());
        if (email != null && appUserMapper.countByEmail(email, id) > 0) {
            throw new IllegalArgumentException("邮箱已存在");
        }

        existing.setNickname(request.getNickname().trim());
        existing.setPhone(phone);
        existing.setEmail(email);
        existing.setStatus(request.getStatus());

        String password = StringValueUtils.trimToNull(request.getPassword());
        existing.setPasswordHash(password == null ? null : passwordEncoder.encode(password));
        appUserMapper.update(existing);
        return toResponse(requireUser(id));
    }

    @Transactional
    public void delete(Long id) {
        requireUser(id);
        appUserMapper.deleteById(id);
    }

    private AppUser requireUser(Long id) {
        AppUser appUser = appUserMapper.findById(id);
        if (appUser == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return appUser;
    }

    private AppUserResponse toResponse(AppUser appUser) {
        return new AppUserResponse(
                appUser.getId(),
                appUser.getUsername(),
                appUser.getNickname(),
                appUser.getPhone(),
                appUser.getEmail(),
                appUser.getStatus(),
                appUser.getLastLoginAt(),
                appUser.getCreatedAt(),
                appUser.getUpdatedAt()
        );
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态只能是0或1");
        }
    }
}
