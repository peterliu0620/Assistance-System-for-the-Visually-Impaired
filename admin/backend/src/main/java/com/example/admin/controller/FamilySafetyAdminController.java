package com.example.admin.controller;

import com.example.admin.dto.FamilyBindingCreateRequest;
import com.example.admin.dto.MedicineProfileRequest;
import com.example.admin.service.FamilySafetyAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class FamilySafetyAdminController {

    private final FamilySafetyAdminService familySafetyAdminService;

    @GetMapping("/family-bindings")
    public Map<String, Object> listBindings(@RequestParam(required = false) String keyword) {
        return Map.of("success", true, "data", familySafetyAdminService.listBindings(keyword));
    }

    @PostMapping("/family-bindings")
    public Map<String, Object> createBinding(@Valid @RequestBody FamilyBindingCreateRequest request) {
        return Map.of("success", true, "data", familySafetyAdminService.createBinding(request));
    }

    @GetMapping("/shared-logs")
    public Map<String, Object> listSharedLogs(@RequestParam Long familyMemberId,
                                              @RequestParam(required = false) Long userId,
                                              @RequestParam(required = false) String mode,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Map.of("success", true, "data", familySafetyAdminService.listSharedLogs(familyMemberId, userId, mode, date));
    }

    @GetMapping("/medicine-profiles")
    public Map<String, Object> listMedicineProfiles(@RequestParam(required = false) Long userId,
                                                    @RequestParam(required = false) Long familyMemberId,
                                                    @RequestParam(required = false) String keyword) {
        return Map.of("success", true, "data", familySafetyAdminService.listMedicineProfiles(userId, familyMemberId, keyword));
    }

    @PostMapping("/medicine-profiles")
    public Map<String, Object> createMedicineProfile(@Valid @RequestBody MedicineProfileRequest request) {
        return Map.of("success", true, "data", familySafetyAdminService.createMedicineProfile(request));
    }

    @PutMapping("/medicine-profiles/{id}")
    public Map<String, Object> updateMedicineProfile(@PathVariable Long id, @Valid @RequestBody MedicineProfileRequest request) {
        return Map.of("success", true, "data", familySafetyAdminService.updateMedicineProfile(id, request));
    }

    @DeleteMapping("/medicine-profiles/{id}")
    public Map<String, Object> deleteMedicineProfile(@PathVariable Long id) {
        familySafetyAdminService.deleteMedicineProfile(id);
        return Map.of("success", true, "data", true);
    }
}
