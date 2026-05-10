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
import com.aigc.intelliengine.common.model.ApiResponse;
import com.aigc.intelliengine.common.security.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
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
    @Operation(summary = "获取所有AI视频列表")
    public ApiResponse<List<AiVideoVO>> listAiVideos() {
        return ApiResponse.success(aiVideoService.listAll(UserContextHolder.getCurrentUserId()).stream().map(this::toAiVideoVO).collect(Collectors.toList()));
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
}
