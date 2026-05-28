package com.cognia.ai.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AIService {

    @Value("${ai.bailian.api-key}")
    private String apiKey;

    @Value("${ai.bailian.base-url}")
    private String baseUrl;

    @Value("${ai.bailian.model}")
    private String model;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    public String chat(String userMessage, String userDNA, String emotion, String context) {
        try {
            String systemPrompt = buildSystemPrompt(userDNA, emotion);
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(createMessage("system", systemPrompt));
            if (context != null && !context.isEmpty()) {
                messages.add(createMessage("system", "对话上下文：" + context));
            }
            messages.add(createMessage("user", userMessage));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);

            RequestBody body = RequestBody.create(
                    JSON.toJSONString(requestBody),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(baseUrl + "/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("AI API调用失败: {}", response.body().string());
                    return "抱歉，AI服务暂时不可用，请稍后再试。";
                }
                String responseBody = response.body().string();
                JSONObject json = JSON.parseObject(responseBody);
                return json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            }
        } catch (IOException e) {
            log.error("AI服务调用异常", e);
            return "抱歉，服务出现错误，请稍后再试。";
        }
    }

    public String analyzeMistake(String mistakeContent, String subject, String userDNA) {
        String prompt = String.format(
            "你是一位专业的学习分析师。请分析以下错题，并给出详细的错因分析和改进建议。\n\n" +
            "学生信息：\n" +
            "- 学习人格类型：%s\n" +
            "- 学科：%s\n\n" +
            "错题内容：\n" +
            "%s\n\n" +
            "请按以下格式输出分析结果（使用中文）：\n\n" +
            "【错误原因】\n" +
            "（简明扼要地指出错误原因）\n\n" +
            "【根本问题】\n" +
            "（深入分析导致错误的根本原因）\n\n" +
            "【改进建议】\n" +
            "1. ...\n" +
            "2. ...\n" +
            "3. ...\n\n" +
            "【推荐资源】\n" +
            "- 相关知识点视频/文档推荐\n" +
            "- 练习题目推荐\n",
            userDNA, subject, mistakeContent
        );

        return chat(prompt, userDNA, "专注", null);
    }

    public String analyzeEmotion(String emotionType, String content, String userDNA) {
        String prompt = String.format(
            "你是一位贴心的学习陪伴AI。学生现在的心情是：%s\n\n" +
            "学生描述：%s\n" +
            "学习人格类型：%s\n\n" +
            "请根据学生的情绪状态，给出温暖的回应和适当的建议。\n" +
            "回应要体现AI的温度感，让学生感到被理解和支持。\n",
            emotionType, content, userDNA
        );

        return chat(prompt, userDNA, emotionType, null);
    }

    public String generateLearningPlan(String userDNA, String focus, Double dailyHours, String notes) {
        String prompt = String.format(
            "你是一位专业的学习规划师。请为以下学生生成个性化的学习计划。\n\n" +
            "学生信息：\n" +
            "- 学习人格类型：%s\n" +
            "- 学习重点：%s\n" +
            "- 每日可用时间：%.1f小时\n" +
            "- 特殊需求：%s\n\n" +
            "请生成一份详细的学习计划，包括：\n" +
            "1. 每日学习安排（考虑学生的人格特点）\n" +
            "2. 具体的学习任务和目标\n" +
            "3. 学习方法和技巧建议\n" +
            "4. 休息和调节建议\n",
            userDNA, focus, dailyHours, notes
        );

        return chat(prompt, userDNA, "专注", null);
    }

    private String buildSystemPrompt(String userDNA, String emotion) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的AI学习教练，名叫Cognia。你的任务是帮助学生更好地学习。\n\n");

        if (userDNA != null && !userDNA.isEmpty()) {
            prompt.append("【学生人格特征】\n");
            switch (userDNA) {
                case "理解驱动型":
                    prompt.append("- 擅长深入理解知识，喜欢探索原理\n");
                    prompt.append("- 适合使用图像化、类比的方式讲解\n");
                    prompt.append("- 建议先理解概念再做题\n");
                    prompt.append("- 使用简短句子，避免冗长说明\n");
                    break;
                case "视觉记忆型":
                    prompt.append("- 对图像、图表记忆深刻\n");
                    prompt.append("- 多用视觉化方式呈现知识\n");
                    prompt.append("- 建议使用思维导图、流程图\n");
                    break;
                case "冲刺爆发型":
                    prompt.append("- 适合短时高效学习\n");
                    prompt.append("- 注意力集中但持续时间短\n");
                    prompt.append("- 建议使用番茄工作法\n");
                    break;
                default:
                    prompt.append("- 采用个性化教学方式\n");
                    prompt.append("- 关注学生的学习反馈\n");
            }
            prompt.append("\n");
        }

        if (emotion != null && !emotion.isEmpty() && !"专注".equals(emotion)) {
            prompt.append("【当前情绪状态】\n");
            prompt.append("学生当前情绪：").append(emotion).append("\n");
            switch (emotion) {
                case "焦虑":
                case "有点累":
                    prompt.append("- 请使用鼓励、温和的语气\n");
                    prompt.append("- 建议降低学习难度，缩短任务时长\n");
                    prompt.append("- 多给予正面反馈\n");
                    break;
                case "烦躁":
                    prompt.append("- 请耐心倾听，表示理解\n");
                    prompt.append("- 建议暂停学习，适当休息\n");
                    break;
                default:
                    prompt.append("- 保持友好、支持的语气\n");
            }
            prompt.append("\n");
        }

        prompt.append("【回复原则】\n");
        prompt.append("1. 使用简短、清晰的句子\n");
        prompt.append("2. 适当使用图像类比帮助理解\n");
        prompt.append("3. 给予鼓励和支持\n");
        prompt.append("4. 根据学生人格调整讲解方式\n");
        prompt.append("5. 保持专业性和温度感\n");

        return prompt.toString();
    }

    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }
}
