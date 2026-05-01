package com.aigc.intelliengine.asset.infrastructure.repository;

import com.aigc.intelliengine.asset.domain.entity.Asset;
import com.aigc.intelliengine.asset.domain.gateway.AssetGateway;
import com.aigc.intelliengine.asset.infrastructure.dataobject.AssetDO;
import com.aigc.intelliengine.asset.infrastructure.mapper.AssetMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class AssetRepositoryImpl implements AssetGateway {
    private final AssetMapper assetMapper;
    
    public AssetRepositoryImpl(AssetMapper assetMapper) {
        this.assetMapper = Objects.requireNonNull(assetMapper);
    }
    
    @Override
    public Asset save(Asset asset) {
        AssetDO assetDO = toDataObject(asset);
        assetMapper.insert(assetDO);
        return toEntity(assetDO);
    }
    
    @Override
    public Optional<Asset> findById(Long id) {
        AssetDO assetDO = assetMapper.selectById(id);
        return Optional.ofNullable(toEntity(assetDO));
    }
    
    @Override
    public Optional<Asset> findByCode(String code) {
        AssetDO assetDO = assetMapper.selectByCode(code);
        return Optional.ofNullable(toEntity(assetDO));
    }
    
    @Override
    public List<Asset> findByOwner(Long ownerId, String ownerType) {
        return assetMapper.selectByOwner(ownerId, ownerType).stream()
            .map(this::toEntity).collect(Collectors.toList());
    }
    
    @Override
    public List<Asset> findByType(String type) {
        return assetMapper.selectByType(type).stream()
            .map(this::toEntity).collect(Collectors.toList());
    }
    
    @Override
    public Asset update(Asset asset) {
        AssetDO assetDO = toDataObject(asset);
        assetMapper.updateById(assetDO);
        return toEntity(assetDO);
    }
    
    @Override
    public boolean remove(Long id) {
        return assetMapper.deleteById(id) > 0;
    }
    
    @Override
    public boolean existsByCode(String code) {
        return assetMapper.existsByCode(code);
    }
    
    private AssetDO toDataObject(Asset asset) {
        if (asset == null) return null;
        AssetDO assetDO = new AssetDO();
        if (asset.getId() != null) assetDO.setId(Long.valueOf(asset.getId()));
        assetDO.setAssetCode(asset.getAssetCode());
        assetDO.setName(asset.getName());
        assetDO.setType(asset.getType());
        assetDO.setOwnerType(asset.getOwnerType());
        if (asset.getOwnerId() != null) assetDO.setOwnerId(Long.valueOf(asset.getOwnerId()));
        if (asset.getSourceAssetId() != null) assetDO.setSourceAssetId(Long.valueOf(asset.getSourceAssetId()));
        assetDO.setSourceVersion(asset.getSourceVersion());
        assetDO.setVersion(asset.getVersion());
        assetDO.setIsLatest(asset.getLatest());
        assetDO.setCommitMessage(asset.getCommitMessage());
        if (asset.getCommittedBy() != null) assetDO.setCommittedBy(Long.valueOf(asset.getCommittedBy()));
        assetDO.setCommittedAt(asset.getCommittedAt());
        assetDO.setStatus(asset.getStatus());
        assetDO.setFileUrl(asset.getFileUrl());
        assetDO.setFileSize(asset.getFileSize());
        assetDO.setFileFormat(asset.getFileFormat());
        assetDO.setDuration(asset.getDuration());
        if (asset.getCreatedBy() != null) assetDO.setCreatedBy(Long.valueOf(asset.getCreatedBy()));
        assetDO.setCreatedAt(asset.getCreateTime());
        assetDO.setUpdatedAt(asset.getUpdateTime());
        assetDO.setIsDeleted(asset.getDeleted());
        return assetDO;
    }
    
    private Asset toEntity(AssetDO assetDO) {
        if (assetDO == null) return null;
        Asset asset = new Asset();
        asset.setId(String.valueOf(assetDO.getId()));
        asset.setAssetCode(assetDO.getAssetCode());
        asset.setName(assetDO.getName());
        asset.setType(assetDO.getType());
        asset.setOwnerType(assetDO.getOwnerType());
        asset.setOwnerId(String.valueOf(assetDO.getOwnerId()));
        asset.setSourceAssetId(assetDO.getSourceAssetId() != null ? String.valueOf(assetDO.getSourceAssetId()) : null);
        asset.setSourceVersion(assetDO.getSourceVersion());
        asset.setVersion(assetDO.getVersion());
        asset.setLatest(assetDO.getIsLatest());
        asset.setCommitMessage(assetDO.getCommitMessage());
        asset.setCommittedBy(assetDO.getCommittedBy() != null ? String.valueOf(assetDO.getCommittedBy()) : null);
        asset.setCommittedAt(assetDO.getCommittedAt());
        asset.setStatus(assetDO.getStatus());
        asset.setFileUrl(assetDO.getFileUrl());
        asset.setFileSize(assetDO.getFileSize());
        asset.setFileFormat(assetDO.getFileFormat());
        asset.setDuration(assetDO.getDuration());
        asset.setCreatedBy(assetDO.getCreatedBy() != null ? String.valueOf(assetDO.getCreatedBy()) : null);
        asset.setCreateTime(assetDO.getCreatedAt());
        asset.setUpdateTime(assetDO.getUpdatedAt());
        asset.setDeleted(assetDO.getIsDeleted());
        return asset;
    }
}
