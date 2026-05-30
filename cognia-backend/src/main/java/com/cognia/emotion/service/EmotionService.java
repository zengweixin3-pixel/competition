package com.cognia.emotion.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cognia.ai.service.AIService;
import com.cognia.emotion.entity.EmotionRecord;
import com.cognia.emotion.mapper.EmotionRecordMapper;
import com.cognia.user.entity.User;
import com.cognia.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmotionService extends ServiceImpl<EmotionRecordMapper, EmotionRecord> {

    private final EmotionRecordMapper emotionRecordMapper;
    private final AIService aiService;
    private final UserMapper userMapper;

    public List<EmotionRecord> getRecords(Long userId, String startDate, String endDate) {
        LambdaQueryWrapper<EmotionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmotionRecord::getUserId, userId)
                .ge(EmotionRecord::getRecordDate, startDate)
                .le(EmotionRecord::getRecordDate, endDate)
                .orderByDesc(EmotionRecord::getRecordDate)
                .orderByDesc(EmotionRecord::getCreateTime);
        return list(wrapper);
    }

    public String addRecord(EmotionRecord record) {
        String aiResponse = aiService.analyzeEmotion(
                record.getEmotionLabel(),
                record.getContent(),
                "emotion-aware"
        );
        if (record.getRecordDate() == null) {
            record.setRecordDate(LocalDate.now());
        }
        if (record.getEmotionScore() == null) {
            record.setEmotionScore(resolveEmotionScore(record.getEmotionType()));
        }
        record.setAiResponse(aiResponse);
        save(record);

        if (record.getUserId() != null) {
            User user = userMapper.selectById(record.getUserId());
            if (user != null) {
                user.setEmotionState(record.getEmotionType());
                userMapper.updateById(user);
            }
        }

        return aiResponse;
    }

    public Map<String, Object> getEmotionStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        LambdaQueryWrapper<EmotionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmotionRecord::getUserId, userId);

        long total = count(wrapper);

        LambdaQueryWrapper<EmotionRecord> positiveWrapper = new LambdaQueryWrapper<>();
        positiveWrapper.eq(EmotionRecord::getUserId, userId)
                .in(EmotionRecord::getEmotionType, "great", "good");
        long positive = count(positiveWrapper);

        LambdaQueryWrapper<EmotionRecord> neutralWrapper = new LambdaQueryWrapper<>();
        neutralWrapper.eq(EmotionRecord::getUserId, userId)
                .eq(EmotionRecord::getEmotionType, "normal");
        long neutral = count(neutralWrapper);

        LambdaQueryWrapper<EmotionRecord> negativeWrapper = new LambdaQueryWrapper<>();
        negativeWrapper.eq(EmotionRecord::getUserId, userId)
                .in(EmotionRecord::getEmotionType, "tired", "frustrated");
        long negative = count(negativeWrapper);

        long stability = total > 0 ? (positive * 100 / total) : 0;

        stats.put("totalRecords", total);
        stats.put("positive", positive);
        stats.put("neutral", neutral);
        stats.put("negative", negative);
        stats.put("stability", stability);

        List<Map<String, Object>> distribution = emotionRecordMapper.selectEmotionDistribution(userId);
        stats.put("distribution", distribution);

        return stats;
    }

    public List<Map<String, Object>> getEmotionTrend(Long userId, String startDate, String endDate) {
        return emotionRecordMapper.selectEmotionTrend(userId, startDate, endDate);
    }

    private Integer resolveEmotionScore(String emotionType) {
        if (emotionType == null) {
            return 3;
        }
        switch (emotionType) {
            case "great":
                return 5;
            case "good":
                return 4;
            case "normal":
                return 3;
            case "tired":
                return 2;
            case "frustrated":
                return 1;
            default:
                return 3;
        }
    }
}
