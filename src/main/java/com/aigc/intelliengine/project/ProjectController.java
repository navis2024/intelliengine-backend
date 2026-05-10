package com.aigc.intelliengine.project;

import com.aigc.intelliengine.common.model.ApiResponse;
import com.aigc.intelliengine.common.model.PageResult;
import com.aigc.intelliengine.common.security.UserContextHolder;
import com.aigc.intelliengine.project.model.dto.ProjectCreateRequest;
import com.aigc.intelliengine.project.model.dto.ProjectUpdateRequest;
import com.aigc.intelliengine.project.model.entity.ProjectMember;
import com.aigc.intelliengine.project.model.vo.ProjectVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "项目管理")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "创建项目")
    public ApiResponse<ProjectVO> create(@Valid @RequestBody ProjectCreateRequest request) {
        return ApiResponse.success(projectService.createProject(request, UserContextHolder.getCurrentUserId()));
    }

    @PostMapping("/{id}/join")
    @Operation(summary = "加入项目", description = "用户输入组ID后加入项目")
    public ApiResponse<Void> join(@PathVariable Long id) {
        projectService.joinProject(id, UserContextHolder.getCurrentUserId());
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取项目详情")
    public ApiResponse<ProjectVO> getById(@PathVariable Long id) {
        return ApiResponse.success(projectService.getProject(id, UserContextHolder.getCurrentUserId()));
    }

    @GetMapping("/my")
    @Operation(summary = "我的项目列表")
    public ApiResponse<PageResult<ProjectVO>> getMyProjects(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(projectService.getMyProjects(UserContextHolder.getCurrentUserId(), pageNum, pageSize, keyword, status));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新项目")
    public ApiResponse<ProjectVO> update(@PathVariable Long id, @Valid @RequestBody ProjectUpdateRequest request) {
        return ApiResponse.success(projectService.updateProject(id, request, UserContextHolder.getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除项目")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        projectService.deleteProject(id, UserContextHolder.getCurrentUserId());
        return ApiResponse.success();
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "获取项目成员")
    public ApiResponse<List<ProjectMember>> getMembers(@PathVariable Long id) {
        return ApiResponse.success(projectService.getMembers(id, UserContextHolder.getCurrentUserId()));
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "添加成员")
    public ApiResponse<Void> addMember(@PathVariable Long id, @RequestParam Long userId, @RequestParam(defaultValue = "MEMBER") String role) {
        projectService.addMember(id, userId, role, UserContextHolder.getCurrentUserId());
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "移除成员")
    public ApiResponse<Void> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        projectService.removeMember(id, userId, UserContextHolder.getCurrentUserId());
        return ApiResponse.success();
    }

    @PutMapping("/{id}/members/{userId}")
    @Operation(summary = "更新成员角色")
    public ApiResponse<Void> updateMemberRole(@PathVariable Long id, @PathVariable Long userId, @RequestParam String role) {
        projectService.updateMemberRole(id, userId, role, UserContextHolder.getCurrentUserId());
        return ApiResponse.success();
    }
}
