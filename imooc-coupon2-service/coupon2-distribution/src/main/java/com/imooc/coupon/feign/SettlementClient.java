package com.imooc.coupon.feign;

import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.feign.hystrix.SettlementClientHystrix;
import com.imooc.coupon.vo.CommonReponse;
import com.imooc.coupon.vo.SettlementInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 优惠券 结算微服务 Feign 接口定义
 */
@FeignClient(value = "eureka-client-coupon-settlement",
fallback = SettlementClientHystrix.class)
public interface SettlementClient {
    @RequestMapping(value = "/coupon-settlement/settlement/compute",
    method = RequestMethod.POST)
    CommonReponse<SettlementInfo> computeRule(
            @RequestBody SettlementInfo settlementInfo) throws CouponException;

}
