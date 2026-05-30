package com.cognia.mistake.controller;

import com.cognia.common.PageResult;
import com.cognia.common.Result;
import com.cognia.mistake.entity.Mistake;
import com.cognia.mistake.service.MistakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/mistake")
@RequiredArgsConstructor
public class MistakeController {

    private final MistakeService mistakeService;

    @GetMapping("/list/{userId}")
    public Result<PageResult<Mistake>> getMistakes(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String mistakeType) {
        return Result.success(mistakeService.getMistakes(userId, pageNum, pageSize, subject, mistakeType));
    }

    @PostMapping("/add")
    public Result<Void> addMistake(@RequestBody Mistake mistake) {
        mistakeService.save(mistake);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> updateMistake(@RequestBody Mistake mistake) {
        mistakeService.updateById(mistake);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteMistake(@PathVariable Long id) {
        mistakeService.removeById(id);
        return Result.success();
    }

    @GetMapping("/stats/{userId}")
    public Result<Map<String, Object>> getMistakeStats(@PathVariable Long userId) {
        return Result.success(mistakeService.getMistakeStats(userId));
    }

    @GetMapping("/detail/{id}")
    public Result<Mistake> getMistakeDetail(@PathVariable Long id) {
        Mistake mistake = mistakeService.getMistakeDetail(id);
        if (mistake == null) {
            return Result.error(404, "错题不存在");
        }
        return Result.success(mistake);
    }

    @PostMapping("/analyze/{id}")
    public Result<Map<String, Object>> analyzeMistake(@PathVariable Long id, @RequestParam String userDNA) {
        Map<String, Object> result = mistakeService.submitMistakeAnalysis(id, userDNA);
        if (result == null) {
            return Result.error(404, "错题不存在");
        }
        return Result.success(result);
    }
}
