package com.aigc.intelliengine.agent.tools;

import com.aigc.intelliengine.agent.AgentReportService;
import com.aigc.intelliengine.agent.model.entity.AgentReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GenerateReportTool implements AgentTool {

    private final AgentReportService reportService;

    @Override public String name() { return "generate_report"; }

    @Override
    public String description() {
        return "生成Agent分析报告。输入title和type，可选projectId和templateId。所有分析完成后使用此工具汇总结果。";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "title", Map.of("type", "string", "description", "报告标题"),
                "type", Map.of("type", "string", "description", "报告类型: VIDEO_ANALYSIS / DATA_COLLECTION / PROMPT_QUALITY"),
                "templateId", Map.of("type", "integer", "description", "报告模板ID，可选"),
                "projectId", Map.of("type", "integer", "description", "项目ID，可选")
            ),
            "required", java.util.List.of("title", "type")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String title = (String) args.get("title");
        String type = (String) args.get("type");
        Long templateId = args.containsKey("templateId") && args.get("templateId") != null
                ? ((Number) args.get("templateId")).longValue() : null;
        Long projectId = args.containsKey("projectId") && args.get("projectId") != null
                ? ((Number) args.get("projectId")).longValue() : null;

        AgentReport report = reportService.generateReport(title, type, templateId, projectId, 1L);
        return String.format("报告 #%d \"%s\" 已生成，类型: %s", report.getId(), report.getTitle(), report.getType());
    }
}
