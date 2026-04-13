package com.example.demo.service;

import com.example.demo.dto.MatchedMedicineProfileSummary;
import com.example.demo.dto.MedicineAlert;
import com.example.demo.dto.VisionAnalyzeResponse;
import com.example.demo.dto.VisionItem;
import com.example.demo.mapper.MedicineSafetyMapper;
import com.example.demo.model.MedicineProfile;
import com.example.demo.model.MedicineScanAlert;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MedicineSafetyService {

    private static final int REPEAT_SCAN_WINDOW_MINUTES = 30;

    private final MedicineSafetyMapper medicineSafetyMapper;

    public MedicineMatchResult matchMedicine(Long userId, VisionAnalyzeResponse response) {
        if (userId == null || response == null || response.getItems() == null || response.getItems().isEmpty()) {
            return MedicineMatchResult.empty();
        }
        List<MedicineProfile> profiles = medicineSafetyMapper.findByUserId(userId);
        if (profiles.isEmpty()) {
            return MedicineMatchResult.empty();
        }

        for (VisionItem item : response.getItems()) {
            MedicineProfile matched = findMatch(profiles, item.getName());
            if (matched != null) {
                MatchedMedicineProfileSummary summary = toSummary(matched);
                MedicineAlert alert = buildAlert(userId, matched);
                return new MedicineMatchResult(summary, alert);
            }
        }
        return MedicineMatchResult.empty();
    }

    @Transactional
    public void persistAlertIfNeeded(Long userId, Long scanRecordId, MedicineMatchResult result) {
        if (userId == null || scanRecordId == null || result == null || result.medicineAlert == null) {
            return;
        }
        if (!Boolean.TRUE.equals(result.medicineAlert.getTriggered())) {
            return;
        }
        MedicineScanAlert alert = new MedicineScanAlert();
        alert.setUserId(userId);
        alert.setScanRecordId(scanRecordId);
        alert.setMedicineProfileId(result.matchedSummary == null ? null : result.matchedSummary.getId());
        alert.setAlertType(result.medicineAlert.getAlertType());
        alert.setAlertMessage(result.medicineAlert.getAlertMessage());
        alert.setCreatedAt(LocalDateTime.now());
        medicineSafetyMapper.insertAlert(alert);
    }

    private MedicineProfile findMatch(List<MedicineProfile> profiles, String detectedName) {
        if (detectedName == null || detectedName.isBlank()) {
            return null;
        }
        String normalized = detectedName.toLowerCase(Locale.ROOT);
        for (MedicineProfile profile : profiles) {
            if (contains(normalized, profile.getMedicineName())
                    || contains(normalized, profile.getGenericName())
                    || contains(normalized, profile.getBarcodeOrAlias())) {
                return profile;
            }
        }
        return null;
    }

    private boolean contains(String normalizedDetectedName, String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return false;
        }
        String normalizedCandidate = candidate.toLowerCase(Locale.ROOT);
        return normalizedDetectedName.contains(normalizedCandidate) || normalizedCandidate.contains(normalizedDetectedName);
    }

    private MedicineAlert buildAlert(Long userId, MedicineProfile profile) {
        MatchedMedicineProfileSummary summary = toSummary(profile);
        if (profile.getExpiryDate() != null && profile.getExpiryDate().isBefore(LocalDate.now())) {
            return new MedicineAlert(
                    true,
                    "expired",
                    "警告：该药品已过期，请暂停服用，并联系家属确认。",
                    "HIGH",
                    summary
            );
        }
        int recentCount = medicineSafetyMapper.countRecentScanByMedicineName(
                userId,
                profile.getMedicineName(),
                LocalDateTime.now().minusMinutes(REPEAT_SCAN_WINDOW_MINUTES)
        );
        if (recentCount > 0) {
            return new MedicineAlert(
                    true,
                    "repeat_scan",
                    "警告：短时间内重复扫描到同一药品，存在误服风险，请暂停服用并联系家属确认。",
                    "HIGH",
                    summary
            );
        }
        return new MedicineAlert(false, null, null, null, summary);
    }

    private MatchedMedicineProfileSummary toSummary(MedicineProfile profile) {
        return new MatchedMedicineProfileSummary(
                profile.getId(),
                profile.getMedicineName(),
                profile.getDosageUsage(),
                profile.getSuitablePeople(),
                profile.getContraindications(),
                profile.getExpiryDate()
        );
    }

    @Data
    @AllArgsConstructor
    public static class MedicineMatchResult {
        private MatchedMedicineProfileSummary matchedSummary;
        private MedicineAlert medicineAlert;

        public static MedicineMatchResult empty() {
            return new MedicineMatchResult(null, null);
        }
    }
}
