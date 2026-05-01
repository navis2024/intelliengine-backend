package com.aigc.intelliengine.asset.infrastructure.mapper;

import com.aigc.intelliengine.asset.infrastructure.dataobject.AssetDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface AssetMapper extends BaseMapper<AssetDO> {
    @Select("SELECT * FROM asset_info WHERE asset_code = #{code} AND is_deleted = 0 LIMIT 1")
    AssetDO selectByCode(@Param("code") String code);
    
    @Select("SELECT * FROM asset_info WHERE owner_id = #{ownerId} AND owner_type = #{ownerType} AND is_deleted = 0 ORDER BY created_at DESC")
    List<AssetDO> selectByOwner(@Param("ownerId") Long ownerId, @Param("ownerType") String ownerType);
    
    @Select("SELECT * FROM asset_info WHERE type = #{type} AND is_deleted = 0 ORDER BY created_at DESC")
    List<AssetDO> selectByType(@Param("type") String type);
    
    @Select("SELECT EXISTS(SELECT 1 FROM asset_info WHERE asset_code = #{code} AND is_deleted = 0)")
    boolean existsByCode(@Param("code") String code);
}
