package com.aigc.intelliengine.project.infrastructure.repository;

import com.aigc.intelliengine.project.domain.entity.ProjectMember;
import com.aigc.intelliengine.project.infrastructure.dataobject.ProjectMemberDO;
import com.aigc.intelliengine.project.infrastructure.mapper.ProjectMemberMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ProjectMemberRepositoryImpl {
    private final ProjectMemberMapper memberMapper;
    
    public ProjectMemberRepositoryImpl(ProjectMemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }
    
    public ProjectMember save(ProjectMember member) {
        ProjectMemberDO memberDO = toDataObject(member);
        memberMapper.insert(memberDO);
        return toEntity(memberDO);
    }
    
    public List<ProjectMember> findByProjectId(Long projectId) {
        return memberMapper.selectByProjectId(projectId).stream()
            .map(this::toEntity).collect(Collectors.toList());
    }
    
    public String findRole(Long projectId, Long userId) {
        return memberMapper.selectRoleByProjectAndUser(projectId, userId);
    }
    
    public boolean remove(Long id) {
        return memberMapper.deleteById(id) > 0;
    }
    
    private ProjectMemberDO toDataObject(ProjectMember member) {
        if (member == null) return null;
        ProjectMemberDO memberDO = new ProjectMemberDO();
        if (member.getId() != null) memberDO.setId(Long.valueOf(member.getId()));
        if (member.getProjectId() != null) memberDO.setProjectId(Long.valueOf(member.getProjectId()));
        if (member.getUserId() != null) memberDO.setUserId(Long.valueOf(member.getUserId()));
        memberDO.setRole(member.getRole());
        memberDO.setJoinedAt(member.getJoinedAt());
        return memberDO;
    }
    
    private ProjectMember toEntity(ProjectMemberDO memberDO) {
        if (memberDO == null) return null;
        ProjectMember member = new ProjectMember();
        member.setId(String.valueOf(memberDO.getId()));
        member.setProjectId(String.valueOf(memberDO.getProjectId()));
        member.setUserId(String.valueOf(memberDO.getUserId()));
        member.setRole(memberDO.getRole());
        member.setJoinedAt(memberDO.getJoinedAt());
        return member;
    }
}
