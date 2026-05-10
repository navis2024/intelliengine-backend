package com.aigc.intelliengine.asset;

import com.aigc.intelliengine.asset.model.entity.AssetInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface AssetMapper extends BaseMapper<AssetInfo> {
    @Select("SELECT * FROM asset_info WHERE owner_id = #{ownerId} AND owner_type = #{ownerType} AND is_deleted = 0 ORDER BY created_at DESC")
    List<AssetInfo> selectByOwner(@Param("ownerId") Long ownerId, @Param("ownerType") String ownerType);

    @Select("SELECT * FROM asset_info WHERE owner_id = #{ownerId} AND owner_type = #{ownerType} AND type = #{type} AND is_deleted = 0 ORDER BY created_at DESC")
    List<AssetInfo> selectByOwnerAndType(@Param("ownerId") Long ownerId, @Param("ownerType") String ownerType, @Param("type") String type);
}
