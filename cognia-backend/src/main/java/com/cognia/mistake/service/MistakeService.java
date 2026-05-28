package com.cognia.mistake.service;

import com.cognia.common.PageResult;
import com.cognia.mistake.entity.Mistake;
import com.cognia.mistake.mapper.MistakeMapper;
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
public class MistakeService extends ServiceImpl<MistakeMapper, Mistake> {

    private final MistakeMapper mistakeMapper;
    private final AIService aiService;

    public PageResult<Mistake> getMistakes(Long userId, Long pageNum, Long pageSize, String subject, String mistakeType) {
        LambdaQueryWrapper<Mistake> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Mistake::getUserId, userId);

        if (subject != null && !subject.isEmpty()) {
            wrapper.eq(Mistake::getSubject, subject);
        }
        if (mistakeType != null && !mistakeType.isEmpty()) {
            wrapper.eq(Mistake::getMistakeType, mistakeType);
        }

        wrapper.orderByDesc(Mistake::getCreateTime);

        long total = count(wrapper);
        long offset = (pageNum - 1) * pageSize;
        wrapper.last("LIMIT " + offset + ", " + pageSize);
        List<Mistake> records = list(wrapper);

        return PageResult.of(total, records, pageNum, pageSize);
    }

    public Map<String, Object> getMistakeStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        LambdaQueryWrapper<Mistake> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Mistake::getUserId, userId);

        long total = count(wrapper);
        stats.put("total", total);

        LambdaQueryWrapper<Mistake> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Mistake::getUserId, userId)
                .eq(Mistake::getStatus, "pending");
        long pending = count(pendingWrapper);
        stats.put("pending", pending);

        LambdaQueryWrapper<Mistake> masteredWrapper = new LambdaQueryWrapper<>();
        masteredWrapper.eq(Mistake::getUserId, userId)
                .eq(Mistake::getStatus, "mastered");
        long mastered = count(masteredWrapper);
        stats.put("mastered", mastered);

        List<Map<String, Object>> typeDist = mistakeMapper.selectMistakeTypeDistribution(userId);
        stats.put("typeDistribution", typeDist);

        List<Map<String, Object>> subjectDist = mistakeMapper.selectSubjectDistribution(userId);
        stats.put("subjectDistribution", subjectDist);

        return stats;
    }

    public String analyzeMistake(Long mistakeId, String userDNA) {
        Mistake mistake = getById(mistakeId);
        if (mistake == null) {
            return "错题不存在";
        }

        String analysis = aiService.analyzeMistake(
                mistake.getContent(),
                mistake.getSubject(),
                userDNA
        );

        mistake.setAiAnalysis(analysis);
        updateById(mistake);

        return analysis;
    }
}
