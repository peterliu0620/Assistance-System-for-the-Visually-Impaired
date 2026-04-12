package com.example.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AppUserAdminControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldSupportCrudForAppUsers() throws Exception {
        String token = loginAndExtractToken();

        String createBody = """
                {
                  "username": "appuser01",
                  "password": "123456",
                  "nickname": "测试用户",
                  "phone": "13800138000",
                  "email": "appuser01@example.com",
                  "status": 1
                }
                """;

        mockMvc.perform(post("/api/admin/app-users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("appuser01"))
                .andExpect(jsonPath("$.data.nickname").value("测试用户"));

        mockMvc.perform(get("/api/admin/app-users")
                        .header("Authorization", "Bearer " + token)
                        .param("keyword", "appuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].username").value("appuser01"));

        String updateBody = """
                {
                  "nickname": "更新后用户",
                  "password": "",
                  "phone": "13900139000",
                  "email": "updated@example.com",
                  "status": 0
                }
                """;

        mockMvc.perform(put("/api/admin/app-users/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("更新后用户"))
                .andExpect(jsonPath("$.data.status").value(0));

        mockMvc.perform(delete("/api/admin/app-users/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void shouldAcceptPhoneAndEmailWithSurroundingWhitespace() throws Exception {
        String token = loginAndExtractToken();

        String createBody = """
                {
                  "username": "appuser02",
                  "password": "123456",
                  "nickname": "测试用户2",
                  "phone": " 18364501214 ",
                  "email": " qaz252385@163.com ",
                  "status": 1
                }
                """;

        mockMvc.perform(post("/api/admin/app-users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.phone").value("18364501214"))
                .andExpect(jsonPath("$.data.email").value("qaz252385@163.com"));
    }

    private String loginAndExtractToken() throws Exception {
        String response = mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int start = response.indexOf("\"token\":\"");
        int from = start + 9;
        int end = response.indexOf('"', from);
        return response.substring(from, end);
    }
}
