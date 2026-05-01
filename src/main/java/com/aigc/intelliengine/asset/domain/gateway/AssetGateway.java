package com.aigc.intelliengine.asset.domain.gateway;

import com.aigc.intelliengine.asset.domain.entity.Asset;
import java.util.List;
import java.util.Optional;

public interface AssetGateway {
    Asset save(Asset asset);
    Optional<Asset> findById(Long id);
    Optional<Asset> findByCode(String code);
    List<Asset> findByOwner(Long ownerId, String ownerType);
    List<Asset> findByType(String type);
    Asset update(Asset asset);
    boolean remove(Long id);
    boolean existsByCode(String code);
}
