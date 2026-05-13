package com.aigc.intelliengine.project.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("project_info")
public class ProjectInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String projectCode;
    private String name;
    private String description;
    private String coverUrl;
    private Long ownerId;
    private String status;
    private String visibility;
    private String groupId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
