package com.example.demo.controller;

import com.example.demo.dto.FamilyDashboardResponse;
import com.example.demo.dto.FamilyBindVisionRequest;
import com.example.demo.dto.FamilyProfileRequest;
import com.example.demo.dto.FamilyProfileResponse;
import com.example.demo.dto.FamilyRecordResponse;
import com.example.demo.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/family")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    @GetMapping("/dashboard")
    public FamilyDashboardResponse dashboard(@RequestParam Long familyUserId,
                                             @RequestParam(required = false) Long visionUserId) {
        return familyService.getDashboard(familyUserId, visionUserId);
    }

    @GetMapping("/records")
    public List<FamilyRecordResponse> records(@RequestParam Long familyUserId,
                                              @RequestParam(required = false) Long visionUserId,
                                              @RequestParam(defaultValue = "10") Integer limit) {
        return familyService.listRecords(familyUserId, visionUserId, limit);
    }

    @GetMapping("/profile")
    public FamilyProfileResponse profile(@RequestParam Long familyUserId,
                                         @RequestParam(required = false) Long visionUserId) {
        return familyService.getProfile(familyUserId, visionUserId);
    }

    @PostMapping("/bind-vision")
    public FamilyProfileResponse bindVision(@RequestParam Long familyUserId,
                                            @RequestBody FamilyBindVisionRequest request) {
        return familyService.bindVisionUser(familyUserId, request);
    }

    @PostMapping("/profile")
    public FamilyProfileResponse saveProfile(@RequestParam Long familyUserId,
                                             @RequestParam(required = false) Long visionUserId,
                                             @RequestBody FamilyProfileRequest request) {
        return familyService.saveProfile(familyUserId, visionUserId, request);
    }
}
