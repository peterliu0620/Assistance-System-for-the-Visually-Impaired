package com.example.demo;

import com.example.demo.controller.FamilyController;
import com.example.demo.dto.FamilyDashboardResponse;
import com.example.demo.dto.FamilyBindVisionRequest;
import com.example.demo.dto.FamilyProfileRequest;
import com.example.demo.dto.FamilyProfileResponse;
import com.example.demo.dto.FamilyRecordResponse;
import com.example.demo.service.FamilyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FamilyController.class)
@Import(com.example.demo.controller.ApiExceptionHandler.class)
class FamilyControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FamilyService familyService;

    @Test
    void shouldReturnFamilyDashboard() throws Exception {
        when(familyService.getDashboard(eq(2L), isNull()))
                .thenReturn(new FamilyDashboardResponse(1L, "张阿姨", 2, 1, 4, 3, "门口有纸箱遮挡，存在绊倒风险。"));

        mockMvc.perform(get("/api/family/dashboard")
                        .param("familyUserId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visionUserNickname").value("张阿姨"))
                .andExpect(jsonPath("$.riskCount").value(1));
    }

    @Test
    void shouldReturnFamilyRecords() throws Exception {
        when(familyService.listRecords(eq(2L), isNull(), eq(5)))
                .thenReturn(List.of(
                        new FamilyRecordResponse(
                                10L,
                                "2026-04-21 09:18",
                                "识别到药盒、水杯和餐桌边缘，播报已完成。",
                                "厨房台面",
                                "药盒 / 水杯",
                                "家中厨房",
                                "提醒饭后服药，已完成语音播报。",
                                true,
                                "药盒靠近热水壶，已提醒注意拿取顺序。"
                        )
                ));

        mockMvc.perform(get("/api/family/records")
                        .param("familyUserId", "2")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].scene").value("厨房台面"))
                .andExpect(jsonPath("$[0].riskAlert").value(true));
    }

    @Test
    void shouldSaveFamilyProfile() throws Exception {
        FamilyProfileResponse response = new FamilyProfileResponse(
                List.of(new FamilyProfileResponse.BindingInfo(1L, "vision_demo", "王阿姨", "儿子")),
                new FamilyProfileResponse.BindingInfo(1L, "vision_demo", "王阿姨", "儿子"),
                new FamilyProfileResponse.BasicInfo("王阿姨", "女", "62", "低视力", "上海市浦东新区", "独居"),
                new FamilyProfileResponse.EmergencyContact("李明", "儿子", "13900001111", "021-66668888"),
                new FamilyProfileResponse.HealthInfo("降压药早晚各一次", "高血压", "青霉素过敏", "药盒放在玄关右侧第二层抽屉")
        );
        when(familyService.saveProfile(eq(2L), isNull(), org.mockito.ArgumentMatchers.any(FamilyProfileRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/family/profile")
                        .param("familyUserId", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "basicInfo": {
                                    "name": "王阿姨",
                                    "gender": "女",
                                    "age": "62",
                                    "visionLevel": "低视力",
                                    "address": "上海市浦东新区",
                                    "notes": "独居"
                                  },
                                  "emergencyContact": {
                                    "name": "李明",
                                    "relation": "儿子",
                                    "phone": "13900001111",
                                    "backupPhone": "021-66668888"
                                  },
                                  "healthInfo": {
                                    "medicine": "降压药早晚各一次",
                                    "diseaseNote": "高血压",
                                    "allergy": "青霉素过敏",
                                    "reminder": "药盒放在玄关右侧第二层抽屉"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basicInfo.name").value("王阿姨"))
                .andExpect(jsonPath("$.healthInfo.allergy").value("青霉素过敏"));
    }

    @Test
    void shouldBindVisionUser() throws Exception {
        FamilyProfileResponse response = new FamilyProfileResponse(
                List.of(new FamilyProfileResponse.BindingInfo(1L, "vision_demo", "王阿姨", "家人")),
                new FamilyProfileResponse.BindingInfo(1L, "vision_demo", "王阿姨", "家人"),
                new FamilyProfileResponse.BasicInfo(),
                new FamilyProfileResponse.EmergencyContact(),
                new FamilyProfileResponse.HealthInfo()
        );
        when(familyService.bindVisionUser(eq(2L), org.mockito.ArgumentMatchers.any(FamilyBindVisionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/family/bind-vision")
                        .param("familyUserId", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "visionUsername": "vision_demo",
                                  "relationship": "家人"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bindingInfo.visionUserId").value(1))
                .andExpect(jsonPath("$.bindingInfo.visionUsername").value("vision_demo"));
    }
}
