package com.imooc.coupon.feign.hystrix;

import com.imooc.coupon.feign.TemplateClient;
import com.imooc.coupon.vo.CommonReponse;
import com.imooc.coupon.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 优惠券feign 接口的熔断降级策略
 */
@Slf4j
@Component
public class TemplateClientHystrix implements TemplateClient {
    @Override
    public CommonReponse<List<CouponTemplateSDK>> findAllUsableTemplate() {
        log.error("[eureka-client-coupon-template] findAllUsableTemplate request error");
        return new CommonReponse<>(-1,
                "[eureka-client-coupon-template] findAllUsableTemplate request error",
                Collections.emptyList());
    }

    @Override
    public CommonReponse<Map<Integer, CouponTemplateSDK>> findIds2TemplateSDK(Collection<Integer> ids) {
       log.error("[eureka-client-coupon-template] findIds2TemplateSDK request error ");
       return new CommonReponse<>(-1,
               "[eureka-client-coupon-template] findIds2TemplateSDK request error ",
               Collections.EMPTY_MAP);
    }
}
