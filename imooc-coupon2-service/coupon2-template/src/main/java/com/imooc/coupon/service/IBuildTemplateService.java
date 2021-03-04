package com.imooc.coupon.service;

import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.vo.TemplateRequest;

/**
 * 构建优惠券模板接口定义
 */
public interface IBuildTemplateService {
    /**
     * 创建优惠券模板
     * @param templateRequest 模板信息请求对象
     * @return CouponTemplate 优惠券模板实体
     * @throws CouponException
     */
    CouponTemplate buildTemplate(TemplateRequest templateRequest) throws CouponException;
}
