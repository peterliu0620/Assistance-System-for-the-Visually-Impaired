package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.dto.TargetBox;
import com.example.demo.dto.TargetTrackResponse;
import com.example.demo.dto.VisionAnalyzeResponse;
import com.example.demo.dto.VisionItem;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Service
public class VisionAnalyzeService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final KnowledgeService knowledgeService;
    private final MedicineSafetyService medicineSafetyService;

    @Value("${vision.qwen.base-url:https://dashscope.aliyuncs.com}")
    private String qwenBaseUrl;

    @Value("${vision.qwen.api-key:}")
    private String apiKey;

    @Value("${vision.qwen.vl-model:qwen-vl-plus-latest}")
    private String vlModel;

    public VisionAnalyzeService(KnowledgeService knowledgeService, MedicineSafetyService medicineSafetyService) {
        this.knowledgeService = knowledgeService;
        this.medicineSafetyService = medicineSafetyService;
    }

    public VisionAnalyzeResponse analyze(MultipartFile image, String command, Long userId, String sessionId,
                                         Double latitude, Double longitude, String locationText) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("图片不能为空，请重新拍照后再试。");
        }
        String normalizedCommand = command == null || command.isBlank() ? "这是什么？" : command;

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("未配置 Qwen API Key，请检查 application.properties。");
        }

        try {
            String base64Image = Base64.getEncoder().encodeToString(image.getBytes());
            String mimeType = image.getContentType() == null || image.getContentType().isBlank() ? "image/jpeg" : image.getContentType();
            JsonNode resultNode = requestVisionResult(base64Image, mimeType, normalizedCommand);
            String scene = resultNode.path("scene").asText("未知场景");
            List<VisionItem> items = parseItems(resultNode.path("items"));
            if (items.isEmpty()) {
                items = List.of(new VisionItem("未识别到明确物体", "画面中心区域", 50));
            }
            String narration = resultNode.path("narration").asText(buildNarration(scene, items));

            VisionAnalyzeResponse response = new VisionAnalyzeResponse(
                    normalizedCommand,
                    LocalDateTime.now(),
                    scene,
                    "已按“场景 > 方位 > 核心物品”完成播报。",
                    items,
                    narration,
                    null,
                    null
            );
            MedicineSafetyService.MedicineMatchResult medicineMatchResult = medicineSafetyService.matchMedicine(userId, response);
            response.setMatchedMedicineProfileSummary(medicineMatchResult.getMatchedSummary());
            response.setMedicineAlert(medicineMatchResult.getMedicineAlert());
            Long recordId = knowledgeService.archiveAnalyze(userId, sessionId, response, latitude, longitude, locationText);
            medicineSafetyService.persistAlertIfNeeded(userId, recordId, medicineMatchResult);
            return response;
        } catch (Exception ex) {
            throw new IllegalStateException("视觉识别失败: " + ex.getMessage());
        }
    }

    public TargetTrackResponse findTarget(MultipartFile image, String targetName, String sessionId, Integer frameIndex,
                                          Long userId, Double latitude, Double longitude, String locationText) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("图片不能为空，请重新拍照后再试。");
        }
        String normalizedTarget = targetName == null ? "" : targetName.trim();
        if (normalizedTarget.isEmpty()) {
            throw new IllegalArgumentException("目标物名称不能为空。");
        }
        String normalizedSessionId = sessionId == null ? "" : sessionId.trim();
        if (normalizedSessionId.isEmpty()) {
            throw new IllegalArgumentException("会话编号不能为空。");
        }
        if (frameIndex == null || frameIndex < 0) {
            throw new IllegalArgumentException("帧序号不合法。");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("未配置 Qwen API Key，请检查 application.properties。");
        }

        try {
            String base64Image = Base64.getEncoder().encodeToString(image.getBytes());
            String mimeType = image.getContentType() == null || image.getContentType().isBlank() ? "image/jpeg" : image.getContentType();
            JsonNode resultNode = requestTargetTrackResult(base64Image, mimeType, normalizedTarget, normalizedSessionId, frameIndex);
            TargetTrackResponse response = buildTrackResponse(resultNode, normalizedTarget, normalizedSessionId, frameIndex);
            knowledgeService.archiveTrackingResult(userId, normalizedSessionId, normalizedTarget, response, latitude, longitude, locationText);
            return response;
        } catch (Exception ex) {
            throw new IllegalStateException("目标追踪失败: " + ex.getMessage());
        }
    }

    private JsonNode requestVisionResult(String base64Image, String mimeType, String command) throws Exception {
        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(normalizeBaseUrl(qwenBaseUrl) + "/compatible-mode/v1")
                .apiKey(apiKey)
                .modelName(vlModel)
                .temperature(0.2d)
                .build();

        String userPrompt = "根据图片回答用户指令：" + command + "。请输出JSON，格式为 {\"scene\":\"\",\"items\":[{\"name\":\"\",\"position\":\"\",\"confidence\":0}],\"narration\":\"\"}。items 至少给出3个最重要物体，position用方位描述，confidence 0-100。";
        ChatResponse response = model.chat(
                SystemMessage.from("你是视障辅助视觉识别助手。请只返回纯 JSON，不要 markdown。"),
                UserMessage.from(
                        TextContent.from(userPrompt),
                        ImageContent.from(base64Image, mimeType)
                )
        );
        String content = response.aiMessage().text();
        if (content.isBlank()) {
            throw new IllegalStateException("视觉识别返回内容为空。");
        }
        String cleaned = extractJsonString(content);
        return objectMapper.readTree(cleaned);
    }

    private JsonNode requestTargetTrackResult(String base64Image, String mimeType, String targetName, String sessionId, Integer frameIndex) throws Exception {
        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(normalizeBaseUrl(qwenBaseUrl) + "/compatible-mode/v1")
                .apiKey(apiKey)
                .modelName(vlModel)
                .temperature(0.1d)
                .build();

        String userPrompt = """
                你正在帮助视障人士实时寻找目标物。目标物是：%s。
                当前会话编号：%s，当前帧序号：%d。
                请只返回 JSON，不要 markdown，不要额外说明。
                JSON 格式必须为：
                {
                  "detected": true,
                  "locked": true,
                  "guidanceLevel": "SEARCHING|ADJUSTING|NEAR|REACHABLE",
                  "horizontalDirection": "LEFT|SLIGHT_LEFT|CENTER|SLIGHT_RIGHT|RIGHT",
                  "verticalDirection": "UP|CENTER|DOWN",
                  "estimatedDistance": "FAR|MID|NEAR|REACHABLE",
                  "proximityLevel": "FAR|MID|NEAR|REACHABLE",
                  "confidence": 0,
                  "guidanceText": "",
                  "suggestedHaptic": "SHORT|DOUBLE_SHORT|LONG",
                  "debugSummary": "",
                  "relativeAngle": 0,
                  "motionHint": "",
                  "targetBox": {
                    "centerX": 0.5,
                    "centerY": 0.5,
                    "width": 0.3,
                    "height": 0.2
                  }
                }
                规则：
                1. 如果没看到目标物，detected=false，locked=false，guidanceLevel=SEARCHING。
                2. 没看到目标时 guidanceText 只能给扫动镜头或轻微抬高/压低的指令。
                3. 看到了但目标偏离中心时，给明确方位指令，例如“向左转30度”“稍微向右一点”“稍微抬高手机”。
                4. 当目标已接近手边时，guidanceLevel=NEAR 或 REACHABLE，并给“手向右一点”“就在前方偏下”“已接近，可以伸手拿取”等提示。
                5. relativeAngle 为粗略角度：LEFT/RIGHT 可给 30，SLIGHT_LEFT/SLIGHT_RIGHT 可给 12，CENTER 给 0。
                6. confidence 范围必须是 0-100。
                """.formatted(targetName, sessionId, frameIndex);

        ChatResponse response = model.chat(
                SystemMessage.from("你是视障人士寻物导盲助手。输出必须是稳定、可机读的纯 JSON。"),
                UserMessage.from(
                        TextContent.from(userPrompt),
                        ImageContent.from(base64Image, mimeType)
                )
        );
        String content = response.aiMessage().text();
        if (content.isBlank()) {
            throw new IllegalStateException("目标追踪返回内容为空。");
        }
        return objectMapper.readTree(extractJsonString(content));
    }

    private List<VisionItem> parseItems(JsonNode itemsNode) {
        List<VisionItem> items = new ArrayList<>();
        if (itemsNode == null || !itemsNode.isArray()) {
            return items;
        }
        for (JsonNode itemNode : itemsNode) {
            String name = itemNode.path("name").asText("未知物体");
            String position = itemNode.path("position").asText("未知方位");
            int confidence = itemNode.path("confidence").asInt(60);
            if (confidence < 0) {
                confidence = 0;
            }
            if (confidence > 100) {
                confidence = 100;
            }
            items.add(new VisionItem(name, position, confidence));
        }
        return items;
    }

    private TargetTrackResponse buildTrackResponse(JsonNode node, String targetName, String sessionId, Integer frameIndex) {
        boolean detected = node.path("detected").asBoolean(false);
        boolean locked = node.path("locked").asBoolean(detected);
        String horizontalDirection = normalizeHorizontal(node.path("horizontalDirection").asText());
        String verticalDirection = normalizeVertical(node.path("verticalDirection").asText());
        String estimatedDistance = normalizeDistance(node.path("estimatedDistance").asText());
        String proximityLevel = normalizeDistance(node.path("proximityLevel").asText(estimatedDistance));
        int confidence = clamp(node.path("confidence").asInt(detected ? 75 : 20), 0, 100);
        String guidanceLevel = normalizeGuidanceLevel(node.path("guidanceLevel").asText());
        Integer relativeAngle = node.path("relativeAngle").isNumber()
                ? node.path("relativeAngle").asInt()
                : defaultAngle(horizontalDirection);
        String guidanceText = normalizeGuidanceText(
                node.path("guidanceText").asText(),
                detected,
                horizontalDirection,
                verticalDirection,
                proximityLevel
        );
        String suggestedHaptic = normalizeHaptic(
                node.path("suggestedHaptic").asText(),
                detected,
                proximityLevel,
                horizontalDirection
        );
        String motionHint = normalizeMotionHint(node.path("motionHint").asText(), detected, horizontalDirection, verticalDirection);
        String debugSummary = node.path("debugSummary").asText(
                detected ? "已识别到目标，建议按指引继续微调。" : "当前帧未发现目标，建议缓慢扫动镜头。"
        );

        return new TargetTrackResponse(
                sessionId,
                targetName,
                frameIndex,
                detected,
                locked,
                guidanceText,
                guidanceLevel,
                horizontalDirection,
                verticalDirection,
                estimatedDistance,
                proximityLevel,
                confidence,
                suggestedHaptic,
                debugSummary,
                relativeAngle,
                parseTargetBox(node.path("targetBox")),
                motionHint,
                LocalDateTime.now()
        );
    }

    private String extractJsonString(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstBrace = trimmed.indexOf("{");
            int lastBrace = trimmed.lastIndexOf("}");
            if (firstBrace >= 0 && lastBrace > firstBrace) {
                return trimmed.substring(firstBrace, lastBrace + 1);
            }
        }
        return trimmed;
    }

    private String buildNarration(String scene, List<VisionItem> items) {
        StringBuilder builder = new StringBuilder();
        builder.append("场景：").append(scene).append("。");
        for (VisionItem item : items) {
            builder.append("方位：").append(item.getPosition())
                    .append("，物品：").append(item.getName())
                    .append("，置信度").append(item.getConfidence()).append("%。");
        }
        return builder.toString();
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private String normalizeHorizontal(String value) {
        String normalized = uppercase(value);
        return switch (normalized) {
            case "LEFT", "SLIGHT_LEFT", "CENTER", "SLIGHT_RIGHT", "RIGHT" -> normalized;
            default -> "CENTER";
        };
    }

    private String normalizeVertical(String value) {
        String normalized = uppercase(value);
        return switch (normalized) {
            case "UP", "CENTER", "DOWN" -> normalized;
            default -> "CENTER";
        };
    }

    private String normalizeDistance(String value) {
        String normalized = uppercase(value);
        return switch (normalized) {
            case "FAR", "MID", "NEAR", "REACHABLE" -> normalized;
            default -> "MID";
        };
    }

    private String normalizeGuidanceLevel(String value) {
        String normalized = uppercase(value);
        return switch (normalized) {
            case "SEARCHING", "ADJUSTING", "NEAR", "REACHABLE" -> normalized;
            default -> "ADJUSTING";
        };
    }

    private String normalizeHaptic(String value, boolean detected, String proximityLevel, String horizontalDirection) {
        String normalized = uppercase(value);
        if ("SHORT".equals(normalized) || "DOUBLE_SHORT".equals(normalized) || "LONG".equals(normalized)) {
            return normalized;
        }
        if (!detected) {
            return "SHORT";
        }
        if ("REACHABLE".equals(proximityLevel)) {
            return "LONG";
        }
        if ("NEAR".equals(proximityLevel)) {
            return "DOUBLE_SHORT";
        }
        if (!"CENTER".equals(horizontalDirection)) {
            return "SHORT";
        }
        return "SHORT";
    }

    private String normalizeGuidanceText(String raw, boolean detected, String horizontalDirection, String verticalDirection, String proximityLevel) {
        String text = raw == null ? "" : raw.trim();
        if (!text.isEmpty()) {
            return text;
        }
        if (!detected) {
            if ("UP".equals(verticalDirection)) {
                return "稍微抬高手机，缓慢左右扫动镜头。";
            }
            if ("DOWN".equals(verticalDirection)) {
                return "稍微压低手机，缓慢左右扫动镜头。";
            }
            return "暂未发现目标，缓慢左右扫动镜头。";
        }
        if ("REACHABLE".equals(proximityLevel)) {
            if ("DOWN".equals(verticalDirection)) {
                return "目标就在前方偏下，已接近，可以伸手拿取。";
            }
            return "目标已接近，可以伸手拿取。";
        }
        if ("NEAR".equals(proximityLevel)) {
            if ("LEFT".equals(horizontalDirection) || "SLIGHT_LEFT".equals(horizontalDirection)) {
                return "手向左一点，目标就在附近。";
            }
            if ("RIGHT".equals(horizontalDirection) || "SLIGHT_RIGHT".equals(horizontalDirection)) {
                return "手向右一点，目标就在附近。";
            }
            if ("DOWN".equals(verticalDirection)) {
                return "目标在前方偏下，慢慢伸手。";
            }
            return "已接近目标，慢慢向前并准备伸手。";
        }
        if ("LEFT".equals(horizontalDirection)) {
            return "向左转30度，再向前走两步。";
        }
        if ("SLIGHT_LEFT".equals(horizontalDirection)) {
            return "稍微向左一点，大约10度。";
        }
        if ("RIGHT".equals(horizontalDirection)) {
            return "向右转30度，再向前走两步。";
        }
        if ("SLIGHT_RIGHT".equals(horizontalDirection)) {
            return "稍微向右一点，大约10度。";
        }
        if ("UP".equals(verticalDirection)) {
            return "稍微抬高手机，继续向前。";
        }
        if ("DOWN".equals(verticalDirection)) {
            return "稍微压低手机，继续向前。";
        }
        return "目标在正前方，向前走两步。";
    }

    private String normalizeMotionHint(String raw, boolean detected, String horizontalDirection, String verticalDirection) {
        String value = raw == null ? "" : raw.trim();
        if (!value.isEmpty()) {
            return value;
        }
        if (!detected) {
            return "SWEEP";
        }
        if ("LEFT".equals(horizontalDirection) || "SLIGHT_LEFT".equals(horizontalDirection)) {
            return "TURN_LEFT";
        }
        if ("RIGHT".equals(horizontalDirection) || "SLIGHT_RIGHT".equals(horizontalDirection)) {
            return "TURN_RIGHT";
        }
        if ("UP".equals(verticalDirection)) {
            return "MOVE_UP";
        }
        if ("DOWN".equals(verticalDirection)) {
            return "MOVE_DOWN";
        }
        return "FORWARD";
    }

    private TargetBox parseTargetBox(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        return new TargetBox(
                readNullableDouble(node.path("centerX")),
                readNullableDouble(node.path("centerY")),
                readNullableDouble(node.path("width")),
                readNullableDouble(node.path("height"))
        );
    }

    private Double readNullableDouble(JsonNode node) {
        return node != null && node.isNumber() ? node.asDouble() : null;
    }

    private int defaultAngle(String horizontalDirection) {
        return switch (horizontalDirection) {
            case "LEFT", "RIGHT" -> 30;
            case "SLIGHT_LEFT", "SLIGHT_RIGHT" -> 12;
            default -> 0;
        };
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private String uppercase(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
