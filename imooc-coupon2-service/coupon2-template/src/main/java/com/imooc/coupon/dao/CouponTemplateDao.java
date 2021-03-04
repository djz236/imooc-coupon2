package com.imooc.coupon.dao;

import com.imooc.coupon.entity.CouponTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * dao接口定义
 */
public interface CouponTemplateDao
        extends JpaRepository<CouponTemplate, Integer> {
    /**
     * 根据模板名称查询模板
     *
     * @param name 模板名称
     * @return
     */
    CouponTemplate findByName(String name);

    /**
     * 根据 Avalable和Expired 标记查找模板记录
     *
     * @param avaliable
     * @param expired
     * @return
     */
    List<CouponTemplate> findAllByAvailableAndExpired(Boolean avaliable,
                                                      Boolean expired);

    /**
     * 根据Expired 标记查找模板记录
     *
     * @param expired
     * @return
     */
    List<CouponTemplate> findAllByExpired(Boolean expired);
}
