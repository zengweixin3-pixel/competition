package com.cognia.ai.controller;

import com.cognia.ai.agent.AgentContext;
import com.cognia.ai.agent.AgentResult;
import com.cognia.ai.agent.AgentType;
import com.cognia.ai.orchestrator.AgentOrchestratorService;
import com.cognia.ai.service.AIService;
import com.cognia.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AgentOrchestratorService agentOrchestratorService;

    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        String message = getString(request, "message");
        String userDNA = getString(request, "userDNA");
        String emotion = getString(request, "emotion");
        String context = getString(request, "context");
        String source = getString(request, "source");
        String forcedAgentValue = getString(request, "forcedAgent");
        if (forcedAgentValue == null || forcedAgentValue.trim().isEmpty()) {
            forcedAgentValue = getString(request, "agent");
        }

        AgentContext agentContext = AgentContext.builder()
                .message(message)
                .userDNA(userDNA)
                .emotion(emotion)
                .context(context)
                .source(source)
                .forcedAgent(AgentType.fromValue(forcedAgentValue))
                .build();

        AgentResult agentResult = agentOrchestratorService.chat(agentContext);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("response", agentResult.getResponse());
        result.put("agent", agentResult.getAgent() == null ? AgentType.COACH.name().toLowerCase() : agentResult.getAgent().name().toLowerCase());
        result.put("agentLabel", agentResult.getAgentLabel());
        result.put("routeReason", agentResult.getRouteReason());
        result.put("trace", agentResult.getTrace());
        result.put("success", agentResult.isSuccess());
        return Result.success(result);
    }

    @PostMapping("/analyze-mistake")
    public Result<Map<String, String>> analyzeMistake(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        String subject = request.get("subject");
        String userDNA = request.get("userDNA");

        AgentResult agentResult = agentOrchestratorService.executeAgent(
                AgentType.ANALYST,
                AgentContext.builder()
                        .message(content)
                        .subject(subject)
                        .userDNA(userDNA)
                        .source("mistake-analysis")
                        .build()
        );
        String analysis = agentResult.getResponse();
        if (AIService.isFailureText(analysis)) {
            return Result.error("后端AI服务异常，请稍后重试");
        }

        Map<String, String> result = new HashMap<String, String>();
        result.put("analysis", analysis);
        return Result.success(result);
    }

    @PostMapping("/analyze-emotion")
    public Result<Map<String, String>> analyzeEmotion(@RequestBody Map<String, String> request) {
        String emotionType = request.get("emotionType");
        String content = request.get("content");
        String userDNA = request.get("userDNA");

        AgentResult agentResult = agentOrchestratorService.executeAgent(
                AgentType.COMPANION,
                AgentContext.builder()
                        .message(content)
                        .emotion(emotionType)
                        .userDNA(userDNA)
                        .source("emotion-analysis")
                        .build()
        );
        String response = agentResult.getResponse();

        Map<String, String> result = new HashMap<String, String>();
        result.put("response", response);
        return Result.success(result);
    }

    @PostMapping("/generate-plan")
    public Result<Map<String, String>> generatePlan(@RequestBody Map<String, Object> request) {
        String userDNA = (String) request.get("userDNA");
        String focus = (String) request.get("focus");
        Number hours = (Number) request.get("dailyHours");
        Double dailyHours = hours == null ? 2.0 : hours.doubleValue();
        String notes = (String) request.get("notes");

        AgentResult agentResult = agentOrchestratorService.executeAgent(
                AgentType.PLANNER,
                AgentContext.builder()
                        .message(focus)
                        .userDNA(userDNA)
                        .dailyHours(dailyHours)
                        .notes(notes)
                        .source("plan-generation")
                        .build()
        );
        String plan = agentResult.getResponse();

        Map<String, String> result = new HashMap<String, String>();
        result.put("plan", plan);
        return Result.success(result);
    }

    private String getString(Map<String, Object> request, String key) {
        Object value = request.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
