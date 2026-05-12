package com.aigc.intelliengine.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("agent_report_template")
public class AgentReportTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    @TableField("template_type")
    private String type;
    private String contentTemplate;
    private String chartConfigs;
    @TableField("default_format")
    private String defaultFormat;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
