package com.imooc.coupon.service;

import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.vo.CouponTemplateSDK;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 优惠券模板基础（delete view...） 服务定义
 */
public interface ITemplateBaseService {
    /**
     * 根据优惠券模板id 获取优惠券模板信息
     * @param id 模板id
     * @return CouponTemplate 优化券模板实体
     * @throws CouponException
     */
    CouponTemplate buildTemplate(Integer id)throws CouponException;

    /**
     * 查找所有可用的优惠券模板
     * @return
     */
    List<CouponTemplate> findAllUsableTemplate();

    /**
     * 获取模板ids到CouponTemplateSdk的映射
     * @param ids 模板ids
     * @return Map<key:模板id,value:CouponTemplate></key:模板id，value:CouponTemplate>
     */
    Map<Integer, CouponTemplateSDK> findIds2TemplateSdk(Collection<Integer> ids);
}
