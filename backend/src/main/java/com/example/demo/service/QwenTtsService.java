package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QwenTtsService {

    private static final Duration AUDIO_TTL = Duration.ofMinutes(10);

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CachedAudio> audioCache = new ConcurrentHashMap<>();

    @Value("${vision.qwen.base-url:https://dashscope.aliyuncs.com}")
    private String qwenBaseUrl;

    @Value("${vision.qwen.api-key:}")
    private String apiKey;

    @Value("${vision.qwen.tts-model:qwen-tts}")
    private String ttsModel;

    @Value("${vision.qwen.tts-voice:Cherry}")
    private String ttsVoice;

    @Value("${vision.qwen.tts-format:mp3}")
    private String ttsFormat;

    public String synthesizeAndCache(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("播报文本不能为空。");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("未配置 Qwen API Key，请检查 application.properties。");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-DashScope-SSE", "disable");

        Map<String, Object> body = new HashMap<>();
        body.put("model", ttsModel);
        body.put("input", Map.of("text", text, "voice", ttsVoice));
        body.put("parameters", Map.of("format", ttsFormat));

        String endpoint = normalizeBaseUrl(qwenBaseUrl) + "/api/v1/services/aigc/multimodal-generation/generation";
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("Qwen TTS 合成失败，请稍后重试。");
            }
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode urlNode = root.path("output").path("audio").path("url");
            if (urlNode.isMissingNode() || urlNode.asText().isBlank()) {
                throw new IllegalStateException("Qwen TTS 返回缺少 audio.url。");
            }
            String remoteUrl = urlNode.asText();
            DownloadedAudio downloadedAudio = downloadAudio(remoteUrl);

            String audioId = UUID.randomUUID().toString().replace("-", "");
            audioCache.put(audioId, new CachedAudio(downloadedAudio.bytes, downloadedAudio.contentType, Instant.now().plus(AUDIO_TTL)));
            cleanupExpired();
            return audioId;
        } catch (HttpStatusCodeException ex) {
            String detail = ex.getResponseBodyAsString();
            throw new IllegalStateException("Qwen TTS 请求失败: HTTP " + ex.getStatusCode().value() + " " + detail);
        } catch (Exception ex) {
            throw new IllegalStateException("Qwen TTS 解析失败: " + ex.getMessage());
        }
    }

    public byte[] getAudioContent(String audioId) {
        cleanupExpired();
        CachedAudio cachedAudio = audioCache.get(audioId);
        if (cachedAudio == null || Instant.now().isAfter(cachedAudio.getExpiresAt())) {
            audioCache.remove(audioId);
            throw new IllegalArgumentException("音频不存在或已过期，请重新请求播报。");
        }
        return cachedAudio.getBytes();
    }

    public String getAudioContentType(String audioId) {
        CachedAudio cachedAudio = audioCache.get(audioId);
        if (cachedAudio == null) {
            throw new IllegalArgumentException("音频不存在或已过期，请重新请求播报。");
        }
        return cachedAudio.getContentType();
    }

    public long getExpiresInSeconds() {
        return AUDIO_TTL.getSeconds();
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        audioCache.entrySet().removeIf(entry -> now.isAfter(entry.getValue().getExpiresAt()));
    }

    private String detectAudioContentType(byte[] bytes) {
        if (bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F') {
            return "audio/wav";
        }
        return "audio/mpeg";
    }

    private DownloadedAudio downloadAudio(String remoteUrl) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(remoteUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(20000);
            connection.setInstanceFollowRedirects(true);

            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
                String errorBody = readAll(connection.getErrorStream());
                throw new IllegalStateException("云端音频下载失败: HTTP " + code + " " + errorBody);
            }

            byte[] bytes = readAllBytes(connection.getInputStream());
            if (bytes.length == 0) {
                throw new IllegalStateException("云端音频下载失败: 内容为空。");
            }
            String contentType = connection.getContentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = detectAudioContentType(bytes);
            }
            return new DownloadedAudio(bytes, contentType);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private byte[] readAllBytes(InputStream inputStream) throws Exception {
        try (InputStream in = inputStream; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            return out.toByteArray();
        }
    }

    private String readAll(InputStream inputStream) {
        if (inputStream == null) {
            return "";
        }
        try {
            return new String(readAllBytes(inputStream));
        } catch (Exception ex) {
            return "";
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    @Getter
    private static class CachedAudio {
        private final byte[] bytes;
        private final String contentType;
        private final Instant expiresAt;

        private CachedAudio(byte[] bytes, String contentType, Instant expiresAt) {
            this.bytes = bytes;
            this.contentType = contentType;
            this.expiresAt = expiresAt;
        }
    }

    private static class DownloadedAudio {
        private final byte[] bytes;
        private final String contentType;

        private DownloadedAudio(byte[] bytes, String contentType) {
            this.bytes = bytes;
            this.contentType = contentType;
        }
    }
}
