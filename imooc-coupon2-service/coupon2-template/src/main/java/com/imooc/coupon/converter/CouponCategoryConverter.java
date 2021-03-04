package com.imooc.coupon.converter;

import com.imooc.coupon.constant.CouponCategory;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;

/**
 * 优惠券分类枚举属性转换器
 */
@Convert
public class CouponCategoryConverter
        implements AttributeConverter<CouponCategory,String> {
    /**
     * 将实体属性X转换为Y存储到数据库中 插入
     * @param couponCategory
     * @return
     */
    @Override
    public String convertToDatabaseColumn(CouponCategory couponCategory) {
        return couponCategory.getCode();
    }

    /**
     * 将数据库中的字段Y转为实体属性X 查询操作执行的动作
     * @param code
     * @return
     */
    @Override
    public CouponCategory convertToEntityAttribute(String code) {
        return CouponCategory.of(code);
    }
}
