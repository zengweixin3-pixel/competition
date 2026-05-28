package com.cognia.user.controller;

import com.cognia.common.Result;
import com.cognia.user.entity.User;
import com.cognia.user.entity.LearningDNA;
import com.cognia.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info/{id}")
    public Result<User> getUserInfo(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PutMapping("/info")
    public Result<Void> updateUserInfo(@RequestBody User user) {
        userService.updateById(user);
        return Result.success();
    }

    @GetMapping("/dna/{userId}")
    public Result<LearningDNA> getLearningDNA(@PathVariable Long userId) {
        return Result.success(userService.getLearningDNA(userId));
    }

    @PostMapping("/dna/analyze")
    public Result<LearningDNA> analyzeLearningDNA(@RequestParam Long userId) {
        return Result.success(userService.analyzeLearningDNA(userId));
    }
}
