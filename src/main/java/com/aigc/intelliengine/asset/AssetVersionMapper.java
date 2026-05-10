package com.aigc.intelliengine.asset;

import com.aigc.intelliengine.asset.model.entity.AssetVersion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface AssetVersionMapper extends BaseMapper<AssetVersion> {
    @Select("SELECT * FROM asset_version WHERE asset_id = #{assetId} ORDER BY version_number DESC")
    List<AssetVersion> selectByAsset(@Param("assetId") Long assetId);
}
