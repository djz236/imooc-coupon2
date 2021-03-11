package com.imooc.coupon.feign.hystrix;

import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.feign.SettlementClient;
import com.imooc.coupon.vo.CommonReponse;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SettlementClientHystrix
implements SettlementClient {

    @Override
    public CommonReponse<SettlementInfo> computeRule(SettlementInfo settlementInfo) throws CouponException {
        return null;
    }
}
