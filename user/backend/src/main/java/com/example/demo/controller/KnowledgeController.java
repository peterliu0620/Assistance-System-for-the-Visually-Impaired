package com.example.demo.controller;

import com.example.demo.dto.KnowledgeAskRequest;
import com.example.demo.dto.KnowledgeAskResponse;
import com.example.demo.dto.KnowledgeRecordListResponse;
import com.example.demo.service.KnowledgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/knowledge")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @GetMapping("/records")
    public KnowledgeRecordListResponse listRecords(@RequestParam Long userId,
                                                   @RequestParam(defaultValue = "10") Integer limit) {
        return knowledgeService.listRecords(userId, limit == null ? 10 : limit);
    }

    @PostMapping("/ask")
    public KnowledgeAskResponse ask(@Valid @RequestBody KnowledgeAskRequest request) {
        return knowledgeService.ask(request.getUserId(), request.getQuestion(), request.getSessionId());
    }
}
