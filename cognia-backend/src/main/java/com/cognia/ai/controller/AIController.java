package com.cognia.ai.controller;

import com.cognia.ai.agent.AgentContext;
import com.cognia.ai.agent.AgentResult;
import com.cognia.ai.agent.AgentType;
import com.cognia.ai.orchestrator.AgentOrchestratorService;
import com.cognia.ai.service.AIService;
import com.cognia.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AgentOrchestratorService agentOrchestratorService;
    private final AIService aiService;
    private final ExecutorService streamExecutor = Executors.newCachedThreadPool();

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

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody Map<String, Object> request) {
        SseEmitter emitter = new SseEmitter(180000L);
        String message = getString(request, "message");
        String userDNA = getString(request, "userDNA");
        String emotion = getString(request, "emotion");
        String context = getString(request, "context");
        String source = getString(request, "source");
        String forcedAgentValue = getString(request, "forcedAgent");
        if (forcedAgentValue == null || forcedAgentValue.trim().isEmpty()) {
            forcedAgentValue = getString(request, "agent");
        }

        AgentType agentType = AgentType.fromValue(forcedAgentValue);
        AgentContext agentContext = AgentContext.builder()
                .message(message)
                .userDNA(userDNA)
                .emotion(emotion)
                .context(context)
                .source(source)
                .forcedAgent(agentType)
                .build();

        if (agentType == AgentType.AUTO) {
            AgentResult routeResult = agentOrchestratorService.chat(agentContext);
            agentType = routeResult.getAgent() != null ? routeResult.getAgent() : AgentType.COACH;
            try {
                emitter.send(SseEmitter.event().name("meta").data("{\"agent\":\"" + agentType.name().toLowerCase() + "\",\"agentLabel\":\"" + agentType.getLabel() + "\",\"routeReason\":\"" + (routeResult.getRouteReason() != null ? routeResult.getRouteReason() : "") + "\"}"));
            } catch (IOException e) {
                emitter.completeWithError(e);
                return emitter;
            }
        } else {
            try {
                emitter.send(SseEmitter.event().name("meta").data("{\"agent\":\"" + agentType.name().toLowerCase() + "\",\"agentLabel\":\"" + agentType.getLabel() + "\",\"routeReason\":\"手动指定\"}"));
            } catch (IOException e) {
                emitter.completeWithError(e);
                return emitter;
            }
        }

        final AgentType resolvedAgentType = agentType;
        final AIService.Agent aiAgent = mapToAIAgent(resolvedAgentType);
        streamExecutor.execute(() -> {
            aiService.streamChat(aiAgent, message, userDNA, emotion, context,
                    chunk -> {
                        try {
                            emitter.send(SseEmitter.event().name("chunk").data(chunk));
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    },
                    full -> {
                        try {
                            emitter.send(SseEmitter.event().name("done").data(full));
                            emitter.complete();
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    },
                    error -> {
                        try {
                            emitter.send(SseEmitter.event().name("error").data(error.getMessage() != null ? error.getMessage() : "AI服务异常"));
                            emitter.complete();
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    }
            );
        });
        return emitter;
    }

    @PostMapping(value = "/analyze-mistake/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter analyzeMistakeStream(@RequestBody Map<String, String> request) {
        SseEmitter emitter = new SseEmitter(180000L);
        String content = request.get("content");
        String subject = request.get("subject");
        String userDNA = request.get("userDNA");
        streamExecutor.execute(() -> {
            aiService.streamAnalyzeMistake(content, subject, userDNA,
                    chunk -> {
                        try {
                            emitter.send(SseEmitter.event().name("chunk").data(chunk));
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    },
                    full -> {
                        try {
                            emitter.send(SseEmitter.event().name("done").data(full));
                            emitter.complete();
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    },
                    error -> {
                        try {
                            emitter.send(SseEmitter.event().name("error").data(error.getMessage() != null ? error.getMessage() : "AI服务异常"));
                            emitter.complete();
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    }
            );
        });
        return emitter;
    }

    @PostMapping(value = "/analyze-emotion/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter analyzeEmotionStream(@RequestBody Map<String, String> request) {
        SseEmitter emitter = new SseEmitter(180000L);
        String emotionType = request.get("emotionType");
        String content = request.get("content");
        String userDNA = request.get("userDNA");
        streamExecutor.execute(() -> {
            aiService.streamAnalyzeEmotion(emotionType, content, userDNA,
                    chunk -> {
                        try {
                            emitter.send(SseEmitter.event().name("chunk").data(chunk));
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    },
                    full -> {
                        try {
                            emitter.send(SseEmitter.event().name("done").data(full));
                            emitter.complete();
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    },
                    error -> {
                        try {
                            emitter.send(SseEmitter.event().name("error").data(error.getMessage() != null ? error.getMessage() : "AI服务异常"));
                            emitter.complete();
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    }
            );
        });
        return emitter;
    }

    @PostMapping(value = "/generate-plan/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generatePlanStream(@RequestBody Map<String, Object> request) {
        SseEmitter emitter = new SseEmitter(180000L);
        String userDNA = (String) request.get("userDNA");
        String focus = (String) request.get("focus");
        Number hours = (Number) request.get("dailyHours");
        Double dailyHours = hours == null ? 2.0 : hours.doubleValue();
        String notes = (String) request.get("notes");
        streamExecutor.execute(() -> {
            aiService.streamGenerateLearningPlan(userDNA, focus, dailyHours, notes,
                    chunk -> {
                        try {
                            emitter.send(SseEmitter.event().name("chunk").data(chunk));
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    },
                    full -> {
                        try {
                            emitter.send(SseEmitter.event().name("done").data(full));
                            emitter.complete();
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    },
                    error -> {
                        try {
                            emitter.send(SseEmitter.event().name("error").data(error.getMessage() != null ? error.getMessage() : "AI服务异常"));
                            emitter.complete();
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    }
            );
        });
        return emitter;
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

    private AIService.Agent mapToAIAgent(AgentType type) {
        switch (type) {
            case ANALYST: return AIService.Agent.ANALYST;
            case COMPANION: return AIService.Agent.COMPANION;
            case PLANNER: return AIService.Agent.PLANNER;
            default: return AIService.Agent.COACH;
        }
    }
}
