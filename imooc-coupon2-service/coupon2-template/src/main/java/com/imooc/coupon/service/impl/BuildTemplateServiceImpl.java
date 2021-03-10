package com.imooc.coupon.service.impl;

import com.imooc.coupon.dao.CouponTemplateDao;
import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.service.IAsyncService;
import com.imooc.coupon.service.IBuildTemplateService;
import com.imooc.coupon.vo.TemplateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 构建优惠券模板接口实现
 */
@Slf4j
@Service
public class BuildTemplateServiceImpl
        implements IBuildTemplateService {
    private final IAsyncService asyncService;
    private final CouponTemplateDao templateDao;

    @Autowired
    public BuildTemplateServiceImpl(IAsyncService asyncService, CouponTemplateDao templateDao) {
        this.asyncService = asyncService;
        this.templateDao = templateDao;
    }

    /**
     * 创建优惠券模板
     *
     * @param templateRequest 模板信息请求对象
     * @return
     * @throws CouponException
     */
    @Override
    public CouponTemplate buildTemplate(TemplateRequest templateRequest) throws CouponException {
        //参数的合法性
        if (!templateRequest.validate()) {
            throw new CouponException("BuildTemplate Param is not Valid!");
        }
        //判断同名优惠券模板是否存在
        CouponTemplate template = templateDao.findByName(templateRequest.getName());
        if (null != template) {
            throw new CouponException("Exist same name template!");
        }
        //构造CouponTemplate 并保存到数据库中
        CouponTemplate couponTemplate = request2Template(templateRequest);
        couponTemplate = templateDao.save(couponTemplate);
        // 根据优惠卷模板异步生成优惠卷码
        asyncService.asyncConstructCouponByTemplate(couponTemplate);
        return couponTemplate;
    }

    /**
     * @param templateRequest
     * @return
     */
    private CouponTemplate request2Template(TemplateRequest templateRequest) {
        return new CouponTemplate(
                templateRequest.getName(),
                templateRequest.getLogo(),
                templateRequest.getDesc(),
                templateRequest.getCategory(),
                templateRequest.getProductLine(),
                templateRequest.getCount(),
                templateRequest.getUserId(),
                templateRequest.getTarget(),
                templateRequest.getRule()
        );
    }
}
