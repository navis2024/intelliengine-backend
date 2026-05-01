package com.aigc.intelliengine.project.infrastructure.mapper;

import com.aigc.intelliengine.project.infrastructure.dataobject.ProjectMemberDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 项目成员数据访问映射器(Project Member Mapper)
 * <p>
 * 对应数据库表: project_member
 *
 * @author 智擎开发团队
 * @since 2024
 */
@Mapper
public interface ProjectMemberMapper extends BaseMapper<ProjectMemberDO> {

    /**
     * 查询项目的所有成员
     */
    @Select("SELECT * FROM project_member WHERE project_id = #{projectId} ORDER BY joined_at")
    List<ProjectMemberDO> selectByProjectId(@Param("projectId") Long projectId);

    /**
     * 查询用户的所有项目成员记录
     */
    @Select("SELECT * FROM project_member WHERE user_id = #{userId} ORDER BY joined_at DESC")
    List<ProjectMemberDO> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户在指定项目中的角色
     */
    @Select("SELECT role FROM project_member WHERE project_id = #{projectId} AND user_id = #{userId} LIMIT 1")
    String selectRoleByProjectAndUser(@Param("projectId") Long projectId, @Param("userId") Long userId);

    /**
     * 删除项目的所有成员
     */
    @Select("DELETE FROM project_member WHERE project_id = #{projectId}")
    int deleteByProjectId(@Param("projectId") Long projectId);
}
