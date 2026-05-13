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

    @Select("<script>SELECT * FROM asset_info WHERE is_deleted=0 AND ((owner_type='USER' AND owner_id=#{userId}) <if test='projectIds!=null and projectIds.size()>0'>OR (owner_type='PROJECT' AND owner_id IN <foreach collection='projectIds' item='pid' open='(' separator=',' close=')'>#{pid}</foreach>)</if>) ORDER BY created_at DESC LIMIT #{limit}</script>")
    List<AssetInfo> selectRecentByUserAndProjects(@Param("userId") Long userId, @Param("projectIds") java.util.List<Long> projectIds, @Param("limit") int limit);

    @Select("<script>SELECT COUNT(*) FROM asset_info WHERE is_deleted=0 AND ((owner_type='USER' AND owner_id=#{userId}) <if test='projectIds!=null and projectIds.size()>0'>OR (owner_type='PROJECT' AND owner_id IN <foreach collection='projectIds' item='pid' open='(' separator=',' close=')'>#{pid}</foreach>)</if>)</script>")
    int countByUserAndProjects(@Param("userId") Long userId, @Param("projectIds") java.util.List<Long> projectIds);
}
