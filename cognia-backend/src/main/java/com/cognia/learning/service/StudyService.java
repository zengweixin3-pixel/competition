package com.cognia.learning.service;

import com.cognia.common.PageResult;
import com.cognia.learning.entity.StudyRecord;
import com.cognia.learning.mapper.StudyRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudyService extends ServiceImpl<StudyRecordMapper, StudyRecord> {

    private final StudyRecordMapper studyRecordMapper;

    public PageResult<StudyRecord> getRecords(Long userId, Long pageNum, Long pageSize) {
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId)
                .orderByDesc(StudyRecord::getStudyDate);

        long total = count(wrapper);
        long offset = (pageNum - 1) * pageSize;
        wrapper.last("LIMIT " + offset + ", " + pageSize);
        List<StudyRecord> records = list(wrapper);

        return PageResult.of(total, records, pageNum, pageSize);
    }

    public List<Map<String, Object>> getStudyTrend(Long userId, String startDate, String endDate) {
        return studyRecordMapper.selectStudyTrend(userId, startDate, endDate);
    }

    public Map<String, Object> getStudyStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        LocalDate monthAgo = today.minusDays(30);

        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId);

        long totalRecords = count(wrapper);
        stats.put("totalRecords", totalRecords);

        List<Map<String, Object>> weekTrend = studyRecordMapper.selectStudyTrend(
                userId, weekAgo.toString(), today.toString());
        stats.put("weekTrend", weekTrend);

        List<Map<String, Object>> subjectDist = studyRecordMapper.selectSubjectDistribution(
                userId, monthAgo.toString(), today.toString());
        stats.put("subjectDistribution", subjectDist);

        return stats;
    }
}
