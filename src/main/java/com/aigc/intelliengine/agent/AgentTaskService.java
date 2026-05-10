package com.aigc.intelliengine.agent;

import com.aigc.intelliengine.agent.model.entity.AgentDataRecord;
import com.aigc.intelliengine.agent.model.entity.AgentDataTask;
import com.aigc.intelliengine.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentTaskService {

    private final AgentDataTaskMapper taskMapper;
    private final AgentDataRecordMapper recordMapper;

    @Transactional
    public AgentDataTask createTask(AgentDataTask task) {
        task.setStatus("PENDING");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setNextExecuteTime(LocalDateTime.now().plusMinutes(5));
        taskMapper.insert(task);
        return task;
    }

    public AgentDataTask getTask(Long id, Long userId) {
        AgentDataTask task = taskMapper.selectById(id);
        if (task == null) throw new BusinessException("任务不存在");
        if (!userId.equals(task.getOwnerId()))
            throw new BusinessException("无权访问该任务");
        return task;
    }

    public List<AgentDataTask> listByOwner(Long ownerId, int page, int size) {
        return taskMapper.selectPage(Page.of(page, size),
            new LambdaQueryWrapper<AgentDataTask>()
                .eq(AgentDataTask::getOwnerId, ownerId)
                .orderByDesc(AgentDataTask::getCreatedAt))
            .getRecords();
    }

    @Transactional
    public void executeTask(Long id, Long userId) {
        AgentDataTask task = taskMapper.selectById(id);
        if (task == null) throw new BusinessException("任务不存在");
        if (!userId.equals(task.getOwnerId()))
            throw new BusinessException("无权执行该任务");
        task.setStatus("RUNNING");
        task.setLastExecuteTime(LocalDateTime.now());
        taskMapper.updateById(task);

        try {
            List<AgentDataRecord> records = simulateDataCollection(task);
            for (AgentDataRecord record : records) {
                detectAnomaly(record);
                recordMapper.insert(record);
            }
            task.setStatus("COMPLETED");
            log.info("Task {} completed: collected {} records", task.getName(), records.size());
        } catch (Exception e) {
            log.error("Task {} failed: {}", task.getName(), e.getMessage());
            task.setStatus("FAILED");
        }
        task.setNextExecuteTime(LocalDateTime.now().plusHours(24));
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    private List<AgentDataRecord> simulateDataCollection(AgentDataTask task) {
        List<AgentDataRecord> records = new ArrayList<>();
        int count = ThreadLocalRandom.current().nextInt(3, 10);
        String platform = task.getPlatform() != null ? task.getPlatform() : "RUNWAY";

        for (int i = 0; i < count; i++) {
            AgentDataRecord record = new AgentDataRecord();
            record.setTaskId(task.getId());
            record.setPlatform(platform);
            record.setWorkId(platform + "_" + UUID.randomUUID().toString().substring(0, 8));
            record.setRawData(generateMockRawData(platform));
            record.setMetrics(generateMockMetrics());
            record.setStatus(0);
            record.setIsAnomaly(0);
            record.setCollectedAt(LocalDateTime.now());
            records.add(record);
        }
        return records;
    }

    private String generateMockRawData(String platform) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("platform", platform);
        data.put("quality", ThreadLocalRandom.current().nextDouble(0.5, 1.0));
        data.put("render_time_ms", ThreadLocalRandom.current().nextInt(2000, 60000));
        data.put("resolution", List.of("1080p", "4K", "720p").get(ThreadLocalRandom.current().nextInt(3)));
        data.put("prompt", "AIGC generated content sample");
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(data);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String generateMockMetrics() {
        return String.format(
            "{\"quality_score\":%.2f,\"render_time_ms\":%d,\"frame_rate\":%d}",
            ThreadLocalRandom.current().nextDouble(0.5, 1.0),
            ThreadLocalRandom.current().nextInt(2000, 60000),
            ThreadLocalRandom.current().nextInt(24, 60)
        );
    }

    private void detectAnomaly(AgentDataRecord record) {
        try {
            String metrics = record.getMetrics();
            if (metrics != null && metrics.contains("\"quality_score\"")) {
                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                var node = mapper.readTree(metrics);
                double quality = node.has("quality_score") ? node.get("quality_score").asDouble() : 1.0;
                if (quality < 0.7) {
                    record.setIsAnomaly(1);
                    record.setAnomalyReason("质量评分过低: " + String.format("%.2f", quality));
                }
            }
            String rawData = record.getRawData();
            if (rawData != null && rawData.contains("\"quality\"")) {
                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                var node = mapper.readTree(rawData);
                double quality = node.has("quality") ? node.get("quality").asDouble() : 1.0;
                if (quality < 0.6) {
                    record.setIsAnomaly(1);
                    record.setAnomalyReason((record.getAnomalyReason() != null ? record.getAnomalyReason() + "; " : "")
                        + "原始数据质量过低: " + String.format("%.2f", quality));
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        AgentDataTask task = taskMapper.selectById(id);
        if (task == null) throw new BusinessException("任务不存在");
        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    @Transactional
    public void deleteTask(Long id, Long userId) {
        AgentDataTask task = taskMapper.selectById(id);
        if (task == null) throw new BusinessException("任务不存在");
        if (!userId.equals(task.getOwnerId()))
            throw new BusinessException("无权删除该任务");
        recordMapper.delete(new LambdaQueryWrapper<AgentDataRecord>().eq(AgentDataRecord::getTaskId, id));
        taskMapper.deleteById(id);
    }

    public List<AgentDataRecord> getRecordsByTask(Long taskId, Long userId) {
        AgentDataTask task = taskMapper.selectById(taskId);
        if (task == null) throw new BusinessException("任务不存在");
        if (!userId.equals(task.getOwnerId()))
            throw new BusinessException("无权访问该任务数据");
        return recordMapper.selectList(new LambdaQueryWrapper<AgentDataRecord>()
            .eq(AgentDataRecord::getTaskId, taskId)
            .orderByDesc(AgentDataRecord::getCollectedAt));
    }
}
