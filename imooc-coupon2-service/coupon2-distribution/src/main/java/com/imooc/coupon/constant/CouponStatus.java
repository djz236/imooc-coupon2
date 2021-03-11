package com.imooc.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 用户优惠券
 */
@Getter
@AllArgsConstructor
public enum CouponStatus {
    USABLE("可用", 1),
    USED("已使用的", 2),
    EXPIRED("过期的(未被使用的)", 3);
    /**
     * 优惠券状态描述信息
     */
    private String description;
    /**
     * 优惠券编码
     */
    private Integer code;

    /**
     * 根据code 获取到 couponStatus
     * @return
     */
    public static CouponStatus of(Integer code) {
        Objects.requireNonNull(code);
        return Stream.of(values())
                .filter(bean->bean.getCode().equals(code))
                .findAny().orElseThrow(()->
                        new IllegalArgumentException(code+" not exists"));
    }
}