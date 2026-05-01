package com.aigc.intelliengine.project.adapter.web;

import com.aigc.intelliengine.common.result.ApiResponse;
import com.aigc.intelliengine.project.app.service.ProjectAppService;
import com.aigc.intelliengine.project.dto.ProjectCreateRequest;
import com.aigc.intelliengine.project.dto.ProjectUpdateRequest;
import com.aigc.intelliengine.project.vo.ProjectVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目管理控制器(Project Controller)
 * <p>
 * 位于适配器层，处理项目相关HTTP请求
 *
 * @author 智擎开发团队
 * @since 2024
 */
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "项目管理", description = "项目创建、查询、更新、删除")
public class ProjectController {

    private final ProjectAppService projectAppService;

    @PostMapping
    @Operation(summary = "创建项目", description = "创建新的项目")
    public ApiResponse<ProjectVO> createProject(
            @Valid @RequestBody ProjectCreateRequest request) {
        // TODO: 从token中获取当前用户ID
        Long currentUserId = 1L;
        ProjectVO project = projectAppService.createProject(request, currentUserId);
        return ApiResponse.success(project);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取项目详情", description = "根据ID查询项目详细信息")
    public ApiResponse<ProjectVO> getProject(
            @Parameter(description = "项目ID") @PathVariable Long id) {
        ProjectVO project = projectAppService.getProjectById(id);
        return ApiResponse.success(project);
    }

    @GetMapping("/my")
    @Operation(summary = "我的项目列表", description = "获取当前用户的项目列表")
    public ApiResponse<List<ProjectVO>> getMyProjects() {
        // TODO: 从token中获取当前用户ID
        Long currentUserId = 1L;
        List<ProjectVO> projects = projectAppService.getUserProjects(currentUserId);
        return ApiResponse.success(projects);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新项目", description = "更新项目信息")
    public ApiResponse<ProjectVO> updateProject(
            @Parameter(description = "项目ID") @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateRequest request) {
        ProjectVO project = projectAppService.updateProject(id, request);
        return ApiResponse.success(project);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除项目", description = "删除指定项目")
    public ApiResponse<Void> deleteProject(
            @Parameter(description = "项目ID") @PathVariable Long id) {
        projectAppService.deleteProject(id);
        return ApiResponse.success();
    }
}
