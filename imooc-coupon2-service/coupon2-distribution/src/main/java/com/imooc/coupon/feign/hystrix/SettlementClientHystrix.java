package com.imooc.coupon.feign.hystrix;

import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.feign.SettlementClient;
import com.imooc.coupon.vo.CommonReponse;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 结算微服务熔断策略实现
 */
@Slf4j
@Component
public class SettlementClientHystrix
        implements SettlementClient {

    @Override
    public CommonReponse<SettlementInfo> computeRule(SettlementInfo settlementInfo) throws CouponException {
        log.info("[eureka-client-coupon-settlement] computeRule request");
        settlementInfo.setEmploy(false);
        settlementInfo.setCost(-1.0);

        return new CommonReponse<>(-1,
                "[eureka-client-coupon-settlement] computeRule request",
                settlementInfo);
    }
}
