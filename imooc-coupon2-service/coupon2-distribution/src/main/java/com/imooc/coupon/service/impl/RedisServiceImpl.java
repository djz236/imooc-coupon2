package com.imooc.coupon.service.impl;

import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.service.IRedisService;

import java.util.List;

public class RedisServiceImpl implements IRedisService {
    @Override
    public List<Coupon> getCachedCoupons(Long userId, Integer status) {
        return null;
    }

    @Override
    public void saveEmptyCouponListToCache(Long userId, List<Integer> status) {

    }

    @Override
    public String tryToAcquireCouponCodeFromCache(Integer templateId) {
        return null;
    }

    @Override
    public Integer addCouponToCache(Long userId, List<Coupon> coupons, Integer status) throws CouponException {
        return null;
    }
}
