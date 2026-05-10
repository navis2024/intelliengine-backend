package com.aigc.intelliengine.agent;

import com.aigc.intelliengine.agent.model.dto.AiVideoCreateRequest;
import com.aigc.intelliengine.agent.model.entity.AssetAiVideo;
import com.aigc.intelliengine.agent.model.entity.VideoFrame;
import com.aigc.intelliengine.asset.AssetMapper;
import com.aigc.intelliengine.asset.model.entity.AssetInfo;
import com.aigc.intelliengine.common.exception.BusinessException;
import com.aigc.intelliengine.common.security.MembershipValidator;
import com.aigc.intelliengine.project.ProjectMemberMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiVideoServiceTest {

    @Mock private AiVideoMapper aiVideoMapper;
    @Mock private VideoFrameMapper videoFrameMapper;
    @Mock private AssetMapper assetMapper;
    @Mock private MembershipValidator validator;
    @Mock private ProjectMemberMapper memberMapper;
    @Mock private RabbitTemplate rabbitTemplate;
    @InjectMocks private AiVideoService aiVideoService;

    @Test
    void createAiVideo_success() {
        when(aiVideoMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        // Non-VIDEO type to skip MQ path (mock insert doesn't set auto-id, causing Map.of NPE)
        AssetInfo asset = new AssetInfo(); asset.setId(1L); asset.setType("IMAGE");
        asset.setFileUrl("jimeng/t"); asset.setCreatedBy(1L); asset.setOwnerType("USER"); asset.setOwnerId(1L);
        when(assetMapper.selectById(1L)).thenReturn(asset);
        AssetAiVideo v = aiVideoService.createAiVideo(req(1L, "Runway", "v3", "sunset", null), 1L);
        assertEquals("Runway", v.getToolType());
    }

    @Test
    void createAiVideo_duplicate_shouldThrow() {
        when(aiVideoMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(new AssetAiVideo());
        assertThrows(BusinessException.class, () -> aiVideoService.createAiVideo(req(1L, "R", "v3", "p", null), 1L));
    }

    @Test
    void createAiVideo_nonVideo_shouldNotSendMQ() {
        when(aiVideoMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        AssetInfo img = new AssetInfo(); img.setId(2L); img.setName("img.png"); img.setType("IMAGE");
        img.setFileUrl("jimeng/img.png"); img.setCreatedBy(1L);
        when(assetMapper.selectById(2L)).thenReturn(img);
        assertNotNull(aiVideoService.createAiVideo(req(2L, "Sora", "v1", "p", null), 1L));
    }

    @Test
    void findByAssetId_success() {
        AssetAiVideo v = new AssetAiVideo(); v.setId(1L); v.setAssetId(1L);
        when(aiVideoMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(v);
        assertNotNull(aiVideoService.findByAssetId(1L, 1L));
        verify(validator).requireAssetAccess(1L, 1L);
    }

    @Test
    void findByAssetId_notFound_returnsNull() {
        when(aiVideoMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        assertNull(aiVideoService.findByAssetId(1L, 1L));
    }

    @Test
    void updateAiVideo_success() {
        AssetAiVideo v = new AssetAiVideo(); v.setId(1L); v.setAssetId(1L); v.setToolType("Runway");
        when(aiVideoMapper.selectById(1L)).thenReturn(v);
        assertEquals("Pika", aiVideoService.updateAiVideo(1L, req(null, "Pika", "v2", "u", null), 1L).getToolType());
    }

    @Test
    void updateAiVideo_notFound_shouldThrow() {
        when(aiVideoMapper.selectById(999L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> aiVideoService.updateAiVideo(999L, req(null, null, null, null, null), 1L));
    }

    @Test
    void addFrame_success() {
        VideoFrame f = aiVideoService.addFrame(1L, BigDecimal.valueOf(1.5), 1, "http://thumb", "p", "{}", 1, "tag");
        assertEquals(1, f.getFrameNumber());
    }

    @Test
    void getFramesByVideoId_shouldReturnOrdered() {
        VideoFrame f1 = new VideoFrame(); f1.setFrameNumber(1);
        when(videoFrameMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(f1));
        assertEquals(1, aiVideoService.getFramesByVideoId(1L).size());
    }

    @Test
    void deleteAiVideo_success() {
        AssetAiVideo v = new AssetAiVideo(); v.setId(1L); v.setAssetId(10L);
        when(aiVideoMapper.selectById(1L)).thenReturn(v);
        aiVideoService.deleteAiVideo(1L, 1L);
        verify(validator).requireAssetAccess(10L, 1L);
    }

    @Test
    void listAll_withAccessible_shouldReturn() {
        when(assetMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(videoAsset(1L, "v1.mp4")));
        when(memberMapper.selectByUser(1L)).thenReturn(List.of());
        when(aiVideoMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(new AssetAiVideo() {{ setId(1L); }}));
        assertEquals(1, aiVideoService.listAll(1L).size());
    }

    @Test
    void listAll_noAccess_shouldReturnEmpty() {
        when(assetMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(memberMapper.selectByUser(1L)).thenReturn(List.of());
        assertTrue(aiVideoService.listAll(1L).isEmpty());
    }

    private AiVideoCreateRequest req(Long assetId, String tool, String ver, String prompt, String neg) {
        AiVideoCreateRequest r = new AiVideoCreateRequest();
        r.setAssetId(assetId); r.setToolType(tool); r.setToolVersion(ver);
        r.setPromptText(prompt); r.setNegativePrompt(neg); r.setFps(30);
        return r;
    }

    private AssetInfo videoAsset(Long id, String url) {
        AssetInfo a = new AssetInfo();
        a.setId(id); a.setName("v.mp4"); a.setType("VIDEO");
        a.setOwnerType("USER"); a.setOwnerId(1L); a.setFileUrl(url); a.setCreatedBy(1L);
        return a;
    }
}
