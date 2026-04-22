package com.example.demo;

import com.example.demo.dto.VisionAnalyzeResponse;
import com.example.demo.dto.VisionItem;
import com.example.demo.mapper.KnowledgeMapper;
import com.example.demo.model.ScanItem;
import com.example.demo.model.ScanRecord;
import com.example.demo.service.KnowledgeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false"
})
@Transactional
class KnowledgeServiceTests {

    @Autowired
    private KnowledgeService knowledgeService;

    @Autowired
    private KnowledgeMapper knowledgeMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUpSchema() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS scan_item");
        jdbcTemplate.execute("DROP TABLE IF EXISTS scan_record");
        jdbcTemplate.execute("""
                CREATE TABLE scan_record (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  user_id BIGINT NOT NULL,
                  session_id VARCHAR(100) DEFAULT NULL,
                  mode VARCHAR(30) NOT NULL,
                  trigger_command VARCHAR(255) DEFAULT NULL,
                  scene VARCHAR(255) DEFAULT NULL,
                  narration TEXT,
                  position_summary VARCHAR(255) DEFAULT NULL,
                  captured_at DATETIME NOT NULL,
                  latitude DOUBLE DEFAULT NULL,
                  longitude DOUBLE DEFAULT NULL,
                  location_text VARCHAR(255) DEFAULT NULL,
                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE scan_item (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  record_id BIGINT NOT NULL,
                  name VARCHAR(100) NOT NULL,
                  position VARCHAR(100) DEFAULT NULL,
                  confidence INT DEFAULT NULL,
                  attributes_json TEXT,
                  is_primary TINYINT NOT NULL DEFAULT 0
                )
                """);
    }

    @Test
    void shouldArchiveAnalyzeResultIntoDatabase() {
        Long userId = 1001L;
        String sessionId = "session-test-001";
        LocalDateTime capturedAt = LocalDateTime.of(2026, 4, 21, 21, 0, 0);

        VisionAnalyzeResponse response = new VisionAnalyzeResponse(
                "识别当前画面",
                capturedAt,
                "客厅",
                "沙发前方有茶几和水杯。",
                List.of(
                        new VisionItem("茶几", "正前方", 96),
                        new VisionItem("水杯", "茶几右侧", 88)
                ),
                "当前位于客厅，前方有茶几，右侧有水杯。",
                null,
                null
        );

        Long recordId = knowledgeService.archiveAnalyze(
                userId,
                sessionId,
                response,
                30.123,
                120.456,
                "客厅窗边"
        );

        assertNotNull(recordId);

        List<ScanRecord> records = knowledgeMapper.findRecentRecords(userId, 10);
        assertFalse(records.isEmpty());

        ScanRecord savedRecord = records.stream()
                .filter(item -> recordId.equals(item.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals("analyze", savedRecord.getMode());
        assertEquals("识别当前画面", savedRecord.getTriggerCommand());
        assertEquals("客厅", savedRecord.getScene());
        assertEquals("当前位于客厅，前方有茶几，右侧有水杯。", savedRecord.getNarration());
        assertEquals("客厅窗边", savedRecord.getLocationText());
        assertEquals(capturedAt, savedRecord.getCapturedAt());

        List<ScanItem> items = knowledgeMapper.findItemsByRecordId(recordId);
        assertEquals(2, items.size());
        assertEquals("茶几", items.get(0).getName());
        assertEquals("正前方", items.get(0).getPosition());
        assertEquals(96, items.get(0).getConfidence());
        assertEquals(Boolean.TRUE, items.get(0).getIsPrimary());
    }
}
