package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@Service
public class VisionAnalyzeService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${vision.qwen.base-url:https://dashscope.aliyuncs.com}")
    private String qwenBaseUrl;

    @Value("${vision.qwen.api-key:}")
    private String apiKey;

    @Value("${vision.qwen.vl-model:qwen-vl-plus-latest}")
    private String vlModel;

    public VisionAnalyzeResponse analyze(MultipartFile image, String command) {
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

            return new VisionAnalyzeResponse(
                    normalizedCommand,
                    LocalDateTime.now(),
                    scene,
                    "已按“场景 > 方位 > 核心物品”完成播报。",
                    items,
                    narration
            );
        } catch (Exception ex) {
            throw new IllegalStateException("视觉识别失败: " + ex.getMessage());
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
}
