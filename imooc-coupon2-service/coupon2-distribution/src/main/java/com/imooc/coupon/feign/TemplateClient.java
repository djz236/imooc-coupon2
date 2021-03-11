package com.imooc.coupon.feign;

import com.imooc.coupon.feign.hystrix.TemplateClientHystrix;
import com.imooc.coupon.vo.CommonReponse;
import com.imooc.coupon.vo.CouponTemplateSDK;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 优惠券模板服务 feign 接口定义
 */
@FeignClient(value = "eureka-client-coupon-template",
        fallback = TemplateClientHystrix.class)
public interface TemplateClient {
    /**
     * 查找所有可用的优惠券模板
     *
     * @return
     */
    @RequestMapping(value = "/coupon-template/template/sdk/all",
            method = RequestMethod.GET)
    CommonReponse<List<CouponTemplateSDK>> findAllUsableTemplate();

    /**
     * 获取模板ids 到CouponTemplateSDK 的映射
     * @param ids
     * @return
     */
    @RequestMapping(value = "/coupon-template/template/sdk/infos",
            method = RequestMethod.GET)
    CommonReponse<Map<Integer, CouponTemplateSDK>> findIds2TemplateSDK(
            @RequestParam("ids") Collection<Integer> ids
    );
}
