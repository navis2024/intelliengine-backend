package com.aigc.intelliengine.agent.collaboration;

import com.aigc.intelliengine.agent.tools.AgentTool;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SupervisorAgent — 任务规划与调度中心.
 *
 * 职责:
 *   1. 接收用户任务 → 分解为子任务
 *   2. 根据工具能力分配Worker
 *   3. 收集Worker结果 → 汇总报告
 *
 * 类比: 团队中的Tech Lead，不自己写代码但负责分配和汇总.
 */
@Slf4j
@Component
public class SupervisorAgent {

    private final AgentBus bus;
    private final List<AgentTool> tools;
    private static final String AGENT_NAME = "SUPERVISOR";

    public SupervisorAgent(AgentBus bus, List<AgentTool> tools) {
        this.bus = bus;
        this.tools = tools;
    }

    @PostConstruct
    public void register() {
        log.info("SupervisorAgent registered — managing {} tools", tools.size());
    }

    /**
     * 规划并分派任务给Worker
     * @return 分派的子任务列表
     */
    public List<AgentMessage> planAndDispatch(String userTask) {
        List<String> toolPlan = planTools(userTask);
        List<AgentMessage> assignments = new ArrayList<>();

        for (int i = 0; i < toolPlan.size(); i++) {
            String toolName = toolPlan.get(i);
            AgentTool tool = tools.stream().filter(t -> t.name().equals(toolName)).findFirst().orElse(null);
            if (tool == null) continue;

            AgentMessage msg = AgentMessage.builder()
                    .fromAgent(AGENT_NAME)
                    .toAgent("WORKER-" + (i + 1))
                    .type(AgentMessage.AgentMessageType.TASK_ASSIGN)
                    .task(toolName)
                    .payload(Map.of(
                            "stepNumber", i + 1,
                            "totalSteps", toolPlan.size(),
                            "toolDescription", tool.description(),
                            "userTask", userTask
                    ))
                    .build();

            bus.send(msg);
            assignments.add(msg);
            log.info("Supervisor dispatched: {} → worker={}", toolName, msg.getToAgent());
        }

        return assignments;
    }

    /** 任务规划 — 根据任务描述决定使用哪些工具 */
    private List<String> planTools(String task) {
        String lower = task.toLowerCase();
        Map<String, Integer> relevance = new LinkedHashMap<>();

        for (AgentTool tool : tools) {
            int score = 0;
            for (String keyword : extractKeywords(tool)) {
                if (lower.contains(keyword)) score++;
            }
            // 关键词命中越多越相关
            if (score > 0) relevance.put(tool.name(), score);
        }

        // 按相关度排序
        return relevance.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Set<String> extractKeywords(AgentTool tool) {
        Set<String> keywords = new HashSet<>();
        for (String word : tool.description().split("[\\p{Punct}\\s]+")) {
            if (word.length() >= 2) keywords.add(word.toLowerCase());
        }
        keywords.add(tool.name().toLowerCase());
        return keywords;
    }

    public String getAgentName() { return AGENT_NAME; }
    public AgentRole getRole() { return AgentRole.SUPERVISOR; }
}
