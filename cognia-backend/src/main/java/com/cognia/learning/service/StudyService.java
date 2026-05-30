package com.cognia.learning.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cognia.common.PageResult;
import com.cognia.learning.entity.StudyRecord;
import com.cognia.learning.mapper.StudyRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
                .orderByDesc(StudyRecord::getStudyDate)
                .orderByDesc(StudyRecord::getStartTime)
                .orderByDesc(StudyRecord::getCreateTime);

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
        LocalDate yesterday = today.minusDays(1);
        LocalDate weekAgo = today.minusDays(7);
        LocalDate monthAgo = today.minusDays(30);

        List<StudyRecord> todayRecords = listByDateRange(userId, today, today);
        List<StudyRecord> yesterdayRecords = listByDateRange(userId, yesterday, yesterday);
        List<StudyRecord> monthRecords = listByDateRange(userId, monthAgo, today);
        List<StudyRecord> lastMonthRecords = listByDateRange(userId, monthAgo.minusDays(30), monthAgo.minusDays(1));

        double todayStudyMinutes = sumDuration(todayRecords, true);
        double yesterdayStudyMinutes = sumDuration(yesterdayRecords, true);
        double monthStudyMinutes = sumDuration(monthRecords, true);
        double lastMonthStudyMinutes = sumDuration(lastMonthRecords, true);

        int todayCompleted = countCompleted(todayRecords);
        int todayTotalTasks = todayRecords.size();
        int monthCompletedTasks = countCompleted(monthRecords);

        int focusScore = averageFocus(todayRecords);
        int accuracy = averageScore(todayRecords);
        int monthAccuracy = averageScore(monthRecords);

        int studyTimeChange = percentageChange(todayStudyMinutes, yesterdayStudyMinutes);
        int hoursChange = percentageChange(monthStudyMinutes, lastMonthStudyMinutes);

        long activeDays = monthRecords.stream()
                .filter(this::isCompletedRecord)
                .map(StudyRecord::getStudyDate)
                .filter(date -> date != null)
                .distinct()
                .count();
        double avgDailyHours = activeDays > 0 ? (monthStudyMinutes / 60.0) / activeDays : 0;

        int streak = calculateStreak(userId, today);

        stats.put("studyTime", roundHours(todayStudyMinutes));
        stats.put("studyTimeChange", studyTimeChange);
        stats.put("focusScore", focusScore);
        stats.put("focusScoreChange", 0);
        stats.put("completedTasks", todayCompleted);
        stats.put("totalTasks", todayTotalTasks);
        stats.put("tasksChange", 0);
        stats.put("accuracy", accuracy);
        stats.put("accuracyChange", 0);

        stats.put("totalHours", roundHours(monthStudyMinutes));
        stats.put("hoursChange", hoursChange);
        stats.put("avgDaily", Math.round(avgDailyHours * 10.0) / 10.0);
        stats.put("dailyChange", 0);
        stats.put("reportCompletedTasks", monthCompletedTasks);
        stats.put("reportTasksChange", 0);
        stats.put("monthAccuracy", monthAccuracy);
        stats.put("streak", streak);

        List<Map<String, Object>> weekTrend = studyRecordMapper.selectStudyTrend(
                userId, weekAgo.toString(), today.toString());
        stats.put("weekTrend", weekTrend);

        List<Map<String, Object>> subjectDist = studyRecordMapper.selectSubjectDistribution(
                userId, monthAgo.toString(), today.toString());
        stats.put("subjectDistribution", subjectDist);

        stats.put("taskTrend", buildTaskTrend(monthRecords));
        stats.put("timeDistribution", buildTimeDistribution(monthRecords));

        return stats;
    }

    private List<StudyRecord> listByDateRange(Long userId, LocalDate start, LocalDate end) {
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId)
                .ge(StudyRecord::getStudyDate, start)
                .le(StudyRecord::getStudyDate, end)
                .orderByAsc(StudyRecord::getStudyDate)
                .orderByAsc(StudyRecord::getStartTime)
                .orderByAsc(StudyRecord::getCreateTime);
        return list(wrapper);
    }

    private boolean isCompletedRecord(StudyRecord record) {
        return record.getEndTime() != null;
    }

    private double sumDuration(List<StudyRecord> records, boolean completedOnly) {
        double total = 0;
        for (StudyRecord record : records) {
            if (completedOnly && !isCompletedRecord(record)) {
                continue;
            }
            total += record.getDuration() != null ? record.getDuration() : 0;
        }
        return total;
    }

    private int countCompleted(List<StudyRecord> records) {
        int total = 0;
        for (StudyRecord record : records) {
            if (isCompletedRecord(record)) {
                total++;
            }
        }
        return total;
    }

    private int averageFocus(List<StudyRecord> records) {
        int total = 0;
        int count = 0;
        for (StudyRecord record : records) {
            if (!isCompletedRecord(record) || record.getFocusLevel() == null) {
                continue;
            }
            total += record.getFocusLevel();
            count++;
        }
        return count > 0 ? total / count : 0;
    }

    private int averageScore(List<StudyRecord> records) {
        int total = 0;
        int count = 0;
        for (StudyRecord record : records) {
            if (!isCompletedRecord(record) || record.getScore() == null) {
                continue;
            }
            total += record.getScore();
            count++;
        }
        return count > 0 ? total / count : 0;
    }

    private int percentageChange(double current, double previous) {
        if (previous <= 0) {
            return 0;
        }
        return (int) Math.round(((current - previous) / previous) * 100);
    }

    private double roundHours(double minutes) {
        return Math.round(minutes / 60.0 * 10.0) / 10.0;
    }

    private int calculateStreak(Long userId, LocalDate today) {
        int streak = 0;
        LocalDate checkDate = today;
        while (true) {
            LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(StudyRecord::getUserId, userId)
                    .eq(StudyRecord::getStudyDate, checkDate)
                    .isNotNull(StudyRecord::getEndTime);
            long count = count(wrapper);
            if (count > 0) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else if (checkDate.equals(today)) {
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    private List<Map<String, Object>> buildTaskTrend(List<StudyRecord> records) {
        Map<LocalDate, int[]> grouped = new HashMap<>();
        for (StudyRecord record : records) {
            LocalDate studyDate = record.getStudyDate();
            if (studyDate == null) {
                continue;
            }
            int[] counts = grouped.computeIfAbsent(studyDate, key -> new int[]{0, 0});
            counts[0]++;
            if (isCompletedRecord(record)) {
                counts[1]++;
            }
        }

        List<LocalDate> dates = new ArrayList<>(grouped.keySet());
        dates.sort(LocalDate::compareTo);

        List<Map<String, Object>> trend = new ArrayList<>();
        for (LocalDate date : dates) {
            int[] counts = grouped.get(date);
            Map<String, Object> row = new HashMap<>();
            row.put("date", date.toString());
            row.put("total", counts[0]);
            row.put("completed", counts[1]);
            row.put("pending", Math.max(counts[0] - counts[1], 0));
            trend.add(row);
        }
        return trend;
    }

    private List<Map<String, Object>> buildTimeDistribution(List<StudyRecord> records) {
        Map<String, Integer> buckets = new HashMap<>();
        buckets.put("morning", 0);
        buckets.put("afternoon", 0);
        buckets.put("evening", 0);
        buckets.put("night", 0);

        for (StudyRecord record : records) {
            if (!isCompletedRecord(record)) {
                continue;
            }
            int duration = record.getDuration() != null ? record.getDuration() : 0;
            LocalDateTime startTime = record.getStartTime();
            int hour = startTime != null ? startTime.getHour() : 20;
            if (hour < 6) {
                buckets.put("night", buckets.get("night") + duration);
            } else if (hour < 12) {
                buckets.put("morning", buckets.get("morning") + duration);
            } else if (hour < 18) {
                buckets.put("afternoon", buckets.get("afternoon") + duration);
            } else {
                buckets.put("evening", buckets.get("evening") + duration);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : buckets.entrySet()) {
            Map<String, Object> row = new HashMap<>();
            row.put("bucket", entry.getKey());
            row.put("duration", entry.getValue());
            result.add(row);
        }
        return result;
    }
}
