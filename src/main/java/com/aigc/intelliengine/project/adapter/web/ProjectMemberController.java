package com.aigc.intelliengine.project.adapter.web;

import com.aigc.intelliengine.common.result.ApiResponse;
import com.aigc.intelliengine.project.infrastructure.dataobject.ProjectMemberDO;
import com.aigc.intelliengine.project.infrastructure.repository.ProjectMemberRepositoryImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/members")
@RequiredArgsConstructor
@Tag(name = "项目成员管理")
public class ProjectMemberController {
    private final ProjectMemberRepositoryImpl memberRepository;

    @GetMapping
    @Operation(summary = "获取项目成员列表")
    public ApiResponse<List<com.aigc.intelliengine.project.domain.entity.ProjectMember>> getMembers(@PathVariable Long projectId) {
        return ApiResponse.success(memberRepository.findByProjectId(projectId));
    }
}
