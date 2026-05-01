package com.aigc.intelliengine.project.infrastructure.mapper;

import com.aigc.intelliengine.project.infrastructure.dataobject.ProjectDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 项目数据访问映射器(Project Mapper)
 * <p>
 * 对应数据库表: project_info
 *
 * @author 智擎开发团队
 * @since 2024
 */
@Mapper
public interface ProjectMapper extends BaseMapper<ProjectDO> {

    /**
     * 根据项目编码查询
     */
    @Select("SELECT * FROM project_info WHERE project_code = #{code} AND is_deleted = 0 LIMIT 1")
    ProjectDO selectByCode(@Param("code") String code);

    /**
     * 根据创建者ID查询项目列表
     */
    @Select("SELECT * FROM project_info WHERE owner_id = #{ownerId} AND is_deleted = 0 ORDER BY created_at DESC")
    List<ProjectDO> selectByOwnerId(@Param("ownerId") Long ownerId);

    /**
     * 检查项目编码是否存在
     */
    @Select("SELECT EXISTS(SELECT 1 FROM project_info WHERE project_code = #{code} AND is_deleted = 0)")
    boolean existsByCode(@Param("code") String code);
}
