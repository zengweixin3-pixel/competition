package com.cognia.user.service;

import com.cognia.user.entity.User;
import com.cognia.user.entity.LearningDNA;
import com.cognia.user.mapper.UserMapper;
import com.cognia.user.mapper.LearningDNAMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final LearningDNAMapper learningDNAMapper;

    public LearningDNA getLearningDNA(Long userId) {
        LambdaQueryWrapper<LearningDNA> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningDNA::getUserId, userId);
        return learningDNAMapper.selectOne(wrapper);
    }

    public LearningDNA analyzeLearningDNA(Long userId) {
        User user = getById(userId);
        if (user == null) {
            return null;
        }

        LearningDNA dna = new LearningDNA();
        dna.setUserId(userId);
        dna.setDnaType("理解驱动型");
        dna.setSubType("夜间高效型");
        dna.setTags("理解驱动型,夜间高效型,焦虑型学习者");
        dna.setStrengths("理解速度快,图像记忆强,深度思考能力");
        dna.setWeaknesses("容易拖延,长时间学习效率下降,完美主义倾向");
        dna.setSuggestions("25分钟番茄钟,晚间学习最佳,先理解后刷题");
        dna.setUnderstanding(85);
        dna.setMemory(72);
        dna.setFocus(65);
        dna.setExecution(58);
        dna.setEmotion(70);
        dna.setLogic(78);

        learningDNAMapper.insert(dna);
        return dna;
    }
}
