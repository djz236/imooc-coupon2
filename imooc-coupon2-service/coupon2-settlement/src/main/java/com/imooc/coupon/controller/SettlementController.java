package com.imooc.coupon.controller;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.executor.ExecuteManager;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 结算服务的Controller
 */
@Slf4j
@RestController
public class SettlementController {
    /**
     * 结算规则执行管理器
     */
    private final ExecuteManager executeManager;

    public SettlementController(ExecuteManager executeManager) {
        this.executeManager = executeManager;
    }

    /**
     * 优惠券结算
     * 127.0.0.1:9000/imooc/coupon-settlement/settlement/compute
     * @param settlement
     * @return
     * @throws CouponException
     */
    @PostMapping("/settlement/compute")
    public SettlementInfo computeRule(@RequestBody
                                              SettlementInfo settlement) throws CouponException {
        log.info("settlementInfo:{}", JSON.toJSONString(settlement));
        return executeManager.computeRule(settlement);
    }

}
