package com.imooc.coupon.service;

import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;

import java.util.List;

/**
 * Redis 相关的操作服务接口定义
 * 1. 用户的三个状态优惠卷 Cache 相关操作
 * 2. 优惠卷模板生成的优惠卷码 Cache  操作
 */
public interface IRedisService {

    /**
     * 根据userId和状态 找到缓存的优惠券列表数据
     * @param userId 用户id
     * @param status 优惠券状态
     * @return
     */
    List<Coupon> getCachedCoupons(Long userId,Integer status);

    /**
     * 保存空的优惠券列表到缓存中 考虑缓存穿透的问题
     * @param userId
     * @param status
     */
    void saveEmptyCouponListToCache(Long userId,List<Integer> status);

    /**
     * 尝试从Cache中获取一个优惠券
     * @param templateId
     * @return
     */
    String tryToAcquireCouponCodeFromCache(Integer templateId);

    /**
     *  将优惠券保存到cache中
     * @param userId
     * @param coupons
     * @param status 优惠券状态
     * @return 保存成功的个数
     * @throws CouponException
     */
    Integer addCouponToCache(Long userId,
                             List<Coupon> coupons,
                             Integer status) throws CouponException;
}
