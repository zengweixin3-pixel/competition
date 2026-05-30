package com.cognia.ai.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AIService {

    public static final String MISTAKE_ANALYSIS_FAILURE = "[AI服务异常] 错题分析暂时不可用，请稍后重试。";
    private static final String COMMON_AI_FAILURE = "AI服务暂时不可用，请稍后重试。";

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

    public enum Agent {
        COACH(
                "学习教练",
                "你是 Cognia 的 AI 学习教练。你的回答要专业、清晰、鼓励式，"
                        + "优先帮助学生理解知识、拆解问题、建立可执行的学习步骤。"
        ),
        ANALYST(
                "错题分析师",
                "你是 Cognia 的错题分析师。你的任务是从错题中定位真实原因，"
                        + "找出概念、方法、审题、计算或习惯层面的根因，并给出具体改进建议。"
        ),
        COMPANION(
                "情绪伙伴",
                "你是 Cognia 的情绪伙伴。你的回答要温和、真诚、有边界感，"
                        + "先理解学生的情绪，再给出能马上执行的小建议。"
        ),
        PLANNER(
                "学习规划师",
                "你是 Cognia 的学习规划师。你的任务是把学生的学习目标拆成清晰可执行的安排，"
                        + "保证计划现实、具体、可落地，并兼顾休息与复盘。"
        );

        public final String displayName;
        public final String systemPrompt;

        Agent(String displayName, String systemPrompt) {
            this.displayName = displayName;
            this.systemPrompt = systemPrompt;
        }
    }

    public String chat(String userMessage, String userDNA, String emotion, String context) {
        return chat(Agent.COACH, userMessage, userDNA, emotion, context);
    }

    public String chat(Agent agent, String userMessage, String userDNA, String emotion, String context) {
        String systemPrompt = buildAgentPrompt(agent, userDNA, emotion);
        return callAI("chat-" + agent.name().toLowerCase(), systemPrompt, context, safeText(userMessage, "请给我一个学习建议。"));
    }

    public String analyzeMistake(String mistakeContent, String subject, String userDNA) {
        if (isBlank(mistakeContent)) {
            return MISTAKE_ANALYSIS_FAILURE;
        }

        StringBuilder taskPrompt = new StringBuilder();
        taskPrompt.append("请分析下面这道错题，并严格按四个小节输出。\n\n");
        taskPrompt.append("学科：").append(safeText(subject, "未分类")).append("\n");
        taskPrompt.append("错题内容：").append(mistakeContent.trim()).append("\n\n");
        taskPrompt.append("输出要求：\n");
        taskPrompt.append("1. 必须包含“错误原因 / 根本问题 / 改进建议 / 推荐资源”四个标题。\n");
        taskPrompt.append("2. 每个标题下给出具体内容，不要只写一句空话。\n");
        taskPrompt.append("3. 改进建议至少写 3 条，尽量能直接执行。\n");
        taskPrompt.append("4. 推荐资源优先给教材章节、练习方向、复盘方法，不要编造外部链接。\n");

        String systemPrompt = buildAgentPrompt(Agent.ANALYST, userDNA, "专注");
        String response = callAI("mistake-analysis", systemPrompt, null, taskPrompt.toString());
        if (isFailureText(response)) {
            return MISTAKE_ANALYSIS_FAILURE;
        }
        return normalizeMistakeAnalysis(response);
    }

    public String analyzeEmotion(String emotionType, String content, String userDNA) {
        StringBuilder taskPrompt = new StringBuilder();
        taskPrompt.append("学生当前情绪：").append(safeText(emotionType, "普通")).append("\n");
        taskPrompt.append("学生描述：").append(safeText(content, "暂时没有额外补充。")).append("\n\n");
        taskPrompt.append("请先共情，再给出 2 到 3 条可以马上执行的建议，语气要温和、简洁。");

        String systemPrompt = buildAgentPrompt(Agent.COMPANION, userDNA, emotionType);
        String response = callAI("emotion-analysis", systemPrompt, null, taskPrompt.toString());
        if (isFailureText(response)) {
            return "我暂时没能连接到 AI 服务，但你的情绪已经保存成功。建议先休息 5 分钟，再继续当前学习任务。";
        }
        return response;
    }

    public String generateLearningPlan(String userDNA, String focus, Double dailyHours, String notes) {
        double resolvedHours = dailyHours == null ? 2.0 : dailyHours;

        StringBuilder taskPrompt = new StringBuilder();
        taskPrompt.append("请为学生生成一份学习计划。\n\n");
        taskPrompt.append("学习重点：").append(safeText(focus, "综合复习")).append("\n");
        taskPrompt.append("每日可用时间：").append(resolvedHours).append(" 小时\n");
        taskPrompt.append("补充说明：").append(safeText(notes, "无")).append("\n\n");
        taskPrompt.append("请输出：\n");
        taskPrompt.append("1. 今日安排\n");
        taskPrompt.append("2. 任务拆解\n");
        taskPrompt.append("3. 复盘建议\n");
        taskPrompt.append("4. 休息提醒\n");

        String systemPrompt = buildAgentPrompt(Agent.PLANNER, userDNA, "专注");
        String response = callAI("generate-plan", systemPrompt, null, taskPrompt.toString());
        if (isFailureText(response)) {
            return "AI服务暂时不可用，请先按“主任务 1 个 + 巩固任务 1 个 + 复盘 1 次”的节奏安排今天的学习。";
        }
        return response;
    }

    public static boolean isFailureText(String text) {
        if (text == null) {
            return true;
        }
        String value = text.trim();
        return value.isEmpty()
                || COMMON_AI_FAILURE.equals(value)
                || value.startsWith("[AI服务异常]");
    }

    private String buildAgentPrompt(Agent agent, String userDNA, String emotion) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(agent.systemPrompt).append("\n\n");
        prompt.append("请始终使用简体中文回答，避免空泛套话，优先给出贴近学习场景的内容。\n");

        if (!isBlank(userDNA)) {
            prompt.append("\n【学生学习画像】\n");
            prompt.append(userDNA.trim()).append("\n");
            appendDnaHints(prompt, userDNA);
        }

        if (!isBlank(emotion) && !"专注".equals(emotion)) {
            prompt.append("\n【当前情绪】\n");
            prompt.append(emotion.trim()).append("\n");
        }

        prompt.append("\n【回答原则】\n");
        switch (agent) {
            case COACH:
                prompt.append("1. 先解释，再给步骤。\n");
                prompt.append("2. 多用短句、分点、类比，帮助学生真正听懂。\n");
                prompt.append("3. 如果问题复杂，拆成更小的学习动作。\n");
                break;
            case ANALYST:
                prompt.append("1. 先指出错在哪里，再分析为什么会错。\n");
                prompt.append("2. 不要只说“粗心”或“多练”，要给出可执行的改进动作。\n");
                prompt.append("3. 输出必须足够具体，便于学生直接复盘。\n");
                break;
            case COMPANION:
                prompt.append("1. 先共情，不要机械安慰。\n");
                prompt.append("2. 建议要轻量、温和、能马上执行。\n");
                prompt.append("3. 避免说教式表达。\n");
                break;
            case PLANNER:
                prompt.append("1. 计划必须可执行，避免过满。\n");
                prompt.append("2. 要兼顾主任务、巩固、复盘和休息。\n");
                prompt.append("3. 输出尽量结构化。\n");
                break;
            default:
                break;
        }

        return prompt.toString();
    }

    private void appendDnaHints(StringBuilder prompt, String userDNA) {
        String dna = userDNA == null ? "" : userDNA;
        if (dna.contains("理解驱动")) {
            prompt.append("- 学生偏向先理解原理，再开始练题。\n");
        }
        if (dna.contains("记忆强化")) {
            prompt.append("- 可以结合归纳总结、卡片记忆和重复回顾。\n");
        }
        if (dna.contains("专注深潜")) {
            prompt.append("- 适合整块时间完成高专注任务。\n");
        }
        if (dna.contains("执行先锋")) {
            prompt.append("- 适合明确里程碑和打勾式任务管理。\n");
        }
        if (dna.contains("从容稳进")) {
            prompt.append("- 需要兼顾节奏稳定和情绪状态。\n");
        }
        if (dna.contains("逻辑推理")) {
            prompt.append("- 适合强调推导过程、因果链和公式来源。\n");
        }
    }

    private String callAI(String scene, String systemPrompt, String context, String userMessage) {
        if (isBlank(apiKey) || isBlank(baseUrl) || isBlank(model)) {
            log.error("AI configuration missing, scene={}, baseUrl={}, modelPresent={}", scene, baseUrl, !isBlank(model));
            return COMMON_AI_FAILURE;
        }

        try {
            List<Map<String, String>> messages = new ArrayList<Map<String, String>>();
            messages.add(createMessage("system", systemPrompt));
            if (!isBlank(context)) {
                messages.add(createMessage("system", "补充上下文：\n" + context.trim()));
            }
            messages.add(createMessage("user", userMessage));

            Map<String, Object> requestBody = new HashMap<String, Object>();
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
                ResponseBody responseBody = response.body();
                String bodyText = responseBody != null ? responseBody.string() : "";

                if (!response.isSuccessful()) {
                    log.error("AI request failed, scene={}, code={}, body={}", scene, response.code(), bodyText);
                    return COMMON_AI_FAILURE;
                }

                String content = extractContent(bodyText);
                if (isBlank(content)) {
                    log.error("AI response empty, scene={}, body={}", scene, bodyText);
                    return COMMON_AI_FAILURE;
                }
                return content.trim();
            }
        } catch (Exception e) {
            log.error("AI request exception, scene={}", scene, e);
            return COMMON_AI_FAILURE;
        }
    }

    private String extractContent(String bodyText) {
        JSONObject json = JSON.parseObject(bodyText);
        if (json == null) {
            return null;
        }

        JSONArray choices = json.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            return null;
        }

        JSONObject firstChoice = choices.getJSONObject(0);
        if (firstChoice == null) {
            return null;
        }

        JSONObject message = firstChoice.getJSONObject("message");
        if (message == null) {
            return null;
        }

        return message.getString("content");
    }

    private String normalizeMistakeAnalysis(String response) {
        String text = response == null ? "" : response.trim();
        if (containsAllSections(text)) {
            return text;
        }

        return "错误原因：\n"
                + text
                + "\n\n根本问题：\n对同类题目的关键判断点还不够稳定，需要继续把知识点和解题步骤对齐。"
                + "\n\n改进建议：\n"
                + "1. 先重新梳理题目对应知识点和公式使用条件。\n"
                + "2. 按“审题 - 列步骤 - 验算/复盘”重新做一遍同类题。\n"
                + "3. 把这道题整理进错题本，隔天再做一次自测。"
                + "\n\n推荐资源：\n"
                + "1. 教材对应章节与课堂笔记。\n"
                + "2. 近三次同类型错题。\n"
                + "3. 同知识点基础题到变式题各 2-3 道。";
    }

    private boolean containsAllSections(String text) {
        return text.contains("错误原因")
                && text.contains("根本问题")
                && text.contains("改进建议")
                && text.contains("推荐资源");
    }

    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<String, String>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String safeText(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
