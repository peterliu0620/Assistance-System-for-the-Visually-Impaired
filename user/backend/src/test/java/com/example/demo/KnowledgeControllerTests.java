package com.example.demo;

import com.example.demo.controller.KnowledgeController;
import com.example.demo.dto.KnowledgeAskResponse;
import com.example.demo.dto.KnowledgeRecordListResponse;
import com.example.demo.service.KnowledgeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KnowledgeController.class)
@Import(com.example.demo.controller.ApiExceptionHandler.class)
class KnowledgeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KnowledgeService knowledgeService;

    @Test
    void shouldReturnKnowledgeRecords() throws Exception {
        when(knowledgeService.listRecords(eq(1L), eq(5)))
                .thenReturn(new KnowledgeRecordListResponse(3, "session-001", List.of()));

        mockMvc.perform(get("/api/knowledge/records")
                        .param("userId", "1")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayScanCount").value(3))
                .andExpect(jsonPath("$.latestSessionId").value("session-001"));
    }

    @Test
    void shouldAnswerKnowledgeQuestion() throws Exception {
        when(knowledgeService.ask(eq(1L), eq("我今天一共扫描了几次药品？"), eq("session-001")))
                .thenReturn(new KnowledgeAskResponse(
                        "今天一共扫描到 2 次“药品”相关记录。",
                        List.of(),
                        "session-001",
                        "today"
                ));

        mockMvc.perform(post("/api/knowledge/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "question": "我今天一共扫描了几次药品？",
                                  "sessionId": "session-001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("今天一共扫描到 2 次“药品”相关记录。"))
                .andExpect(jsonPath("$.usedSessionId").value("session-001"));
    }
}
