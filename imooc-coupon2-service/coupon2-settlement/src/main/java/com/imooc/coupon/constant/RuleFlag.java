package com.imooc.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 规则类型枚举定义
 */
@Getter
@AllArgsConstructor
public enum RuleFlag {
    //单类别优惠券定义
    MANJIAN("满减卷的计算规则"),
    ZHEKOU("折扣卷的计算规则"),
    LIJIAN("立减卷的计算规则"),
    // 多类别优惠卷定义
    MANJIAN_ZHEKOU("满减卷+折扣卷的计算规则");
    /*规则的描述*/
    private String discription;
}
