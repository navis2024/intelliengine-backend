package com.aigc.intelliengine.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("agent_report")
public class AgentReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String type;
    private String content;
    private Long templateId;
    private Long projectId;
    private Long generatedBy;
    private LocalDateTime generatedAt;
    private LocalDateTime createdAt;
}
