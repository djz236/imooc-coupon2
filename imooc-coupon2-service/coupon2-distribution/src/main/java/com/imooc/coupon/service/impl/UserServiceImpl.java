package com.imooc.coupon.service.impl;

import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.service.IUserService;
import com.imooc.coupon.vo.AcquireTemplateRequest;
import com.imooc.coupon.vo.CouponTemplateSDK;
import com.imooc.coupon.vo.SettlementInfo;

import java.util.List;

public class UserServiceImpl implements IUserService {
    @Override
    public List<Coupon> findCouponByStatus(Long userId, Integer status) throws CouponException {
        return null;
    }

    @Override
    public List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException {
        return null;
    }

    @Override
    public Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException {
        return null;
    }

    @Override
    public SettlementInfo settlement(SettlementInfo info) throws CouponException {
        return null;
    }
}
