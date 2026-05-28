package com.cognia.emotion.controller;

import com.cognia.common.Result;
import com.cognia.emotion.entity.EmotionRecord;
import com.cognia.emotion.service.EmotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emotion")
@RequiredArgsConstructor
public class EmotionController {

    private final EmotionService emotionService;

    @GetMapping("/records/{userId}")
    public Result<List<EmotionRecord>> getRecords(
            @PathVariable Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return Result.success(emotionService.getRecords(userId, startDate, endDate));
    }

    @PostMapping("/record")
    public Result<String> addRecord(@RequestBody EmotionRecord record) {
        String aiResponse = emotionService.addRecord(record);
        return Result.success(aiResponse);
    }

    @GetMapping("/stats/{userId}")
    public Result<Map<String, Object>> getEmotionStats(@PathVariable Long userId) {
        return Result.success(emotionService.getEmotionStats(userId));
    }

    @GetMapping("/trend/{userId}")
    public Result<List<Map<String, Object>>> getEmotionTrend(
            @PathVariable Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return Result.success(emotionService.getEmotionTrend(userId, startDate, endDate));
    }
}
