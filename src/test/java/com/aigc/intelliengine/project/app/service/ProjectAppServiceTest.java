package com.aigc.intelliengine.project.app.service;

import com.aigc.intelliengine.project.domain.entity.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 项目应用服务测试类
 */
class ProjectAppServiceTest {

    @BeforeEach
    void setUp() {
        // 测试准备
    }

    @Test
    void testProjectEntity() {
        // 测试领域实体
        Project project = new Project();
        project.setId("1");
        project.setProjectCode("PROJ_001");
        project.setName("测试项目");
        project.setDescription("这是一个测试项目");
        project.setOwnerId("1");
        project.setStatus("ACTIVE");
        project.setVisibility("PRIVATE");

        assertNotNull(project);
        assertEquals("1", project.getId());
        assertEquals("测试项目", project.getName());
        assertTrue(project.isActive());
        assertFalse(project.isPublic());
    }

    @Test
    void testProjectStatus() {
        Project project = new Project();
        project.setStatus("ACTIVE");
        
        assertTrue(project.isActive());
        
        project.archive();
        assertFalse(project.isActive());
    }
}
