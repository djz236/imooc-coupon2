package com.imooc.coupon.executor.impl;

import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.executor.AbstractExecutor;
import com.imooc.coupon.executor.RuleExecutor;
import com.imooc.coupon.vo.CouponTemplateSDK;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * <h1>折扣优惠卷结算规则执行器</h1>
 */
@Slf4j
@Component
public class ZhekouExecutor extends AbstractExecutor implements RuleExecutor {
    /**
     * <h2>规则类型标记</h2>
     * @return {@link RuleFlag}
     */
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.ZHEKOU;
    }

    /**
     * <h2>优惠卷规则的计算</h2>
     * @param settlement {@link SettlementInfo} 包含了选择的优惠卷
     * @return {@link SettlementInfo} 修正过的结算信息
     */
    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {
        double goodsSum = return2Decimals(
                goodsCostSum(settlement.getGoodsInfos())
        );
        SettlementInfo probability = processGoodsTypeNotSatisfy(settlement,goodsSum);
        if(null!=probability){
            log.debug("ZheKou Template Is Not Match To GoodsType!");
            return probability;
        }
        // 折扣优惠卷可以直接使用
        CouponTemplateSDK templateSDK = settlement.getCouponAndTemplateInfos().get(0).getTemplateSDK();
        double quota = (double)templateSDK.getRule().getDiscount().getQuota();
        // 计算使用优惠卷之后的价格
        settlement.setCost(
                return2Decimals((goodsSum * (quota * 1.0 /100))) > minCost()?
                        return2Decimals((goodsSum * (quota * 1.0 /100))):minCost()
        );
        log.debug("Use ZheKou Coupon Make Goods Cost From {} to {}",
                goodsSum,settlement.getCost());
        return settlement;
    }
}
