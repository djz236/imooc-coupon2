package com.imooc.coupon.feign.hystrix;

import com.imooc.coupon.feign.TemplateClient;
import com.imooc.coupon.vo.CommonReponse;
import com.imooc.coupon.vo.CouponTemplateSDK;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TemplateClientHystrix implements TemplateClient {
    @Override
    public CommonReponse<List<CouponTemplateSDK>> findAllUsableTemplate() {
        return null;
    }

    @Override
    public CommonReponse<Map<Integer, CouponTemplateSDK>> findIds2TemplateSDK(Collection<Integer> ids) {
        return null;
    }
}
