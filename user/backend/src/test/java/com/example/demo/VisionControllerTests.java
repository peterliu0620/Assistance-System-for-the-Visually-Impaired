package com.example.demo;

import com.example.demo.dto.TargetTrackResponse;
import com.example.demo.controller.VisionController;
import com.example.demo.service.VisionAnalyzeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VisionController.class)
@Import(com.example.demo.controller.ApiExceptionHandler.class)
class VisionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VisionAnalyzeService visionAnalyzeService;

    @Test
    void shouldFindTargetWithTrackingPayload() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "frame.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "demo".getBytes()
        );

        when(visionAnalyzeService.findTarget(any(), eq("杯子"), eq("session-001"), eq(2), eq(null), eq(null), eq(null), eq(null)))
                .thenReturn(new TargetTrackResponse(
                        "session-001",
                        "杯子",
                        2,
                        true,
                        true,
                        "向左转30度，再向前走两步。",
                        "ADJUSTING",
                        "LEFT",
                        "CENTER",
                        "MID",
                        "MID",
                        83,
                        "SHORT",
                        "目标位于画面左侧。",
                        30,
                        null,
                        "TURN_LEFT",
                        LocalDateTime.of(2026, 4, 3, 10, 0, 0)
                ));

        mockMvc.perform(multipart("/api/vision/find-target")
                        .file(image)
                        .param("targetName", "杯子")
                        .param("sessionId", "session-001")
                        .param("frameIndex", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("session-001"))
                .andExpect(jsonPath("$.targetName").value("杯子"))
                .andExpect(jsonPath("$.guidanceText").value("向左转30度，再向前走两步。"))
                .andExpect(jsonPath("$.horizontalDirection").value("LEFT"))
                .andExpect(jsonPath("$.relativeAngle").value(30));
    }
}
