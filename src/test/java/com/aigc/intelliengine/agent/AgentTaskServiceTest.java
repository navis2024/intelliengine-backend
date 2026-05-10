package com.aigc.intelliengine.agent;

import com.aigc.intelliengine.agent.model.entity.AgentDataRecord;
import com.aigc.intelliengine.agent.model.entity.AgentDataTask;
import com.aigc.intelliengine.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentTaskServiceTest {

    @Mock private AgentDataTaskMapper taskMapper;
    @Mock private AgentDataRecordMapper recordMapper;
    @InjectMocks private AgentTaskService taskService;

    @Test
    void createTask_shouldSetPending() {
        AgentDataTask t = new AgentDataTask(); t.setName("test");
        AgentDataTask r = taskService.createTask(t);
        assertEquals("PENDING", r.getStatus());
        assertNotNull(r.getNextExecuteTime());
    }

    @Test
    void getTask_success() {
        AgentDataTask t = new AgentDataTask(); t.setId(1L); t.setOwnerId(1L);
        when(taskMapper.selectById(1L)).thenReturn(t);
        assertEquals(1L, taskService.getTask(1L, 1L).getId());
    }

    @Test
    void getTask_notFound_shouldThrow() {
        when(taskMapper.selectById(999L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> taskService.getTask(999L, 1L));
    }

    @Test
    void getTask_notOwner_shouldThrow() {
        AgentDataTask t = new AgentDataTask(); t.setId(1L); t.setOwnerId(99L);
        when(taskMapper.selectById(1L)).thenReturn(t);
        assertThrows(BusinessException.class, () -> taskService.getTask(1L, 1L));
    }

    @Test
    void listByOwner_shouldReturnPaged() {
        AgentDataTask t1 = new AgentDataTask(); t1.setId(1L);
        when(taskMapper.selectPage(any(), any()))
                .thenReturn(new Page<>(1, 10, 1) {{ setRecords(List.of(t1)); }});
        assertEquals(1, taskService.listByOwner(1L, 1, 10).size());
    }

    @Test
    void executeTask_success() {
        AgentDataTask t = new AgentDataTask(); t.setId(1L); t.setOwnerId(1L);
        t.setName("collect"); t.setPlatform("RUNWAY");
        when(taskMapper.selectById(1L)).thenReturn(t);
        taskService.executeTask(1L, 1L);
        assertEquals("COMPLETED", t.getStatus());
    }

    @Test
    void executeTask_notOwner_shouldThrow() {
        AgentDataTask t = new AgentDataTask(); t.setId(1L); t.setOwnerId(99L);
        when(taskMapper.selectById(1L)).thenReturn(t);
        assertThrows(BusinessException.class, () -> taskService.executeTask(1L, 1L));
    }

    @Test
    void deleteTask_success() {
        AgentDataTask t = new AgentDataTask(); t.setId(1L); t.setOwnerId(1L);
        when(taskMapper.selectById(1L)).thenReturn(t);
        assertDoesNotThrow(() -> taskService.deleteTask(1L, 1L));
    }

    @Test
    void deleteTask_notOwner_shouldThrow() {
        AgentDataTask t = new AgentDataTask(); t.setId(1L); t.setOwnerId(99L);
        when(taskMapper.selectById(1L)).thenReturn(t);
        assertThrows(BusinessException.class, () -> taskService.deleteTask(1L, 1L));
    }

    @Test
    void getRecordsByTask_success() {
        AgentDataTask t = new AgentDataTask(); t.setId(1L); t.setOwnerId(1L);
        when(taskMapper.selectById(1L)).thenReturn(t);
        AgentDataRecord r = new AgentDataRecord(); r.setId(1L);
        when(recordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(r));
        assertEquals(1, taskService.getRecordsByTask(1L, 1L).size());
    }

    @Test
    void getRecordsByTask_notOwner_shouldThrow() {
        AgentDataTask t = new AgentDataTask(); t.setId(1L); t.setOwnerId(99L);
        when(taskMapper.selectById(1L)).thenReturn(t);
        assertThrows(BusinessException.class, () -> taskService.getRecordsByTask(1L, 1L));
    }

    @Test
    void updateStatus_success() {
        AgentDataTask t = new AgentDataTask(); t.setId(1L);
        when(taskMapper.selectById(1L)).thenReturn(t);
        taskService.updateStatus(1L, "PAUSED");
        assertEquals("PAUSED", t.getStatus());
    }
}
