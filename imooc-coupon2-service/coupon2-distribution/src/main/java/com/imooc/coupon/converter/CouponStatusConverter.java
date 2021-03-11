package com.imooc.coupon.converter;

import com.imooc.coupon.constant.CouponStatus;

import javax.persistence.AttributeConverter;

/**
 * 优惠卷状态枚举类转换器
 */
public class CouponStatusConverter implements AttributeConverter<CouponStatus,Integer> {

    @Override
    public Integer convertToDatabaseColumn(CouponStatus couponStatus) {
        return couponStatus.getCode();
    }

    @Override
    public CouponStatus convertToEntityAttribute(Integer code) {
        return CouponStatus.of(code);
    }
}
