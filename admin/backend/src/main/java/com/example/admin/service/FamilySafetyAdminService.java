package com.example.admin.service;

import com.example.admin.dto.FamilyBindingCreateRequest;
import com.example.admin.dto.FamilyBindingResponse;
import com.example.admin.dto.MedicineProfileRequest;
import com.example.admin.dto.MedicineProfileResponse;
import com.example.admin.dto.SharedLogResponse;
import com.example.admin.mapper.FamilySafetyAdminMapper;
import com.example.admin.model.AppUser;
import com.example.admin.model.FamilyBinding;
import com.example.admin.model.FamilyMember;
import com.example.admin.model.MedicineProfile;
import com.example.admin.util.StringValueUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Clob;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FamilySafetyAdminService {

    private final FamilySafetyAdminMapper mapper;

    public List<FamilyBindingResponse> listBindings(String keyword) {
        return mapper.findBindings(StringValueUtils.trimToNull(keyword)).stream().map(this::toBindingResponse).toList();
    }

    @Transactional
    public FamilyBindingResponse createBinding(FamilyBindingCreateRequest request) {
        AppUser user = requireUser(request.getUserId());
        FamilyMember familyMember = resolveFamilyMember(request);
        if (mapper.countBinding(familyMember.getId(), user.getId()) > 0) {
            throw new IllegalArgumentException("该家属与用户已存在绑定关系");
        }

        FamilyBinding binding = new FamilyBinding();
        binding.setFamilyMemberId(familyMember.getId());
        binding.setUserId(user.getId());
        binding.setRelationship(request.getRelationship().trim());
        binding.setStatus(normalizeStatus(request.getStatus()));
        binding.setCreatedAt(LocalDateTime.now());
        mapper.insertBinding(binding);

        return new FamilyBindingResponse(
                binding.getId(),
                familyMember.getId(),
                familyMember.getName(),
                familyMember.getPhone(),
                familyMember.getEmail(),
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                binding.getRelationship(),
                binding.getStatus(),
                binding.getCreatedAt()
        );
    }

    public List<MedicineProfileResponse> listMedicineProfiles(Long userId, Long familyMemberId, String keyword) {
        return mapper.findMedicineProfiles(userId, familyMemberId, StringValueUtils.trimToNull(keyword)).stream()
                .map(this::toMedicineProfileResponse)
                .toList();
    }

    @Transactional
    public MedicineProfileResponse createMedicineProfile(MedicineProfileRequest request) {
        requireUser(request.getUserId());
        MedicineProfile profile = new MedicineProfile();
        fillMedicineProfile(profile, request);
        mapper.insertMedicineProfile(profile);
        return getMedicineProfile(profile.getId());
    }

    @Transactional
    public MedicineProfileResponse updateMedicineProfile(Long id, MedicineProfileRequest request) {
        MedicineProfile profile = requireMedicineProfile(id);
        requireUser(request.getUserId());
        fillMedicineProfile(profile, request);
        mapper.updateMedicineProfile(profile);
        return getMedicineProfile(id);
    }

    @Transactional
    public void deleteMedicineProfile(Long id) {
        requireMedicineProfile(id);
        mapper.deleteMedicineProfile(id);
    }

    public List<SharedLogResponse> listSharedLogs(Long familyMemberId, Long userId, String mode, LocalDate date) {
        if (familyMemberId == null) {
            throw new IllegalArgumentException("familyMemberId 不能为空");
        }
        LocalDateTime start = date == null ? null : date.atStartOfDay();
        LocalDateTime end = date == null ? null : date.plusDays(1).atStartOfDay().minusSeconds(1);
        return mapper.findSharedLogs(familyMemberId, userId, StringValueUtils.trimToNull(mode), start, end).stream()
                .map(this::toSharedLogResponse)
                .toList();
    }

    private MedicineProfileResponse getMedicineProfile(Long id) {
        Map<String, Object> row = mapper.findMedicineProfileDetailById(id);
        if (row == null || row.isEmpty()) {
            throw new IllegalArgumentException("药品档案不存在");
        }
        return toMedicineProfileResponse(row);
    }

    private FamilyMember resolveFamilyMember(FamilyBindingCreateRequest request) {
        if (request.getFamilyMemberId() != null) {
            FamilyMember byId = mapper.findFamilyMemberById(request.getFamilyMemberId());
            if (byId == null) {
                throw new IllegalArgumentException("家属账号不存在");
            }
            return byId;
        }
        String name = StringValueUtils.trimToNull(request.getFamilyName());
        if (name == null) {
            throw new IllegalArgumentException("家属姓名不能为空");
        }
        String phone = StringValueUtils.trimToNull(request.getFamilyPhone());
        FamilyMember existing = mapper.findFamilyMember(name, phone);
        if (existing != null) {
            return existing;
        }

        FamilyMember familyMember = new FamilyMember();
        familyMember.setName(name);
        familyMember.setPhone(phone);
        familyMember.setEmail(StringValueUtils.trimToNull(request.getFamilyEmail()));
        mapper.insertFamilyMember(familyMember);
        return familyMember;
    }

    private AppUser requireUser(Long userId) {
        AppUser user = mapper.findUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    private MedicineProfile requireMedicineProfile(Long id) {
        MedicineProfile profile = mapper.findMedicineProfileById(id);
        if (profile == null) {
            throw new IllegalArgumentException("药品档案不存在");
        }
        return profile;
    }

    private void fillMedicineProfile(MedicineProfile profile, MedicineProfileRequest request) {
        profile.setUserId(request.getUserId());
        profile.setFamilyMemberId(request.getFamilyMemberId());
        profile.setMedicineName(request.getMedicineName().trim());
        profile.setGenericName(StringValueUtils.trimToNull(request.getGenericName()));
        profile.setDescription(StringValueUtils.trimToNull(request.getDescription()));
        profile.setDosageUsage(StringValueUtils.trimToNull(request.getDosageUsage()));
        profile.setSuitablePeople(StringValueUtils.trimToNull(request.getSuitablePeople()));
        profile.setContraindications(StringValueUtils.trimToNull(request.getContraindications()));
        profile.setExpiryDate(request.getExpiryDate());
        profile.setBarcodeOrAlias(StringValueUtils.trimToNull(request.getBarcodeOrAlias()));
    }

    private FamilyBindingResponse toBindingResponse(Map<String, Object> row) {
        return new FamilyBindingResponse(
                asLong(row.get("id")),
                asLong(row.get("family_member_id")),
                asString(row.get("family_name")),
                asString(row.get("family_phone")),
                asString(row.get("family_email")),
                asLong(row.get("user_id")),
                asString(row.get("username")),
                asString(row.get("nickname")),
                asString(row.get("relationship")),
                asString(row.get("status")),
                asDateTime(row.get("created_at"))
        );
    }

    private MedicineProfileResponse toMedicineProfileResponse(Map<String, Object> row) {
        return new MedicineProfileResponse(
                asLong(row.get("id")),
                asLong(row.get("user_id")),
                asString(row.get("username")),
                asString(row.get("nickname")),
                asLong(row.get("family_member_id")),
                asString(row.get("family_name")),
                asString(row.get("medicine_name")),
                asString(row.get("generic_name")),
                asString(row.get("description")),
                asString(row.get("dosage_usage")),
                asString(row.get("suitable_people")),
                asString(row.get("contraindications")),
                asLocalDate(row.get("expiry_date")),
                asString(row.get("barcode_or_alias")),
                asDateTime(row.get("created_at")),
                asDateTime(row.get("updated_at"))
        );
    }

    private SharedLogResponse toSharedLogResponse(Map<String, Object> row) {
        String alertType = asString(row.get("alert_type"));
        return new SharedLogResponse(
                asLong(row.get("record_id")),
                asLong(row.get("user_id")),
                asString(row.get("username")),
                asString(row.get("nickname")),
                asString(row.get("mode")),
                asString(row.get("trigger_command")),
                asString(row.get("scene")),
                asString(row.get("core_item")),
                asString(row.get("location_text")),
                asDateTime(row.get("captured_at")),
                alertType != null && !alertType.isBlank(),
                alertType,
                asString(row.get("alert_message"))
        );
    }

    private String normalizeStatus(String status) {
        String normalized = StringValueUtils.trimToNull(status);
        return normalized == null ? "ACTIVE" : normalized.toUpperCase();
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private String asString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Clob clob) {
            try {
                return clob.getSubString(1, (int) clob.length());
            } catch (Exception ex) {
                return String.valueOf(value);
            }
        }
        return String.valueOf(value);
    }

    private LocalDateTime asDateTime(Object value) {
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return null;
    }

    private LocalDate asLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        return null;
    }
}
