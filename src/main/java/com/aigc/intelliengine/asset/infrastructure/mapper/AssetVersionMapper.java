package com.aigc.intelliengine.asset.infrastructure.mapper;

import com.aigc.intelliengine.asset.infrastructure.dataobject.AssetVersionDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface AssetVersionMapper extends BaseMapper<AssetVersionDO> {
    @Select("SELECT * FROM asset_version WHERE asset_id = #{assetId} ORDER BY version_number DESC")
    List<AssetVersionDO> selectByAssetId(@Param("assetId") Long assetId);
    
    @Select("SELECT * FROM asset_version WHERE asset_id = #{assetId} AND version_number = #{version} LIMIT 1")
    AssetVersionDO selectByAssetAndVersion(@Param("assetId") Long assetId, @Param("version") Integer version);
}
