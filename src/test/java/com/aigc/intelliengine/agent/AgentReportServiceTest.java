package com.aigc.intelliengine.agent;

import com.aigc.intelliengine.agent.model.entity.AgentReport;
import com.aigc.intelliengine.agent.model.entity.AgentReportTemplate;
import com.aigc.intelliengine.asset.AssetMapper;
import com.aigc.intelliengine.common.exception.BusinessException;
import com.aigc.intelliengine.common.security.MembershipValidator;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentReportServiceTest {

    @Mock private AgentReportMapper reportMapper;
    @Mock private AgentReportTemplateMapper templateMapper;
    @Mock private AgentDataRecordMapper recordMapper;
    @Mock private PromptLibraryMapper promptMapper;
    @Mock private AssetMapper assetMapper;
    @Mock private MembershipValidator validator;
    @InjectMocks private AgentReportService reportService;

    @Test
    void generateReport_withProjectId_shouldCheckMembership() {
        when(assetMapper.selectCount(any())).thenReturn(10L);
        when(recordMapper.selectCount(any())).thenReturn(100L);
        when(promptMapper.selectCount(any())).thenReturn(50L);
        when(promptMapper.selectPage(any(), any())).thenReturn(
                new Page<>(1, 5) {{ setRecords(List.of()); }});

        AgentReport report = reportService.generateReport("Weekly Report", "WEEKLY", null, 1L, 1L);
        assertNotNull(report);
        assertEquals("Weekly Report", report.getTitle());
        verify(validator).requireMembership(1L, 1L);
        verify(reportMapper).insert(ArgumentMatchers.<AgentReport>any());
    }

    @Test
    void generateReport_withoutProjectId_shouldNotCheckMembership() {
        when(assetMapper.selectCount(any())).thenReturn(5L);
        when(recordMapper.selectCount(any())).thenReturn(20L);
        when(promptMapper.selectCount(any())).thenReturn(10L);
        when(promptMapper.selectPage(any(), any())).thenReturn(
                new Page<>(1, 5) {{ setRecords(List.of()); }});

        AgentReport report = reportService.generateReport("Personal Report", "DAILY", null, null, 1L);
        assertNotNull(report);
        verify(validator, never()).requireMembership(anyLong(), anyLong());
    }

    @Test
    void getReport_success() {
        AgentReport r = new AgentReport(); r.setId(1L); r.setTitle("Test Report");
        when(reportMapper.selectById(1L)).thenReturn(r);
        assertEquals("Test Report", reportService.getReport(1L).getTitle());
    }

    @Test
    void getReport_notFound_shouldThrow() {
        when(reportMapper.selectById(999L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> reportService.getReport(999L));
    }

    @Test
    void listByProject_withProjectId_shouldCheckMembership() {
        AgentReport r = new AgentReport(); r.setId(1L); r.setProjectId(1L);
        when(reportMapper.selectPage(any(), any())).thenReturn(
                new Page<>(1, 10, 1) {{ setRecords(List.of(r)); }});
        assertEquals(1, reportService.listByProject(1L, 1, 10, 1L).size());
        verify(validator).requireMembership(1L, 1L);
    }

    @Test
    void listByProject_nullProject_shouldFilterByUserId() {
        when(reportMapper.selectPage(any(), any())).thenReturn(
                new Page<>(1, 10, 0) {{ setRecords(List.of()); }});
        assertTrue(reportService.listByProject(null, 1, 10, 1L).isEmpty());
    }

    @Test
    void deleteReport_success() {
        reportService.deleteReport(1L);
        verify(reportMapper).deleteById(1L);
    }

    @Test
    void listTemplates_shouldReturnAll() {
        when(templateMapper.selectList(null)).thenReturn(List.of(new AgentReportTemplate(), new AgentReportTemplate()));
        assertEquals(2, reportService.listTemplates().size());
    }
}
