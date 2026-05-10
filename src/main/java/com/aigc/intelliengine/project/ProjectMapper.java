package com.aigc.intelliengine.project;

import com.aigc.intelliengine.project.model.entity.ProjectInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ProjectMapper extends BaseMapper<ProjectInfo> {
    @Select("SELECT * FROM project_info WHERE owner_id = #{ownerId} AND is_deleted = 0 ORDER BY created_at DESC")
    List<ProjectInfo> selectByOwner(@Param("ownerId") Long ownerId);
}
