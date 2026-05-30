package com.cognia.mistake.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cognia.common.PageResult;
import com.cognia.mistake.entity.Mistake;
import com.cognia.mistake.mapper.MistakeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MistakeService extends ServiceImpl<MistakeMapper, Mistake> {

    public static final String ANALYSIS_PENDING_TEXT = "[AI分析中] 正在生成分析，请稍后查看。";

    private final MistakeMapper mistakeMapper;
    private final MistakeAnalysisAsyncService mistakeAnalysisAsyncService;

    public PageResult<Mistake> getMistakes(Long userId, Long pageNum, Long pageSize, String subject, String mistakeType) {
        LambdaQueryWrapper<Mistake> wrapper = new LambdaQueryWrapper<Mistake>();
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
        Map<String, Object> stats = new HashMap<String, Object>();

        LambdaQueryWrapper<Mistake> wrapper = new LambdaQueryWrapper<Mistake>();
        wrapper.eq(Mistake::getUserId, userId);

        long total = count(wrapper);
        stats.put("total", total);

        LambdaQueryWrapper<Mistake> pendingWrapper = new LambdaQueryWrapper<Mistake>();
        pendingWrapper.eq(Mistake::getUserId, userId)
                .eq(Mistake::getStatus, "pending");
        long pending = count(pendingWrapper);
        stats.put("pending", pending);

        LambdaQueryWrapper<Mistake> masteredWrapper = new LambdaQueryWrapper<Mistake>();
        masteredWrapper.eq(Mistake::getUserId, userId)
                .eq(Mistake::getStatus, "mastered");
        long mastered = count(masteredWrapper);
        stats.put("mastered", mastered);

        LambdaQueryWrapper<Mistake> reviewingWrapper = new LambdaQueryWrapper<Mistake>();
        reviewingWrapper.eq(Mistake::getUserId, userId)
                .eq(Mistake::getStatus, "reviewing");
        long reviewing = count(reviewingWrapper);
        stats.put("reviewing", reviewing);

        long improvement = total > 0 ? Math.round(mastered * 100.0f / total) : 0;
        stats.put("improvement", improvement);

        List<Map<String, Object>> typeDist = mistakeMapper.selectMistakeTypeDistribution(userId);
        stats.put("typeDistribution", typeDist);

        List<Map<String, Object>> subjectDist = mistakeMapper.selectSubjectDistribution(userId);
        stats.put("subjectDistribution", subjectDist);

        return stats;
    }

    public Mistake getMistakeDetail(Long mistakeId) {
        return getById(mistakeId);
    }

    public Map<String, Object> submitMistakeAnalysis(Long mistakeId, String userDNA) {
        Mistake mistake = getById(mistakeId);
        if (mistake == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("submitted", true);
        result.put("mistakeId", mistakeId);

        if (isAnalysisPending(mistake.getAiAnalysis())) {
            result.put("status", "processing");
            result.put("message", "该错题正在分析中，请稍后查看结果");
            return result;
        }

        mistake.setAiAnalysis(ANALYSIS_PENDING_TEXT);
        updateById(mistake);

        log.info("AI mistake analysis submitted, mistakeId={}, subject={}", mistakeId, mistake.getSubject());
        mistakeAnalysisAsyncService.analyzeMistakeAsync(mistakeId, userDNA);

        result.put("status", "processing");
        result.put("message", "已提交 AI 分析请求，请稍后查看结果");
        return result;
    }

    public boolean isAnalysisPending(String aiAnalysis) {
        return aiAnalysis != null && aiAnalysis.trim().startsWith("[AI分析中]");
    }
}
