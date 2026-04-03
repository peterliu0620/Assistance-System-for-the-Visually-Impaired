package com.example.demo.service;

import com.example.demo.dto.KnowledgeAskResponse;
import com.example.demo.dto.KnowledgeRecordItemResponse;
import com.example.demo.dto.KnowledgeRecordListResponse;
import com.example.demo.dto.KnowledgeRecordResponse;
import com.example.demo.dto.TargetTrackResponse;
import com.example.demo.dto.VisionAnalyzeResponse;
import com.example.demo.dto.VisionItem;
import com.example.demo.mapper.KnowledgeMapper;
import com.example.demo.model.ScanItem;
import com.example.demo.model.ScanRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeMapper knowledgeMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${vision.qwen.base-url:https://dashscope.aliyuncs.com}")
    private String qwenBaseUrl;

    @Value("${vision.qwen.api-key:}")
    private String apiKey;

    @Value("${vision.qwen.vl-model:qwen-vl-plus-latest}")
    private String vlModel;

    @Transactional
    public void archiveAnalyze(Long userId,
                               String sessionId,
                               VisionAnalyzeResponse response,
                               Double latitude,
                               Double longitude,
                               String locationText) {
        if (userId == null || response == null) {
            return;
        }
        ScanRecord record = new ScanRecord();
        record.setUserId(userId);
        record.setSessionId(emptyToNull(sessionId));
        record.setMode("analyze");
        record.setTriggerCommand(response.getTriggerCommand());
        record.setScene(response.getScene());
        record.setNarration(response.getNarration());
        record.setPositionSummary(response.getPositionSummary());
        record.setCapturedAt(response.getCapturedAt() == null ? LocalDateTime.now() : response.getCapturedAt());
        record.setLatitude(latitude);
        record.setLongitude(longitude);
        record.setLocationText(trimToNull(locationText));
        knowledgeMapper.insertRecord(record);

        List<VisionItem> items = response.getItems() == null ? List.of() : response.getItems();
        for (int i = 0; i < items.size(); i++) {
            VisionItem source = items.get(i);
            ScanItem item = new ScanItem();
            item.setRecordId(record.getId());
            item.setName(source.getName());
            item.setPosition(source.getPosition());
            item.setConfidence(source.getConfidence());
            item.setAttributesJson(toJson(Map.of("category", inferCategory(source.getName()))));
            item.setIsPrimary(i == 0);
            knowledgeMapper.insertItem(item);
        }
    }

    @Transactional
    public void archiveTrackingResult(Long userId,
                                      String sessionId,
                                      String targetName,
                                      TargetTrackResponse response,
                                      Double latitude,
                                      Double longitude,
                                      String locationText) {
        if (userId == null || response == null || sessionId == null || sessionId.isBlank()) {
            return;
        }
        if (!"REACHABLE".equalsIgnoreCase(response.getProximityLevel())) {
            return;
        }
        if (knowledgeMapper.countBySessionAndMode(userId, sessionId, "find-target") > 0) {
            return;
        }

        ScanRecord record = new ScanRecord();
        record.setUserId(userId);
        record.setSessionId(sessionId);
        record.setMode("find-target");
        record.setTriggerCommand("寻找" + (targetName == null ? "目标物" : targetName));
        record.setScene("实时寻物");
        record.setNarration(response.getGuidanceText());
        record.setPositionSummary(response.getDebugSummary());
        record.setCapturedAt(response.getAnalyzedAt() == null ? LocalDateTime.now() : response.getAnalyzedAt());
        record.setLatitude(latitude);
        record.setLongitude(longitude);
        record.setLocationText(trimToNull(locationText));
        knowledgeMapper.insertRecord(record);

        ScanItem item = new ScanItem();
        item.setRecordId(record.getId());
        item.setName(targetName == null ? response.getTargetName() : targetName);
        item.setPosition(buildTrackPosition(response));
        item.setConfidence(response.getConfidence());
        item.setAttributesJson(toJson(Map.of(
                "category", inferCategory(item.getName()),
                "proximityLevel", defaultString(response.getProximityLevel(), "UNKNOWN"),
                "guidanceLevel", defaultString(response.getGuidanceLevel(), "UNKNOWN")
        )));
        item.setIsPrimary(true);
        knowledgeMapper.insertItem(item);
    }

    public KnowledgeRecordListResponse listRecords(Long userId, int limit) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        int safeLimit = Math.max(1, Math.min(limit, 30));
        List<ScanRecord> records = knowledgeMapper.findRecentRecords(userId, safeLimit);
        List<KnowledgeRecordResponse> responses = records.stream().map(this::toRecordResponse).toList();
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        int todayScanCount = knowledgeMapper.countRecordsBetween(userId, start, end);
        String latestSessionId = knowledgeMapper.findLatestSessionId(userId);
        return new KnowledgeRecordListResponse(todayScanCount, latestSessionId, responses);
    }

    public KnowledgeAskResponse ask(Long userId, String question, String sessionId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        String normalizedQuestion = trimToNull(question);
        if (normalizedQuestion == null) {
            throw new IllegalArgumentException("问题不能为空");
        }

        String usedSessionId = trimToNull(sessionId);
        if (usedSessionId == null) {
            usedSessionId = knowledgeMapper.findLatestSessionId(userId);
        }

        List<ScanRecord> sessionRecords = usedSessionId == null ? List.of() : knowledgeMapper.findRecordsBySession(userId, usedSessionId, 5);
        List<ScanRecord> todayRecords = knowledgeMapper.findRecordsBetween(
                userId,
                LocalDate.now().atStartOfDay(),
                LocalDateTime.of(LocalDate.now(), LocalTime.MAX)
        );

        String answer;
        List<ScanRecord> matchedRecords;
        if (isStatsQuestion(normalizedQuestion)) {
            String keyword = extractKeyword(normalizedQuestion);
            List<ScanRecord> hits = findRecordsContaining(todayRecords, keyword);
            int count = hits.size();
            answer = buildStatsAnswer(keyword, count);
            matchedRecords = hits;
        } else {
            matchedRecords = sessionRecords.isEmpty() ? todayRecords.stream().limit(3).toList() : sessionRecords;
            answer = answerRelationQuestion(normalizedQuestion, matchedRecords);
        }

        if (matchedRecords.isEmpty()) {
            matchedRecords = sessionRecords.isEmpty() ? todayRecords.stream().limit(3).toList() : sessionRecords;
        }

        List<KnowledgeRecordResponse> responseRecords = matchedRecords.stream()
                .sorted(Comparator.comparing(ScanRecord::getCapturedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(3)
                .map(this::toRecordResponse)
                .toList();
        return new KnowledgeAskResponse(answer, responseRecords, usedSessionId, "today");
    }

    private String answerRelationQuestion(String question, List<ScanRecord> records) {
        if (records.isEmpty()) {
            return "最近没有可用的识别记录，暂时无法回答这个问题。";
        }

        String anchor = extractKeyword(question);
        String relation = extractRelation(question);
        ScanRecord matched = findBestAnchorRecord(records, anchor);
        if (matched == null) {
            return "最近一次会话里没有找到你提到的物体，暂时无法继续追问。";
        }
        List<ScanItem> items = knowledgeMapper.findItemsByRecordId(matched.getId());
        if (items.isEmpty()) {
            return "最近记录里缺少物品明细，暂时无法判断相对位置。";
        }

        ScanItem target = findAnchorItem(items, anchor);
        List<ScanItem> candidates = findCandidatesByRelation(items, relation, target);
        if (candidates.isEmpty()) {
            return tryModelAnswer(question, matched, items,
                    "最近一次记录里没有足够信息判断“" + question + "”，只能确认相关物体已被识别，但相对位置不够明确。");
        }

        ScanItem best = candidates.get(0);
        String fallback = "根据最近一次记录，" + defaultString(anchor, "目标物体") + relationText(relation) +
                "更可能是“" + best.getName() + "”，它位于" + defaultString(best.getPosition(), "附近") + "。";
        return tryModelAnswer(question, matched, items, fallback);
    }

    private String tryModelAnswer(String question, ScanRecord record, List<ScanItem> items, String fallback) {
        if (apiKey == null || apiKey.isBlank()) {
            return fallback;
        }
        try {
            ChatModel model = OpenAiChatModel.builder()
                    .baseUrl(normalizeBaseUrl(qwenBaseUrl) + "/compatible-mode/v1")
                    .apiKey(apiKey)
                    .modelName(vlModel)
                    .temperature(0.2d)
                    .build();
            String context = buildAskContext(record, items);
            String prompt = "请仅基于以下结构化记录回答用户问题，不要编造不存在的物体。问题：" + question + "。上下文：" + context;
            String answer = model.chat(prompt);
            return trimToNull(answer) == null ? fallback : answer.trim();
        } catch (Exception ex) {
            return fallback;
        }
    }

    private String buildAskContext(ScanRecord record, List<ScanItem> items) {
        StringBuilder builder = new StringBuilder();
        builder.append("场景=").append(defaultString(record.getScene(), "未知场景")).append(";");
        builder.append("指令=").append(defaultString(record.getTriggerCommand(), "未知指令")).append(";");
        builder.append("地点=").append(defaultString(record.getLocationText(), "未知地点")).append(";");
        for (ScanItem item : items) {
            builder.append("物品=").append(defaultString(item.getName(), "未知"))
                    .append(",位置=").append(defaultString(item.getPosition(), "未知"))
                    .append(",置信度=").append(item.getConfidence() == null ? 0 : item.getConfidence())
                    .append(";");
        }
        return builder.toString();
    }

    private String buildStatsAnswer(String keyword, int count) {
        String normalized = keyword == null ? "" : keyword.trim();
        if (normalized.isEmpty()) {
            return "今天一共扫描了 " + count + " 次相关记录。";
        }
        if (count == 0) {
            return "今天还没有识别到“" + normalized + "”相关记录。";
        }
        return "今天一共扫描到 " + count + " 次“" + normalized + "”相关记录。";
    }

    private List<ScanRecord> findRecordsContaining(List<ScanRecord> records, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return records;
        }
        String normalized = keyword.toLowerCase(Locale.ROOT);
        List<ScanRecord> matches = new ArrayList<>();
        for (ScanRecord record : records) {
            List<ScanItem> items = knowledgeMapper.findItemsByRecordId(record.getId());
            boolean matched = items.stream().anyMatch(item ->
                    containsIgnoreCase(item.getName(), normalized) ||
                            containsIgnoreCase(item.getAttributesJson(), normalized)
            );
            if (matched) {
                matches.add(record);
            }
        }
        return matches;
    }

    private ScanRecord findBestAnchorRecord(List<ScanRecord> records, String anchor) {
        for (ScanRecord record : records) {
            List<ScanItem> items = knowledgeMapper.findItemsByRecordId(record.getId());
            if (findAnchorItem(items, anchor) != null) {
                return record;
            }
        }
        return records.get(0);
    }

    private ScanItem findAnchorItem(List<ScanItem> items, String anchor) {
        if (items.isEmpty()) {
            return null;
        }
        if (anchor == null || anchor.isBlank()) {
            return items.stream().filter(item -> Boolean.TRUE.equals(item.getIsPrimary())).findFirst().orElse(items.get(0));
        }
        return items.stream()
                .filter(item -> containsIgnoreCase(item.getName(), anchor))
                .findFirst()
                .orElse(items.stream().filter(item -> Boolean.TRUE.equals(item.getIsPrimary())).findFirst().orElse(items.get(0)));
    }

    private List<ScanItem> findCandidatesByRelation(List<ScanItem> items, String relation, ScanItem anchor) {
        List<ScanItem> candidates = new ArrayList<>();
        for (ScanItem item : items) {
            if (anchor != null && item.getId() != null && item.getId().equals(anchor.getId())) {
                continue;
            }
            String position = defaultString(item.getPosition(), "");
            if ("LEFT".equals(relation) && (position.contains("左") || position.contains("left"))) {
                candidates.add(item);
            } else if ("RIGHT".equals(relation) && (position.contains("右") || position.contains("right"))) {
                candidates.add(item);
            } else if ("DOWN".equals(relation) && (position.contains("下") || position.contains("低"))) {
                candidates.add(item);
            } else if ("UP".equals(relation) && (position.contains("上") || position.contains("高"))) {
                candidates.add(item);
            }
        }
        if (candidates.isEmpty()) {
            return items.stream().filter(item -> anchor == null || !item.equals(anchor)).limit(1).toList();
        }
        return candidates;
    }

    private String relationText(String relation) {
        return switch (relation) {
            case "LEFT" -> "左边";
            case "RIGHT" -> "右边";
            case "UP" -> "上方";
            case "DOWN" -> "下方";
            default -> "附近";
        };
    }

    private boolean isStatsQuestion(String question) {
        return question.contains("几次") || question.contains("多少次") || question.contains("一共扫描") || question.contains("今天");
    }

    private String extractRelation(String question) {
        if (question.contains("左")) {
            return "LEFT";
        }
        if (question.contains("右")) {
            return "RIGHT";
        }
        if (question.contains("上")) {
            return "UP";
        }
        if (question.contains("下")) {
            return "DOWN";
        }
        return "NEAR";
    }

    private String extractKeyword(String question) {
        String normalized = question.replace("刚才那个", "")
                .replace("刚才", "")
                .replace("我今天一共扫描了几次", "")
                .replace("我今天扫描了几次", "")
                .replace("今天一共扫描了几次", "")
                .replace("今天扫描了几次", "")
                .replace("左边是什么", "")
                .replace("右边是什么", "")
                .replace("上边是什么", "")
                .replace("下面是什么", "")
                .replace("是什么", "")
                .replace("？", "")
                .replace("?", "")
                .trim();
        return normalized;
    }

    private KnowledgeRecordResponse toRecordResponse(ScanRecord record) {
        List<KnowledgeRecordItemResponse> items = knowledgeMapper.findItemsByRecordId(record.getId()).stream()
                .map(item -> new KnowledgeRecordItemResponse(
                        item.getId(),
                        item.getName(),
                        item.getPosition(),
                        item.getConfidence(),
                        item.getAttributesJson(),
                        item.getIsPrimary()
                ))
                .toList();
        return new KnowledgeRecordResponse(
                record.getId(),
                record.getUserId(),
                record.getSessionId(),
                record.getMode(),
                record.getTriggerCommand(),
                record.getScene(),
                record.getNarration(),
                record.getPositionSummary(),
                record.getCapturedAt(),
                record.getLatitude(),
                record.getLongitude(),
                record.getLocationText(),
                items
        );
    }

    private String buildTrackPosition(TargetTrackResponse response) {
        StringBuilder builder = new StringBuilder();
        if (response.getHorizontalDirection() != null) {
            builder.append(response.getHorizontalDirection());
        }
        if (response.getVerticalDirection() != null) {
            if (builder.length() > 0) {
                builder.append(" / ");
            }
            builder.append(response.getVerticalDirection());
        }
        return builder.length() == 0 ? "CENTER" : builder.toString();
    }

    private String inferCategory(String name) {
        String text = defaultString(name, "");
        if (text.contains("药") || text.contains("胶囊") || text.contains("片")) {
            return "药品";
        }
        if (text.contains("瓶")) {
            return "容器";
        }
        return "普通物品";
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private boolean containsIgnoreCase(String source, String target) {
        return source != null && target != null && source.toLowerCase(Locale.ROOT).contains(target.toLowerCase(Locale.ROOT));
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String emptyToNull(String value) {
        return trimToNull(value);
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}
