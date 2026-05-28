package com.cognia.learning.controller;

import com.cognia.common.Result;
import com.cognia.common.PageResult;
import com.cognia.learning.entity.StudyRecord;
import com.cognia.learning.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    @GetMapping("/records/{userId}")
    public Result<PageResult<StudyRecord>> getRecords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {
        return Result.success(studyService.getRecords(userId, pageNum, pageSize));
    }

    @PostMapping("/record")
    public Result<Void> addRecord(@RequestBody StudyRecord record) {
        studyService.save(record);
        return Result.success();
    }

    @GetMapping("/trend/{userId}")
    public Result<List<Map<String, Object>>> getStudyTrend(
            @PathVariable Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return Result.success(studyService.getStudyTrend(userId, startDate, endDate));
    }

    @GetMapping("/stats/{userId}")
    public Result<Map<String, Object>> getStudyStats(@PathVariable Long userId) {
        return Result.success(studyService.getStudyStats(userId));
    }
}
