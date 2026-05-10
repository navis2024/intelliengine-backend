package com.aigc.intelliengine.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("agent_report_template")
public class AgentReportTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type;
    private String contentTemplate;
    private String chartConfigs;
    private Integer isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
