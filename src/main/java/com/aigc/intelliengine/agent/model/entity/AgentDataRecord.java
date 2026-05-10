package com.aigc.intelliengine.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("agent_data_record")
public class AgentDataRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String platform;
    private String workId;
    private String rawData;
    private String cleanedData;
    private String metrics;
    private Integer status;
    private Integer isAnomaly;
    private String anomalyReason;
    private LocalDateTime collectedAt;
    private LocalDateTime processedAt;
}
