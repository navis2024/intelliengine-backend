package com.aigc.intelliengine.project.infrastructure.repository;

import com.aigc.intelliengine.project.domain.entity.Project;
import com.aigc.intelliengine.project.domain.gateway.ProjectGateway;
import com.aigc.intelliengine.project.infrastructure.dataobject.ProjectDO;
import com.aigc.intelliengine.project.infrastructure.mapper.ProjectMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 项目仓储实现(Project Repository Implementation)
 * <p>
 * 位于基础设施层，实现ProjectGateway接口
 *
 * @author 智擎开发团队
 * @since 2024
 */
@Repository
public class ProjectRepositoryImpl implements ProjectGateway {

    private final ProjectMapper projectMapper;

    public ProjectRepositoryImpl(ProjectMapper projectMapper) {
        this.projectMapper = Objects.requireNonNull(projectMapper);
    }

    @Override
    public Project save(Project project) {
        ProjectDO projectDO = toDataObject(project);
        projectMapper.insert(projectDO);
        return toEntity(projectDO);
    }

    @Override
    public Optional<Project> findById(Long id) {
        ProjectDO projectDO = projectMapper.selectById(id);
        return Optional.ofNullable(toEntity(projectDO));
    }

    @Override
    public Optional<Project> findByCode(String code) {
        ProjectDO projectDO = projectMapper.selectByCode(code);
        return Optional.ofNullable(toEntity(projectDO));
    }

    @Override
    public List<Project> findByOwnerId(Long ownerId) {
        return projectMapper.selectByOwnerId(ownerId).stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Project update(Project project) {
        ProjectDO projectDO = toDataObject(project);
        projectMapper.updateById(projectDO);
        return toEntity(projectDO);
    }

    @Override
    public boolean remove(Long id) {
        return projectMapper.deleteById(id) > 0;
    }

    @Override
    public boolean existsByCode(String code) {
        return projectMapper.existsByCode(code);
    }

    private ProjectDO toDataObject(Project project) {
        if (project == null) return null;
        ProjectDO projectDO = new ProjectDO();
        if (project.getId() != null) {
            projectDO.setId(Long.valueOf(project.getId()));
        }
        projectDO.setProjectCode(project.getProjectCode());
        projectDO.setName(project.getName());
        projectDO.setDescription(project.getDescription());
        projectDO.setCoverUrl(project.getCoverUrl());
        if (project.getOwnerId() != null) {
            projectDO.setOwnerId(Long.valueOf(project.getOwnerId()));
        }
        projectDO.setStatus(project.getStatus());
        projectDO.setVisibility(project.getVisibility());
        projectDO.setCreatedAt(project.getCreateTime());
        projectDO.setUpdatedAt(project.getUpdateTime());
        projectDO.setIsDeleted(project.getDeleted());
        return projectDO;
    }

    private Project toEntity(ProjectDO projectDO) {
        if (projectDO == null) return null;
        Project project = new Project();
        project.setId(String.valueOf(projectDO.getId()));
        project.setProjectCode(projectDO.getProjectCode());
        project.setName(projectDO.getName());
        project.setDescription(projectDO.getDescription());
        project.setCoverUrl(projectDO.getCoverUrl());
        project.setOwnerId(String.valueOf(projectDO.getOwnerId()));
        project.setStatus(projectDO.getStatus());
        project.setVisibility(projectDO.getVisibility());
        project.setCreateTime(projectDO.getCreatedAt());
        project.setUpdateTime(projectDO.getUpdatedAt());
        project.setDeleted(projectDO.getIsDeleted());
        return project;
    }
}
