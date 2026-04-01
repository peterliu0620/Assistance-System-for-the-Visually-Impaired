package com.example.demo.controller;

import com.example.demo.dto.VisionAnalyzeResponse;
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
                                         @RequestParam(value = "command", required = false) String command) {
        return visionAnalyzeService.analyze(image, command);
    }
}
