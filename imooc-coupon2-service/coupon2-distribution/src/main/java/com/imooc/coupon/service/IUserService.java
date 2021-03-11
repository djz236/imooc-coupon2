package com.imooc.coupon.service;

import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.vo.AcquireTemplateRequest;
import com.imooc.coupon.vo.CouponTemplateSDK;
import com.imooc.coupon.vo.SettlementInfo;

import java.util.List;

/**
 * 用户 服务相关的接口
 * 1.用户三类状态优惠卷信息展示服务
 * 2.查看用户当前 可以领取的优惠卷模板 - coupon-template 微服务配合实现
 * 3.用户领取优惠卷服务
 * 4.用户消费优惠卷服务 - coupon-settlement 微服务配合实现
 */
public interface IUserService {
    /**
     * 根据用户id和状态查询优惠卷记录
     *
     * @param userId 用户 id
     * @param status 优惠券状态
     * @return
     * @throws CouponException
     */
    List<Coupon> findCouponByStatus(Long userId,
                                    Integer status) throws CouponException;

    /**
     * 根据用户id查到当前可以领取的优惠券模板
     *
     * @param userId
     * @return
     * @throws CouponException
     */
    List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException;

    /**
     * 用户领取优惠券
     *
     * @param request
     * @return
     * @throws CouponException
     */
    Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException;

    /*
    结算（核销），优惠券
     */
    SettlementInfo settlement(SettlementInfo info) throws CouponException;
}
