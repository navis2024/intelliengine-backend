package com.aigc.intelliengine.project.app.service;

import com.aigc.intelliengine.project.domain.entity.Project;
import com.aigc.intelliengine.project.domain.gateway.ProjectGateway;
import com.aigc.intelliengine.project.dto.ProjectCreateRequest;
import com.aigc.intelliengine.project.dto.ProjectUpdateRequest;
import com.aigc.intelliengine.project.vo.ProjectVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 项目应用服务(Project App Service)
 * <p>
 * 位于应用层，负责协调领域服务完成用例
 *
 * @author 智擎开发团队
 * @since 2024
 */
@Service
@RequiredArgsConstructor
public class ProjectAppService {

    private final ProjectGateway projectGateway;

    /**
     * 创建项目
     */
    @Transactional
    public ProjectVO createProject(ProjectCreateRequest request, Long ownerId) {
        // 生成项目编码
        String projectCode = generateProjectCode();

        Project project = new Project();
        project.setProjectCode(projectCode);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setCoverUrl(request.getCoverUrl());
        project.setOwnerId(String.valueOf(ownerId));
        project.setStatus("ACTIVE");
        project.setVisibility(request.getVisibility() != null ? request.getVisibility() : "PRIVATE");
        project.setCreateTime(LocalDateTime.now());
        project.setUpdateTime(LocalDateTime.now());
        project.setDeleted(0);

        Project saved = projectGateway.save(project);
        return toVO(saved);
    }

    /**
     * 根据ID查询
     */
    public ProjectVO getProjectById(Long id) {
        return projectGateway.findById(id)
                .map(this::toVO)
                .orElse(null);
    }

    /**
     * 查询用户的项目列表
     */
    public List<ProjectVO> getUserProjects(Long ownerId) {
        return projectGateway.findByOwnerId(ownerId).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 更新项目
     */
    @Transactional
    public ProjectVO updateProject(Long id, ProjectUpdateRequest request) {
        Project project = projectGateway.findById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setCoverUrl(request.getCoverUrl());
        project.setVisibility(request.getVisibility());
        project.setUpdateTime(LocalDateTime.now());

        Project updated = projectGateway.update(project);
        return toVO(updated);
    }

    /**
     * 删除项目
     */
    @Transactional
    public boolean deleteProject(Long id) {
        return projectGateway.remove(id);
    }

    /**
     * 生成项目编码
     */
    private String generateProjectCode() {
        return "PROJ_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 转换为VO
     */
    private ProjectVO toVO(Project project) {
        ProjectVO vo = new ProjectVO();
        vo.setId(project.getId());
        vo.setProjectCode(project.getProjectCode());
        vo.setName(project.getName());
        vo.setDescription(project.getDescription());
        vo.setCoverUrl(project.getCoverUrl());
        vo.setOwnerId(project.getOwnerId());
        vo.setStatus(project.getStatus());
        vo.setVisibility(project.getVisibility());
        vo.setCreateTime(project.getCreateTime());
        vo.setUpdateTime(project.getUpdateTime());
        return vo;
    }
}
