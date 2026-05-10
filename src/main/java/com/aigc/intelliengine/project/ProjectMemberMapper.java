package com.aigc.intelliengine.project;

import com.aigc.intelliengine.project.model.entity.ProjectMember;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ProjectMemberMapper extends BaseMapper<ProjectMember> {
    @Select("SELECT * FROM project_member WHERE project_id = #{projectId}")
    List<ProjectMember> selectByProject(@Param("projectId") Long projectId);

    @Select("SELECT * FROM project_member WHERE user_id = #{userId}")
    List<ProjectMember> selectByUser(@Param("userId") Long userId);

    @Select("SELECT EXISTS(SELECT 1 FROM project_member WHERE project_id = #{projectId} AND user_id = #{userId})")
    boolean existsByProjectAndUser(@Param("projectId") Long projectId, @Param("userId") Long userId);

    @Select("SELECT EXISTS(SELECT 1 FROM project_member WHERE project_id = #{projectId} AND user_id = #{userId} AND role = 'OWNER')")
    boolean isOwner(@Param("projectId") Long projectId, @Param("userId") Long userId);
}
