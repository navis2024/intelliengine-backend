package com.aigc.intelliengine.asset.domain.entity;

import java.time.LocalDateTime;

/**
 * 资产版本领域实体(Asset Version Domain Entity)
 * <p>
 * 位于COLA架构的领域层(Domain Layer)
 * 用于存储资产的历史版本快照
 * <p>
 * 设计原则：
 * 1. 纯Java类，不依赖任何框架注解
 * 2. 每次资产发生重大变更时保存版本快照
 * 3. 支持版本回滚和历史查看
 *
 * @author 智擎开发团队
 * @version 1.0.0
 * @since 2024
 */
public class AssetVersion {

    // ==================== 核心字段 ====================

    /**
     * 版本记录ID
     * 数据库自增主键
     */
    private Long id;

    /**
     * 关联的资产ID
     * 外键，关联asset_info表
     */
    private Long assetId;

    /**
     * 版本号
     * 资产的版本标识
     */
    private Integer versionNumber;

    // ==================== 版本内容 ====================

    /**
     * 版本快照数据（JSON格式）
     * 存储资产在此版本时的完整数据快照
     */
    private String snapshotData;

    /**
     * 变更说明/版本日志
     * 记录此版本的主要变更内容
     */
    private String changeLog;

    // ==================== 审计字段 ====================

    /**
     * 创建者ID
     * 创建此版本的用户ID
     */
    private Long createdBy;

    /**
     * 创建时间
     * 版本创建时间
     */
    private LocalDateTime createdAt;

    // ==================== 构造器 ====================

    /**
     * 默认构造器
     */
    public AssetVersion() {
    }

    /**
     * 构造器 - 用于创建新版本记录
     *
     * @param assetId       资产ID
     * @param versionNumber 版本号
     * @param snapshotData  快照数据（JSON）
     * @param changeLog     变更说明
     * @param createdBy     创建者ID
     */
    public AssetVersion(Long assetId, Integer versionNumber, String snapshotData, String changeLog, Long createdBy) {
        this.assetId = assetId;
        this.versionNumber = versionNumber;
        this.snapshotData = snapshotData;
        this.changeLog = changeLog;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    // ==================== Getter方法 ====================

    public Long getId() {
        return id;
    }

    public Long getAssetId() {
        return assetId;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public String getSnapshotData() {
        return snapshotData;
    }

    public String getChangeLog() {
        return changeLog;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ==================== Setter方法 ====================

    public void setId(Long id) {
        this.id = id;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public void setSnapshotData(String snapshotData) {
        this.snapshotData = snapshotData;
    }

    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ==================== 业务方法 ====================

    /**
     * 更新变更说明
     *
     * @param changeLog 新的变更说明
     */
    public void updateChangeLog(String changeLog) {
        if (changeLog != null && !changeLog.isEmpty()) {
            this.changeLog = changeLog;
        }
    }

    /**
     * 更新快照数据
     *
     * @param snapshotData 新的快照数据（JSON）
     */
    public void updateSnapshotData(String snapshotData) {
        if (snapshotData != null && !snapshotData.isEmpty()) {
            this.snapshotData = snapshotData;
        }
    }

    // ==================== Object方法 ====================

    @Override
    public String toString() {
        return "AssetVersion{" +
                "id=" + id +
                ", assetId=" + assetId +
                ", versionNumber=" + versionNumber +
                ", changeLog='" + changeLog + '\'' +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                '}';
    }
}
