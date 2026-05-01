package com.aigc.intelliengine.market.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 市场订单VO
 * <p>
 * 订单信息展示对象
 *
 * @author 智擎开发团队
 * @version 1.0.0
 * @since 2024
 */
@Data
@Schema(description = "市场订单信息")
public class MarketOrderVO {

    /**
     * 订单ID
     */
    @Schema(description = "订单ID", example = "10001")
    private Long id;

    /**
     * 订单编号
     */
    @Schema(description = "订单编号", example = "ORD_20240101120001")
    private String orderNo;

    /**
     * 买家ID
     */
    @Schema(description = "买家ID", example = "1")
    private Long buyerId;

    /**
     * 买家用户名
     */
    @Schema(description = "买家用户名", example = "zhangsan123")
    private String buyerName;

    /**
     * 卖家ID
     */
    @Schema(description = "卖家ID", example = "2")
    private Long sellerId;

    /**
     * 卖家用户名
     */
    @Schema(description = "卖家用户名", example = "lisi456")
    private String sellerName;

    /**
     * 模板ID
     */
    @Schema(description = "模板ID", example = "1")
    private Long templateId;

    /**
     * 模板标题
     */
    @Schema(description = "模板标题", example = "商业宣传视频模板")
    private String templateTitle;

    /**
     * 模板缩略图
     */
    @Schema(description = "模板缩略图URL")
    private String templateThumbnail;

    /**
     * 订单金额
     */
    @Schema(description = "订单金额（元）", example = "99.99")
    private BigDecimal totalAmount;

    /**
     * 支付方式
     */
    @Schema(description = "支付方式(ALIPAY/WECHAT_PAY)", example = "ALIPAY")
    private String payMethod;

    /**
     * 支付时间
     */
    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    /**
     * 订单状态
     */
    @Schema(description = "订单状态(PENDING_PAID=待支付/PAID=已支付/COMPLETED=已完成/CANCELLED=已取消)",
            example = "PAID")
    private String status;

    /**
     * 订单状态中文描述
     */
    @Schema(description = "订单状态描述", example = "已支付")
    private String statusText;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
