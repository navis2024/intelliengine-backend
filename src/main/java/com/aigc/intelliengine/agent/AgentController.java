package com.aigc.intelliengine.agent;

import com.aigc.intelliengine.agent.model.dto.AiVideoCreateRequest;
import com.aigc.intelliengine.agent.model.dto.PromptCreateRequest;
import com.aigc.intelliengine.agent.model.entity.AgentDataRecord;
import com.aigc.intelliengine.agent.model.entity.AgentDataTask;
import com.aigc.intelliengine.agent.model.entity.AgentReport;
import com.aigc.intelliengine.agent.model.entity.AgentReportTemplate;
import com.aigc.intelliengine.agent.model.entity.AssetAiVideo;
import com.aigc.intelliengine.agent.model.entity.PromptLibrary;
import com.aigc.intelliengine.agent.model.entity.VideoFrame;
import com.aigc.intelliengine.agent.model.vo.*;
import com.aigc.intelliengine.agent.collaboration.AgentBus;
import com.aigc.intelliengine.agent.collaboration.AgentMessage;
import com.aigc.intelliengine.agent.collaboration.WorkflowEngine;
import com.aigc.intelliengine.agent.rag.RagEvaluator;
import com.aigc.intelliengine.agent.tools.AgentTool;
import com.aigc.intelliengine.common.model.ApiResponse;
import com.aigc.intelliengine.common.security.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/agent")
@RequiredArgsConstructor
@Tag(name = "Agent", description = "AI智能体 - Prompt分析、Prompt库、AI视频元数据")
public class AgentController {

    private final PromptAnalysisService promptAnalysisService;
    private final PromptLibraryService promptLibraryService;
    private final AiVideoService aiVideoService;
    private final AgentTaskService agentTaskService;
    private final AgentReportService agentReportService;
    private final AgentOrchestrator orchestrator;
    private final List<AgentTool> tools;
    private final WorkflowEngine workflowEngine;
    private final AgentBus agentBus;
    private final RagEvaluator ragEvaluator;

    @PostMapping("/frames/{frameId}/analyze")
    @Operation(summary = "分析帧提示词")
    public ApiResponse<FrameAnalysisVO> analyzeFrame(@PathVariable Long frameId) {
        VideoFrame frame = aiVideoService.findFrameById(frameId);
        if (frame == null) return ApiResponse.error("帧不存在");
        AnalysisResult result = promptAnalysisService.analyzeFrame(frame);
        FrameAnalysisVO vo = new FrameAnalysisVO();
        vo.setFrameId(String.valueOf(result.getFrameId()));
        vo.setFrameNumber(result.getFrameNumber());
        vo.setOriginalPrompt(result.getOriginalPrompt());
        vo.setAnalyzedPrompt(result.getAnalyzedPrompt());
        vo.setSuggestedTags(result.getSuggestedTags());
        vo.setConfidence(result.getConfidence());
        vo.setModel(result.getModel());
        vo.setAnalysisTime(result.getAnalysisTime());
        return ApiResponse.success(vo);
    }

    @PostMapping("/prompts")
    @Operation(summary = "保存Prompt到库")
    public ApiResponse<PromptVO> createPrompt(@Valid @RequestBody PromptCreateRequest request) {
        PromptLibrary entity = new PromptLibrary();
        entity.setPromptText(request.getPromptText());
        entity.setPromptType(request.getPromptType());
        entity.setStyleTags(request.getStyleTags());
        entity.setSourceVideoId(request.getSourceVideoId());
        entity.setSourceFrameId(request.getSourceFrameId());
        entity.setCreatedBy(UserContextHolder.getCurrentUserId());
        return ApiResponse.success(toPromptVO(promptLibraryService.create(entity)));
    }

    @GetMapping("/prompts/{id}")
    @Operation(summary = "获取Prompt详情")
    public ApiResponse<PromptVO> getPrompt(@PathVariable Long id) {
        PromptLibrary prompt = promptLibraryService.findById(id);
        if (prompt == null) return ApiResponse.error("Prompt不存在");
        return ApiResponse.success(toPromptVO(prompt));
    }

    @GetMapping("/prompts")
    @Operation(summary = "搜索Prompt库")
    public ApiResponse<List<PromptVO>> searchPrompts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String promptType,
            @RequestParam(required = false) String styleTag,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(promptLibraryService.search(keyword, promptType, styleTag, page, size)
                .stream().map(this::toPromptVO).collect(Collectors.toList()));
    }

    @PostMapping("/prompts/{id}/use")
    @Operation(summary = "记录Prompt使用")
    public ApiResponse<Void> recordPromptUse(@PathVariable Long id) { promptLibraryService.recordUse(id); return ApiResponse.success(null); }

    @DeleteMapping("/prompts/{id}")
    @Operation(summary = "删除Prompt")
    public ApiResponse<Void> deletePrompt(@PathVariable Long id) { promptLibraryService.deleteById(id); return ApiResponse.success(null); }

    @GetMapping("/prompts/search")
    @Operation(summary = "RAG语义检索Prompt — 基于向量相似度，支持自然语言查询")
    public ApiResponse<List<SemanticPromptVO>> semanticSearchPrompts(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int topK) {
        return ApiResponse.success(promptLibraryService.semanticSearch(q, topK));
    }

    @PostMapping("/prompts/reindex")
    @Operation(summary = "重建全量RAG索引")
    public ApiResponse<Map<String, Object>> reindexPrompts() {
        int count = promptLibraryService.reindexAll();
        return ApiResponse.success(Map.of("indexed", count, "status", "ok"));
    }

    @GetMapping("/videos/asset/{assetId}")
    @Operation(summary = "获取资产关联的AI视频元数据")
    public ApiResponse<AiVideoVO> getAiVideoByAsset(@PathVariable Long assetId) {
        AssetAiVideo aiVideo = aiVideoService.findByAssetId(assetId, UserContextHolder.getCurrentUserId());
        if (aiVideo == null) return ApiResponse.error("未找到AI视频元数据");
        return ApiResponse.success(toAiVideoVO(aiVideo));
    }

    @GetMapping("/videos/{videoId}/frames")
    @Operation(summary = "获取视频帧列表")
    public ApiResponse<List<VideoFrameVO>> getVideoFrames(@PathVariable Long videoId) {
        return ApiResponse.success(aiVideoService.getFramesByVideoId(videoId).stream().map(this::toVideoFrameVO).collect(Collectors.toList()));
    }

    @GetMapping("/videos/{videoId}/keyframes")
    @Operation(summary = "获取视频关键帧列表")
    public ApiResponse<List<VideoFrameVO>> getKeyframes(@PathVariable Long videoId) {
        return ApiResponse.success(aiVideoService.getKeyframesByVideoId(videoId).stream().map(this::toVideoFrameVO).collect(Collectors.toList()));
    }

    @PostMapping("/videos")
    @Operation(summary = "创建AI视频元数据")
    public ApiResponse<AiVideoVO> createAiVideo(@Valid @RequestBody AiVideoCreateRequest request) {
        return ApiResponse.success(toAiVideoVO(aiVideoService.createAiVideo(request, UserContextHolder.getCurrentUserId())));
    }

    @PutMapping("/videos/{id}")
    @Operation(summary = "更新AI视频元数据")
    public ApiResponse<AiVideoVO> updateAiVideo(@PathVariable Long id, @Valid @RequestBody AiVideoCreateRequest request) {
        return ApiResponse.success(toAiVideoVO(aiVideoService.updateAiVideo(id, request, UserContextHolder.getCurrentUserId())));
    }

    @GetMapping("/videos")
    @Operation(summary = "获取AI视频列表，可按项目过滤")
    public ApiResponse<List<AiVideoVO>> listAiVideos(@RequestParam(required = false) Long projectId) {
        List<AssetAiVideo> videos;
        if (projectId != null) {
            videos = aiVideoService.listByProject(projectId, UserContextHolder.getCurrentUserId());
        } else {
            videos = aiVideoService.listAll(UserContextHolder.getCurrentUserId());
        }
        return ApiResponse.success(videos.stream().map(this::toAiVideoVO).collect(Collectors.toList()));
    }

    @PostMapping("/videos/{videoId}/generate-next-version")
    @Operation(summary = "AI生成下一版本 — 综合审阅意见和帧标注，生成优化后的Prompt和资产版本")
    public ApiResponse<Map<String, Object>> generateNextVersion(@PathVariable Long videoId, @RequestParam Long projectId) {
        return ApiResponse.success(aiVideoService.generateNextVersion(videoId, projectId, UserContextHolder.getCurrentUserId()));
    }

    @GetMapping("/frames/{frameId}/thumbnail")
    @Operation(summary = "获取帧缩略图（302重定向到新鲜预签名URL）")
    public void getFrameThumbnail(@PathVariable Long frameId, jakarta.servlet.http.HttpServletResponse resp) throws java.io.IOException {
        VideoFrame frame = aiVideoService.findFrameById(frameId);
        if (frame == null || frame.getThumbnailUrl() == null) { resp.sendError(404); return; }
        String objectPath = "frames/" + frame.getVideoId() + "/thumb_" + frame.getFrameNumber() + ".jpg";
        String fresh = aiVideoService.getThumbnailPresignedUrl(objectPath);
        resp.sendRedirect(fresh != null ? fresh : frame.getThumbnailUrl());
    }

    @PostMapping("/videos/{videoId}/extract-frames")
    @Operation(summary = "触发FFmpeg帧提取 — 从真实视频文件中提取关键帧缩略图并上传MinIO")
    public ApiResponse<Map<String, Object>> extractFrames(@PathVariable Long videoId) {
        int count = aiVideoService.triggerFrameExtraction(videoId, UserContextHolder.getCurrentUserId());
        return ApiResponse.success(Map.of("videoId", videoId, "framesExtracted", count));
    }

    @PostMapping("/videos/{videoId}/analyze-vision")
    @Operation(summary = "多模态视觉分析 — 用Kimi vision模型分析每帧画面并生成中文描述")
    public ApiResponse<Map<String, Object>> analyzeVision(@PathVariable Long videoId) {
        return ApiResponse.success(aiVideoService.analyzeFramesWithVision(videoId, UserContextHolder.getCurrentUserId()));
    }

    @DeleteMapping("/videos/{id}")
    @Operation(summary = "删除AI视频元数据")
    public ApiResponse<Void> deleteAiVideo(@PathVariable Long id) {
        aiVideoService.deleteAiVideo(id, UserContextHolder.getCurrentUserId());
        return ApiResponse.success();
    }

    @PostMapping("/videos/{videoId}/frames")
    @Operation(summary = "添加视频帧")
    public ApiResponse<VideoFrameVO> addFrame(@PathVariable Long videoId,
            @RequestParam(required = false, defaultValue = "0") Integer frameNumber,
            @RequestParam(required = false) String thumbnailUrl,
            @RequestParam(required = false) String promptText,
            @RequestParam(required = false) String parameters,
            @RequestParam(required = false, defaultValue = "0") Integer isKeyframe,
            @RequestParam(required = false) String tags) {
        return ApiResponse.success(toVideoFrameVO(aiVideoService.addFrame(videoId, null,
                frameNumber, thumbnailUrl, promptText, parameters, isKeyframe, tags)));
    }

    private PromptVO toPromptVO(PromptLibrary p) {
        PromptVO vo = new PromptVO();
        vo.setId(p.getId()); vo.setPromptText(p.getPromptText()); vo.setPromptType(p.getPromptType());
        vo.setStyleTags(p.getStyleTags()); vo.setSourceVideoId(p.getSourceVideoId());
        vo.setSourceFrameId(p.getSourceFrameId()); vo.setCreatedBy(p.getCreatedBy());
        vo.setUseCount(p.getUseCount()); vo.setRating(p.getRating()); vo.setCreatedAt(p.getCreatedAt());
        return vo;
    }

    private AiVideoVO toAiVideoVO(AssetAiVideo v) {
        AiVideoVO vo = new AiVideoVO();
        vo.setId(v.getId()); vo.setAssetId(v.getAssetId()); vo.setToolType(v.getToolType());
        vo.setToolVersion(v.getToolVersion()); vo.setPromptText(v.getPromptText());
        vo.setNegativePrompt(v.getNegativePrompt()); vo.setParameters(v.getParameters());
        vo.setOriginalUrl(v.getOriginalUrl()); vo.setFps(v.getFps()); vo.setCreatedAt(v.getCreatedAt());
        return vo;
    }

    private VideoFrameVO toVideoFrameVO(VideoFrame f) {
        VideoFrameVO vo = new VideoFrameVO();
        vo.setId(f.getId()); vo.setVideoId(f.getVideoId()); vo.setTimestamp(f.getTimestamp());
        vo.setFrameNumber(f.getFrameNumber()); vo.setThumbnailUrl(f.getThumbnailUrl());
        vo.setPromptText(f.getPromptText()); vo.setParameters(f.getParameters());
        vo.setIsKeyframe(f.getIsKeyframe()); vo.setTags(f.getTags()); vo.setCreatedAt(f.getCreatedAt());
        return vo;
    }

    // ==================== Agent Data Tasks ====================

    @PostMapping("/tasks")
    @Operation(summary = "创建数据采集任务")
    public ApiResponse<AgentDataTask> createTask(@RequestBody AgentDataTask task) {
        task.setOwnerId(UserContextHolder.getCurrentUserId());
        return ApiResponse.success(agentTaskService.createTask(task));
    }

    @GetMapping("/tasks")
    @Operation(summary = "查询我的任务列表")
    public ApiResponse<List<AgentDataTask>> listTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(agentTaskService.listByOwner(UserContextHolder.getCurrentUserId(), page, size));
    }

    @PostMapping("/tasks/{id}/execute")
    @Operation(summary = "立即执行任务")
    public ApiResponse<Void> executeTask(@PathVariable Long id) {
        agentTaskService.executeTask(id, UserContextHolder.getCurrentUserId());
        return ApiResponse.success();
    }

    @DeleteMapping("/tasks/{id}")
    @Operation(summary = "删除任务")
    public ApiResponse<Void> deleteTask(@PathVariable Long id) {
        agentTaskService.deleteTask(id, UserContextHolder.getCurrentUserId());
        return ApiResponse.success();
    }

    // ==================== Agent Reports ====================

    @PostMapping("/reports")
    @Operation(summary = "生成分析报告")
    public ApiResponse<AgentReport> generateReport(
            @RequestParam String title, @RequestParam String type,
            @RequestParam(required = false) Long templateId,
            @RequestParam(required = false) Long projectId) {
        return ApiResponse.success(agentReportService.generateReport(title, type, templateId, projectId, UserContextHolder.getCurrentUserId()));
    }

    @GetMapping("/reports")
    @Operation(summary = "查询报告列表")
    public ApiResponse<List<AgentReport>> listReports(
            @RequestParam(required = false) Long projectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(agentReportService.listByProject(projectId, page, size, UserContextHolder.getCurrentUserId()));
    }

    @GetMapping("/records/{taskId}")
    @Operation(summary = "查询任务数据记录")
    public ApiResponse<List<AgentDataRecord>> getTaskRecords(@PathVariable Long taskId) {
        return ApiResponse.success(agentTaskService.getRecordsByTask(taskId, UserContextHolder.getCurrentUserId()));
    }

    @DeleteMapping("/reports/{id}")
    @Operation(summary = "删除报告")
    public ApiResponse<Void> deleteReport(@PathVariable Long id) {
        agentReportService.deleteReport(id);
        return ApiResponse.success();
    }

    @GetMapping("/templates")
    @Operation(summary = "获取报告模板列表")
    public ApiResponse<List<AgentReportTemplate>> listTemplates() {
        return ApiResponse.success(agentReportService.listTemplates());
    }

    // ==================== Agent Orchestrator ====================

    @GetMapping("/tools")
    @Operation(summary = "获取Agent可用工具列表")
    public ApiResponse<List<Map<String, Object>>> listTools() {
        return ApiResponse.success(tools.stream().map(t -> Map.<String, Object>of(
                "name", t.name(),
                "description", t.description(),
                "inputSchema", t.inputSchema()
        )).collect(Collectors.toList()));
    }

    @PostMapping(value = "/execute", produces = "text/event-stream;charset=UTF-8")
    @Operation(summary = "SSE流式执行Agent任务 — ReAct模式，实时推送思考/行动/观察步骤")
    public SseEmitter executeAgent(@RequestParam String task) {
        return orchestrator.execute(task, UserContextHolder.getCurrentUserId());
    }

    @PostMapping(value = "/workflow", produces = "text/event-stream;charset=UTF-8")
    @Operation(summary = "SSE多Agent协同工作流 — Supervisor规划→Workers执行→Auditor审查→汇总")
    public SseEmitter launchWorkflow(@RequestParam String task) {
        return workflowEngine.launch(task);
    }

    @GetMapping("/inbox")
    @Operation(summary = "查看Agent通信消息")
    public ApiResponse<Map<String, Object>> viewInbox(@RequestParam(defaultValue = "SUPERVISOR") String agent) {
        return ApiResponse.success(Map.of(
                "agent", agent,
                "inbox", agentBus.pollInbox(agent),
                "history", agentBus.getHistory(agent)
        ));
    }

    // ==================== RAG Evaluation ====================

    @PostMapping("/rag/eval")
    @Operation(summary = "RAG检索质量评估 — Recall@K + MRR")
    public ApiResponse<RagEvaluator.EvalResult> evaluateRag(@RequestBody Map<String, List<Long>> testCases) {
        Map<String, Set<Long>> cases = new LinkedHashMap<>();
        testCases.forEach((query, ids) -> cases.put(query, Set.copyOf(ids)));
        return ApiResponse.success(ragEvaluator.evaluate(cases, 10));
    }

    // ==================== Monitoring ====================

    @GetMapping("/stats")
    @Operation(summary = "Agent模块运行统计")
    public ApiResponse<Map<String, Object>> agentStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("toolsCount", tools.size());
        stats.put("toolNames", tools.stream().map(AgentTool::name).toList());
        stats.put("agentBusInbox", agentBus.pollInbox("SUPERVISOR").size());
        stats.put("planningMode", "LLM + keyword fallback");
        return ApiResponse.success(stats);
    }
}
