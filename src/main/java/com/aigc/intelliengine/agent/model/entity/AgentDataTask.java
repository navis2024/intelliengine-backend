package com.aigc.intelliengine.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("agent_data_task")
public class AgentDataTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String platform;
    private String status;
    private String configJson;
    private String scheduleType;
    private String cronExpression;
    private Long ownerId;
    private String ownerType;
    private LocalDateTime lastExecuteTime;
    private LocalDateTime nextExecuteTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
