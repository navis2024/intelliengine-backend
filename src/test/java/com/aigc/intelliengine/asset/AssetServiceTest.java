package com.aigc.intelliengine.asset;

import com.aigc.intelliengine.asset.model.dto.AssetCreateRequest;
import com.aigc.intelliengine.asset.model.dto.AssetUpdateRequest;
import com.aigc.intelliengine.asset.model.entity.AssetInfo;
import com.aigc.intelliengine.asset.model.entity.AssetVersion;
import com.aigc.intelliengine.asset.model.vo.AssetVO;
import com.aigc.intelliengine.common.exception.BusinessException;
import com.aigc.intelliengine.common.model.PageResult;
import com.aigc.intelliengine.common.security.MembershipValidator;
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
class AssetServiceTest {

    @Mock private AssetMapper assetMapper;
    @Mock private AssetVersionMapper versionMapper;
    @Mock private FileStorageService fileStorageService;
    @Mock private MembershipValidator validator;
    @InjectMocks private AssetService assetService;

    @Test
    void createAsset_shouldReturnVO() {
        AssetCreateRequest req = new AssetCreateRequest();
        req.setName("test.mp4"); req.setType("VIDEO"); req.setOwnerType("USER"); req.setOwnerId(1L);
        AssetVO vo = assetService.createAsset(req, 1L);
        assertNotNull(vo);
        assertEquals("test.mp4", vo.getName());
    }

    @Test
    void createAsset_projectType_shouldCheckMembership() {
        AssetCreateRequest req = new AssetCreateRequest();
        req.setName("proj-asset"); req.setType("IMAGE"); req.setOwnerType("PROJECT"); req.setOwnerId(10L);
        doThrow(new BusinessException("您不是该项目的成员")).when(validator).requireMembership(10L, 1L);
        assertThrows(BusinessException.class, () -> assetService.createAsset(req, 1L));
    }

    @Test
    void getAsset_shouldReturnVO() {
        AssetInfo a = asset(1L, "img.png", "IMAGE", "USER", 1L, 1L);
        when(validator.requireAssetAccess(1L, 1L)).thenReturn(a);
        assertEquals("img.png", assetService.getAsset(1L, 1L).getName());
    }

    @Test
    void getAsset_notFound_shouldThrow() {
        when(validator.requireAssetAccess(999L, 1L)).thenThrow(new BusinessException("资产不存在"));
        assertThrows(BusinessException.class, () -> assetService.getAsset(999L, 1L));
    }

    @Test
    void listAssets_shouldReturnPaged() {
        Page<AssetInfo> mp = new Page<>(1, 10, 1);
        mp.setRecords(List.of(asset(1L, "a.mp4", "VIDEO", "USER", 1L, 1L)));
        when(assetMapper.selectPage(any(), any())).thenReturn(mp);
        PageResult<AssetVO> p = assetService.listAssets(null, null, null, null, 1, 10, 1L);
        assertEquals(1L, p.getTotal()); assertEquals(1, p.getList().size());
    }

    @Test
    void updateAsset_success() {
        when(assetMapper.selectById(1L)).thenReturn(asset(1L, "old", "IMAGE", "USER", 1L, 1L));
        AssetUpdateRequest req = new AssetUpdateRequest(); req.setName("new"); req.setStatus("READY");
        assertEquals("new", assetService.updateAsset(1L, req, 1L).getName());
    }

    @Test
    void updateAsset_notOwner_shouldThrow403() {
        when(assetMapper.selectById(1L)).thenReturn(asset(1L, "img", "IMAGE", "USER", 1L, 99L));
        AssetUpdateRequest req = new AssetUpdateRequest(); req.setName("hack");
        BusinessException ex = assertThrows(BusinessException.class, () -> assetService.updateAsset(1L, req, 1L));
        assertEquals(403, ex.getCode());
    }

    @Test
    void updateAsset_notFound_shouldThrow() {
        when(assetMapper.selectById(999L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> assetService.updateAsset(999L, new AssetUpdateRequest(), 1L));
    }

    @Test
    void linkToProject_success() {
        when(assetMapper.selectById(1L)).thenReturn(asset(1L, "v.mp4", "VIDEO", "USER", 1L, 1L));
        AssetVO vo = assetService.linkToProject(1L, 10L, 1L);
        assertEquals("PROJECT", vo.getOwnerType());
        verify(validator).requireMembership(10L, 1L);
    }

    @Test
    void linkToProject_notOwner_shouldThrow403() {
        when(assetMapper.selectById(1L)).thenReturn(asset(1L, "v.mp4", "VIDEO", "USER", 1L, 99L));
        assertThrows(BusinessException.class, () -> assetService.linkToProject(1L, 10L, 1L));
    }

    @Test
    void deleteAsset_success() {
        when(assetMapper.selectById(1L)).thenReturn(asset(1L, "del", "VIDEO", "USER", 1L, 1L));
        assertDoesNotThrow(() -> assetService.deleteAsset(1L, 1L));
    }

    @Test
    void deleteAsset_notOwner_shouldThrow() {
        when(assetMapper.selectById(1L)).thenReturn(asset(1L, "del", "VIDEO", "USER", 1L, 99L));
        assertThrows(BusinessException.class, () -> assetService.deleteAsset(1L, 1L));
    }

    @Test
    void getVersions_shouldReturnList() {
        when(versionMapper.selectByAsset(1L)).thenReturn(List.of(new AssetVersion()));
        assertEquals(1, assetService.getVersions(1L).size());
    }

    @Test
    void createVersion_success() {
        when(assetMapper.selectById(1L)).thenReturn(asset(1L, "v", "VIDEO", "USER", 1L, 1L));
        when(versionMapper.selectByAsset(1L)).thenReturn(List.of());
        AssetVersion v = assetService.createVersion(1L, "init", 1L);
        assertEquals(1, v.getVersionNumber());
    }

    @Test
    void diffVersions_success() {
        AssetVersion v1 = new AssetVersion(); v1.setVersionNumber(1);
        AssetVersion v2 = new AssetVersion(); v2.setVersionNumber(2);
        when(versionMapper.selectOne(any())).thenReturn(v1).thenReturn(v2);
        assertEquals(1L, assetService.diffVersions(1L, 1, 2).get("assetId"));
    }

    @Test
    void rollbackToVersion_success() {
        when(assetMapper.selectById(1L)).thenReturn(asset(1L, "v", "VIDEO", "USER", 1L, 1L));
        when(versionMapper.selectOne(any())).thenReturn(new AssetVersion() {{ setVersionNumber(1); }});
        assertNotNull(assetService.rollbackToVersion(1L, 1, 1L));
    }

    private AssetInfo asset(Long id, String name, String type, String oType, Long oId, Long by) {
        AssetInfo a = new AssetInfo();
        a.setId(id); a.setAssetCode("T" + id); a.setName(name); a.setType(type);
        a.setOwnerType(oType); a.setOwnerId(oId); a.setVersion(1); a.setIsLatest(1);
        a.setStatus("READY"); a.setFileUrl("jimeng/t"); a.setFileSize(1024L);
        a.setFileFormat("mp4"); a.setCreatedBy(by); a.setCreatedAt(java.time.LocalDateTime.now());
        return a;
    }
}
