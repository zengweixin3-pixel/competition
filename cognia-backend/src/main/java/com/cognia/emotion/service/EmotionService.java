package com.cognia.emotion.service;

import com.cognia.emotion.entity.EmotionRecord;
import com.cognia.emotion.mapper.EmotionRecordMapper;
import com.cognia.ai.service.AIService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmotionService extends ServiceImpl<EmotionRecordMapper, EmotionRecord> {

    private final EmotionRecordMapper emotionRecordMapper;
    private final AIService aiService;

    public List<EmotionRecord> getRecords(Long userId, String startDate, String endDate) {
        LambdaQueryWrapper<EmotionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmotionRecord::getUserId, userId)
                .ge(EmotionRecord::getRecordDate, startDate)
                .le(EmotionRecord::getRecordDate, endDate)
                .orderByDesc(EmotionRecord::getRecordDate);
        return list(wrapper);
    }

    public String addRecord(EmotionRecord record) {
        String aiResponse = aiService.analyzeEmotion(
                record.getEmotionLabel(),
                record.getContent(),
                "理解驱动型"
        );
        record.setAiResponse(aiResponse);
        save(record);
        return aiResponse;
    }

    public Map<String, Object> getEmotionStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        LambdaQueryWrapper<EmotionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmotionRecord::getUserId, userId);

        long total = count(wrapper);
        stats.put("totalRecords", total);

        List<Map<String, Object>> distribution = emotionRecordMapper.selectEmotionDistribution(userId);
        stats.put("distribution", distribution);

        return stats;
    }

    public List<Map<String, Object>> getEmotionTrend(Long userId, String startDate, String endDate) {
        return emotionRecordMapper.selectEmotionTrend(userId, startDate, endDate);
    }
}
