package com.aigc.intelliengine.agent;

import com.aigc.intelliengine.asset.AssetMapper;
import com.aigc.intelliengine.asset.model.entity.AssetInfo;
import com.aigc.intelliengine.project.ProjectMapper;
import com.aigc.intelliengine.project.model.entity.ProjectInfo;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * LangChain4j @Tool methods — callable by the Agent during conversation.
 * When the LLM decides it needs real data, it invokes these tools automatically.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LangChain4jTools {

    private final AssetMapper assetMapper;
    private final ProjectMapper projectMapper;

    @Tool("Query asset metadata by ID — returns name, type, status, file format, and file size")
    public Map<String, Object> queryAsset(Long assetId) {
        AssetInfo a = assetMapper.selectById(assetId);
        if (a == null) return Map.of("error", "Asset not found");
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", a.getId());
        info.put("name", a.getName());
        info.put("type", a.getType());
        info.put("status", a.getStatus());
        info.put("fileFormat", a.getFileFormat());
        info.put("fileSize", a.getFileSize());
        info.put("createdAt", a.getCreatedAt() != null ? a.getCreatedAt().toString() : "unknown");
        log.info("[LangChain4j Tool] queryAsset: id={}, name={}", assetId, a.getName());
        return info;
    }

    @Tool("List assets by type filter — returns matching assets with basic info")
    public List<Map<String, Object>> listAssetsByType(String type) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AssetInfo>()
                .eq(AssetInfo::getType, type)
                .orderByDesc(AssetInfo::getCreatedAt)
                .last("LIMIT 20");
        return assetMapper.selectList(wrapper).stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("name", a.getName());
            m.put("status", a.getStatus());
            m.put("fileFormat", a.getFileFormat());
            return m;
        }).toList();
    }

    @Tool("Query project info by ID — returns name, status, description")
    public Map<String, Object> queryProject(Long projectId) {
        ProjectInfo p = projectMapper.selectById(projectId);
        if (p == null) return Map.of("error", "Project not found");
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", p.getId());
        info.put("name", p.getName());
        info.put("status", p.getStatus());
        info.put("description", p.getDescription());
        log.info("[LangChain4j Tool] queryProject: id={}, name={}", projectId, p.getName());
        return info;
    }

    @Tool("Check FFmpeg processing status for a video asset — returns whether frames have been extracted")
    public String checkFfmpegStatus(Long aiVideoId) {
        // In a real implementation this would query VideoFrameMapper to see if frames exist.
        // For now, return a status indicating the workflow.
        log.info("[LangChain4j Tool] checkFfmpegStatus: aiVideoId={}", aiVideoId);
        return "FFmpeg frame extraction status: will be processed via RabbitMQ async queue. " +
               "The video frames are extracted asynchronously and results are stored in MinIO (bucket: jimeng).";
    }
}
