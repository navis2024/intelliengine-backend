package com.aigc.intelliengine.agent;

import com.aigc.intelliengine.agent.model.entity.AgentReport;
import com.aigc.intelliengine.agent.model.entity.AgentReportTemplate;
import com.aigc.intelliengine.asset.AssetMapper;
import com.aigc.intelliengine.common.exception.BusinessException;
import com.aigc.intelliengine.common.security.MembershipValidator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AgentReportService {

    private final AgentReportMapper reportMapper;
    private final AgentReportTemplateMapper templateMapper;
    private final AgentDataRecordMapper recordMapper;
    private final PromptLibraryMapper promptMapper;
    private final AssetMapper assetMapper;
    private final MembershipValidator validator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public AgentReport generateReport(String title, String type, Long templateId, Long projectId, Long userId) {
        if (projectId != null) validator.requireMembership(projectId, userId);
        AgentReport report = new AgentReport();
        report.setTitle(title);
        report.setType(type);
        report.setTemplateId(templateId);
        report.setProjectId(projectId);
        report.setGeneratedBy(userId);
        report.setGeneratedAt(LocalDateTime.now());
        report.setCreatedAt(LocalDateTime.now());

        Map<String, Object> content = buildReportContent(type, projectId);
        try {
            report.setContent(objectMapper.writeValueAsString(content));
        } catch (Exception e) {
            report.setContent("{}");
        }
        reportMapper.insert(report);
        return report;
    }

    private Map<String, Object> buildReportContent(String type, Long projectId) {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("generatedAt", LocalDateTime.now().toString());
        content.put("reportType", type);

        // Asset statistics
        Map<String, Object> assetStats = new LinkedHashMap<>();
        Long assetCount = assetMapper.selectCount(new LambdaQueryWrapper<>());
        assetStats.put("totalAssets", assetCount);
        // Count by type
        for (String t : List.of("VIDEO", "IMAGE", "AUDIO", "TEMPLATE")) {
            long count = assetMapper.selectCount(
                new LambdaQueryWrapper<com.aigc.intelliengine.asset.model.entity.AssetInfo>()
                    .eq(com.aigc.intelliengine.asset.model.entity.AssetInfo::getType, t));
            assetStats.put(t.toLowerCase() + "Count", count);
        }
        content.put("assetStatistics", assetStats);

        // Prompt library statistics
        Map<String, Object> promptStats = new LinkedHashMap<>();
        Long totalPrompts = promptMapper.selectCount(new LambdaQueryWrapper<>());
        promptStats.put("totalPrompts", totalPrompts);

        // Most used prompts
        var topPrompts = promptMapper.selectPage(
            Page.of(1, 5),
            new LambdaQueryWrapper<com.aigc.intelliengine.agent.model.entity.PromptLibrary>()
                .orderByDesc(com.aigc.intelliengine.agent.model.entity.PromptLibrary::getUseCount))
            .getRecords();
        List<Map<String, Object>> topPromptList = new ArrayList<>();
        for (var p : topPrompts) {
            Map<String, Object> pm = new LinkedHashMap<>();
            pm.put("id", p.getId());
            pm.put("text", p.getPromptText() != null && p.getPromptText().length() > 80
                ? p.getPromptText().substring(0, 80) + "..." : p.getPromptText());
            pm.put("useCount", p.getUseCount());
            pm.put("type", p.getPromptType());
            topPromptList.add(pm);
        }
        promptStats.put("topPrompts", topPromptList);
        content.put("promptStatistics", promptStats);

        // Anomaly detection summary
        Map<String, Object> anomalyStats = new LinkedHashMap<>();
        Long anomalyCount = recordMapper.selectCount(
            new LambdaQueryWrapper<com.aigc.intelliengine.agent.model.entity.AgentDataRecord>()
                .eq(com.aigc.intelliengine.agent.model.entity.AgentDataRecord::getIsAnomaly, 1));
        Long totalRecords = recordMapper.selectCount(new LambdaQueryWrapper<>());
        anomalyStats.put("totalRecords", totalRecords);
        anomalyStats.put("anomalyCount", anomalyCount);
        anomalyStats.put("anomalyRate", totalRecords > 0
            ? String.format("%.2f%%", anomalyCount * 100.0 / totalRecords) : "0%");
        content.put("anomalyStatistics", anomalyStats);

        // Summary
        content.put("summary", String.format(
            "系统总资产 %d 个，Prompt库 %d 条，数据记录 %d 条（异常 %d 条）",
            assetCount, totalPrompts, totalRecords, anomalyCount));

        return content;
    }

    public AgentReport getReport(Long id) {
        AgentReport report = reportMapper.selectById(id);
        if (report == null) throw new BusinessException("报告不存在");
        return report;
    }

    public List<AgentReport> listByProject(Long projectId, int page, int size, Long userId) {
        if (projectId != null) validator.requireMembership(projectId, userId);
        return reportMapper.selectPage(Page.of(page, size),
            new LambdaQueryWrapper<AgentReport>()
                .eq(projectId != null, AgentReport::getProjectId, projectId)
                .eq(projectId == null, AgentReport::getGeneratedBy, userId)
                .orderByDesc(AgentReport::getCreatedAt))
            .getRecords();
    }

    @Transactional
    public void deleteReport(Long id) {
        reportMapper.deleteById(id);
    }

    public List<AgentReportTemplate> listTemplates() {
        return templateMapper.selectList(null);
    }
}
