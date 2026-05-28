package com.cognia.ai.controller;

import com.cognia.common.Result;
import com.cognia.ai.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping("/chat")
    public Result<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String userDNA = request.get("userDNA");
        String emotion = request.get("emotion");
        String context = request.get("context");

        String response = aiService.chat(message, userDNA, emotion, context);

        Map<String, String> result = new HashMap<>();
        result.put("response", response);
        return Result.success(result);
    }

    @PostMapping("/analyze-mistake")
    public Result<Map<String, String>> analyzeMistake(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        String subject = request.get("subject");
        String userDNA = request.get("userDNA");

        String analysis = aiService.analyzeMistake(content, subject, userDNA);

        Map<String, String> result = new HashMap<>();
        result.put("analysis", analysis);
        return Result.success(result);
    }

    @PostMapping("/analyze-emotion")
    public Result<Map<String, String>> analyzeEmotion(@RequestBody Map<String, String> request) {
        String emotionType = request.get("emotionType");
        String content = request.get("content");
        String userDNA = request.get("userDNA");

        String response = aiService.analyzeEmotion(emotionType, content, userDNA);

        Map<String, String> result = new HashMap<>();
        result.put("response", response);
        return Result.success(result);
    }

    @PostMapping("/generate-plan")
    public Result<Map<String, String>> generatePlan(@RequestBody Map<String, Object> request) {
        String userDNA = (String) request.get("userDNA");
        String focus = (String) request.get("focus");
        Double dailyHours = ((Number) request.get("dailyHours")).doubleValue();
        String notes = (String) request.get("notes");

        String plan = aiService.generateLearningPlan(userDNA, focus, dailyHours, notes);

        Map<String, String> result = new HashMap<>();
        result.put("plan", plan);
        return Result.success(result);
    }
}
