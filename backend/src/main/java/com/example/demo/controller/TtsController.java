package com.example.demo.controller;

import com.example.demo.dto.TtsRequest;
import com.example.demo.dto.TtsResponse;
import com.example.demo.service.QwenTtsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TtsController {

    private final QwenTtsService qwenTtsService;

    @PostMapping("/voice/tts")
    public TtsResponse synthesize(@RequestBody TtsRequest request, HttpServletRequest httpServletRequest) {
        String audioId = qwenTtsService.synthesizeAndCache(request.getText());
        String base = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName() + ":" + httpServletRequest.getServerPort();
        String audioUrl = base + "/api/voice/tts/audio/" + audioId;
        return new TtsResponse(audioId, audioUrl, qwenTtsService.getExpiresInSeconds());
    }

    @GetMapping("/voice/tts/audio/{audioId}")
    public ResponseEntity<byte[]> getAudio(@PathVariable String audioId) {
        byte[] bytes = qwenTtsService.getAudioContent(audioId);
        String contentType = qwenTtsService.getAudioContentType(audioId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(MediaType.parseMediaType(contentType))
                .body(bytes);
    }
}
