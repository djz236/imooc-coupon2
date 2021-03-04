package com.imooc.coupon.converter;

import com.imooc.coupon.constant.DisTributeTarget;

import javax.persistence.AttributeConverter;

public class DistributeTargetConverter
        implements AttributeConverter<DisTributeTarget,Integer> {
    @Override
    public Integer convertToDatabaseColumn(DisTributeTarget disTributeTarget) {
        return disTributeTarget.getCode();
    }

    @Override
    public DisTributeTarget convertToEntityAttribute(Integer code) {
        return DisTributeTarget.of(code);
    }
}
