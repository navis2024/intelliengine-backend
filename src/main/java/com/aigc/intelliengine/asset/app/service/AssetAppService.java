package com.aigc.intelliengine.asset.app.service;

import com.aigc.intelliengine.asset.domain.entity.Asset;
import com.aigc.intelliengine.asset.domain.gateway.AssetGateway;
import com.aigc.intelliengine.asset.dto.AssetCreateRequest;
import com.aigc.intelliengine.asset.vo.AssetVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetAppService {
    private final AssetGateway assetGateway;
    
    @Transactional
    public AssetVO createAsset(AssetCreateRequest request, Long userId) {
        Asset asset = new Asset();
        asset.setAssetCode(generateAssetCode());
        asset.setName(request.getName());
        asset.setType(request.getType());
        asset.setOwnerType(request.getOwnerType());
        asset.setOwnerId(String.valueOf(request.getOwnerId()));
        asset.setVersion(1);
        asset.setLatest(1);
        asset.setStatus("DRAFT");
        asset.setCreatedBy(String.valueOf(userId));
        asset.setCreateTime(LocalDateTime.now());
        asset.setUpdateTime(LocalDateTime.now());
        asset.setDeleted(0);
        
        Asset saved = assetGateway.save(asset);
        return toVO(saved);
    }
    
    public AssetVO getAssetById(Long id) {
        return assetGateway.findById(id).map(this::toVO).orElse(null);
    }
    
    public List<AssetVO> getAssetsByOwner(Long ownerId, String ownerType) {
        return assetGateway.findByOwner(ownerId, ownerType).stream()
            .map(this::toVO).collect(Collectors.toList());
    }
    
    private String generateAssetCode() {
        return "AST_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private AssetVO toVO(Asset asset) {
        AssetVO vo = new AssetVO();
        vo.setId(asset.getId());
        vo.setAssetCode(asset.getAssetCode());
        vo.setName(asset.getName());
        vo.setType(asset.getType());
        vo.setOwnerType(asset.getOwnerType());
        vo.setOwnerId(asset.getOwnerId());
        vo.setVersion(asset.getVersion());
        vo.setStatus(asset.getStatus());
        vo.setFileUrl(asset.getFileUrl());
        vo.setCreateTime(asset.getCreateTime());
        return vo;
    }
}
