package com.aigc.intelliengine.project;

import com.aigc.intelliengine.asset.AssetMapper;
import com.aigc.intelliengine.common.exception.BusinessException;
import com.aigc.intelliengine.common.model.PageResult;
import com.aigc.intelliengine.common.security.MembershipValidator;
import com.aigc.intelliengine.project.model.dto.ProjectCreateRequest;
import com.aigc.intelliengine.project.model.dto.ProjectUpdateRequest;
import com.aigc.intelliengine.project.model.entity.ProjectInfo;
import com.aigc.intelliengine.project.model.entity.ProjectMember;
import com.aigc.intelliengine.project.model.vo.ProjectVO;
import com.aigc.intelliengine.user.UserAccountMapper;
import com.aigc.intelliengine.user.model.entity.UserAccount;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper memberMapper;
    private final UserAccountMapper userAccountMapper;
    private final AssetMapper assetMapper;
    private final MembershipValidator validator;

    @Transactional
    public ProjectVO createProject(ProjectCreateRequest request, Long ownerId) {
        ProjectInfo project = new ProjectInfo();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setCoverUrl(request.getCoverUrl());
        project.setVisibility(request.getVisibility() != null ? request.getVisibility() : "PRIVATE");
        project.setOwnerId(ownerId);
        project.setStatus("ACTIVE");
        project.setProjectCode("PROJ_" + System.currentTimeMillis());
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        projectMapper.insert(project);
        ProjectMember owner = new ProjectMember();
        owner.setProjectId(project.getId());
        owner.setUserId(ownerId);
        owner.setRole("OWNER");
        owner.setJoinedAt(LocalDateTime.now());
        memberMapper.insert(owner);
        return toVO(project);
    }

    @Transactional
    public void joinProject(Long projectId, Long userId) {
        validator.requireProjectExists(projectId);
        if (memberMapper.existsByProjectAndUser(projectId, userId)) {
            throw new BusinessException("您已是该项目成员");
        }
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole("MEMBER");
        member.setJoinedAt(LocalDateTime.now());
        memberMapper.insert(member);
    }

    public ProjectVO getProject(Long id, Long userId) {
        ProjectInfo project = projectMapper.selectById(id);
        if (project == null || project.getIsDeleted() != null && project.getIsDeleted() == 1) {
            throw new BusinessException("项目不存在");
        }
        validator.requireMembership(id, userId);
        return toVO(project);
    }

    public PageResult<ProjectVO> getMyProjects(Long userId, Integer pageNum, Integer pageSize, String keyword, String status) {
        List<Long> projectIds = memberMapper.selectByUser(userId).stream()
                .map(ProjectMember::getProjectId).toList();
        if (projectIds.isEmpty()) return PageResult.empty(pageNum, pageSize);

        LambdaQueryWrapper<ProjectInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ProjectInfo::getId, projectIds);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(ProjectInfo::getName, keyword);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(ProjectInfo::getStatus, status);
        }
        wrapper.orderByDesc(ProjectInfo::getCreatedAt);
        Page<ProjectInfo> page = projectMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords().stream().map(this::toVO).toList(), page.getTotal(), pageNum, pageSize);
    }

    @Transactional
    public ProjectVO updateProject(Long id, ProjectUpdateRequest request, Long userId) {
        validator.requireProjectOwner(id, userId);
        ProjectInfo project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        if (request.getName() != null) project.setName(request.getName());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getCoverUrl() != null) project.setCoverUrl(request.getCoverUrl());
        if (request.getStatus() != null) project.setStatus(request.getStatus());
        if (request.getVisibility() != null) project.setVisibility(request.getVisibility());
        project.setUpdatedAt(LocalDateTime.now());
        projectMapper.updateById(project);
        return toVO(project);
    }

    @Transactional
    public void deleteProject(Long id, Long userId) {
        validator.requireProjectOwner(id, userId);
        projectMapper.deleteById(id);
    }

    public List<ProjectMember> getMembers(Long projectId, Long userId) {
        validator.requireMembership(projectId, userId);
        return memberMapper.selectByProject(projectId);
    }

    @Transactional
    public void addMember(Long projectId, Long userId, String role, Long operatorId) {
        validator.requireProjectOwner(projectId, operatorId);
        if (memberMapper.existsByProjectAndUser(projectId, userId)) {
            throw new BusinessException("该用户已是项目成员");
        }
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole(role != null ? role : "MEMBER");
        member.setJoinedAt(LocalDateTime.now());
        memberMapper.insert(member);
    }

    @Transactional
    public void removeMember(Long projectId, Long userId, Long operatorId) {
        validator.requireProjectOwner(projectId, operatorId);
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId).eq(ProjectMember::getUserId, userId);
        memberMapper.delete(wrapper);
    }

    private ProjectVO toVO(ProjectInfo p) {
        if (p == null) return null;
        ProjectVO vo = new ProjectVO();
        vo.setId(String.valueOf(p.getId()));
        vo.setProjectCode(p.getProjectCode());
        vo.setName(p.getName());
        vo.setDescription(p.getDescription());
        vo.setCoverUrl(p.getCoverUrl());
        vo.setOwnerId(String.valueOf(p.getOwnerId()));
        UserAccount owner = userAccountMapper.selectById(p.getOwnerId());
        vo.setOwnerName(owner != null ? owner.getUsername() : null);
        vo.setStatus(p.getStatus());
        vo.setVisibility(p.getVisibility());
        List<ProjectMember> members = memberMapper.selectByProject(p.getId());
        vo.setMemberCount(members.size());
        vo.setAssetCount(assetMapper.selectCount(
            new LambdaQueryWrapper<com.aigc.intelliengine.asset.model.entity.AssetInfo>()
                .eq(com.aigc.intelliengine.asset.model.entity.AssetInfo::getOwnerId, p.getId())
                .eq(com.aigc.intelliengine.asset.model.entity.AssetInfo::getOwnerType, "PROJECT")));
        vo.setCreateTime(p.getCreatedAt());
        vo.setUpdateTime(p.getUpdatedAt());
        return vo;
    }

    @Transactional
    public void updateMemberRole(Long projectId, Long userId, String role, Long operatorId) {
        validator.requireProjectOwner(projectId, operatorId);
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId).eq(ProjectMember::getUserId, userId);
        ProjectMember member = memberMapper.selectOne(wrapper);
        if (member == null) throw new BusinessException("该用户不是项目成员");
        member.setRole(role);
        memberMapper.updateById(member);
    }

    @Transactional
    public void setGroupId(Long projectId, String groupId, Long userId) {
        ProjectInfo project = projectMapper.selectById(projectId);
        if (project == null) throw new BusinessException("项目不存在");
        if (!project.getOwnerId().equals(userId))
            throw new BusinessException(403, "只有项目所有者可以设置组ID");
        project.setGroupId(groupId);
        project.setUpdatedAt(LocalDateTime.now());
        projectMapper.updateById(project);
    }

    @Transactional
    public void joinByGroupId(String groupId, Long userId) {
        if (groupId == null || groupId.isBlank()) throw new BusinessException("组ID不能为空");
        ProjectInfo project = projectMapper.selectByGroupId(groupId);
        if (project == null) throw new BusinessException("未找到该组ID对应的项目");
        LambdaQueryWrapper<ProjectMember> w = new LambdaQueryWrapper<>();
        w.eq(ProjectMember::getProjectId, project.getId()).eq(ProjectMember::getUserId, userId);
        if (memberMapper.selectCount(w) > 0) throw new BusinessException("您已是该项目成员");
        ProjectMember member = new ProjectMember();
        member.setProjectId(project.getId());
        member.setUserId(userId);
        member.setRole("MEMBER");
        member.setJoinedAt(LocalDateTime.now());
        memberMapper.insert(member);
    }

    private void checkOwner(Long projectId, Long userId) {
        ProjectInfo project = projectMapper.selectById(projectId);
        if (project == null) throw new BusinessException("项目不存在");
        if (!project.getOwnerId().equals(userId))
            throw new BusinessException(403, "只有项目所有者可以执行此操作");
    }
}
