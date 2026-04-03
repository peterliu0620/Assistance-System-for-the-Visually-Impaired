package com.example.demo.controller;

import com.example.demo.dto.VisionAnalyzeResponse;
import com.example.demo.dto.TargetTrackResponse;
import com.example.demo.service.VisionAnalyzeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class VisionController {

    private final VisionAnalyzeService visionAnalyzeService;

    @PostMapping(value = "/vision/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public VisionAnalyzeResponse analyze(@RequestPart("image") MultipartFile image,
                                         @RequestParam(value = "command", required = false) String command,
                                         @RequestParam(value = "userId", required = false) Long userId,
                                         @RequestParam(value = "sessionId", required = false) String sessionId,
                                         @RequestParam(value = "latitude", required = false) Double latitude,
                                         @RequestParam(value = "longitude", required = false) Double longitude,
                                         @RequestParam(value = "locationText", required = false) String locationText) {
        return visionAnalyzeService.analyze(image, command, userId, sessionId, latitude, longitude, locationText);
    }

    @PostMapping(value = "/vision/find-target", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TargetTrackResponse findTarget(@RequestPart("image") MultipartFile image,
                                          @RequestParam("targetName") String targetName,
                                          @RequestParam("sessionId") String sessionId,
                                          @RequestParam("frameIndex") Integer frameIndex,
                                          @RequestParam(value = "userId", required = false) Long userId,
                                          @RequestParam(value = "latitude", required = false) Double latitude,
                                          @RequestParam(value = "longitude", required = false) Double longitude,
                                          @RequestParam(value = "locationText", required = false) String locationText) {
        return visionAnalyzeService.findTarget(image, targetName, sessionId, frameIndex, userId, latitude, longitude, locationText);
    }
}
