package com.aigc.intelliengine.project.domain.gateway;

import com.aigc.intelliengine.project.domain.entity.Project;

import java.util.List;
import java.util.Optional;

/**
 * 项目仓储接口(Project Gateway)
 * <p>
 * 位于领域层，定义项目领域的持久化操作
 * 由基础设施层实现（ProjectRepositoryImpl）
 *
 * @author 智擎开发团队
 * @since 2024
 */
public interface ProjectGateway {

    /**
     * 保存项目
     */
    Project save(Project project);

    /**
     * 根据ID查询
     */
    Optional<Project> findById(Long id);

    /**
     * 根据项目编码查询
     */
    Optional<Project> findByCode(String code);

    /**
     * 根据创建者ID查询列表
     */
    List<Project> findByOwnerId(Long ownerId);

    /**
     * 更新项目
     */
    Project update(Project project);

    /**
     * 删除项目（逻辑删除）
     */
    boolean remove(Long id);

    /**
     * 检查项目编码是否存在
     */
    boolean existsByCode(String code);
}
