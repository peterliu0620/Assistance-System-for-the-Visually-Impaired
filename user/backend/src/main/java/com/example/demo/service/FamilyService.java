package com.example.demo.service;

import com.example.demo.dto.FamilyBindVisionRequest;
import com.example.demo.dto.FamilyDashboardResponse;
import com.example.demo.dto.FamilyProfileRequest;
import com.example.demo.dto.FamilyProfileResponse;
import com.example.demo.dto.FamilyRecordResponse;
import com.example.demo.mapper.FamilyMapper;
import com.example.demo.mapper.KnowledgeMapper;
import com.example.demo.mapper.MedicineSafetyMapper;
import com.example.demo.mapper.SysUserMapper;
import com.example.demo.model.FamilyUserBinding;
import com.example.demo.model.ScanItem;
import com.example.demo.model.ScanRecord;
import com.example.demo.model.SysUser;
import com.example.demo.model.UserCareProfile;
import com.example.demo.util.StringValueUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final FamilyMapper familyMapper;
    private final SysUserMapper sysUserMapper;
    private final KnowledgeMapper knowledgeMapper;
    private final MedicineSafetyMapper medicineSafetyMapper;

    @Transactional(readOnly = true)
    public FamilyDashboardResponse getDashboard(Long familyUserId, Long visionUserId) {
        SysUser familyUser = requireFamilyUser(familyUserId);
        Long selectedVisionUserId = resolveVisionUserId(familyUser.getId(), visionUserId);
        SysUser visionUser = selectedVisionUserId == null ? null : sysUserMapper.findById(selectedVisionUserId);
        int todayCount = selectedVisionUserId == null ? 0 : knowledgeMapper.countRecordsBetween(
                selectedVisionUserId,
                LocalDate.now().atStartOfDay(),
                LocalDateTime.of(LocalDate.now(), LocalTime.MAX)
        );
        int recordsCount = selectedVisionUserId == null ? 0 : knowledgeMapper.findRecentRecords(selectedVisionUserId, 20).size();
        int riskCount = selectedVisionUserId == null ? 0 : medicineSafetyMapper.countAlertsByUserId(selectedVisionUserId);
        String recentRiskText = selectedVisionUserId == null ? "当前家人账号尚未关联视障人士。"
                : defaultString(medicineSafetyMapper.findLatestAlertMessageByUserId(selectedVisionUserId), "当前暂无新的风险提醒。");
        int completedSections = countCompletedSections(selectedVisionUserId == null ? null : familyMapper.findProfileByUserId(selectedVisionUserId));
        return new FamilyDashboardResponse(
                selectedVisionUserId,
                visionUser == null ? "" : defaultString(visionUser.getNickname(), defaultString(visionUser.getUsername(), "")),
                todayCount,
                riskCount,
                recordsCount,
                completedSections,
                recentRiskText
        );
    }

    @Transactional(readOnly = true)
    public List<FamilyRecordResponse> listRecords(Long familyUserId, Long visionUserId, Integer limit) {
        requireFamilyUser(familyUserId);
        Long selectedVisionUserId = resolveVisionUserId(familyUserId, visionUserId);
        if (selectedVisionUserId == null) {
            return List.of();
        }
        int safeLimit = Math.max(1, Math.min(limit == null ? 10 : limit, 20));
        return knowledgeMapper.findRecentRecords(selectedVisionUserId, safeLimit).stream()
                .map(this::toFamilyRecordResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FamilyProfileResponse getProfile(Long familyUserId, Long visionUserId) {
        requireFamilyUser(familyUserId);
        List<FamilyUserBinding> bindings = familyMapper.findBindingsByFamilyUserId(familyUserId);
        Long selectedVisionUserId = resolveVisionUserId(bindings, visionUserId);
        if (selectedVisionUserId == null) {
            return emptyProfileResponse(bindings);
        }
        FamilyUserBinding binding = findBinding(bindings, selectedVisionUserId);
        return toProfileResponse(selectedVisionUserId, familyMapper.findProfileByUserId(selectedVisionUserId), binding, bindings);
    }

    @Transactional
    public FamilyProfileResponse saveProfile(Long familyUserId, Long visionUserId, FamilyProfileRequest request) {
        requireFamilyUser(familyUserId);
        List<FamilyUserBinding> bindings = familyMapper.findBindingsByFamilyUserId(familyUserId);
        Long selectedVisionUserId = resolveVisionUserId(bindings, visionUserId);
        if (selectedVisionUserId == null) {
            throw new IllegalArgumentException("当前家人账号尚未关联视障人士");
        }
        UserCareProfile profile = new UserCareProfile();
        profile.setUserId(selectedVisionUserId);
        profile.setSubjectName(trim(request.getBasicInfo().getName()));
        profile.setGender(trim(request.getBasicInfo().getGender()));
        profile.setAge(trim(request.getBasicInfo().getAge()));
        profile.setVisionLevel(trim(request.getBasicInfo().getVisionLevel()));
        profile.setAddress(trim(request.getBasicInfo().getAddress()));
        profile.setNotes(trim(request.getBasicInfo().getNotes()));
        profile.setEmergencyName(trim(request.getEmergencyContact().getName()));
        profile.setEmergencyRelation(trim(request.getEmergencyContact().getRelation()));
        profile.setEmergencyPhone(trim(request.getEmergencyContact().getPhone()));
        profile.setEmergencyBackupPhone(trim(request.getEmergencyContact().getBackupPhone()));
        profile.setMedicine(trim(request.getHealthInfo().getMedicine()));
        profile.setDiseaseNote(trim(request.getHealthInfo().getDiseaseNote()));
        profile.setAllergy(trim(request.getHealthInfo().getAllergy()));
        profile.setReminder(trim(request.getHealthInfo().getReminder()));
        familyMapper.upsertProfile(profile);
        FamilyUserBinding binding = findBinding(bindings, selectedVisionUserId);
        return toProfileResponse(selectedVisionUserId, familyMapper.findProfileByUserId(selectedVisionUserId), binding, bindings);
    }

    @Transactional
    public FamilyProfileResponse bindVisionUser(Long familyUserId, FamilyBindVisionRequest request) {
        requireFamilyUser(familyUserId);
        String visionUsername = request == null ? null : trim(request.getVisionUsername());
        if (visionUsername == null) {
            throw new IllegalArgumentException("视障人士账号不能为空");
        }
        SysUser visionUser = sysUserMapper.findByUsername(visionUsername);
        if (visionUser == null || !AuthService.ROLE_VISION.equals(visionUser.getRole())) {
            throw new IllegalArgumentException("未找到对应的视障人士账号");
        }

        FamilyUserBinding binding = new FamilyUserBinding();
        binding.setFamilyUserId(familyUserId);
        binding.setVisionUserId(visionUser.getId());
        binding.setRelationship(defaultString(trim(request.getRelationship()), "家人"));
        familyMapper.upsertBinding(binding);

        List<FamilyUserBinding> bindings = familyMapper.findBindingsByFamilyUserId(familyUserId);
        FamilyUserBinding selectedBinding = findBinding(bindings, visionUser.getId());
        return toProfileResponse(visionUser.getId(), familyMapper.findProfileByUserId(visionUser.getId()), selectedBinding, bindings);
    }

    private SysUser requireFamilyUser(Long familyUserId) {
        if (familyUserId == null) {
            throw new IllegalArgumentException("家人用户ID不能为空");
        }
        SysUser user = sysUserMapper.findById(familyUserId);
        if (user == null) {
            throw new IllegalArgumentException("家人用户不存在");
        }
        if (!AuthService.ROLE_FAMILY.equals(user.getRole())) {
            throw new IllegalArgumentException("当前账号不是家人身份");
        }
        return user;
    }

    private Long resolveVisionUserId(Long familyUserId, Long requestedVisionUserId) {
        return resolveVisionUserId(familyMapper.findBindingsByFamilyUserId(familyUserId), requestedVisionUserId);
    }

    private Long resolveVisionUserId(List<FamilyUserBinding> bindings, Long requestedVisionUserId) {
        if (bindings == null || bindings.isEmpty()) {
            return null;
        }
        if (requestedVisionUserId == null) {
            return bindings.get(0).getVisionUserId();
        }
        FamilyUserBinding binding = findBinding(bindings, requestedVisionUserId);
        if (binding == null) {
            throw new IllegalArgumentException("未找到对应的绑定视障人士");
        }
        return binding.getVisionUserId();
    }

    private FamilyUserBinding findBinding(List<FamilyUserBinding> bindings, Long visionUserId) {
        if (bindings == null || visionUserId == null) {
            return null;
        }
        return bindings.stream()
                .filter(item -> visionUserId.equals(item.getVisionUserId()))
                .findFirst()
                .orElse(null);
    }

    private FamilyRecordResponse toFamilyRecordResponse(ScanRecord record) {
        List<ScanItem> items = knowledgeMapper.findItemsByRecordId(record.getId());
        String imageLabel = items.stream()
                .limit(2)
                .map(ScanItem::getName)
                .reduce((left, right) -> left + " / " + right)
                .orElse(defaultString(record.getScene(), "暂无"));
        boolean riskAlert = medicineSafetyMapper.countAlertsByRecordId(record.getId()) > 0;
        String riskText = riskAlert ? medicineSafetyMapper.findLatestAlertMessageByRecordId(record.getId()) : "";
        return new FamilyRecordResponse(
                record.getId(),
                record.getCapturedAt() == null ? "" : record.getCapturedAt().format(TIME_FORMATTER),
                defaultString(record.getNarration(), defaultString(record.getPositionSummary(), "已完成识别")),
                defaultString(record.getScene(), "未知场景"),
                imageLabel,
                defaultString(record.getLocationText(), "未记录位置"),
                defaultString(record.getPositionSummary(), defaultString(record.getNarration(), "")),
                riskAlert,
                defaultString(riskText, "")
        );
    }

    private FamilyProfileResponse emptyProfileResponse(List<FamilyUserBinding> bindings) {
        return new FamilyProfileResponse(
                toBindingInfos(bindings),
                new FamilyProfileResponse.BindingInfo(),
                new FamilyProfileResponse.BasicInfo(),
                new FamilyProfileResponse.EmergencyContact(),
                new FamilyProfileResponse.HealthInfo()
        );
    }

    private FamilyProfileResponse toProfileResponse(Long visionUserId,
                                                    UserCareProfile profile,
                                                    FamilyUserBinding binding,
                                                    List<FamilyUserBinding> bindings) {
        FamilyProfileResponse.BindingInfo currentBindingInfo = toBindingInfo(visionUserId, binding);
        if (profile == null) {
            return new FamilyProfileResponse(
                    toBindingInfos(bindings),
                    currentBindingInfo,
                    new FamilyProfileResponse.BasicInfo(),
                    new FamilyProfileResponse.EmergencyContact(),
                    new FamilyProfileResponse.HealthInfo()
            );
        }
        return new FamilyProfileResponse(
                toBindingInfos(bindings),
                currentBindingInfo,
                new FamilyProfileResponse.BasicInfo(
                        defaultString(profile.getSubjectName(), ""),
                        defaultString(profile.getGender(), ""),
                        defaultString(profile.getAge(), ""),
                        defaultString(profile.getVisionLevel(), ""),
                        defaultString(profile.getAddress(), ""),
                        defaultString(profile.getNotes(), "")
                ),
                new FamilyProfileResponse.EmergencyContact(
                        defaultString(profile.getEmergencyName(), ""),
                        defaultString(profile.getEmergencyRelation(), ""),
                        defaultString(profile.getEmergencyPhone(), ""),
                        defaultString(profile.getEmergencyBackupPhone(), "")
                ),
                new FamilyProfileResponse.HealthInfo(
                        defaultString(profile.getMedicine(), ""),
                        defaultString(profile.getDiseaseNote(), ""),
                        defaultString(profile.getAllergy(), ""),
                        defaultString(profile.getReminder(), "")
                )
        );
    }

    private List<FamilyProfileResponse.BindingInfo> toBindingInfos(List<FamilyUserBinding> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return List.of();
        }
        return bindings.stream()
                .map(item -> toBindingInfo(item.getVisionUserId(), item))
                .toList();
    }

    private FamilyProfileResponse.BindingInfo toBindingInfo(Long visionUserId, FamilyUserBinding binding) {
        SysUser visionUser = visionUserId == null ? null : sysUserMapper.findById(visionUserId);
        return new FamilyProfileResponse.BindingInfo(
                visionUserId,
                visionUser == null ? "" : defaultString(visionUser.getUsername(), ""),
                visionUser == null ? "" : defaultString(visionUser.getNickname(), ""),
                binding == null ? "" : defaultString(binding.getRelationship(), "家人")
        );
    }

    private int countCompletedSections(UserCareProfile profile) {
        if (profile == null) {
            return 0;
        }
        int count = 0;
        if (StringValueUtils.trimToNull(profile.getSubjectName()) != null && StringValueUtils.trimToNull(profile.getAge()) != null) {
            count += 1;
        }
        if (StringValueUtils.trimToNull(profile.getEmergencyName()) != null && StringValueUtils.trimToNull(profile.getEmergencyPhone()) != null) {
            count += 1;
        }
        if (StringValueUtils.trimToNull(profile.getMedicine()) != null || StringValueUtils.trimToNull(profile.getDiseaseNote()) != null) {
            count += 1;
        }
        return count;
    }

    private String trim(String value) {
        return StringValueUtils.trimToNull(value);
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
